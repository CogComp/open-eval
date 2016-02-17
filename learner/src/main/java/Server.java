import edu.illinois.cs.cogcomp.annotation.Annotator;
import fi.iki.elonen.router.RouterNanoHTTPD;

import java.io.IOException;

/**
 * This class is responsible for communicating with the Open Eval site. Run the start method to listen for request from
 * the system. Use fi.iki.elonen.util.ServerRunner.executeInstance to run the server without it shutting down when
 * your code is done executing. This is a blocking call, and you can press enter in the command line
 * to shut down the server.
 */
public class Server extends RouterNanoHTTPD
{
    private static String INFO_ROUTE = "/info";
    private static String INSTANCE_ROUTE = "/instance";

    private Annotator annotator;
    private InfoController infoController;
    private InstanceController instanceController;

    /**
     * Creates a server that is ready to bind to the port and use annotator. This does not start the server.
     * @param port The port to bind to
     * @param annotator The annotator that represents your learner.
     * @throws IOException
     */
    public Server(int port, Annotator annotator) throws IOException {
        super(port);
        this.annotator = annotator;
        addMappings();
    }

    @Override
    public void addMappings(){
        super.addMappings();
        addRoute(INSTANCE_ROUTE,InstanceController.class,annotator);
        addRoute(INFO_ROUTE,InfoController.class,annotator);
    }

    public Annotator getAnnotator() {
        return annotator;
    }

}
