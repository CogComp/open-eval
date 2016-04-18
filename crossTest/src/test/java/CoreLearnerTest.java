/**
 * Created by Dhruv on 1/6/2016.
 */
import controllers.Core;
import edu.illinois.cs.cogcomp.openeval.learner.Server;
import edu.illinois.cs.cogcomp.openeval.learner.ServerPreferences;
import models.Job;
import models.LearnerInstancesResponse;
import models.LearnerInterface;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import models.LearnerSettings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;

public class CoreLearnerTest
{
    Server server;

    @Before
    public void setup(){

        try
        {
            server = new Server(5757, new ServerPreferences(1000, 10), new SaulPosAnnotator());
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
                ArrayList<TextAnnotation> goldInstances = new ArrayList<>();

                TextAnnotation goldTextAnnotation = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd,false);
                View goldPosView = goldTextAnnotation.getView(ViewNames.POS);
                goldTextAnnotation.removeView(ViewNames.POS);

                goldInstances.add(goldTextAnnotation);

                List<TextAnnotation> redactedInstances = Core.cleanseInstances(goldInstances, Arrays.asList(viewsToAdd));

                Job newJob = new Job(learner, redactedInstances, goldInstances);

                System.out.println("Request");

                LearnerInstancesResponse response = newJob.sendAndReceiveRequestsFromSolver(redactedInstances);

                goldTextAnnotation.addView(ViewNames.POS,goldPosView);

                assertTrue(response.textAnnotations[0].hasView(ViewNames.POS));
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
                ArrayList<TextAnnotation> goldInstances = new ArrayList<>();
                ArrayList<View> removedViews = new ArrayList<>();

                for(int i=0; i<2; i++) {
                    TextAnnotation goldTextAnnotation = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd, false);
                    removedViews.add(goldTextAnnotation.getView(ViewNames.POS));

                    goldTextAnnotation.removeView(ViewNames.POS);

                    goldInstances.add(goldTextAnnotation);
                }

                List<TextAnnotation> redactedInstances = Core.cleanseInstances(goldInstances, Arrays.asList(viewsToAdd));

                Job newJob = new Job(learner, redactedInstances, goldInstances);

                System.out.println("Request");

                LearnerInstancesResponse response = newJob.sendAndReceiveRequestsFromSolver(redactedInstances);

                List<TextAnnotation> solverInstances = newJob.getSolverInstances();

                for(int i=0; i<2; i++) {
                    TextAnnotation goldTextAnnotation = goldInstances.get(i);
                    goldTextAnnotation.addView(ViewNames.POS, removedViews.get(i));
                    assertTrue(response.textAnnotations[i].hasView(ViewNames.POS));
                }
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
                ArrayList<TextAnnotation> goldInstances = new ArrayList<>();
                ArrayList<View> removedViews = new ArrayList<>();

                for(int i=0; i<10; i++) {
                    TextAnnotation goldTextAnnotation = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd, false);
                    removedViews.add(goldTextAnnotation.getView(ViewNames.POS));

                    goldTextAnnotation.removeView(ViewNames.POS);

                    goldInstances.add(goldTextAnnotation);
                }

                List<TextAnnotation> redactedInstances = Core.cleanseInstances(goldInstances, Arrays.asList(viewsToAdd));

                Job newJob = new Job(learner, redactedInstances, goldInstances);

                System.out.println("Request");

                LearnerInstancesResponse response = newJob.sendAndReceiveRequestsFromSolver(redactedInstances);

                List<TextAnnotation> solverInstances = newJob.getSolverInstances();

                for(int i=0; i<10; i++) {
                    TextAnnotation goldTextAnnotation = goldInstances.get(i);
                    goldTextAnnotation.addView(ViewNames.POS, removedViews.get(i));
                    assertTrue(response.textAnnotations[i].hasView(ViewNames.POS));
                }
            }
        });
    }

    @Test
    public void infoTest() throws Exception
    {
        running(fakeApplication(), new Runnable() {
            public void run() {
                LearnerInterface learner = new LearnerInterface("http://localhost:5757/");

                LearnerSettings settings = learner.getInfo();
                List<String> requiredViews = Arrays.asList(new String[]{"TOKENS"});

                System.out.println(settings);
                assertEquals(requiredViews, settings.requiredViews);
            }
        });
    }
}

