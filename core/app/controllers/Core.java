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
		return new Job(learner, cleansedInstances);
	}

	public static void storeResultsOfRunInDatabase(Job newJob, String record_id, String conf_id) {
		Configuration runConfig = getConfigurationFromDb(conf_id);
		List<TextAnnotation> correctInstances = getInstancesFromDb(runConfig);
		List<TextAnnotation> solvedInstances = newJob.getSolverInstances();
		List<Boolean> skip = newJob.getSkip();
		EvaluationRecord eval = evaluate(runConfig, correctInstances, solvedInstances, skip);
		storeEvaluationIntoDb(eval, record_id);
	}

	/**
	 * Send instances to the solver and return back an evaluation on the results
	 *
	 * @param conf_id
	 *            - database key for the configuration used to define the task
	 * @param url
	 *            - url of the server to send the instances through API calls to
	 * @return - The evaluation on the solver result
	 */
	public static WSResponse startJob(String conf_id, String url, String record_id) {
		Configuration runConfig = getConfigurationFromDb(conf_id);

		List<TextAnnotation> correctInstances = getInstancesFromDb(runConfig);
		List<TextAnnotation> cleansedInstances = getInstancesFromDb(runConfig);
		System.out.println(url);
		LearnerInterface learner = new LearnerInterface(url);
		String jsonInfo = learner.getInfo();
		if (jsonInfo.equals("err")) {
			System.out.println("Could not connect to server");
			return null;
		}
		cleanseInstances(cleansedInstances, jsonInfo);
		if (cleansedInstances == null) {
			System.out.println("Error in cleanser");
			return null;
		}
		Job newJob = new Job(learner, cleansedInstances);
		WSResponse solverResponse = newJob.sendAndReceiveRequestsFromSolver();
		List<TextAnnotation> solvedInstances = newJob.getSolverInstances();
		List<Boolean> skip = newJob.getSkip();
		EvaluationRecord eval = evaluate(runConfig, correctInstances, solvedInstances, skip);
		storeEvaluationIntoDb(eval, record_id);
		System.out.println(solverResponse);
		return solverResponse;
	}

	/**
	 * Retrieve a stored configuration from the database
	 *
	 * @param conf_id
	 *            - database key for the configuration used to define the task
	 * @return - The Configuration object from the database
	 */
	private static Configuration getConfigurationFromDb(int conf_id) {
		FrontEndDBInterface db = new FrontEndDBInterface();
		return db.getConfigInformation(conf_id);
	}

	/**
	 * Create a new Evaluator to be used on the solved instances
	 *
	 * @param runConfig
	 *            - Defines the type of evaluator the user needs
	 * @return - Evaluator object to be used
	 */
	public static EvaluationRecord evaluate(Configuration runConfig, List<TextAnnotation> correctInstances,
			List<TextAnnotation> solvedInstances, List<Boolean> skip) {
		Evaluator evaluator = new ConstituentLabelingEvaluator();
		ClassificationTester eval = new ClassificationTester();
		for (int i = 0; i < correctInstances.size(); i++) {
			if (skip.get(i)) {
				continue;
			}
			View gold = correctInstances.get(i).getView(ViewNames.POS);
			View predicted = solvedInstances.get(i).getView(ViewNames.POS);
			evaluator.setViews(gold, predicted);
			evaluator.evaluate(eval);
		}
		System.out.println(eval.getEvaluationRecord());
		return eval.getEvaluationRecord();
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

	/**
	 * Retrieve a stored dataset from the database
	 *
	 * @param runConfig
	 *            - database key for the dataset to use as test data
	 * @return - list of TextAnnotation instances from the database
	 */
	private static List<TextAnnotation> getInstancesFromDb(Configuration runConfig) {
		String datasetName = runConfig.dataset;
		POSReader posReader = new POSReader();
		System.out.println("Retrieving instances from db");
		// TODO: don't hardcode this
		List<TextAnnotation> TextAnnotations = posReader.getTextAnnotationsFromDB("test-10.br");
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