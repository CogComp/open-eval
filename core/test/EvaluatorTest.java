import controllers.Core;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import edu.illinois.cs.cogcomp.core.experiments.EvaluationRecord;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.validation.constraints.AssertTrue;
import java.util.ArrayList;
import java.util.List;

import models.Configuration;

/**
 * Created by Dhruv on 2/9/2016.
 */
public class EvaluatorTest {

    String[] viewsToAdd;

    @Before
    public void setup(){
        viewsToAdd = new String[]{ViewNames.POS};
    }
    @Test
    public void basicTest(){
        List<TextAnnotation> correct = new ArrayList<>();
        List<TextAnnotation> guessed = new ArrayList<>();
        List<Boolean> skip = new ArrayList<>();
        TextAnnotation correctTextAnnotation = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd,false);
        TextAnnotation incorrectTextAnnotation = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd,false);
        correct.add(correctTextAnnotation);
        guessed.add(incorrectTextAnnotation);
        skip.add(false);
        Configuration config = new Configuration("testName", "testDescrip", "testDataset", "testTaskType", "testTskVar", "Constituent Labeling", "1");
        EvaluationRecord record = Core.evaluate(config, correct, guessed, skip);
        Assert.assertTrue(record.getGoldCount()>= 1);
        Assert.assertTrue(record.getPredictedCount()>= 1);
    }

    @Test
    public void multiTest() {
        List<TextAnnotation> correct = new ArrayList<>();
        List<TextAnnotation> guessed = new ArrayList<>();
        List<Boolean> skip = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            TextAnnotation correctTextAnnotation = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd,false);
            TextAnnotation incorrectTextAnnotation = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd,false);
            correct.add(correctTextAnnotation);
            guessed.add(incorrectTextAnnotation);
            skip.add(false);
        }
        Configuration config = new Configuration("testName", "testDescrip", "testDataset", "testTaskType", "testTskVar", "Constituent Labeling", "1");
        EvaluationRecord record = Core.evaluate(config, correct, guessed, skip);
        Assert.assertTrue(record.getGoldCount()>=1000);
        Assert.assertTrue(record.getPredictedCount()>=1000);
        Assert.assertTrue(record.getCorrectCount()<=record.getPredictedCount() && record.getCorrectCount()>=0);
    }
}
