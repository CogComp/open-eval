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
        
        List<Record> records = new ArrayList<>();
        records.add(new Record("24-May-11", "comment", "repo", "author"));
        records.add(new Record("24-Apr-11", "comment", "repo", "author"));
        records.add(new Record("24-May-10", "comment", "repo", "author"));
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
        // Run + Save run to db here
        // System.out.println(bindedForm.get("url"));
		WSResponse response = Core.startJob(configuration_id, url);
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
        // should find record by lookup
        Record associated_record = new Record();
        associated_record.metrics = new Metrics(
            1.1,2.2,3.3,4,5,6,7,8
        );
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

    public Result instructions() {
        // Change to go to instructions page
        return redirect("/");
    }
}