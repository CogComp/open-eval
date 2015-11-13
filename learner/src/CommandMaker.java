import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by rnkelch on 11/7/2015.
 */
public class CommandMaker {

    private String preJson;
    private String postJson;

    public CommandMaker(String command){
        String replaceRegex = "\\{json}";
        String[] vals = command.split(replaceRegex);
        if (vals.length == 1 && vals[0].length() < command.length()){
            preJson = vals[0];
            postJson = "";
        } else if (vals.length == 2) {
            preJson = vals[0];
            postJson = vals[1];
        } else {
            throw new IllegalArgumentException("There was not exactly one instance of {json} in the command");
        }
    }

    public String makeCommand(String json)
    {
        return preJson + json + postJson;
    }
}
