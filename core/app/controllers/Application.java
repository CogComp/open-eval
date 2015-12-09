package controllers;

import play.*;
import play.mvc.*;
import play.data.DynamicForm;

import views.html.*;

import java.util.*;

import models.*;


import play.Logger;

import controllers.evaluators.Evaluation;

public class Application extends Controller {

    private List<models.Configuration> getConfigurations() {
        List<models.Configuration> configurations = new ArrayList<>();
        
        FrontEndDatabase f = new FrontEndDatabase(); 
		
        List<models.Configuration> configList = f.getConfigList();
    
        if (configList != null) {
            for (int i = 0; i < configList.size(); i++) {
                configurations.add(configList.get(i));
            }
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
            task_variants_i.add("task_variant A" + (char) i);
            task_variants_i.add("task_variant B" + (char) i);
            task_variants_i.add("task_variant C" + (char) i);
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
        FrontEndDatabase f = new FrontEndDatabase(); 

        String json = "{"
            + "\"configuration\" : {"
            + "\"datasetName\": \"" + bindedForm.get("dataset") + "\","
            + "\"teamName\" : \"" + bindedForm.get("teamname")+ "\","
            + "\"description\" : \"" + bindedForm.get("description")+ "\","
            + "\"evaluator\" : \"" + bindedForm.get("evaluator")+ "\","
            + "\"taskType\" : \"Text Annotation\""
            + "},"
            + "\"taskVariants\" : ["
            + "\"tskVar1\","
            + "\"tskVar2\","
            + "\"tskVar3\""
            + "]"
            + "}";
        f.storeConfig(json);

        return redirect("/");
    }

    public Result configuration(String configuration_id) {
        RecipeViewModel viewModel = new RecipeViewModel();

        
        FrontEndDatabase f = new FrontEndDatabase(); 
        models.Configuration conf = f.getConfigInformation(Integer.parseInt(configuration_id)); 


        List<Record> records = new ArrayList<>();
        records.add(new Record("date", "comment", "repo", "author",95.1));
        records.add(new Record("date2", "comment", "repo", "author",36.1));
        records.add(new Record("date3", "comment", "repo", "author",97.1));
        conf.records = records;
        viewModel.configuration = conf;

        viewModel.history = "history B";
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
		int status = Core.startJob(configuration_id, url);
		if(status == 200)
			return redirect("/configuration?conf="+configuration_id);
		else
			return status(status);
    }

    public Result record(String record_id) {
        RecordViewModel viewModel = new RecordViewModel();
        // should find record by lookup
        Record associated_record = new Record();
        viewModel.record = associated_record;
        return ok(record.render(viewModel));
    }

    public Result about() {
        return ok(about.render());
    }

    public Result thinClient() {
        // Change to download of thin client
        return redirect("/");
    }
}
