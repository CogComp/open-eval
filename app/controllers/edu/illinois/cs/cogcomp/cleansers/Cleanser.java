package controllers.edu.illinois.cs.cogcomp.cleansers;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

import java.util.List;

public abstract class Cleanser {
    public abstract List<TextAnnotation> removeAnnotations(List<TextAnnotation> textAnnotations);
}
