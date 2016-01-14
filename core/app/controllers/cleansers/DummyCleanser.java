package controllers.cleansers;

import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;

public class DummyCleanser extends Cleanser {
	
    /** Removes the TOKEN_LABEL_VIEW from an annotation by name. */
    public List<TextAnnotation> removeAnnotations(List<TextAnnotation> textAnnotations) {
        for (TextAnnotation textAnnotation : textAnnotations) {
        	textAnnotation.removeView(ViewNames.TOKENS);
        }
        return textAnnotations;
    }
    
	/*
	public List<TextAnnotation> removeTokenLabels(List<TextAnnotation> textAnnotations) {
		for (TextAnnotation textAnnotation : textAnnotations) {
			// get the TOKENS view
			View spanLabelView = textAnnotation.getView(ViewNames.TOKENS);
			// this list will contain the current, labelled constituents
			List<Constituent> oldConstituents = new ArrayList<>();
			for (Constituent cons : spanLabelView.getConstituents()) {
				// creates a replica constituent without a label
				Constituent noLabelConstituent = new Constituent("", cons.getConstituentScore(), 
							cons.getViewName(), textAnnotation, cons.getStartSpan(), cons.getEndSpan());
				oldConstituents.add(cons);
				spanLabelView.addConstituent(noLabelConstituent);
			}
			for (Constituent oldConstituent : oldConstituents) {
				spanLabelView.removeConstituent(oldConstituent);
			}
		}
		
		return textAnnotations;
	} */
}
