package controllers;

import play.*;
import play.mvc.*;
import play.mvc.Http.*;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import java.util.*;

import controllers.evaluators.Evaluator;
import controllers.evaluators.Evaluation;
import models.Configuration;

public class Core{
	
		public static Evaluation startJob(String conf_id, String url) {
			Configuration runConfig = getConfigurationFromDb(conf_id);
			
			Evaluator newEval = getEvaluator(runConfig.evaluator);
			List<TextAnnotation> instances = getInstancesFromDb(runConfig.dataset);
			
			DummySolver solver = new DummySolver(url);
			
			Job newJob = new Job(solver, instances, newEval);
			newJob.sendAndReceiveRequestsFromSolver();
			Evaluation eval = newJob.evaluateSolver();
			return eval;
		}
		
		private static Configuration getConfigurationFromDb(String conf_id) {
			return null;
		}
		
		private static List<TextAnnotation> getInstancesFromDb(String dataset){
			return null;
		}
		
		private static Evaluator getEvaluator(String evalType){
			return null;
		}
}