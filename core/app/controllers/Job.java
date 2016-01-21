package controllers;

import java.util.ArrayList;
import java.util.List;

import controllers.cleansers.Cleanser;
import controllers.cleansers.DummyCleanser;
import controllers.evaluators.Evaluation;
import controllers.evaluators.Evaluator;
import controllers.evaluators.SpanLabelingEvaluator;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

/**
 * Class representing one job to send to the solver.  
 */

 public class Job {

	/** Problem domain */
	Domain domain;

 	/** Solver object for processing TextAnnotation objects. */
 	private DummySolver solver;

	/** A list of evaluators, used to evaluate solver using an evaluation metric specified in the implementing class. */
 	private Evaluator evaluator;

	/** Evaluation containing the evaluation returned by the evaluator. */
 	private Evaluation evaluation;

 	/** List of correct text annotation instances */
 	private List<TextAnnotation> correctInstances;

 	/** List of unprocessed text annotation instances */
	private List<TextAnnotation> unprocessedInstances;

 	/** List of TextAnnotation instances returned by the solver */
 	private List<TextAnnotation> solverInstances;

 	public Job(DummySolver solver, List<TextAnnotation> correctInstances, Evaluator evaluator) {
 		this.solver = solver;
 		this.correctInstances = correctInstances;
 		this.solverInstances = new ArrayList<>();
 		this.evaluator = evaluator;
		this.domain = Domain.TOY;
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
 	public Evaluation evaluateSolver() {
 		evaluation = evaluator.evaluate(correctInstances, solverInstances);
		return evaluation;
 	}

	/** Based on the domain type, prepares cleaned instances ready to be sent to a solver */
	private void populateCleanedAnnotations() {
		Cleanser cleanser;
		switch (this.domain)
		{
			/*
			case Domain.BINARY_CLASSIFICATION:
				// TODO
				break;
			case Domain.MULTICLASS_CLASSIFICATION:
				// TODO
				break;
			case Domain.CLUSTERING:
				// TODO
				break;
			*/
			//case Domain.TOY:
			default:
				cleanser = new DummyCleanser();
				System.out.println("Warning: unknown domain!");
		}

		this.unprocessedInstances = cleanser.removeAnnotations(correctInstances);
	}

	public Domain getDomain() {
		return domain;
	}

	public Evaluation getEvaluation() {
		return evaluation;
	}

	public List<TextAnnotation> getSolverInstances() {
		return solverInstances;
	}	
	
	public List<TextAnnotation> getCorrectInstances() {
		return correctInstances;
	}
 }
