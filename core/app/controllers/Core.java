package controllers;

import java.util.ArrayList;
import java.util.List;

import controllers.evaluators.Evaluation;
import controllers.evaluators.Evaluator;
import controllers.evaluators.SpanLabelingEvaluator;
import controllers.evaluators.SpanSplittingEvaluator;
import controllers.io.DatabaseCommunication;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import play.mvc.Controller;
import play.mvc.Result;

public class Core extends Controller {
	
	public  Result startJob() {
		/*
		Map<String, String[]> params = request().body().asFormUrlEncoded();
		List<TextAnnotation> instances = dbQuery(params.get("dataset")[0], params.get("problemType")[0]);
		List<Evaluator> evaluators = getEvaluator(params.get("eval")[0]);
		Job newJob = new Job(params.get("url")[0], instances, newEval);
		*/
		Job job = Job.setUpToyJob();
		job.sendAndReceiveRequestsFromSolver();
		storeSolvedAnnotations(job.getSolverInstances());
		job.evaluateSolver();
		storeEvaluations(job.getEvaluations());
		return ok("Received request");
	}
	
	private void storeSolvedAnnotations(List<TextAnnotation> textAnnotations) {
		// TODO: Implement once a database has been set up.
	}
	
	private void storeEvaluations(List<Evaluation> evaluation) {
		// TODO: Implement once a database has been set up.
	}
	
	private List<TextAnnotation> dbQuery(String datasetName, String problemType){
		DatabaseCommunication dbComm = new DatabaseCommunication();
		return dbComm.retrieveDataset(datasetName);
	}
	
	private List<Evaluator> getEvaluator(String evalType){
		return null;
	}
}