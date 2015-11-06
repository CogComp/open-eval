package controllers.edu.illinois.cs.cogcomp;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import java.util.*;

public interface Evaluator {

	public Evaluation evaluate(List<TextAnnotation> correctAnnotations, List<TextAnnotation> solverAnnotations);

}