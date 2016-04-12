package models;

import edu.illinois.cs.cogcomp.core.experiments.ClassificationTester;

/**
 * Container for the status of a run.
 */
public class RunStatus {

    private int completed;
    private int skipped;
    private int total;
    private ClassificationTester eval;
    private String error;

    public RunStatus(int completed, int skipped, int total, ClassificationTester eval, String error) {
        super();
        this.completed = completed;
        this.skipped = skipped;
        this.total = total;
        this.eval = eval;
        this.error = error;
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
    public ClassificationTester getEvaluation() { return eval; }
    public String getError() { return error; }
}
