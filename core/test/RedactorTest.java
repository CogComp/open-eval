import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import controllers.Redactor;
import controllers.cleansers.TokenLabelCleanser;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;

public class RedactorTest {

    /** Generates a toy annotation with a TOKEN_LABEL_VIEW and a SPAN_LABEL_VIEW and removes
     * the TOKEN_LABEL_VIEW.
     */
    @Test
    public void redactorTest() {
    	List<TextAnnotation> toyAnnotations = new ArrayList<>();
        for (int i = 0; i < 3 ; i++) {
            TextAnnotation ta =  DummyTextAnnotationGenerator.generateBasicTextAnnotation(1); //ToyTextAnnotationGenerator.generateToyTextAnnotation(3);
            toyAnnotations.add(ta);
        }
        List<TextAnnotation> tokensViewRemoved = Redactor.removeAnnotations(toyAnnotations, new ArrayList<String>());
        for (TextAnnotation ta : tokensViewRemoved) {
            Assert.assertFalse(ta.hasView(ViewNames.TOKENS));
        }
    }
    
    @Test
    public void removeTokenLabelsTest() {
        List<TextAnnotation> toyAnnotations = new ArrayList<>();
        for (int i = 0; i < 3 ; i++) {
            TextAnnotation ta =  DummyTextAnnotationGenerator.generateBasicTextAnnotation(1); //ToyTextAnnotationGenerator.generateToyTextAnnotation(3);
            toyAnnotations.add(ta);
        }
        List<TextAnnotation> tokenLabelsRemoved = Redactor.removeTokenLabels(toyAnnotations);
        for (TextAnnotation ta : tokenLabelsRemoved) {
            View tokenLabelView = ta.getView(ViewNames.TOKENS);
            for (Constituent c : tokenLabelView.getConstituents()) {
                Assert.assertEquals("", c.getLabel());
            }
        }
    }
}
