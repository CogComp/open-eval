import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.router.RouterNanoHTTPD;

import java.util.HashMap;
import java.util.Map;

public class InstanceController implements RouterNanoHTTPD.UriResponder
{
    public static final String INSTANCES_KEY = "instances";
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

        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(body).getAsJsonObject();
        JsonArray jInstances = jsonObject.get(INSTANCES_KEY).getAsJsonArray();

        TextAnnotation[] textAnnotations = new TextAnnotation[jInstances.size()];
        try
        {
            for(int i=0;i<textAnnotations.length;i++){
                textAnnotations[i] = SerializationHelper.deserializeFromJson(jInstances.get(i).toString());
            }
        } catch (Exception e)
        {
            return ResponseGenerator.generateErrorResponse(e, "There was an error parsing the body");
        }

        try
        {
            for(TextAnnotation textAnnotation: textAnnotations) {
                annotator.addView(textAnnotation);
            }
        } catch (AnnotatorException e)
        {
            return  ResponseGenerator.generateErrorResponse(e, "There was an error adding the view to the instance");
        }

        JsonArray newJInstances = new JsonArray();
        for(int i=0;i<textAnnotations.length;i++){
            String jsonTextAnnotation = SerializationHelper.serializeToJson(textAnnotations[i]);
            JsonObject jInstance = parser.parse(jsonTextAnnotation).getAsJsonObject();
            newJInstances.add(jInstance);
        }
        JsonObject result = new JsonObject();
        result.add(INSTANCES_KEY, newJInstances);
        return NanoHTTPD.newFixedLengthResponse(result.getAsString());
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
