package controllers;

import play.*;
import play.mvc.*;
import play.mvc.Http.*;

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
		public static int startJob(String conf_id, String url) {
			Configuration runConfig = getConfigurationFromDb(conf_id);
			
			Evaluator newEval = getEvaluator(runConfig);
			List<TextAnnotation> instances = getInstancesFromDb(runConfig);
			
			DummySolver solver = new DummySolver(url);
			int status = solver.testURL();
			System.out.println(status);
			if(status != 200)
				return status;
			Job newJob = new Job(solver, instances, newEval);
			newJob.sendAndReceiveRequestsFromSolver();
			Evaluation eval = newJob.evaluateSolver();
			//TODO: Add new evaluation to database
			return 200;
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
		 * @param dataset - database key for the dataset to use as test data
		 * @return -  list of TextAnnotation instances from the database
		 */
		private static List<TextAnnotation> getInstancesFromDb(Configuration runConfig){
			return null;
		}
		
		/**
		 * UNIMPLEMENTED
		 * Create a new Evaluator to be used on the solved instances
		 * @param evalType - Defines the type of evaluator the user needs
		 * @return - Evaluator object to be used
		 */
		private static Evaluator getEvaluator(Configuration runConfig){
			return null;
		}
}