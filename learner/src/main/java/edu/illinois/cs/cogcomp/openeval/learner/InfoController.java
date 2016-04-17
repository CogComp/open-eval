package edu.illinois.cs.cogcomp.openeval.learner;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import edu.illinois.cs.cogcomp.annotation.Annotator;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

import java.util.Map;

public class InfoController implements RouterNanoHTTPD.UriResponder
{
    @Override
    public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> map, NanoHTTPD.IHTTPSession ihttpSession)
    {
        Annotator annotator = uriResource.initParameter(Annotator.class);
        ServerPreferences serverPreferences = uriResource.initParameter(1, ServerPreferences.class);

        JsonObject jObject = new JsonObject();
        addAnnotatorDetails(jObject, annotator);
        addServerSettings(jObject, serverPreferences);
        String json = jObject.toString();
        return NanoHTTPD.newFixedLengthResponse(json);
    }

    private void addAnnotatorDetails(JsonObject jObject, Annotator annotator){
        String[] requiredViews = annotator.getRequiredViews();
        JsonArray jViews = new JsonArray();
        for(String view:requiredViews){
            jViews.add(new JsonPrimitive(view));
        }
        jObject.add("requiredViews", jViews);
        jObject.add("addedView", new JsonPrimitive(annotator.getViewName()));
    }

    private void addServerSettings(JsonObject jObject, ServerPreferences serverPreferences){
        jObject.add("maxAmountBytesAccepted", new JsonPrimitive(serverPreferences.getMaxAmountBytesAccepted()));
        jObject.add("maxNumInstancesAccepted", new JsonPrimitive(serverPreferences.getMaxNumInstancesAccepted()));
    }

    @Override
    public NanoHTTPD.Response put(RouterNanoHTTPD.UriResource uriResource, Map<String, String> map, NanoHTTPD.IHTTPSession ihttpSession)
    {
        return ResponseGenerator.generateMethodNotAllowedResponse();
    }

    @Override
    public NanoHTTPD.Response post(RouterNanoHTTPD.UriResource uriResource, Map<String, String> map, NanoHTTPD.IHTTPSession ihttpSession)
    {
        return ResponseGenerator.generateMethodNotAllowedResponse();
    }

    @Override
    public NanoHTTPD.Response delete(RouterNanoHTTPD.UriResource uriResource, Map<String, String> map, NanoHTTPD.IHTTPSession ihttpSession)
    {
        return ResponseGenerator.generateMethodNotAllowedResponse();
    }

    @Override
    public NanoHTTPD.Response other(String s, RouterNanoHTTPD.UriResource uriResource, Map<String, String> map, NanoHTTPD.IHTTPSession ihttpSession)
    {
        return ResponseGenerator.generateMethodNotAllowedResponse();
    }
}
