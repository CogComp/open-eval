package controllers;

import play.*;
import play.mvc.*;
import play.mvc.Http.*;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import java.util.*;

public class Core extends Controller{
	
		public  Result startJob() {
			Map<String, String[]> params = request().body().asFormUrlEncoded();
			List<TextAnnotation> instances = dbQuery(params.get("dataset")[0], params.get("problemType")[0]);
			Evaluator newEval = getEvaluator(params.get("eval")[0]);
			Job newJob = new Job(params.get("url")[0], instances, newEval);
			newJob.sendAndReceiveRequestsFromSolver();
			return ok("Received request");
		}
		
		private List<TextAnnotation> dbQuery(String dataset, String problemType){
			return null;
		}
		
		private Evaluator getEvaluator(String evalType){
			return null;
		}
}