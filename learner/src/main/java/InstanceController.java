import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.router.RouterNanoHTTPD;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class InstanceController implements RouterNanoHTTPD.UriResponder
{
    @Override
    public Response post(RouterNanoHTTPD.UriResource uriResource, Map<String, String> map, NanoHTTPD.IHTTPSession session)
    {
        Annotator annotator = uriResource.initParameter(Annotator.class);
        String body = null;
        try{
            body = readBody(session);
        } catch (Exception ex){
            Response errorResponse = ResponseGenerator.generateErrorResponse(ex, "Error reading request");
            return errorResponse;
        }

        TextAnnotation textAnnotation;
        try
        {
            textAnnotation = SerializationHelper.deserializeFromJson(body);
        } catch (Exception e)
        {
            return ResponseGenerator.generateErrorResponse(e, "There was an error parsing the body");
        }

        try
        {
            annotator.addView(textAnnotation);
        } catch (AnnotatorException e)
        {
            return  ResponseGenerator.generateErrorResponse(e, "There was an error adding the view to the instance");
        }

        String jsonAnnotation = SerializationHelper.serializeToJson(textAnnotation);
        return NanoHTTPD.newFixedLengthResponse(jsonAnnotation);
    }

    private String readBody(NanoHTTPD.IHTTPSession session) throws Exception{
        Map<String, String> files = new HashMap<String, String>();
        String body;
        session.parseBody(files);
        body = files.get("postData");
        return body;
    }

    @Override
    public Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> map, NanoHTTPD.IHTTPSession ihttpSession)
    {
        return ResponseGenerator.generateMethodNotAllowedResponse();
    }

    @Override
    public Response put(RouterNanoHTTPD.UriResource uriResource, Map<String, String> map, NanoHTTPD.IHTTPSession ihttpSession)
    {
        return ResponseGenerator.generateMethodNotAllowedResponse();
    }

    @Override
    public Response delete(RouterNanoHTTPD.UriResource uriResource, Map<String, String> map, NanoHTTPD.IHTTPSession ihttpSession)
    {
        return ResponseGenerator.generateMethodNotAllowedResponse();
    }

    @Override
    public Response other(String s, RouterNanoHTTPD.UriResource uriResource, Map<String, String> map, NanoHTTPD.IHTTPSession ihttpSession)
    {
        return ResponseGenerator.generateMethodNotAllowedResponse();
    }
}
