import edu.illinois.cs.cogcomp.annotation.Annotator;
import fi.iki.elonen.router.RouterNanoHTTPD;

import java.io.IOException;

public class Server extends RouterNanoHTTPD
{
    private static String INFO_ROUTE = "/info";
    private static String INSTANCE_ROUTE = "/instance";

    private Annotator annotator;
    private InfoController infoController;
    private InstanceController instanceController;

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
