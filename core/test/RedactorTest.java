import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import controllers.Redactor;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
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
            TextAnnotation ta =  DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(true); //ToyTextAnnotationGenerator.generateToyTextAnnotation(3);
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
            TextAnnotation ta =  DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(true); //ToyTextAnnotationGenerator.generateToyTextAnnotation(3);
            toyAnnotations.add(ta);
        }
        for (TextAnnotation ta : toyAnnotations) {
            View tokenLabelView = ta.getView(ViewNames.POS);
            for (Constituent c : tokenLabelView.getConstituents()) {
                //System.out.println("Uncleansed Constituent: " + c.getLabel());
            }
        }
        List<TextAnnotation> tokenLabelsRemoved = Redactor.removeTokenLabels(toyAnnotations);
        for (TextAnnotation ta : tokenLabelsRemoved) {
            View tokenLabelView = ta.getView(ViewNames.POS);
            for (Constituent c : tokenLabelView.getConstituents()) {
                Assert.assertEquals("", c.getLabel());
                //System.out.println("Cleansed Constituent: " + c.getLabel());
            }
        }
    }
    
    @Test
    public void removePredicateArgumentRelationsTest() {
        List<TextAnnotation> toyAnnotations = new ArrayList<>();
        for (int i = 0; i < 3 ; i++) {
            TextAnnotation ta =  DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(true); //ToyTextAnnotationGenerator.generateToyTextAnnotation(3);
            toyAnnotations.add(ta);
        }
        for (TextAnnotation ta : toyAnnotations) {
            PredicateArgumentView predicateArgumentView = (PredicateArgumentView)ta.getView(ViewNames.SRL_VERB);
            for (Constituent c : predicateArgumentView.getPredicates()) {
                System.out.println("Predicate: " + c.getLabel());
                for (Relation relation : c.getOutgoingRelations()) {
                    System.out.println("Outgoing Relations:" + relation.getRelationName());
                }
            }
            for (Relation relation : predicateArgumentView.getRelations()) {
                System.out.println("Relation: " + relation.getRelationName());
            }
        }
        List<TextAnnotation> cleansedAnnotations = Redactor.removePredicatesArgumentsAndRelations(toyAnnotations);
        for (TextAnnotation ta : cleansedAnnotations) {
            PredicateArgumentView predicateArgumentView = (PredicateArgumentView)ta.getView(ViewNames.SRL_VERB);
            for (Constituent c : predicateArgumentView.getPredicates()) {
                System.out.println("Predicate: " + c.getLabel());
                for (Relation relation : c.getOutgoingRelations()) {
                    System.out.println("Outgoing Relations:" + relation.getRelationName());
                }
            }
            for (Relation relation : predicateArgumentView.getRelations()) {
                System.out.println("Relation: " + relation.getRelationName());
            }
        }
    }
}
