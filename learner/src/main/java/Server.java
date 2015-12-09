import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Server extends NanoHTTPD {

    private TextAnnotator textAnnotator;

    public Server(int port, TextAnnotator textAnnotator) throws IOException {
        super(port);
        this.textAnnotator = textAnnotator;
    }

    @Override
    public Response serve(IHTTPSession session){
        Map<String, String> files = new HashMap<String, String>();
        Method httpMethod = session.getMethod();
        if (httpMethod.equals(Method.POST)) {
            String json;
            try {
                session.parseBody(files);
            } catch (IOException ex) {
                Response errorResponse = generateErrorResonse(ex, "Error reading request");
                return errorResponse;
            } catch (ResponseException e)
            {
                Response errorResponse = generateErrorResonse(e,"Error reading request");
                return errorResponse;
            }
            json = files.get("postData");

            TextAnnotation partial = null;
            try
            {
                partial = SerializationHelper.deserializeFromJson(json);
            } catch (Exception e)
            {
                Response errorResponse = generateErrorResonse(e, "Error parsing text annotation");
                return errorResponse;
            }
            TextAnnotatorResult textAnnotatorResult = textAnnotator.run(partial);

            if (textAnnotatorResult.successful){
                String jsonResult = SerializationHelper.serializeToJson(textAnnotatorResult.textAnnotation);
                return new Response(jsonResult);
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

    private Response generateErrorResonse(Exception e, String msg)
    {
        e.printStackTrace(System.err);
        Response errorResponse = new Response(msg);
        errorResponse.setStatus(Response.Status.INTERNAL_ERROR);
        return errorResponse;
    }
}
