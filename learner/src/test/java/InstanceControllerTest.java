import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class InstanceControllerTest
{

    private JsonParser parser;

    @Before
    public void setup(){
        parser = new JsonParser();
    }

    @Test
    public void testPost() throws Exception
    {
        TestCase testCase = new TestCase();
        NanoHTTPD.Response response = testCase.execute(getMultipleAnnotationRequestBody());
        JsonArray instances = getInstancesFromJson(response);

        assertEquals(2, instances.size());

        JsonObject instance = instances.get(0).getAsJsonObject();
        assertTrue(instance.has("textAnnotation"));
        assertFalse(instance.has("error"));

        assertEquals(NanoHTTPD.Response.Status.OK, response.getStatus());
    }

    @Test
    public void testLearnerError() throws Exception
    {
        TestCase testCase = new TestCase();
        doThrow(new AnnotatorException("")).when(testCase.annotator).addView(any());
        NanoHTTPD.Response response = testCase.execute(getMultipleAnnotationRequestBody());
        JsonArray instances = getInstancesFromJson(response);

        assertEquals(2, instances.size());

        JsonObject instance = instances.get(0).getAsJsonObject();

        assertTrue(instance.has("error"));
        assertFalse(instance.has("textAnnotation"));
        String error = instance.get("error").getAsString();

        assertTrue(error.startsWith("There was an error adding the view to the instance:"));
        assertEquals(NanoHTTPD.Response.Status.OK, response.getStatus());
    }

    @Test
    public void testGet() throws Exception
    {
        InstanceController instanceController = new InstanceController();
        NanoHTTPD.Response response = instanceController.get(null,null,null);

        String body = IOUtils.toString(response.getData());

        assertEquals("Error, method not allowed", body);
        assertEquals(NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    public void testBadJson() throws IOException, NanoHTTPD.ResponseException
    {
        TestCase testCase = new TestCase();
        NanoHTTPD.Response response = testCase.execute("Bad Json");

        assertEquals(NanoHTTPD.Response.Status.BAD_REQUEST.getRequestStatus(), response.getStatus().getRequestStatus());
        String body = IOUtils.toString(response.getData());
        assertTrue(body.startsWith("Error reading request"));
    }

    @Test
    public void testIOException() throws Exception {
        Annotator annotator = mock(Annotator.class);
        RouterNanoHTTPD.UriResource uriResource = mock(RouterNanoHTTPD.UriResource.class);
        when(uriResource.initParameter(Annotator.class)).thenReturn(annotator);

        NanoHTTPD.IHTTPSession session = mock(NanoHTTPD.IHTTPSession.class);
        doThrow(new IOException()).when(session).parseBody(anyMap());

        InstanceController controller = new InstanceController();
        NanoHTTPD.Response response = controller.post(uriResource,null,session);

        assertEquals(NanoHTTPD.Response.Status.BAD_REQUEST, response.getStatus());
        String body = IOUtils.toString(response.getData());
        assertTrue(body.startsWith("Error reading request"));
    }

    private TextAnnotation getBasicTextAnnotation()
    {
        String[] sentences = {"The dog runs"};
        ArrayList<String[]> list = new ArrayList<String[]>();
        list.add(sentences);
        return BasicTextAnnotationBuilder.createTextAnnotationFromTokens(list);
    }

    private static NanoHTTPD.IHTTPSession mockPostData(String body) throws IOException, NanoHTTPD.ResponseException
    {
        NanoHTTPD.IHTTPSession session = mock(NanoHTTPD.IHTTPSession.class);
        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                Object[] args = invocationOnMock.getArguments();
                Map<String,String> files = (Map)args[0];
                files.put("postData",body);
                return null;
            }
        }).when(session).parseBody(anyMap());
        return session;
    }

    private String getMultipleAnnotationRequestBody(){
        TextAnnotation[] textAnnotations = new TextAnnotation[] {getBasicTextAnnotation(), getBasicTextAnnotation()};
        JsonArray jAnnotations = JsonTools.createJsonArrayFromArray(textAnnotations);
        JsonObject request = new JsonObject();
        request.add("instances", jAnnotations);

        return request.toString();
    }

    private JsonArray getInstancesFromJson(NanoHTTPD.Response response) throws IOException {
        String responseBody = IOUtils.toString(response.getData());
        JsonObject result = parser.parse(responseBody).getAsJsonObject();
        return result.get("instances").getAsJsonArray();
    }

    private class TestCase {
        public Annotator annotator;
        public RouterNanoHTTPD.UriResource uriResource;

        public TestCase(){
            annotator = mock(Annotator.class);
            uriResource = mock(RouterNanoHTTPD.UriResource.class);
            when(uriResource.initParameter(Annotator.class)).thenReturn(annotator);
        }

        public NanoHTTPD.Response execute(String requestBody) throws IOException, NanoHTTPD.ResponseException {
            NanoHTTPD.IHTTPSession session = InstanceControllerTest.mockPostData(requestBody);

            InstanceController controller = new InstanceController();
            NanoHTTPD.Response response = controller.post(uriResource,null,session);

            return response;
        }
    }
}