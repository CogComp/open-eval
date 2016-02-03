import controllers.Core;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dhruv on 2/2/2016.
 */
public class CoreCleanserTest {

    String[] viewsToAdd;
    String requiredJson;

    @Before
    public void setup(){
        viewsToAdd = new String[]{ViewNames.POS, ViewNames.SENTENCE};
        requiredJson = "{\"requiredViews\":[\"SENTENCE\"]}";
    }
    @Test
    public void basicTest(){
        List<TextAnnotation> correct = new ArrayList<>();
        correct.add(DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd,false));
        List<TextAnnotation> cleansed = Core.cleanseInstances(correct, requiredJson);
        assert(cleansed.size()==1);
        TextAnnotation cleanTA = cleansed.get(0);
        Assert.assertTrue(cleanTA.hasView(ViewNames.SENTENCE));
        Assert.assertFalse(cleanTA.hasView(ViewNames.POS));
    }

    @Test
    public void multiTest(){
        List<TextAnnotation> correct = new ArrayList<>();
        for(int i=0; i<1000; i++){
            correct.add(DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd,false));
        }
        List<TextAnnotation> cleansed = Core.cleanseInstances(correct, requiredJson);
        assert(cleansed.size()==correct.size());
        for(TextAnnotation cleanTA: cleansed){
            Assert.assertTrue(cleanTA.hasView(ViewNames.SENTENCE));
            Assert.assertFalse(cleanTA.hasView(ViewNames.POS));
        }
    }

    @Test
    public void badJson(){
        String json = "josn?!";
        String json2 = "{\"requiredVeiws\":[\"SENTENCE\"]}";
        List<TextAnnotation> correct = new ArrayList<>();
        correct.add(DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd,false));
        List<TextAnnotation> cleansed = Core.cleanseInstances(correct, json);
        Assert.assertNull(cleansed);
        cleansed = Core.cleanseInstances(correct, json);
        Assert.assertNull(cleansed);
    }
}
