import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.JsonSerializer;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by rnkelch on 12/20/2015.
 */
public class InstanceControllerTest
{

    @Test
    public void testPost() throws Exception
    {
        Annotator annotator = mock(Annotator.class);
        RouterNanoHTTPD.UriResource uriResource = mock(RouterNanoHTTPD.UriResource.class);
        when(uriResource.initParameter(Annotator.class)).thenReturn(annotator);

        TextAnnotation textAnnotation = getBasicTextAnnotation();
        String requestBody = SerializationHelper.serializeToJson(textAnnotation);
        NanoHTTPD.IHTTPSession session = mockPostData(requestBody);

        InstanceController controller = new InstanceController();
        NanoHTTPD.Response response = controller.post(uriResource,null,session);

        String responseBody = IOUtils.toString(response.getData());
        assertEquals(requestBody,responseBody);
        assertEquals(NanoHTTPD.Response.Status.OK, response.getStatus());
    }

    @Test
    public void testLearnerError() throws Exception
    {
        Annotator annotator = mock(Annotator.class);
        doThrow(new AnnotatorException("")).when(annotator).addView(any());
        RouterNanoHTTPD.UriResource uriResource = mock(RouterNanoHTTPD.UriResource.class);
        when(uriResource.initParameter(Annotator.class)).thenReturn(annotator);

        TextAnnotation textAnnotation = getBasicTextAnnotation();
        String requestBody = SerializationHelper.serializeToJson(textAnnotation);
        NanoHTTPD.IHTTPSession session = mockPostData(requestBody);

        InstanceController controller = new InstanceController();
        NanoHTTPD.Response response = controller.post(uriResource,null,session);

        String responseBody = IOUtils.toString(response.getData());
        assertEquals("There was an error adding the view to the instance",responseBody);
        assertEquals(NanoHTTPD.Response.Status.INTERNAL_ERROR, response.getStatus());
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
        NanoHTTPD.IHTTPSession session = mockPostData("Bad json");

        Annotator annotator = mock(Annotator.class);
        RouterNanoHTTPD.UriResource uriResource = mock(RouterNanoHTTPD.UriResource.class);
        when(uriResource.initParameter(Annotator.class)).thenReturn(annotator);

        InstanceController controller = new InstanceController();
        NanoHTTPD.Response response = controller.post(uriResource,null,session);

        assertEquals(NanoHTTPD.Response.Status.INTERNAL_ERROR.getRequestStatus(), response.getStatus().getRequestStatus());
        String body = IOUtils.toString(response.getData());
        assertEquals("There was an error parsing the body",body);
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

        assertEquals(NanoHTTPD.Response.Status.INTERNAL_ERROR, response.getStatus());
        String body = IOUtils.toString(response.getData());
        assertEquals("Error reading request",body);
    }

    private TextAnnotation getBasicTextAnnotation()
    {
        String[] sentences = {"The dog runs"};
        ArrayList<String[]> list = new ArrayList<String[]>();
        list.add(sentences);
        return BasicTextAnnotationBuilder.createTextAnnotationFromTokens(list);
    }

    private NanoHTTPD.IHTTPSession mockPostData(String body) throws IOException, NanoHTTPD.ResponseException
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
}