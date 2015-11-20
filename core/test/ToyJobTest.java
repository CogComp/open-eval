import static org.junit.Assert.*;

import org.junit.Test;

import controllers.Job;

public class ToyJobTest {

	private final int NUM_EVALUATIONS = 2;
	
	@Test
	public void testToyJob() {
		Job job = Job.setUpToyJob();
		assertNotNull(job.getCorrectInstances());
		job.sendAndReceiveRequestsFromSolver();
		assertEquals(job.getCorrectInstances().size(), job.getSolverInstances().size());
		job.evaluateSolver();
		assertEquals(NUM_EVALUATIONS, job.getEvaluations().size());
	}

}
