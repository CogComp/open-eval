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
        models.Configuration conf1 = new models.Configuration("Team A", "Desc A", "dataset 1");
        models.Configuration conf2 = new models.Configuration("Team B", "Desc B", "dataset 2");
        configurations.add(conf1);
        configurations.add(conf2);
        return configurations;
    }

    public Result index() {
        IndexViewModel viewModel = new IndexViewModel();
        viewModel.configurations = getConfigurations();
        return ok(index.render(viewModel));
    }

    public Result addConfiguration() {
        return ok(addConfiguration.render());
    }

    public Result submitConfiguration() {
        DynamicForm bindedForm = new DynamicForm().bindFromRequest();

        // Save config to db here
        // System.out.println(bindedForm.get("dataset"));

        return redirect("/");
    }

    public Result configuration(String conf) {
        RecipeViewModel viewModel = new RecipeViewModel();
        // get configuration from db using configuration_id = conf
        viewModel.configuration = new models.Configuration("Team B", "Desc B", "dataset 2");
        viewModel.history = "history B";
        return ok(recipe.render(viewModel));
    }

    public Result addRun(String configuration_id) {
        return ok(addRun.render(configuration_id));
    }

    public Result submitRun() {
        DynamicForm bindedForm = new DynamicForm().bindFromRequest();

        String configuration_id = bindedForm.get("configuration_id");
		String url = bindedForm.get("url");
        // Run + Save run to db here
        // System.out.println(bindedForm.get("url"));
		Evaluation eval = Core.startJob(configuration_id, url);
        return redirect("/configuration?conf="+configuration_id);
    }
}
