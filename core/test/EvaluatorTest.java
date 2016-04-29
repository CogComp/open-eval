import controllers.Core;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.experiments.ClassificationTester;
import edu.illinois.cs.cogcomp.core.experiments.evaluators.ConstituentLabelingEvaluator;
import edu.illinois.cs.cogcomp.core.experiments.evaluators.Evaluator;
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
        TextAnnotation correctTextAnnotation = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd,false);
        TextAnnotation incorrectTextAnnotation = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd,false);
        Evaluator evaluator = new ConstituentLabelingEvaluator();
        String viewName = ViewNames.POS;
        ClassificationTester eval = new ClassificationTester();
        try {
            Core.evaluate(evaluator, eval, correctTextAnnotation, incorrectTextAnnotation, viewName);
        }
        catch(Exception e){
            Assert.fail();
        }
        Assert.assertTrue(eval.getEvaluationRecord().getGoldCount()>= 1);
        Assert.assertTrue(eval.getEvaluationRecord().getPredictedCount()>= 1);
    }

    @Test
    public void multiTest() {
        Evaluator evaluator = new ConstituentLabelingEvaluator();
        ClassificationTester eval = new ClassificationTester();
        String viewName = ViewNames.POS;
        for (int i = 0; i < 1000; i++) {
            TextAnnotation correctTextAnnotation = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd,false);
            TextAnnotation incorrectTextAnnotation = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd,false);
            try {
                Core.evaluate(evaluator, eval, correctTextAnnotation, incorrectTextAnnotation, viewName);
            }
            catch(Exception e){
                Assert.fail();
            }
        }
        EvaluationRecord record = eval.getEvaluationRecord();
        Assert.assertTrue(record.getGoldCount()>=1000);
        Assert.assertTrue(record.getPredictedCount()>=1000);
        Assert.assertTrue(record.getCorrectCount()<=record.getPredictedCount() && record.getCorrectCount()>=0);
    }
}
