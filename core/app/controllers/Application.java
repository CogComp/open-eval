package controllers;

import play.*;
import play.libs.ws.WSResponse;
import play.mvc.*;
import play.data.DynamicForm;
import play.Logger;

import views.html.*;

import java.util.*;

import models.*;

import controllers.evaluators.Evaluation;


public class Application extends Controller {

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
        // should delete stuff :S
        DynamicForm bindedForm = new DynamicForm().bindFromRequest();
        System.out.println(bindedForm.get("conf"));
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
            f.insertConfigToDB(bindedForm.get("dataset"), bindedForm.get("teamname"), bindedForm.get("description"), bindedForm.get("evaluator"), "Text Annotation", taskVariants); 
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


    // If there is a failure, it should return back to the add run page
    // with default values set to the sent form values, and an error message
    public Result submitRun() {
        DynamicForm bindedForm = new DynamicForm().bindFromRequest();

        String configuration_id = bindedForm.get("configuration_id");
        String url = bindedForm.get("url");
        String author = bindedForm.get("author"); 
        String repo = bindedForm.get("repo");
        String comment = bindedForm.get("comment"); 
        
        FrontEndDBInterface f = new FrontEndDBInterface();
        String record_id = f.storeRunInfo(Integer.parseInt(configuration_id), url, author, repo, comment); 
       
        WSResponse response = Core.startJob(configuration_id, url, record_id);
        if(response == null)
            return internalServerError("Server Not Found");
        else
        if(response.getStatus()==500)
            return internalServerError(response.getBody());
        else
            return redirect("/configuration?conf="+configuration_id);
    }

    public Result record(String record_id) {
        RecordViewModel viewModel = new RecordViewModel();
        FrontEndDBInterface f = new FrontEndDBInterface();
        Record associated_record = f.getRecordFromRecordID(Integer.parseInt(record_id));
   
        viewModel.record = associated_record;
        return ok(record.render(viewModel));
    }

    public Result deleteRecord() {
        // should delete record from db
        DynamicForm bindedForm = new DynamicForm().bindFromRequest();
        System.out.println(bindedForm.get("record_id"));
        return redirect("/");
    }

    public Result about() {
        return ok(about.render());
    }

    public Result thinClient() {
        // Change to download of thin client
        return redirect("/");
    }
}