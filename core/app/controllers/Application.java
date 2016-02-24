package controllers;

import play.libs.ws.WSResponse;
import play.mvc.*;
import play.data.DynamicForm;

import views.html.*;

import java.util.*;

import models.*;


import controllers.evaluators.Evaluation;

import edu.illinois.cs.cogcomp.core.experiments.EvaluationRecord;


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

        List<String> tasks = new ArrayList<>();

        tasks.add("SPAN_IDENTIFICATION");
        tasks.add("SPAN_TAGGING");
        tasks.add("PREDICATE_ARGUMENT_LABELING");
        tasks.add("SPAN_CLUSTERING");
        tasks.add("DEPENDENCY_PARSING");
        tasks.add("CONSTITUENCY_PARSING");

        Map<String, List<String>> task_variants = new HashMap<>();
        Map<String, List<String>> datasets = new HashMap<>();

        for (int i = 65; i < 65+3; i++) {
            
            List<String> task_variants_i = new ArrayList<String>();
            task_variants_i.add("TVA");
            task_variants_i.add("TVB");
            task_variants_i.add("TVC");
            List<String> datasets_i = new ArrayList<String>();
            datasets_i.add("DSA");
            datasets_i.add("DSB");
            datasets_i.add("DSC");

            for (int j = 0; j < tasks.size(); j++) {
                task_variants.put(tasks.get(j), task_variants_i);
                datasets.put(tasks.get(j), datasets_i);
            }
        }

        viewModel.tasks = tasks;
        viewModel.datasets = datasets;
        viewModel.task_variants = task_variants;

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

        // should have map from strings to evaluators
        // need to change taskVariants too
        
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
       
        WSResponse response = Core.startJob(configuration_id, url, record_id);
        if(response == null) {
            AddRunViewModel viewModel = new AddRunViewModel();
            viewModel.configuration_id = configuration_id;
            viewModel.default_url = url;
            viewModel.default_author = author;
            viewModel.default_repo = repo;
            viewModel.default_comment = comment;
            viewModel.error_message = "Server at given address was not found";

            return ok(addRun.render(viewModel));
        }
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
        DynamicForm bindedForm = new DynamicForm().bindFromRequest();
        String conf_id = bindedForm.get("configuration_id");
        FrontEndDBInterface f = new FrontEndDBInterface();
        f.deleteRecordFromRecordID(Integer.parseInt(bindedForm.get("record_id")));
        return redirect("/configuration?conf="+conf_id);
    }

    public Result about() {
        return ok(about.render());
    }
}