package controllers.cleansers;

import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

public class SpanLabelCleanser extends Cleanser {

	/** Removes all views of type SPAN_LABEL_VIEW. */
	@Override
	public List<TextAnnotation> removeAnnotations(List<TextAnnotation> textAnnotations) {
		for (TextAnnotation textAnnotation : textAnnotations) {
			textAnnotation.removeView(ViewNames.SENTENCE);
			textAnnotation.removeView(ViewNames.PARAGRAPH);
			textAnnotation.removeView(ViewNames.NER_CONLL);
			textAnnotation.removeView(ViewNames.NER_ONTONOTES);
			textAnnotation.removeView(ViewNames.SHALLOW_PARSE);
			textAnnotation.removeView(ViewNames.QUANTITIES);
			textAnnotation.removeView(ViewNames.WIKIFIER);
			textAnnotation.removeView(ViewNames.CLAUSES_CHARNIAK);
			textAnnotation.removeView(ViewNames.CLAUSES_STANFORD);
			textAnnotation.removeView(ViewNames.CLAUSES_BERKELEY);
		}
		return textAnnotations;
	}
}
