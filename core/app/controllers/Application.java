package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import play.libs.ws.WSResponse;
import play.mvc.*;
import play.data.DynamicForm;
import play.mvc.Result;
import play.libs.F.*;
import views.html.*;
import java.util.*;
import models.*;
import controllers.evaluators.Evaluation;
import edu.illinois.cs.cogcomp.core.experiments.EvaluationRecord;
import play.libs.F.Function;
import play.libs.F.Promise;
import actors.MasterActor;
import actors.Messages.*;
import akka.actor.*;
import akka.*;
import play.mvc.Controller;
import javax.inject.*;

import static akka.pattern.Patterns.ask;

@Singleton
public class Application extends Controller {

	final ActorRef masterActor;
	
	/**
	 * The master actor is created when the application starts.
	 * @param system
	 */
	@Inject public Application(ActorSystem system) {
        masterActor = system.actorOf(MasterActor.props);
    }
	
    private List<models.Configuration> getConfigurations() {
        FrontEndDBInterface f = new FrontEndDBInterface();
        List<models.Configuration> configList;         
        try {
            configList = f.getConfigList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return configList;
    }

    public Result index() {
        IndexViewModel viewModel = new IndexViewModel();
        viewModel.configurations = getConfigurations();
        return ok(index.render(viewModel));
    }

    public Result addConfiguration() {
        AddConfigurationViewModel viewModel = new AddConfigurationViewModel();

        List<String> datasets = new ArrayList<>();
        datasets.add("dataset A");
        datasets.add("dataset B");
        datasets.add("dataset C");

        Map<String, List<String>> task_variants = new HashMap<>();
        Map<String, List<String>> evaluators = new HashMap<>();

        for (int i = 65; i < 65+3; i++) {
            List<String> task_variants_i = new ArrayList<>();

            task_variants_i.add("SPAN_IDENTIFICATION");
            task_variants_i.add("SPAN_TAGGING");
            task_variants_i.add("PREDICATE_ARGUMENT_LABELING");
            task_variants_i.add("SPAN_CLUSTERING");
            task_variants_i.add("DEPENDENCY_PARSING");
            task_variants_i.add("CONSTITUENCY_PARSING");
            
            task_variants.put("dataset "+(char) i, task_variants_i);

            List<String> evaluator_i = new ArrayList<>();
            evaluator_i.add("evaluator A" + (char) i);
            evaluator_i.add("evaluator B" + (char) i);
            evaluator_i.add("evaluator C" + (char) i);
            for (int j = 65; j < 65+3; j++) 
                evaluators.put("task_variant "+(char)i+(char)j, evaluator_i);
        }

        viewModel.datasets = datasets;
        viewModel.task_variants = task_variants;
        viewModel.evaluators = evaluators;

        return ok(addConfiguration.render(viewModel));
    }

    public Result deleteConfiguration() {
        DynamicForm bindedForm = new DynamicForm().bindFromRequest();
        FrontEndDBInterface f = new FrontEndDBInterface();
        f.deleteConfigAndRecords(Integer.parseInt(bindedForm.get("conf")));
        return redirect("/");
    }

    public Result submitConfiguration() {
        DynamicForm bindedForm = new DynamicForm().bindFromRequest();
        FrontEndDBInterface f = new FrontEndDBInterface(); 
        
        List<String> taskVariants = new ArrayList<>(); 
        taskVariants.add("tskVar1"); 
        taskVariants.add("tskVar2");
        taskVariants.add("tskVar3");
        
        try {
            f.insertConfigToDB(bindedForm.get("dataset"), bindedForm.get("configurationname"),
                               bindedForm.get("description"), bindedForm.get("evaluator"),
                               "Text Annotation", taskVariants); 
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return redirect("/");
    }

    public Result configuration(String configuration_id) {
        RecipeViewModel viewModel = new RecipeViewModel();
        FrontEndDBInterface f = new FrontEndDBInterface(); 
        models.Configuration conf; 
        
        try {
            conf = f.getConfigInformation(Integer.parseInt(configuration_id)); 
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        List<Record> records = f.getRecordsFromConfID(Integer.parseInt(configuration_id));
        conf.records = records;
        viewModel.configuration = conf;
        return ok(recipe.render(viewModel));
    }

    public Result addRun(String configuration_id) {
        AddRunViewModel viewModel = new AddRunViewModel();

        viewModel.configuration_id = configuration_id;
        viewModel.default_url = "";
        viewModel.default_author = "";
        viewModel.default_repo = "";
        viewModel.default_comment = "";
        viewModel.error_message = "";

        return ok(addRun.render(viewModel));
    }

    public Result submitRun() {
        DynamicForm bindedForm = new DynamicForm().bindFromRequest();

        String configuration_id = bindedForm.get("configuration_id");

        String url = bindedForm.get("url");
        String author = bindedForm.get("author"); 
        String repo = bindedForm.get("repo");
        String comment = bindedForm.get("comment"); 
        
        FrontEndDBInterface f = new FrontEndDBInterface();
        String record_id = f.storeRunInfo(Integer.parseInt(configuration_id), url, author, repo, comment); 
       
        // TODO: Test connection. Replace Core.startJob() with Core.testConnection();
        if(Core.testConnection(url) == null) {
            AddRunViewModel viewModel = new AddRunViewModel();
            viewModel.configuration_id = configuration_id;
            viewModel.default_url = url;
            viewModel.default_author = author;
            viewModel.default_repo = repo;
            viewModel.default_comment = comment;
            viewModel.error_message = "Server at given address was not found";

            return ok(addRun.render(viewModel));
        }
        
        masterActor.tell(new SetUpJobMessage(configuration_id, url, record_id), masterActor);
        WorkingViewModel viewModel = new WorkingViewModel();
    	viewModel.configuration_id = configuration_id;	
    	viewModel.percent_complete = 0;
    	return ok(working.render(viewModel));
    }
    
    public Result progressBar(String configuration_id) {
    	WorkingViewModel viewModel = new WorkingViewModel();
    	viewModel.configuration_id = configuration_id;	
    	viewModel.percent_complete = 0;
    	return ok(working.render(viewModel));
    }
    
    public Promise<Result> getCurrentProgress() {
        return Promise.wrap(ask(masterActor, new StatusRequest("requesting status"), 60000)).map(
                        new Function<Object,Result>() {
                            public Result apply(Object response) {

                                if(response instanceof StatusUpdate)
                                {
                                	StatusUpdate update = ((StatusUpdate) response);
                                	double comp = update.getCompleted()/update.getTotal();
                                	int percentComplete = (int)(comp*100);
                                	return ok(Integer.toString(percentComplete));
                                }
                                return ok(response.toString());
                            }
                        }
                );
    }

    public Result record(String record_id) {
        RecordViewModel viewModel = new RecordViewModel();
        FrontEndDBInterface f = new FrontEndDBInterface();
        Record associated_record = f.getRecordFromRecordID(Integer.parseInt(record_id));
   
        viewModel.record = associated_record;
        return ok(record.render(viewModel));
    }

    public Result deleteRecord() {
        DynamicForm bindedForm = new DynamicForm().bindFromRequest();
        FrontEndDBInterface f = new FrontEndDBInterface();
        f.deleteRecordFromRecordID(Integer.parseInt(bindedForm.get("record_id")));
        return redirect("/");
    }
    
    public Result about() {
        return ok(about.render());
    }
}