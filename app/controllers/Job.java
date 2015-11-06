package controllers;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import java.util.*;

/**
 * Class representing one job to send to the solver.  
 *
 * @author Joshua Camp
 */
 
 public class Job {
 
 	/**
 	 *	Solver object for processing TextAnnotation objects.
 	 */
 	private DummySolver solver;
 	/**
 	 *  Evaluator interface.  Evaluates solver using an evaluation metric specified in the implementing class.
 	 */
 	private Evaluator evaluator;
 	/**
 	 *  Evaluation containing the evaluation returned by the evaluator.
 	 */
 	private Evaluation evaluation;
 	/**
 	 *	List of correct text annotation instances.
 	 */
 	private List<TextAnnotation> correctInstances;
 	/**
 	 *	List of unprocessed text annotation instances.
 	 */
 	private List<TextAnnotation> unprocessedInstances;
 	/**
 	 *  List of text annotation instances returned by the solver.
 	 */
 	private List<TextAnnotation> solverInstances;

 	public Job(DummySolver solver, List<TextAnnotation> correctInstances, Evaluator evaluator) {
 		this.solver = solver;
 		this.correctInstances = correctInstances;
 		this.evaluator = evaluator;
 		this.unprocessedInstances = removeAnnotations(correctInstances);
 	}

 	/**
 	 *	Removes annotations from solved instances.
 	 */
 	private List<TextAnnotation> removeAnnotations(List<TextAnnotation> textAnnotations) {
 		List<TextAnnotation> annotationsWithoutTokens = new ArrayList<>();
 		for (TextAnnotation textAnnotation : textAnnotations) {
 			String corpusId = textAnnotation.getCorpusId();
 			String id = textAnnotation.getId();
 			String text = textAnnotation.getText();

 			//TODO: Retrieve character offsets from the TextAnnotation.
 			//TODO: Retrieve sentence end positions.

 			String[] tokens = textAnnotation.getTokens();
 			for (String token : tokens) {
 				token = "";
 			}
 		}
 		return annotationsWithoutTokens;
 	}

 	/**
 	 *	Sends all unprocessed instances to the solver and receives the results.
 	 */
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
 	public void evaluateSolver() {
 		this.evaluation = evaluator.evaluate(correctInstances, solverInstances);
 	}
 }
