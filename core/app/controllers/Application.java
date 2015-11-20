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
				models.Configuration conf = new models.Configuration(config[0], config[1], config[2]);
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
        return ok(addConfiguration.render());
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

    public Result configuration(String conf) {
        RecipeViewModel viewModel = new RecipeViewModel();
		/* How do we get the id of this configuration?
        FrontEndDatabase f = new FrontEndDatabase(); 
		f.getConfigInformation(); 
		*/
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
        // Run + Save run to db here
        // System.out.println(bindedForm.get("url"));

        return redirect("/configuration?conf="+configuration_id);
    }
}
