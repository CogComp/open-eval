import edu.illinois.cs.cogcomp.annotation.Annotator;
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
        when(annotator.getRequiredViews()).thenReturn(new String[]{"a", "b", "c"});
        RouterNanoHTTPD.UriResource uriResource = mock(RouterNanoHTTPD.UriResource.class);
        when(uriResource.initParameter(Annotator.class)).thenReturn(annotator);

        NanoHTTPD.Response response = infoController.get(uriResource, null, null);
        String body = IOUtils.toString(response.getData());
        String expected = "{\"requiredViews\":[\"a\",\"b\",\"c\"]}";
        assertEquals(expected, body);
        assertEquals(NanoHTTPD.Response.Status.OK, response.getStatus());
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