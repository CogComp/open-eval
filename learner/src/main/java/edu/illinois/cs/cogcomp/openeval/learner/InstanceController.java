package edu.illinois.cs.cogcomp.openeval.learner;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
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
    @Override
    public Response post(RouterNanoHTTPD.UriResource uriResource, Map<String, String> map, NanoHTTPD.IHTTPSession session)
    {
        Annotator annotator = uriResource.initParameter(Annotator.class);
        JsonArray jInstances;
        JsonParser parser = new JsonParser();

        try{
            String body = readBody(session);
            jInstances = parser.parse(body).getAsJsonArray();
        } catch (Exception ex){
            return ResponseGenerator.generateResponse(String.format("Error reading request: %s", ex.toString()), Response.Status.BAD_REQUEST);
        }

        TextAnnotation[] textAnnotations = new TextAnnotation[jInstances.size()];
        String[] errors = new String[textAnnotations.length];

        parseTextAnnotations(jInstances, textAnnotations, errors);
        annotateInstances(annotator, textAnnotations, errors);

        JsonArray newJInstances = serializeInstances(parser, textAnnotations, errors);

        return NanoHTTPD.newFixedLengthResponse(newJInstances.toString());
    }

    private JsonArray serializeInstances(JsonParser parser, TextAnnotation[] textAnnotations, String[] errors) {
        JsonArray newJInstances = new JsonArray();
        for(int i=0;i<textAnnotations.length;i++){
            JsonObject instanceObject = new JsonObject();

            if(textAnnotations[i] != null) {
                String jsonTextAnnotation = SerializationHelper.serializeToJson(textAnnotations[i]);
                JsonObject jTextAnnotation = parser.parse(jsonTextAnnotation).getAsJsonObject();
                instanceObject.add("textAnnotation", jTextAnnotation);
            }
            if(errors[i] != null){
                instanceObject.add("error", new JsonPrimitive(errors[i]));
            }
            newJInstances.add(instanceObject);
        }
        return newJInstances;
    }

    private void annotateInstances(Annotator annotator, TextAnnotation[] textAnnotations, String[] errors) {
        for(int i=0;i<textAnnotations.length;i++) {
            if (textAnnotations[i] != null){
                try{
                    annotator.addView(textAnnotations[i]);
                } catch (AnnotatorException e) {
                    textAnnotations[i] = null;
                    errors[i] = String.format("There was an error adding the view to the instance: %s", e.toString());
                }
            }
        }
    }

    private void parseTextAnnotations(JsonArray jInstances, TextAnnotation[] textAnnotations, String[] errors) {
        for(int i=0;i<textAnnotations.length;i++){
            try {
                textAnnotations[i] = SerializationHelper.deserializeFromJson(jInstances.get(i).toString());
            } catch (Exception e){
                textAnnotations[i] = null;
                errors[i] = String.format("There was an error parsing the TextAnnotation: %s", e.toString());
            }
        }
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
