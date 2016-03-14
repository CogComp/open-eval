package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import play.libs.ws.WSResponse;
import play.mvc.*;
import play.data.DynamicForm;
import play.mvc.Result;
import play.libs.Json;
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

import com.fasterxml.jackson.databind.node.ObjectNode;

import static akka.pattern.Patterns.ask;

@Singleton
public class Application extends Controller {

	final ActorRef masterActor;

	/**
	 * The master actor is created when the application starts.
	 * 
	 * @param system
	 */
	@Inject
	public Application(ActorSystem system) {
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

		FrontEndDBInterface f = new FrontEndDBInterface();

		List<String> tasks = f.getTasks();

		Map<String, List<String>> task_variants = new HashMap<>();
		Map<String, List<String>> datasets = new HashMap<>();

		for (String task : tasks) {
			List<String> task_variants_i = f.getTaskVariantsForTask(task);
			List<String> datasets_i = f.getDatasetsForTask(task);
			task_variants.put(task, task_variants_i);
			datasets.put(task, datasets_i);
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

		String taskName = bindedForm.get("task");
		String taskVariant = bindedForm.get("taskvariant");
		String evaluator = f.getEvaluatorForTask(taskName);

		try {
			f.insertConfigToDB(bindedForm.get("dataset"), bindedForm.get("configurationname"),
					bindedForm.get("description"), evaluator, taskName, taskVariant);
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

		// should also get name passed through here
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

		// TODO: Test connection. Replace Core.startJob() with
		// Core.testConnection();
		if (Core.testConnection(url) == null) {
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
		viewModel.conf_id = configuration_id;
		viewModel.percent_complete = 0;
		return ok(working.render(viewModel));
	}

	public Result progressBar(String configuration_id) {
		WorkingViewModel viewModel = new WorkingViewModel();
		viewModel.conf_id = configuration_id;
		viewModel.percent_complete = 0;
		return ok(working.render(viewModel));
	}

	public Promise<Result> getCurrentProgress() {
		return Promise.wrap(ask(masterActor, new StatusRequest("requesting status"), 60000))
				.map(new Function<Object, Result>() {
					public Result apply(Object response) {
						ObjectNode result = Json.newObject();
						if (response instanceof StatusUpdate) {
							StatusUpdate update = ((StatusUpdate) response);
							double comp = ((double) update.getCompleted()) / ((double) update.getTotal());
							int percentComplete = (int) (comp * 100.0);
							System.out.println("Percent Complete: " + Integer.toString(percentComplete));
							result.put("percent_complete", Integer.toString(percentComplete));
							result.put("completed", Integer.toString(update.getCompleted()));
							result.put("skipped", Integer.toString(update.getSkipped()));
							result.put("total", Integer.toString(update.getTotal()));
							return ok(result);
						}
						result.put("percent_complete", "0");
						result.put("completed", "0");
						result.put("skipped", "0");
						result.put("total", "0");
						return ok(result);
					}
				});
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
		return redirect("/configuration?conf=" + conf_id);
	}

	public Result about() {
		return ok(about.render());
	}
}