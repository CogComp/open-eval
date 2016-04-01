package models;

import controllers.Domain;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import play.libs.F.Promise;
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

	/** List of unprocessed text annotation instances */
	private List<TextAnnotation> unprocessedInstances;

	/** List of correct text annotation instances */
	private List<TextAnnotation> goldInstances;

	/** List of `TextAnnotation` instances returned by the solver */
	private List<TextAnnotation> solverInstances;

	private List<Boolean> skip;

	public Job(LearnerInterface learner, List<TextAnnotation> cleansedInstances, List<TextAnnotation> goldInstances) {
		this.learnerInterface = learner;
		this.unprocessedInstances = cleansedInstances;
		this.goldInstances = goldInstances;
		this.solverInstances = new ArrayList<>();
		this.domain = Domain.TOY;
		skip = new ArrayList<>();
	}

	public Promise<LearnerInstancesResponse> sendAndReceiveRequestsFromSolver(List<TextAnnotation> tas) {
		return learnerInterface.processRequests(tas);
	}


	public List<TextAnnotation> getUnprocessedInstances() {
		return unprocessedInstances;
	}

	public List<Boolean> getSkip() {
		return this.skip;
	}

	public List<TextAnnotation> getSolverInstances() {
		return this.solverInstances;
	}

	public List<TextAnnotation> getGoldInstances() { return this.goldInstances; }

	public TextAnnotation getSolverInstance(int i) {
		return getSolverInstances().get(i);
	}
}
