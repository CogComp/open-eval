package models;

import controllers.Domain;
import controllers.evaluators.Evaluation;
import controllers.evaluators.Evaluator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import play.libs.ws.WSResponse;

import java.util.ArrayList;
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
 	private LearnerInterface learnerInterface;

	/** A list of evaluators, used to evaluate solver using an evaluation metric specified in the implementing class. */
 	private Evaluator evaluator;

	/** Evaluation containing the evaluation returned by the evaluator. */
 	private Evaluation evaluation;

 	/** List of unprocessed text annotation instances */
	private List<TextAnnotation> unprocessedInstances;

 	/** List of `TextAnnotation` instances returned by the solver */
 	private List<TextAnnotation> solverInstances;

 	public Job(LearnerInterface learner, List<TextAnnotation> instances) {
 		this.learnerInterface = learner;
 		this.unprocessedInstances = instances;
		this.domain = Domain.TOY;
 	}

 	/** Sends all unprocessed instances to the solver and receives the results. */
 	public WSResponse sendAndReceiveRequestsFromSolver() {
		this.solverInstances = new ArrayList<>();
		WSResponse response = null;
		String resultJson;
		TextAnnotation processedInstance;
 		for (TextAnnotation ta : unprocessedInstances) {
 			response = learnerInterface.processRequest(ta);
			try{
				resultJson = response.getBody();
				processedInstance = SerializationHelper.deserializeFromJson(resultJson);
			}
			catch(Exception e){
				break;
			}
 			solverInstances.add(processedInstance);
 		}
		return response;
 	}
 	
 	/**
 	 *	Runs the specified evaluator on the instances returned from the solver and stores
 	 *  the results in an Evaluation object.
 	 */
 	public Evaluation evaluateSolver() {
 		//this.evaluation = evaluator.evaluate(correctInstances, solverInstances);
		return evaluation;

 	}

	public List<TextAnnotation> getSolverInstances(){
		return this.solverInstances;
	}

	public TextAnnotation getSolverInstance(int i) {
		return getSolverInstances().get(i);
	}
 }
