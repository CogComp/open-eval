package controllers;

import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

public class Redactor {

	/**
	 * Static method to remove views from `TextAnnotation` objects.  Each task variant has a number of views
	 * necessary for the learner to solve it.  All other views should be removed.
	 * @param textAnnotations
	 * @param viewsToKeep
	 * @return
	 */
	public static List<TextAnnotation> removeAnnotations(List<TextAnnotation> textAnnotations, List<String> viewsToKeep) {
		for (TextAnnotation textAnnotation : textAnnotations) {
			for (String viewName : textAnnotation.getAvailableViews()) {
				if (!viewsToKeep.contains(viewName)) {
					textAnnotation.removeView(viewName);
				}
			}
		}
		return textAnnotations;
	}
}
