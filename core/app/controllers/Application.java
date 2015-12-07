package controllers;

import play.*;
import play.mvc.*;
import play.data.DynamicForm;

import views.html.*;

import java.util.*;

import models.*;

import controllers.evaluators.Evaluation;

public class Application extends Controller {

    private List<models.Configuration> getConfigurations() {
        List<models.Configuration> configurations = new ArrayList<>();
        models.Configuration conf1 = new models.Configuration(
            "Team A", "Description for first configuration", "dataset 1", "task_variant_a", "evaluator_a"
        );
        models.Configuration conf2 = new models.Configuration(
            "Team B", "Description for second configuration", "dataset 2", "task_variant_b", "evaluator_b"
        );
        models.Configuration conf3 = new models.Configuration(
            "Team C", "Description for second configuration", "dataset 3", "task_variant_c", "evaluator_c"
        );
        models.Configuration conf4 = new models.Configuration(
            "Team D", "Description for fourth configuration", "dataset 4", "task_variant_d", "evaluator_d"
        );
        configurations.add(conf1);
        configurations.add(conf2);
        configurations.add(conf3);
        configurations.add(conf4);
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

        // Save config to db here
        // System.out.println(bindedForm.get("dataset"));

        return redirect("/");
    }

    public Result configuration(String configuration_id) {
        RecipeViewModel viewModel = new RecipeViewModel();
        // get configuration from db using configuration_id = conf
        models.Configuration conf = new models.Configuration(
            "Team B", "Description for second configuration", "dataset 2", "task_variant_b", "evaluator_b"
        );
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
