package controllers.edu.illinois.cs.cogcomp;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import java.util.*;

public class AccuracyEvaluator implements Evaluator {

	public AccuracyEvaluator() {
		super();
	}

	public Evaluation evaluate(List<TextAnnotation> correctAnnotations, List<TextAnnotation> solverAnnotations) {
		return new Evaluation();
	}

}