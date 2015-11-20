import fi.iki.elonen.NanoHTTPD;
import org.junit.Test;
import static org.mockito.Mockito.*;

import static org.junit.Assert.*;

/**
 * Created by rnkelch on 11/20/2015.
 */
public class ServerTest {

    @Test
    public void testServe() throws Exception {
        NanoHTTPD.IHTTPSession session = mock(NanoHTTPD.IHTTPSession.class);
        when(session.getMethod()).thenReturn(NanoHTTPD.Method.GET);

        Server server = new Server(0,null,null);
        NanoHTTPD.Response response = server.serve(session);

        assertEquals(NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED.getRequestStatus(), response.getStatus().getRequestStatus());
    }
}