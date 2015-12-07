import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import fi.iki.elonen.NanoHTTPD;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ServerTest {

    @Test
    public void testSuccess() throws IOException, NanoHTTPD.ResponseException
    {
        TextAnnotation textAnnotation = getBasicTextAnnotation();
        String json = SerializationHelper.serializeToJson(textAnnotation);

        TextAnnotator annotator = mock(TextAnnotator.class);
        when(annotator.run(any())).thenReturn(new TextAnnotatorResult(textAnnotation,true,""));

        NanoHTTPD.IHTTPSession session = getIhttpSession(NanoHTTPD.Method.POST);
        mockPostData(session,json);

        NanoHTTPD.Response response = getResponse(annotator, session);

        assertEquals(NanoHTTPD.Response.Status.OK.getRequestStatus(), response.getStatus().getRequestStatus());
        verify(annotator).run(any());
        String body = IOUtils.toString(response.getData());
        assertEquals(json,body);
    }

    @Test
    public void testLearnerError() throws IOException, NanoHTTPD.ResponseException
    {
        TextAnnotation textAnnotation = getBasicTextAnnotation();
        String json = SerializationHelper.serializeToJson(textAnnotation);

        TextAnnotator annotator = mock(TextAnnotator.class);
        when(annotator.run(any())).thenReturn(new TextAnnotatorResult(null,false,"Error"));

        NanoHTTPD.IHTTPSession session = getIhttpSession(NanoHTTPD.Method.POST);
        mockPostData(session,json);

        NanoHTTPD.Response response = getResponse(annotator, session);

        assertEquals(NanoHTTPD.Response.Status.INTERNAL_ERROR.getRequestStatus(), response.getStatus().getRequestStatus());
        verify(annotator).run(any());
        String body = IOUtils.toString(response.getData());
        assertEquals("Error",body);
        System.out.println("Test");
        assertEquals(1,0);
    }

    @Test
    public void testGetMethod() throws Exception {
        NanoHTTPD.IHTTPSession session = getIhttpSession(NanoHTTPD.Method.GET);

        NanoHTTPD.Response response = getResponse(null, session);

        assertEquals(NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED.getRequestStatus(), response.getStatus().getRequestStatus());
    }

    @Test
    public void testIOException() throws Exception {
        NanoHTTPD.IHTTPSession session = getIhttpSession(NanoHTTPD.Method.POST);
        doThrow(new IOException()).when(session).parseBody(anyMap());

        NanoHTTPD.Response response = getResponse(null, session);

        assertEquals(NanoHTTPD.Response.Status.INTERNAL_ERROR.getRequestStatus(), response.getStatus().getRequestStatus());
        String body = IOUtils.toString(response.getData());
        assertEquals("Error reading request",body);
    }

    private NanoHTTPD.IHTTPSession getIhttpSession(NanoHTTPD.Method method)
    {
        NanoHTTPD.IHTTPSession session = mock(NanoHTTPD.IHTTPSession.class);
        when(session.getMethod()).thenReturn(method);
        return session;
    }

    @Test
    public void textBadJson() throws IOException, NanoHTTPD.ResponseException
    {
        NanoHTTPD.IHTTPSession session = getIhttpSession(NanoHTTPD.Method.POST);
        mockPostData(session,"Bad json");

        NanoHTTPD.Response response = getResponse(null, session);

        assertEquals(NanoHTTPD.Response.Status.INTERNAL_ERROR.getRequestStatus(), response.getStatus().getRequestStatus());
        String body = IOUtils.toString(response.getData());
        assertEquals("Error parsing text annotation",body);
    }

    private void mockPostData(NanoHTTPD.IHTTPSession session, String body) throws IOException, NanoHTTPD.ResponseException
    {
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
    }

    private TextAnnotation getBasicTextAnnotation()
    {
        String[] sentences = {"The dog runs"};
        ArrayList<String[]> list = new ArrayList<String[]>();
        list.add(sentences);
        return BasicTextAnnotationBuilder.createTextAnnotationFromTokens(list);
    }

    private NanoHTTPD.Response getResponse(TextAnnotator annotator, NanoHTTPD.IHTTPSession session) throws IOException
    {
        Server server = new Server(0,annotator);
        return server.serve(session);
    }
}