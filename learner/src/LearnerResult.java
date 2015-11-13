/**
 * Created by rnkelch on 11/8/2015.
 */
public class LearnerResult {
    public String result;
    public boolean successful;
    public String errorMessage;

    public LearnerResult(String result, boolean successful, String errorMessage){
        this.result = result;
        this.successful = successful;
        this.errorMessage = errorMessage;
    }
}
