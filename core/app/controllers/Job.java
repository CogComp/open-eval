package controllers;

import java.util.ArrayList;
import java.util.List;

import controllers.cleansers.Cleanser;
import controllers.cleansers.DummyCleanser;
import controllers.evaluators.Evaluation;
import controllers.evaluators.Evaluator;
import controllers.evaluators.SpanLabelingEvaluator;
import controllers.evaluators.SpanSplittingEvaluator;
import controllers.io.DatabaseCommunication;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

/**
 * Class representing one job to send to the solver.  
 *
 * @author Joshua Camp
 */

 public class Job {

	/** Problem domain */
	Domain domain;

 	/** Solver object for processing TextAnnotation objects. */
 	private DummySolver solver;

	/** A list of evaluators, used to evaluate solver using an evaluation metric specified in the implementing class. */
 	private List<Evaluator> evaluators;

	/** Evaluation containing the evaluation returned by the evaluator. */
 	private List<Evaluation> evaluations;

 	/** List of correct text annotation instances */
 	private List<TextAnnotation> correctInstances;

 	/** List of unprocessed text annotation instances */
	private List<TextAnnotation> unprocessedInstances;

 	/** List of TextAnnotation instances returned by the solver */
 	private List<TextAnnotation> solverInstances;

 	public Job(DummySolver solver, List<TextAnnotation> correctInstances, List<Evaluator> evaluator, Domain domain) {
 		this.solver = solver;
 		this.correctInstances = correctInstances;
 		this.solverInstances = new ArrayList<>();
 		this.evaluators = evaluator;
 		this.evaluations = new ArrayList<>();
		this.domain = domain;
		this.populateCleanedAnnotations();
 	}

 	/** Sends all unprocessed instances to the solver and receives the results. */
 	public void sendAndReceiveRequestsFromSolver() {
 		for (TextAnnotation ta : unprocessedInstances) {
 			TextAnnotation processedInstance = solver.processRequest(ta);
 			solverInstances.add(processedInstance);
 		}
 	}
 	
 	/**
 	 *	Runs the specified evaluators on the instances returned from the solver and stores
 	 *  the results in Evaluation objects.
 	 */
 	public void evaluateSolver() {
 		for (Evaluator evaluator : evaluators) {
 			Evaluation evaluation = evaluator.evaluate(correctInstances, solverInstances);
 			evaluations.add(evaluation);
 		}
 	}

	/** Based on the domain type, prepares cleaned instances ready to be sent to a solver */
	private void populateCleanedAnnotations() {
		Cleanser cleanser;
		switch (this.domain)
		{
			/*
			case BINARY_CLASSIFICATION:
				// TODO
				break;
			case MULTICLASS_CLASSIFICATION:
				// TODO
				break;
			case CLUSTERING:
				// TODO
				break;
			*/
			case TOY:
			default:
				cleanser = new DummyCleanser();
				System.out.println("Warning: unknown domain!");
		}

		this.unprocessedInstances = cleanser.removeAnnotations(correctInstances);
	}
	
	public static Job setUpToyJob() {
		DatabaseCommunication dbComm = new DatabaseCommunication();
		
		DummySolver dummySolver = new DummySolver();
		List<Evaluator> evaluators = new ArrayList<>();
		List<TextAnnotation> instances = dbComm.retrieveDataset("toyDataset");
		
		// Create evaluators for the toy job
		SpanLabelingEvaluator spanLabelingEvaluator = new SpanLabelingEvaluator(ViewNames.SENTENCE);
		SpanSplittingEvaluator spanSplittingEvaluator = new SpanSplittingEvaluator(ViewNames.SENTENCE);
		evaluators.add(spanSplittingEvaluator);
		evaluators.add(spanLabelingEvaluator);
		
		return new Job(dummySolver, instances, evaluators, Domain.TOY);
	}

	public Domain getDomain() {
		return domain;
	}

	public List<Evaluation> getEvaluations() {
		return evaluations;
	}

	public List<TextAnnotation> getSolverInstances() {
		return solverInstances;
	}	
	
	public List<TextAnnotation> getCorrectInstances() {
		return correctInstances;
	}
 }
