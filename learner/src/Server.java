/**
 * Created by rnkelch on 11/7/2015.
 */

import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;

public class Server extends NanoHTTPD {

    private LearnerRunner learnerRunner;
    StreamReader streamReader;

    public Server(int port, LearnerRunner learnerRunner, StreamReader streamReader) throws IOException {
        super(port);
        this.learnerRunner = learnerRunner;
        this.streamReader = streamReader;
    }

    @Override
    public Response serve(IHTTPSession session){
        Method httpMethod = session.getMethod();
        if (httpMethod.equals(Method.POST)) {
            String json;
            try {
                json = streamReader.readAll(session.getInputStream());
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
                Response errorResponse = new Response("Error reading request");
                errorResponse.setStatus(Response.Status.INTERNAL_ERROR);
                return errorResponse;
            }
            LearnerResult learnerResult = learnerRunner.runCommand(json);
            if (learnerResult.successful){
                return new Response(learnerResult.result);
            } else {
                Response errorResponse = new Response(learnerResult.errorMessage);
                errorResponse.setStatus(Response.Status.INTERNAL_ERROR);
                return errorResponse;
            }
        }
        else {
            Response badRequestResponse = new Response("This server only accepts POST request");
            badRequestResponse.setStatus(Response.Status.METHOD_NOT_ALLOWED);
            return badRequestResponse;
        }
    }
}
