package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
		Job job = setUpToyJob();
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
	
	private Job setUpToyJob() {
		DummySolver dummySolver = new DummySolver();
		List<Evaluator> evaluators = new ArrayList<>();
		List<TextAnnotation> instances = dbQuery("toyDataset", "");
		
		// Create evaluators for the toy job
		SpanLabelingEvaluator spanLabelingEvaluator = new SpanLabelingEvaluator();
		SpanSplittingEvaluator spanSplittingEvaluator = new SpanSplittingEvaluator();
		evaluators.add(spanSplittingEvaluator);
		evaluators.add(spanLabelingEvaluator);
		
		return new Job(dummySolver, instances, evaluators, Domain.TOY);
	}
}