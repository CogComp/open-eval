package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.cogcomp.core.experiments.ClassificationTester;
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

    // eventually things should all be seperated into a class, and this should be at the class level
    @Security.Authenticated(Secured.class)
    public Result index() {
        IndexViewModel viewModel = new IndexViewModel();
        viewModel.configurations = getConfigurations();
        return ok(index.render(viewModel));
    }

    public Result welcome() {
        WelcomeViewModel viewModel = new WelcomeViewModel();
        return ok(welcome.render(viewModel));
    }

    @Security.Authenticated(Secured.class)
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

    @Security.Authenticated(Secured.class)
    public Result deleteConfiguration() {
        DynamicForm bindedForm = new DynamicForm().bindFromRequest();
        FrontEndDBInterface f = new FrontEndDBInterface();
        String conf_id = bindedForm.get("conf");
        if (!Secured.canAccess(request().username(), conf_id)) {
            return this.authError();
        }
        f.deleteConfigAndRecords(Integer.parseInt(conf_id));
        return redirect("/");
    }

    @Security.Authenticated(Secured.class)
    public Result submitConfiguration() {
        DynamicForm bindedForm = new DynamicForm().bindFromRequest();
        FrontEndDBInterface f = new FrontEndDBInterface();

        String taskName = bindedForm.get("task");
        String taskVariant = bindedForm.get("taskvariant");
        String username = request().username();
        String teamName = f.getTeamnameFromUsername(username);
        
        String evaluator = f.getEvaluator(taskName, taskVariant);

        try {
            f.insertConfigToDB(bindedForm.get("dataset"), bindedForm.get("configurationname"),
                    bindedForm.get("description"), evaluator, taskName, taskVariant, teamName);
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

    @Security.Authenticated(Secured.class)
    public Result addRun(String configuration_id) {
        if (!Secured.canAccess(request().username(), configuration_id)) {
            return this.authError();
        }
        AddRunViewModel viewModel = new AddRunViewModel();

        // should also get name passed through here
        viewModel.configuration_id = configuration_id;
        viewModel.default_url = "";
        viewModel.default_author = request().username();
        viewModel.default_repo = "";
        viewModel.default_comment = "";
        viewModel.error_message = "";

        return ok(addRun.render(viewModel));
    }

    @Security.Authenticated(Secured.class)
    public Result submitRun() {
        DynamicForm bindedForm = new DynamicForm().bindFromRequest();

        String conf_id = bindedForm.get("configuration_id");

        if (!Secured.canAccess(request().username(), conf_id)) {
            return this.authError();
        }

		String url = bindedForm.get("url");
		String author = bindedForm.get("author");
		String repo = bindedForm.get("repo");
		String comment = bindedForm.get("comment");

		FrontEndDBInterface f = new FrontEndDBInterface();

		LearnerSettings settings = Core.testConnection(url);
        if (settings.error != null) {
            AddRunViewModel viewModel = new AddRunViewModel();
            viewModel.configuration_id = conf_id;
            viewModel.default_url = url;
            viewModel.default_author = author;
            viewModel.default_repo = repo;
            viewModel.default_comment = comment;
            viewModel.error_message = settings.error;

            return ok(addRun.render(viewModel));
        }

        String record_id = f.storeRunInfo(Integer.parseInt(conf_id), url, author, repo, comment);
        Record rec = f.getRecordFromRecordID(Integer.parseInt(record_id));
        masterActor.tell(new SetUpJobMessage(conf_id, url, record_id, settings), masterActor);
        RecordViewModel viewModel = new RecordViewModel();
        viewModel.record = rec;
        return ok(record.render(viewModel));
    }

    public Promise<Result> getCurrentProgress(String record_id) {
        return Promise.wrap(ask(masterActor, new StatusRequest(record_id), 60000))
            .map(new Function<Object, Result>() {
                public Result apply(Object response) {
                    ObjectNode result = Json.newObject();
                    if (response instanceof StatusUpdate) {
                        StatusUpdate update = ((StatusUpdate) response);
                        int percentComplete;
                        if (update.getTotal() > 0) {
                            double comp = ((double) (
                                update.getCompleted() + update.getSkipped())) / ((double) update.getTotal()
                            );
                            percentComplete = (int) (comp * 100.0);
                        } else {
                            percentComplete = 0;
                        }
                        result.put("percent_complete", Integer.toString(percentComplete));
                        result.put("completed", Integer.toString(update.getCompleted()));
                        result.put("skipped", Integer.toString(update.getSkipped()));
                        result.put("total", Integer.toString(update.getTotal()));
                        ClassificationTester ct = update.getEvaluation();
                        String error = update.getError();
                        if(error!=null)
                            result.put("status", error);
                        else
                        if(update.getTotal()==0)
                            result.put("status", "Receiving instances from database");
                        else
                            result.put("status", "Send and receiving Text Annotations");
                        if(ct != null) {
                            EvaluationRecord eval = ct.getEvaluationRecord();
                            result.put("precision", eval.getPrecision());
                            result.put("recall", eval.getRecall());
                            result.put("f1", eval.getF1());
                            result.put("goldCount", eval.getGoldCount());
                            result.put("correctCount", eval.getCorrectCount());
                            result.put("predictedCount", eval.getPredictedCount());
                            result.put("missedCount", eval.getMissedCount());
                            result.put("extraCount", eval.getExtraCount());
                        }
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

    @Security.Authenticated(Secured.class)
    public Result record(String record_id) {
        RecordViewModel viewModel = new RecordViewModel();
        FrontEndDBInterface f = new FrontEndDBInterface();
        Record associated_record = f.getRecordFromRecordID(Integer.parseInt(record_id));

        if (!Secured.canAccess(request().username(), associated_record.configuration_id)) {
            return this.authError();
        }

        viewModel.record = associated_record;
        return ok(record.render(viewModel));
    }

    @Security.Authenticated(Secured.class)
    public Result deleteRecord() {
        DynamicForm bindedForm = new DynamicForm().bindFromRequest();
        String conf_id = bindedForm.get("configuration_id");
        if (!Secured.canAccess(request().username(), conf_id)) {
            return this.authError();
        }
        FrontEndDBInterface f = new FrontEndDBInterface();
        f.deleteRecordFromRecordID(Integer.parseInt(bindedForm.get("record_id")));
        return redirect("/configuration?conf=" + conf_id);
    }

    @Security.Authenticated(Secured.class)
    public Result stopRun() {
        DynamicForm bindedForm = new DynamicForm().bindFromRequest();
        String record_id = bindedForm.get("record_id");
        String conf_id = bindedForm.get("configuration_id");
        masterActor.tell(new StopRunMessage(record_id), masterActor);
        if (!Secured.canAccess(request().username(), conf_id)) {
            return this.authError();
        }
        FrontEndDBInterface f = new FrontEndDBInterface();
        f.deleteRecordFromRecordID(Integer.parseInt(record_id));
        return redirect("/configuration?conf=" + conf_id);
    }

    private List<String> getTeamNames() {
        List<String> teamNames = new ArrayList<>();

        teamNames.add("team NN1");
        teamNames.add("team NN2");
        teamNames.add("team CRF1");
        teamNames.add("team CRF2");
        teamNames.add("team Perc1");
        teamNames.add("team Perc2");
        teamNames.add("team SVM1");
        teamNames.add("team SVM2");
        teamNames.add("team Exp1");
        teamNames.add("team Exp2");

        return teamNames;
    }

    public Result login() {
        LoginViewModel viewModel = new LoginViewModel();

        viewModel.teamNames = getTeamNames();

        return ok(login.render(viewModel));
    }

    public Result addUser() {
        DynamicForm bindedForm = new DynamicForm().bindFromRequest();

        String username = bindedForm.get("username");
        String verify = bindedForm.get("verify");
        String password = bindedForm.get("password");
        String teamName = bindedForm.get("teamName");

        LoginViewModel viewModel = new LoginViewModel();
        viewModel.username = username;
        viewModel.teamNames = getTeamNames();
        viewModel.teamName = teamName;
        if(! password.equals(verify)) {
            String error = "Password and password verification do not match";
            viewModel.errorMessage = error;
            return ok(login.render(viewModel));
        } else if (password.length() == 0) {
            String error = "Password cannot be of length 0";
            viewModel.errorMessage = error;
            return ok(login.render(viewModel));
        }

        String teamPassword = bindedForm.get("teamPassword");
        FrontEndDBInterface f = new FrontEndDBInterface();
        boolean teamPassCorrect = f.checkTeamPassword(teamName, teamPassword);
        //@Deepak add check that the team password is correct.
        if (!teamPassCorrect) {
            String error = "Team password is incorrect";
            viewModel.errorMessage = error;
            return ok(login.render(viewModel));
        }


        //@Deepak insert a record into the user db with this username and pw
        f.insertNewUserToDB(username, password, teamName);

        session().clear();
        session("username", username);
        return redirect("/");
    }

    public Result loginUser() {
        DynamicForm bindedForm = new DynamicForm().bindFromRequest();

        String username = bindedForm.get("loginUsername");
        String password = bindedForm.get("loginPassword");

        FrontEndDBInterface f = new FrontEndDBInterface();
        boolean passCheck = f.authenticateUser(username, password);
        //@Deepak get password for username from db

        if (!passCheck) {
            String error = "Some error";
            LoginViewModel viewModel = new LoginViewModel();
            viewModel.errorMessage = error;
            viewModel.loginUsername = username;
            viewModel.teamNames = getTeamNames();
            return ok(login.render(viewModel));
        }

        session().clear();
        session("username", username);
        return redirect("/");
    }

    @Security.Authenticated(Secured.class)
    public Result logout() {
        session().clear();
        return redirect("/");
    }

    private Result authError() {
        return forbidden("403 Forbidden: User does not have access to this configuration");
    }
}