package controllers;

import controllers.cleansers.DummyCleanser;
import controllers.cleansers.Cleanser;
import controllers.evaluators.Evaluation;
import controllers.evaluators.Evaluator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

import java.util.List;

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
 	private Evaluator evaluator;

	/** Evaluation containing the evaluation returned by the evaluator. */
 	private Evaluation evaluation;

 	/** List of correct text annotation instances */
 	private List<TextAnnotation> correctInstances;

 	/** List of unprocessed text annotation instances */
	private List<TextAnnotation> unprocessedInstances;

 	/** List of `TextAnnotation` instances returned by the solver */
 	private List<TextAnnotation> solverInstances;

 	public Job(DummySolver solver, List<TextAnnotation> correctInstances, Evaluator evaluator) {
 		this.solver = solver;
 		this.correctInstances = correctInstances;
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
 	 *	Runs the specified evaluator on the instances returned from the solver and stores
 	 *  the results in an Evaluation object.
 	 */
 	public Evaluation evaluateSolver() {
 		//this.evaluation = evaluator.evaluate(correctInstances, solverInstances);
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
 }
