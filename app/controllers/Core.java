package controllers;

import play.*;
import play.mvc.*;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import java.util.*;

public class Core extends Controller{
	
		public Result startJob() {
			Map<String, String[]> params = request.body.asFormUrlEncoded();
			List<TextAnnotation> instances = dbQuery(params.get("dataset"), params.get("problemType"));
			Evaluator newEval = getEvaluator(params.get("eval"));
			Job newJob = new Job(params.get("url"), instances, newEval);
		}
		
		private List<TextAnnotation> dbQuery(String dataset, String problemType){
			return null;
		}
		
		private Evaluator getEvaluator(String evalType){
			return null;
		}
}