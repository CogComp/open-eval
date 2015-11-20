package controllers;

import play.*;
import play.mvc.*;
import play.data.DynamicForm;

import views.html.*;

import java.util.*;

import models.*;

//import FrontEndDatabase;
import play.Logger;


public class Application extends Controller {

    private List<models.Configuration> getConfigurations() {
        List<models.Configuration> configurations = new ArrayList<>();
        
        FrontEndDatabase f = new FrontEndDatabase(); 
        ArrayList<String[]> configList = f.getConfigList();
    
        if (configList != null) {
            for (int i = 0; i < configList.size(); i++) {
                String config[] = configList.get(i);
                models.Configuration conf = new models.Configuration(config[0], config[1], config[2], config[3], config[4], config[5]);
                configurations.add(conf);
            }
        }
        return configurations;
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

        List<String> task_variants = new ArrayList<>();
        task_variants.add("task_variant A");
        task_variants.add("task_variant B");
        task_variants.add("task_variant C");

        List<String> evaluators = new ArrayList<>();
        evaluators.add("evaluator A");
        evaluators.add("evaluator B");
        evaluators.add("evaluator C");

        viewModel.datasets = datasets;
        viewModel.task_variants = task_variants;
        viewModel.evaluators = evaluators;

        return ok(addConfiguration.render(viewModel));
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
        String[] configInfo = f.getConfigInformation(Integer.parseInt(configuration_id)); 
        //Check if null?
        models.Configuration conf = new models.Configuration(configInfo[0], configInfo[1], configInfo[2], configInfo[3], configInfo[4], configInfo[5]);


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
        // Run + Save run to db here
        // System.out.println(bindedForm.get("url"));

        return redirect("/configuration?conf="+configuration_id);
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
