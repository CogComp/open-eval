package controllers;

import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import controllers.readers.POSReader;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.experiments.ClassificationTester;
import edu.illinois.cs.cogcomp.core.experiments.EvaluationRecord;
import edu.illinois.cs.cogcomp.core.experiments.evaluators.ConstituentLabelingEvaluator;
import edu.illinois.cs.cogcomp.core.experiments.evaluators.Evaluator;
import models.Configuration;
import models.Job;
import models.LearnerInterface;
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
    public static String testConnection(String url) {
        LearnerInterface learner = new LearnerInterface(url);
        String jsonInfo = learner.getInfo();
        if (jsonInfo.equals("err")) {
            System.out.println("Could not connect to server");
            return null;
        }
        return jsonInfo;
    }

    /**
     * Gets the instances from the database, cleanses them, and returns the
     * resulting Job.
     */
    public static Job setUpJob(String conf_id, String url, String record_id) {
        Configuration runConfig = getConfigurationFromDb(conf_id);

        List<TextAnnotation> correctInstances = getInstancesFromDb(runConfig);
        System.out.println(url);
        LearnerInterface learner = new LearnerInterface(url);
        String jsonInfo = learner.getInfo();
        if (jsonInfo.equals("err")) {
            System.out.println("Could not connect to server");
            return null;
        }
        List<TextAnnotation> cleansedInstances =  cleanseInstances(correctInstances, jsonInfo);
        if (cleansedInstances == null) {
            System.out.println("Error in cleanser");
            return null;
        }
        return new Job(learner, cleansedInstances, correctInstances);
    }

    public static void storeResultsOfRunInDatabase(ClassificationTester evaluation, String record_id) {
        storeEvaluationIntoDb(evaluation.getEvaluationRecord(), record_id);
    }

	public static void evaluate(Evaluator evaluator, ClassificationTester eval,  TextAnnotation gold,
			TextAnnotation predicted) {
		View goldView = gold.getView(ViewNames.POS);
		View predictedView = predicted.getView(ViewNames.POS);
		evaluator.setViews(goldView, predictedView);
		evaluator.evaluate(eval);
	}

    /**
     * Cleanse the correct instances
     *
     * @param correctInstances
     *            - The correct instances from the databse
     * @param jsonInfo
     *            - The information given by the learner
     * @return - The cleansed instance
     */
    public static List<TextAnnotation> cleanseInstances(List<TextAnnotation> correctInstances, String jsonInfo) {
        System.out.println("Cleansing");
        JSONParser parser = new JSONParser();
        List<String> requiredViews = null;
        try {
            JSONObject obj = (JSONObject) parser.parse(jsonInfo);
            requiredViews = (List<String>) obj.get("requiredViews");
        } catch (Exception pe) {
            pe.printStackTrace();
            return null;
        }
        if (requiredViews == null)
            return null;
        return Redactor.removeAnnotations(correctInstances, requiredViews);
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
    
    public static Evaluator getEvaluator(String conf_id) {
        Configuration runConfig = getConfigurationFromDb(conf_id);
        Evaluator evaluator = null;
        
        switch (runConfig.evaluator) {
            case "Constituent Labeling": 
                evaluator = new ConstituentLabelingEvaluator();
                break;
        }
        
        return evaluator;
    }

    /**
     * Retrieve a stored dataset from the database
     *
     * @param runConfig
     *            - database key for the dataset to use as test data
     * @return - list of TextAnnotation instances from the database
     */
    private static List<TextAnnotation> getInstancesFromDb(Configuration runConfig) {
        POSReader posReader = new POSReader();
        System.out.println("Retrieving instances from db");
        List<TextAnnotation> TextAnnotations = posReader.getTextAnnotationsFromDB(runConfig.dataset);
        return TextAnnotations;
    }

    /**
     * Store evaluation from the run into the DB. (Right now only stores the
     * macroEvaluationRecord.)
     */
    private static void storeEvaluationIntoDb(EvaluationRecord eval, String record_id) {
        FrontEndDBInterface f = new FrontEndDBInterface();
        f.insertEvaluationIntoDB(eval, Integer.parseInt(record_id));
    }
}