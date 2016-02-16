/**
 * Created by Dhruv on 1/6/2016.
 */
import models.Job;
import models.LearnerInterface;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.libs.ws.WSResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;

public class CoreLearnerTest
{
    Server server;

    @Before
    public void setup(){

        try
        {
            server = new Server(5757, new SaulPosAnnotator());
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
        running(fakeApplication(), new Runnable() {
            public void run() {
                LearnerInterface learner = new LearnerInterface("http://localhost:5757/");

                String[] viewsToAdd = {ViewNames.POS, ViewNames.SENTENCE};
                ArrayList<TextAnnotation> instances = new ArrayList<>();

                TextAnnotation goldTextAnnotation = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd,false);
                View goldPosView = goldTextAnnotation.getView(ViewNames.POS);
                goldTextAnnotation.removeView(ViewNames.POS);

                instances.add(goldTextAnnotation);

                Job newJob = new Job(learner, instances);

                System.out.println("Request");

                WSResponse response = newJob.sendAndReceiveRequestsFromSolver();

                System.out.println("Response: " + response.getBody());

                goldTextAnnotation.addView(ViewNames.POS,goldPosView);

                assertEquals(goldTextAnnotation,newJob.getSolverInstances().get(0));
                assertEquals(200,response.getStatus());
            }
        });
    }

    @Test
    public void twoRequestTest() throws Exception
    {
        running(fakeApplication(), new Runnable() {
            public void run() {
                LearnerInterface learner = new LearnerInterface("http://localhost:5757/");

                String[] viewsToAdd = {ViewNames.POS};
                ArrayList<TextAnnotation> instances = new ArrayList<>();
                ArrayList<View> removedViews = new ArrayList<>();

                for(int i=0; i<2; i++) {
                    TextAnnotation goldTextAnnotation = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd, false);
                    removedViews.add(goldTextAnnotation.getView(ViewNames.POS));

                    goldTextAnnotation.removeView(ViewNames.POS);

                    instances.add(goldTextAnnotation);
                }

                Job newJob = new Job(learner, instances);

                System.out.println("Request");

                WSResponse response = newJob.sendAndReceiveRequestsFromSolver();

                System.out.println("Response: " + response.getBody());

                List<TextAnnotation> solverInstances = newJob.getSolverInstances();

                for(int i=0; i<2; i++) {
                    TextAnnotation goldTextAnnotation = instances.get(i);
                    goldTextAnnotation.addView(ViewNames.POS, removedViews.get(i));
                    assertEquals(goldTextAnnotation, solverInstances.get(i));
                }
                assertEquals(200,response.getStatus());
            }
        });
    }

    @Test
    public void multiRequestTest() throws Exception
    {
        running(fakeApplication(), new Runnable() {
            public void run() {
                LearnerInterface learner = new LearnerInterface("http://localhost:5757/");

                String[] viewsToAdd = {ViewNames.POS};
                ArrayList<TextAnnotation> instances = new ArrayList<>();
                ArrayList<View> removedViews = new ArrayList<>();

                for(int i=0; i<10; i++) {
                    TextAnnotation goldTextAnnotation = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd, false);
                    removedViews.add(goldTextAnnotation.getView(ViewNames.POS));

                    goldTextAnnotation.removeView(ViewNames.POS);

                    instances.add(goldTextAnnotation);
                }

                Job newJob = new Job(learner, instances);

                System.out.println("Request");

                WSResponse response = newJob.sendAndReceiveRequestsFromSolver();

                System.out.println("Response: " + response.getBody());

                List<TextAnnotation> solverInstances = newJob.getSolverInstances();

                for(int i=0; i<10; i++) {
                    TextAnnotation goldTextAnnotation = instances.get(i);
                    goldTextAnnotation.addView(ViewNames.POS, removedViews.get(i));
                    assertEquals(goldTextAnnotation, solverInstances.get(i));
                }
                assertEquals(200,response.getStatus());
            }
        });
    }

    @Test
    public void infoTest() throws Exception
    {
        running(fakeApplication(), new Runnable() {
            public void run() {
                LearnerInterface learner = new LearnerInterface("http://localhost:5757/");

                String json = learner.getInfo();
                String expected = "{\"requiredViews\":[\"TOKENS\"]}";
                System.out.println(json);
                assertEquals(json, expected);
            }
        });
    }
}

