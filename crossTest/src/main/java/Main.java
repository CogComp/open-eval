import edu.illinois.cs.cogcomp.openeval.learner.Server;
import edu.illinois.cs.cogcomp.openeval.learner.ServerPreferences;

import java.io.IOException;

/**
 * Created by rnkelch on 12/12/2015.
 */
public class Main
{
    public static void main(String[] args) throws IOException
    {
        ServerPreferences serverPreferences = new ServerPreferences(1000000, 25);
        Server server = new Server(5757, serverPreferences, new SaulPosAnnotator());
        fi.iki.elonen.util.ServerRunner.executeInstance(server);
    }
}
