import controllers.Core;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Dhruv on 2/2/2016.
 */
public class CoreCleanserTest {

    String[] viewsToAdd;
    List<String> requiredViews;

    @Before
    public void setup(){
        viewsToAdd = new String[]{ViewNames.POS, ViewNames.SENTENCE};
        requiredViews = Arrays.asList(new String[]{"SENTENCE"});
    }
    /*
    @Test
    public void basicTest(){
        List<TextAnnotation> correct = new ArrayList<>();
        correct.add(DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd,false));
        Assert.assertTrue(correct.get(0).hasView(ViewNames.POS));
        List<TextAnnotation> cleansed = Core.cleanseInstances(correct, requiredViews);
        assert(cleansed.size()==1);
        TextAnnotation cleanTA = cleansed.get(0);
        Assert.assertTrue(cleanTA.hasView(ViewNames.SENTENCE));
        Assert.assertFalse(cleanTA.hasView(ViewNames.POS));
        Assert.assertTrue(correct.get(0).hasView(ViewNames.POS));
    }

    @Test
    public void multiTest(){
        List<TextAnnotation> correct = new ArrayList<>();
        for(int i=0; i<100; i++){
            correct.add(DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd,false));
        }
        List<TextAnnotation> cleansed = Core.cleanseInstances(correct, requiredViews);
        assert(cleansed.size()==correct.size());
        for(TextAnnotation cleanTA: cleansed){
            Assert.assertTrue(cleanTA.hasView(ViewNames.SENTENCE));
            Assert.assertFalse(cleanTA.hasView(ViewNames.POS));
        }
    } */
}
