import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class FunctionalTest
{
    Server server;

    @Before
    public void setup(){

        try
        {
            server = new Server(5757, new ServerPreferences(1000, 10), new ToyPosAnnotator());
            server.start();

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown(){
        server.stop();
    }

    @Test
    public void testBasicRequest() throws Exception
    {
        String[] viewsToAdd = {ViewNames.POS};
        TextAnnotation goldTextAnnotation = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd,false);
        View goldPosView = goldTextAnnotation.getView(ViewNames.POS);
        goldTextAnnotation.removeView(ViewNames.POS);
        String json = SerializationHelper.serializeToJson(goldTextAnnotation);

        System.out.println("Request: " + json);

        HttpEntity body = EntityBuilder.create().setText(json).build();
        HttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost("http://localhost:5757/instance");
        post.setEntity(body);
        HttpResponse response = client.execute(post);
        String responseBody = EntityUtils.toString(response.getEntity());

        System.out.println("Response: " + responseBody);


        TextAnnotation learnerResult = SerializationHelper.deserializeFromJson(responseBody);
        goldTextAnnotation.addView(ViewNames.POS,goldPosView);

        System.out.println("Solution: " + SerializationHelper.serializeToJson(goldTextAnnotation));

        assertEquals(goldTextAnnotation,learnerResult);
        assertEquals(200,response.getStatusLine().getStatusCode());
    }
}
