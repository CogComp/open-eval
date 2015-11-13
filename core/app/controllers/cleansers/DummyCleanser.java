package controllers.cleansers;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

import java.util.ArrayList;
import java.util.List;

public class DummyCleanser extends Cleanser{

    /** Removes annotations from instances. */
    public List<TextAnnotation> removeAnnotations(List<TextAnnotation> textAnnotations) {
        List<TextAnnotation> annotationsWithoutTokens = new ArrayList<>();
        for (TextAnnotation textAnnotation : textAnnotations) {
            String corpusId = textAnnotation.getCorpusId();
            String id = textAnnotation.getId();
            String text = textAnnotation.getText();

            //TODO: Retrieve character offsets from the TextAnnotation.
            //TODO: Retrieve sentence end positions.

            String[] tokens = textAnnotation.getTokens();
            for (String token : tokens) {
                token = "";
            }
        }
        return annotationsWithoutTokens;
    }
}
