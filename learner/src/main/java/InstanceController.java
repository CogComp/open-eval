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
        Map<String, String> files = new HashMap<String, String>();
        String json;
        try {
            session.parseBody(files);
        } catch (IOException ex) {
            Response errorResponse = ResponseGenerator.generateErrorResponse(ex, "Error reading request");
            return errorResponse;
        } catch (NanoHTTPD.ResponseException e)
        {
            Response errorResponse = ResponseGenerator.generateErrorResponse(e,"Error reading request");
            return errorResponse;
        }
        json = files.get("postData");
        ResultStruct result = this.getInstance(annotator,json);
        if (result.error){
            return ResponseGenerator.generateResponse(result.errorText, Response.Status.INTERNAL_ERROR);
        }
        else {
            return NanoHTTPD.newFixedLengthResponse(result.result);
        }
    }

    public ResultStruct getInstance(Annotator annotator, String body){
        TextAnnotation textAnnotation;
        try
        {
            textAnnotation = SerializationHelper.deserializeFromJson(body);
        } catch (Exception e)
        {
            return new ResultStruct(null,true,"There was an error parsing the body: " + e.toString());
        }

        try
        {
            annotator.addView(textAnnotation);
        } catch (AnnotatorException e)
        {
            String errorMessage = "There was an error adding the view to the instance: " + e.toString();
            return new ResultStruct(null,true,errorMessage);
        }

        String jsonAnnotation = SerializationHelper.serializeToJson(textAnnotation);
        return new ResultStruct(jsonAnnotation,false,null);
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
