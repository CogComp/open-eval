import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Created by rnkelch on 11/7/2015.
 */
public class LearnerRunner {

    private CommandMaker commandMaker;
    private StreamReader streamReader;

    public LearnerRunner(CommandMaker commandMaker, StreamReader streamReader)
    {
        this.commandMaker = commandMaker;
        this.streamReader = streamReader;
    }

    public LearnerResult runCommand(String json)
    {
        String command = commandMaker.makeCommand(json);

        Runtime runtime = Runtime.getRuntime();
        Process process;

        try {
            process = runtime.exec(command);
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return new LearnerResult(null, false, "Unable to start learner");
        }

        try {
            String result = streamReader.readAll(process.getInputStream());
            int exitVal = process.waitFor();
            if (exitVal != 0) {
                String error = streamReader.readAll(process.getErrorStream());
                return new LearnerResult(null, false, error);
            }
            else {
                return new LearnerResult(result, true, null);
            }

        } catch(InterruptedException e) {
            e.printStackTrace(System.err);
            return new LearnerResult(null, false, "The learner was interrupted");
        }
        catch (IOException ioe) {
            ioe.printStackTrace(System.err);
            return new LearnerResult(null, false, "Error reading result from learner");
        }
    }
}
