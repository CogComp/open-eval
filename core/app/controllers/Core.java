package controllers;

import models.Job;
import models.LearnerInterface;
import play.libs.ws.WSResponse;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import java.util.*;

import controllers.evaluators.Evaluator;
import controllers.evaluators.Evaluation;
import models.Configuration;

/**
 * This class connects all the back-end modules, i.e. the solver, the evaluation and the database
 */
public class Core{
	
		/**
		 * Send instances to the solver and return back an evaluation on the results
		 * @param conf_id - database key for the configuration used to define the task
		 * @param url - url of the server to send the instances through API calls to
		 * @return - The evaluation on the solver result
		 */
		public static WSResponse startJob(String conf_id, String url) {
			Configuration runConfig = getConfigurationFromDb(conf_id);
			
			Evaluator newEval = getEvaluator(runConfig);
			List<TextAnnotation> instances = getInstancesFromDb(runConfig);

			LearnerInterface learner = new LearnerInterface(url);

			String jsonInfo = learner.getInfo();
			if(jsonInfo.equals("err"))
				return null;

			instances = cleanseInstances(instances, jsonInfo);

			Job newJob = new Job(learner, instances);
			WSResponse solverResponse = newJob.sendAndReceiveRequestsFromSolver();
			Evaluation eval = newJob.evaluateSolver();
			//TODO: Add new evaluation to database
			return solverResponse;
		}
		
		/**
		 * UNIMPLEMENTED
		 * Retrieve a stored configuration from the database
		 * @param conf_id - database key for the configuration used to define the task
		 * @return - The Configuration object from the database
		 */
		private static Configuration getConfigurationFromDb(String conf_id) {
			return null;
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
		 * UNIMPLEMENTED
		 * Create a new Evaluator to be used on the solved instances
		 * @param runConfig - Defines the type of evaluator the user needs
		 * @return - Evaluator object to be used
		 */
		private static Evaluator getEvaluator(Configuration runConfig){
			return null;
		}

	/**
	 * UNIMPLEMENTED
	 * Cleanse the correct instances
	 * @param correctInstances - The correct instances from the databse
	 * @param jsonInfo - The information given by the learner
	 * @return  - The cleansed instance
	 */
		private static List<TextAnnotation> cleanseInstances(List<TextAnnotation> correctInstances, String jsonInfo){
			return correctInstances;
		}
}