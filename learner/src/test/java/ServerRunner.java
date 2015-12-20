import java.io.IOException;

/**
 * Created by rnkelch on 12/12/2015.
 */
public class ServerRunner
{
    public static void main(String[] args) throws IOException
    {
        Server server = new Server(5757, new ToyPosAnnotator());
        fi.iki.elonen.util.ServerRunner.executeInstance(server);
    }
}
