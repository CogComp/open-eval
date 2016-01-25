package controllers;

import play.*;
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