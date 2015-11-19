package controllers;

import java.util.List;
import java.util.Map;

import controllers.evaluators.Evaluator;
import controllers.io.DatabaseCommunication;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import play.mvc.Controller;
import play.mvc.Result;

public class Core extends Controller {
	
	public  Result startJob() {
		Map<String, String[]> params = request().body().asFormUrlEncoded();
		List<TextAnnotation> instances = dbQuery(params.get("dataset")[0], params.get("problemType")[0]);
		List<Evaluator> evaluators = getEvaluator(params.get("eval")[0]);
		//Job newJob = new Job(params.get("url")[0], instances, newEval);
		DummySolver dummySolver = new DummySolver();
		Job job = new Job(dummySolver, instances, evaluators, Domain.TOY);
		job.sendAndReceiveRequestsFromSolver();
		return ok("Received request");
	}
	
	private List<TextAnnotation> dbQuery(String datasetName, String problemType){
		DatabaseCommunication dbComm = new DatabaseCommunication();
		return dbComm.retrieveDataset(datasetName);
	}
	
	private List<Evaluator> getEvaluator(String evalType){
		return null;
	}
}