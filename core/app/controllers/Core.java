package controllers;

import java.util.List;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import edu.illinois.cs.cogcomp.core.experiments.evaluators.Evaluator;
import models.LearnerSettings;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import controllers.readers.Reader;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.experiments.ClassificationTester;
import edu.illinois.cs.cogcomp.core.experiments.EvaluationRecord;
import edu.illinois.cs.cogcomp.core.experiments.evaluators.ConstituentLabelingEvaluator;
import models.Configuration;
import models.Job;
import models.LearnerInterface;
import models.LearnerSettings;
import play.libs.ws.WSResponse;

/**
 * This class connects all the back-end modules, i.e. the solver, the evaluation
 * and the database
 */
public class Core {
    /**
     * Returns null if there was an error trying to connect to the server.
     * Otherwise, it returns the info string returned by the learner server.
     */
    public static LearnerSettings testConnection(String url) {
        LearnerInterface learner;
        try {
            learner = new LearnerInterface(url);
        }
        catch(Exception e){
            return new LearnerSettings("You have entered an invalid url");
        }
        LearnerSettings settings = learner.getInfo();
        return settings;
    }

    /**
     * Gets the instances from the database, cleanses them, and returns the
     * resulting Job.
     */
    public static Job setUpJob(String conf_id, String url, String record_id) {
        Configuration runConfig = getConfigurationFromDb(conf_id);

        List<TextAnnotation> correctInstances = getInstancesFromDb(runConfig);
        System.out.println(url);
        LearnerInterface learner;
        try {
            learner = new LearnerInterface(url);
        }
        catch(Exception e){
            return new Job("Invalid url");
        }
        LearnerSettings settings = learner.getInfo();
        if (settings.error != null) {
            System.out.println("Could not connect to server");
            return new Job(settings.error);
        }
        List<TextAnnotation> cleansedInstances =  cleanseInstances(correctInstances, runConfig);
        if (cleansedInstances == null) {
            return new Job("Error in cleanser. Please check your requiredViews");
        }
        return new Job(learner, cleansedInstances, correctInstances);
    }

    public static void storeResultsOfRunInDatabase(ClassificationTester evaluation, String record_id, boolean isRunning) {
        storeEvaluationIntoDb(evaluation.getEvaluationRecord(), record_id, isRunning);
    }

    public static void evaluate(Evaluator evaluator, ClassificationTester eval,  TextAnnotation gold,
            TextAnnotation predicted, String viewName) {
        View goldView = gold.getView(viewName);
        View predictedView = predicted.getView(viewName);
        evaluator.setViews(goldView, predictedView);
        evaluator.evaluate(eval);
    }

    /**
     * Cleanse the correct instances
     *
     * @param correctInstances
     *            - The correct instances from the databse
     * @return - The cleansed instance
     */
    public static List<TextAnnotation> cleanseInstances(List<TextAnnotation> correctInstances, Configuration runConfig) {
        return Redactor.removeAnnotations(correctInstances, runConfig);
    }

    /**
     * Retrieve a stored configuration from the database
     *
     * @param conf_id
     *            - database key for the configuration used to define the task
     * @return - The Configuration object from the database
     */
    private static Configuration getConfigurationFromDb(String conf_id) {
        FrontEndDBInterface f = new FrontEndDBInterface();
        Configuration config = f.getConfigInformation(Integer.parseInt(conf_id));
        return config;
    }
    
    public static Evaluator getEvaluator(String conf_id) throws Exception{
        Config conf = ConfigFactory.load();
        Configuration runConfig = getConfigurationFromDb(conf_id);
        FrontEndDBInterface f = new FrontEndDBInterface();
        String evaluator = conf.getString("evaluator.root")+f.getEvaluator(runConfig.task, runConfig.task_variant);
        System.out.println("Evaluator: "+evaluator);
        Class<?> cls = Class.forName(evaluator);
        return (Evaluator)cls.newInstance();
    }

    public static String getEvaluatorView(String conf_id) {
        Configuration runConfig = getConfigurationFromDb(conf_id);
        FrontEndDBInterface f = new FrontEndDBInterface();
        String viewName = f.getEvaluatorView(runConfig.task);
        System.out.println("View: "+viewName);
        return viewName;
    }


    /**
     * Retrieve a stored dataset from the database
     *
     * @param runConfig
     *            - database key for the dataset to use as test data
     * @return - list of TextAnnotation instances from the database
     */
    private static List<TextAnnotation> getInstancesFromDb(Configuration runConfig) {
        Reader reader = new Reader();
        System.out.println("Retrieving instances from db");
        List<TextAnnotation> TextAnnotations = reader.getTextAnnotationsFromDB(runConfig.dataset);
        return TextAnnotations;
    }

    /**
     * Store evaluation from the run into the DB. (Right now only stores the
     * macroEvaluationRecord.)
     */
    private static void storeEvaluationIntoDb(EvaluationRecord eval, String record_id, boolean isRunning) {
        FrontEndDBInterface f = new FrontEndDBInterface();
        f.insertEvaluationIntoDB(eval, Integer.parseInt(record_id), isRunning); //Need to change this so it doesn't just always send true.
    }
}