package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.experiments.ClassificationTester;
import edu.illinois.cs.cogcomp.core.experiments.EvaluationRecord;
import edu.illinois.cs.cogcomp.core.experiments.evaluators.Evaluator;
import edu.illinois.cs.cogcomp.core.experiments.evaluators.SpanLabelingEvaluator;
import models.Job;
import models.LearnerInterface;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import play.libs.ws.WSResponse;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import java.util.*;

import models.Configuration;

/**
 * This class connects all the back-end modules, i.e. the solver, the evaluation and the database
 */
public class Core {
	
		/**
		 * Send instances to the solver and return back an evaluation on the results
		 * @param conf_id - database key for the configuration used to define the task
		 * @param url - url of the server to send the instances through API calls to
		 * @return - The evaluation on the solver result
		 */
		public static WSResponse startJob(int conf_id, String url) {
			Configuration runConfig = getConfigurationFromDb(conf_id);
			List<TextAnnotation> instances;
			try {
				instances = getInstancesFromDb(runConfig);
			}
			catch(java.lang.RuntimeException e){
				return null;
			}

			LearnerInterface learner = new LearnerInterface(url);

			String jsonInfo = learner.getInfo();
			if(jsonInfo.equals("err"))
				return null;

			List<TextAnnotation> cleansedInstances = cleanseInstances(instances, jsonInfo);
			if(cleansedInstances == null){
				return null;
			}

			Job newJob = new Job(learner, cleansedInstances);
			WSResponse solverResponse = newJob.sendAndReceiveRequestsFromSolver();

			List<TextAnnotation> solvedInstances = newJob.getSolverInstances();
			EvaluationRecord eval = evaluate(runConfig, instances, solvedInstances);
			return solverResponse;
		}
		
		/**
		 * Retrieve a stored configuration from the database
		 * @param conf_id - database key for the configuration used to define the task
		 * @return - The Configuration object from the database
		 */
		private static Configuration getConfigurationFromDb(int conf_id) {
			FrontEndDBInterface db = new FrontEndDBInterface();
			return db.getConfigInformation(conf_id);
		}
		
		/**
		 * UNIMPLEMENTED 
		 * Retrieve a stored dataset from the database
		 * @param runConfig - database key for the dataset to use as test data
		 * @return -  list of TextAnnotation instances from the database
		 */
		private static List<TextAnnotation> getInstancesFromDb(Configuration runConfig){
			return null;
		}
		
		/**
		 * Create a new Evaluator to be used on the solved instances
		 * @param runConfig - Defines the type of evaluator the user needs
		 * @return - Evaluator object to be used
		 */
		public static EvaluationRecord evaluate(Configuration runConfig, List<TextAnnotation> correctInstances, List<TextAnnotation> solvedInstances){
			Evaluator evaluator = new SpanLabelingEvaluator();
			ClassificationTester eval = new ClassificationTester();
			for(int i=0; i<correctInstances.size(); i++){
				View gold = correctInstances.get(i).getView(ViewNames.POS);
				View predicted = solvedInstances.get(i).getView(ViewNames.POS);
				evaluator.setViews(gold, predicted);
				evaluator.evaluate(eval);
			}
			return eval.getEvaluationRecord();
		}

	/**
	 * UNIMPLEMENTED
	 * Cleanse the correct instances
	 * @param correctInstances - The correct instances from the databse
	 * @param jsonInfo - The information given by the learner
	 * @return  - The cleansed instance
	 */
		public static List<TextAnnotation> cleanseInstances(List<TextAnnotation> correctInstances, String jsonInfo){
			JSONParser parser = new JSONParser();
			List<String> requiredViews = null;
			try{
				JSONObject obj = (JSONObject) parser.parse(jsonInfo);
				requiredViews = (List<String>) obj.get("requiredViews");
			}catch(Exception pe) {
				pe.printStackTrace();
				return null;
			}
			return Redactor.removeAnnotations(correctInstances, requiredViews);
		}
}