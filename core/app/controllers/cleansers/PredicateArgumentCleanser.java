package controllers.cleansers;

import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

public class PredicateArgumentCleanser extends Cleanser {

	@Override
	public List<TextAnnotation> removeAnnotations(List<TextAnnotation> textAnnotations) {
		for (TextAnnotation textAnnotation : textAnnotations) {
			textAnnotation.removeView(ViewNames.SRL_VERB);
			textAnnotation.removeView(ViewNames.SRL_NOM);
			textAnnotation.removeView(ViewNames.SRL_PREP);
		}
		return textAnnotations;
	}

}
