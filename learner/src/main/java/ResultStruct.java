/**
 * Created by rnkelch on 12/12/2015.
 */
public class ResultStruct
{
    String result;
    boolean error;
    String errorText;

    public ResultStruct(String result, boolean error, String errorText){
        this.result = result;
        this.error = error;
        this.errorText = errorText;
    }
}
