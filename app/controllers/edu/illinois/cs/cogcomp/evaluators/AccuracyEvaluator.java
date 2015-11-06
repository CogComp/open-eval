package controllers.edu.illinois.cs.cogcomp.evaluators;

import controllers.edu.illinois.cs.cogcomp.Evaluation;
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