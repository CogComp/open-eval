package edu.illinois.cs.cogcomp.openeval.learner;

import com.google.gson.*;
import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.openeval.learner.InfoController;
import edu.illinois.cs.cogcomp.openeval.learner.ServerPreferences;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InfoControllerTest
{

    @Test
    public void testGet() throws Exception
    {
        InfoController infoController = new InfoController();
        Annotator annotator = mock(Annotator.class);
        String[] requiredViews = new String[]{"a", "b", "c"};
        when(annotator.getRequiredViews()).thenReturn(requiredViews);
        when(annotator.getViewName()).thenReturn("view");

        RouterNanoHTTPD.UriResource uriResource = mock(RouterNanoHTTPD.UriResource.class);
        when(uriResource.initParameter(Annotator.class)).thenReturn(annotator);

        ServerPreferences serverPreferences = new ServerPreferences(100, 10);
        when(uriResource.initParameter(1, ServerPreferences.class)).thenReturn(serverPreferences);

        NanoHTTPD.Response response = infoController.get(uriResource, null, null);
        String body = IOUtils.toString(response.getData());

        JsonObject object = new JsonParser().parse(body).getAsJsonObject();

        assertInfoEqual(object, requiredViews, "view", serverPreferences);
        assertEquals(NanoHTTPD.Response.Status.OK, response.getStatus());
    }

    public static void assertInfoEqual(JsonObject object, String[] requiredViews, String addedView, ServerPreferences serverPreferences) {
        JsonArray expectedArray = new JsonArray();
        for(String viewName : requiredViews){
            expectedArray.add(new JsonPrimitive(viewName));
        }

        assertEquals(object.get("requiredViews").getAsJsonArray(),expectedArray);
        assertEquals(object.get("addedView").getAsString(), addedView);
        assertEquals(object.get("maxAmountBytesAccepted").getAsInt(), serverPreferences.getMaxAmountBytesAccepted());
        assertEquals(object.get("maxNumInstancesAccepted").getAsInt(), serverPreferences.getMaxNumInstancesAccepted());
    }


    @Test
    public void testPost() throws Exception
    {
        InfoController infoController = new InfoController();
        NanoHTTPD.Response response = infoController.post(null,null,null);

        String body = IOUtils.toString(response.getData());

        assertEquals("Error, method not allowed", body);
        assertEquals(NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED, response.getStatus());
    }
}