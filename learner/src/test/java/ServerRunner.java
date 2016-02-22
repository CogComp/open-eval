import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by rnkelch on 12/12/2015.
 */
public class ServerRunner
{
    public static void main(String[] args) throws IOException
    {
        Server server = new Server(5757, new SaulPosAnnotator());
        fi.iki.elonen.util.ServerRunner.executeInstance(server);
    }
    
    @Test
    public void run() throws IOException {
        Server server = new Server(5757, new SaulPosAnnotator());
        fi.iki.elonen.util.ServerRunner.executeInstance(server);
    }
}
