package models;

import edu.illinois.cs.cogcomp.core.experiments.ClassificationTester;

/**
 * Container for the status of a run.
 */
public class RunStatus {

    private int completed;
    private int skipped;
    private int total;
    private String error;
    private ClassificationTester eval;
    
    public RunStatus(int completed, int skipped, int total, String error, ClassificationTester eval) {
        super();
        this.completed = completed;
        this.skipped = skipped;
        this.total = total;
        this.error = error;
        this.eval = eval;
    }
	public int getCompleted() {
		return completed;
	}
	public int getSkipped() {
		return skipped;
	}
	public int getTotal() {
		return total;
	}
    public String getError() { return error; }
    public ClassificationTester getEvaluation() { return eval; }
}
