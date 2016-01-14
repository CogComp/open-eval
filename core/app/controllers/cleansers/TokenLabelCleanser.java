package controllers.cleansers;

import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;

public class TokenLabelCleanser extends Cleanser {

	/**
	 * Removes the labels from the constituents of a {@code TokenLabelView}.
	 */
	@Override
	public List<TextAnnotation> removeAnnotations(List<TextAnnotation> textAnnotations) {
		for (TextAnnotation textAnnotation : textAnnotations) {
			removeAnnotation(textAnnotation);
		}
		return textAnnotations;
	}
	
	/**
	 * Removes the token label from all views for which the {@code ViewType} is {@code TokenLabelView}.	
	 * @param textAnnotation
	 */
	private void removeAnnotation(TextAnnotation textAnnotation) {
		
		View view = textAnnotation.getView(ViewNames.TOKENS);
		
		// Iterate through the views and replace each constituent with one whose label is empty.
		List<Constituent> constituents = view.getConstituents();
		for (Constituent c : constituents) {
			String cleansedLabel = "";
			String cleansedViewName = c.getViewName();
			int start = c.getStartCharOffset();
			int end = c.getEndCharOffset();
			Constituent cleansedConstituent = new Constituent(cleansedLabel, cleansedViewName,
											textAnnotation, start, end);
			view.removeConstituent(c);
			view.addConstituent(cleansedConstituent);
		}
		textAnnotation.addView(view.getViewName(), view);
	}

}
