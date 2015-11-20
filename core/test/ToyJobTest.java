import static org.junit.Assert.*;

import org.junit.Test;

import controllers.Job;

public class ToyJobTest {
	
	@Test
	public void testToyJob() {
		Job job = Job.setUpToyJob();
		assertNotNull(job.getCorrectInstances());
		job.sendAndReceiveToyRequestsFromDummySolver();
		assertEquals(job.getCorrectInstances().size(), job.getSolverInstances().size());
		job.evaluateSolver();
		assertNotNull(job.getEvaluation());
	}

}
