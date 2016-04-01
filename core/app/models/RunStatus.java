package models;
/**
 * Container for the status of a run.
 */
public class RunStatus {

    private int completed;
    private int skipped;
    private int total;
    private String error;
    
    public RunStatus(int completed, int skipped, int total, String error) {
        super();
        this.completed = completed;
        this.skipped = skipped;
        this.total = total;
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
    public String getError() { return error; }
}
