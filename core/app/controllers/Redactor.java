package controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

public class Redactor {

	/**
	 * Calls removeAnnotations without the need for the user to create a singleton list.
	 * @param textAnnotations
	 * @param viewToKeep
	 * @return
	 */
	public static List<TextAnnotation> removeAnnotations(List<TextAnnotation> textAnnotations, String viewToKeep) {
		List<String> viewsToKeep = new ArrayList<>();
		viewsToKeep.add(viewToKeep);
		return removeAnnotations(textAnnotations, viewsToKeep);
	}
	/**
	 * Static method to remove views from `TextAnnotation` objects.  Each task variant has a number of views
	 * necessary for the learner to solve it.  All other views should be removed.
	 * @param textAnnotations
	 * @param viewsToKeep
	 * @return
	 */
	public static List<TextAnnotation> removeAnnotations(List<TextAnnotation> textAnnotations, List<String> viewsToKeep) {
		if (viewsToKeep == null) {
			viewsToKeep = Collections.EMPTY_LIST;
		}
		viewsToKeep.add(ViewNames.SENTENCE);
		List<String> viewsToRemove = new ArrayList<>();
		List<TextAnnotation> cleansed = new ArrayList<>();
		for (TextAnnotation textAnnotation : textAnnotations) {
			for (String viewName : textAnnotation.getAvailableViews()) {
				if (!viewsToKeep.contains(viewName)) {
					viewsToRemove.add(viewName);
				}
			}
			TextAnnotation cleansedAnnotation;
			try {
				cleansedAnnotation = (TextAnnotation)textAnnotation.clone();
			}
			catch(CloneNotSupportedException ce){
				cleansed.add(null);
				continue;
			}
			for(String viewName: viewsToRemove){
				cleansedAnnotation.removeView(viewName);
			}
			cleansed.add(cleansedAnnotation);
		}
		return cleansed;
	}
}
