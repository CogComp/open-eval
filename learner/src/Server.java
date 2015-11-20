/**
 * Created by rnkelch on 11/7/2015.
 */

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;

public class Server extends NanoHTTPD {

    private TextAnnotator textAnnotator;
    StreamReader streamReader;

    public Server(int port, TextAnnotator textAnnotator, StreamReader streamReader) throws IOException {
        super(port);
        this.textAnnotator = textAnnotator;
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

            TextAnnotation partial = TextAnnotationSerializer.deserialize(json);
            TextAnnotatorResult textAnnotatorResult = textAnnotator.run(partial);

            if (textAnnotatorResult.successful){
                String resultJson = TextAnnotationSerializer.serialize(textAnnotatorResult.textAnnotation);
                return new Response(resultJson);
            } else {
                Response errorResponse = new Response(textAnnotatorResult.errorMessage);
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
