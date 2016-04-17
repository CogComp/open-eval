package edu.illinois.cs.cogcomp.openeval.learner;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import fi.iki.elonen.router.RouterNanoHTTPD;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

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
    private ServerPreferences serverPreferences;
    private InfoController infoController;
    private InstanceController instanceController;

    /**
     * Creates a server that is ready to bind to the port and use annotator. This does not start the server.
     * @param port The port to bind to
     * @param serverPreferences The settings of the edu.illinois.cs.cogcomp.openeval.learner.Server instance
     * @param annotator The annotator that represents your learner.
     * @throws IOException
     */
    public Server(int port, ServerPreferences serverPreferences, Annotator annotator) throws IOException {
        super(port);
        this.annotator = annotator;
        this.serverPreferences = serverPreferences;
        addMappings();
    }

    @Override
    public void addMappings(){
        super.addMappings();
        addRoute(INSTANCE_ROUTE,InstanceController.class, annotator);
        addRoute(INFO_ROUTE,InfoController.class,annotator, serverPreferences);
    }

    @Override
    public void start() throws IOException {
        super.start();
        printListeningAddress();
    }

    @Override
    public void start(int timeout, boolean dameon) throws IOException {
        super.start(timeout, dameon);
        printListeningAddress();
    }

    private void printListeningAddress() throws UnknownHostException {
        InetAddress address = getLocalAddress();
        int port  = this.getListeningPort();
        System.out.println(String.format("Your learner endpoint is listening at address is http://%s:%d/", address.toString(), port));
    }

    private static InetAddress getLocalAddress(){
        try {
            Enumeration<NetworkInterface> b = NetworkInterface.getNetworkInterfaces();
            while( b.hasMoreElements()){
                for ( InterfaceAddress f : b.nextElement().getInterfaceAddresses())
                    if ( f.getAddress().isSiteLocalAddress())
                        return f.getAddress();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Annotator getAnnotator() {
        return annotator;
    }

}
