package controllers.cleansers;

import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

public class DependencyCleanser extends Cleanser {

	/** Removes all views with ViewType DEPENDENCY_VIEW. */
	@Override
	public List<TextAnnotation> removeAnnotations(List<TextAnnotation> textAnnotations) {
		for (TextAnnotation textAnnotation : textAnnotations) {
			textAnnotation.removeView(ViewNames.DEPENDENCY);
			textAnnotation.removeView(ViewNames.DEPENDENCY_STANFORD);
		}
		return textAnnotations;
	}

}
