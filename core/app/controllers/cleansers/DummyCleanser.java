package controllers.cleansers;

import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;

public class DummyCleanser extends Cleanser {
	
    /** Does nothing */
    public List<TextAnnotation> removeAnnotations(List<TextAnnotation> textAnnotations) {
        // Does not remove any views.
        return textAnnotations;
    }
}
