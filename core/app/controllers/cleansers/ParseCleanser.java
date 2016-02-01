package controllers.cleansers;

import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

public class ParseCleanser extends Cleanser {

	@Override
	public List<TextAnnotation> removeAnnotations(List<TextAnnotation> textAnnotations) {
		for (TextAnnotation textAnnotation : textAnnotations) {
			textAnnotation.removeView(ViewNames.PARSE_GOLD);
			textAnnotation.removeView(ViewNames.PARSE_CHARNIAK);
			textAnnotation.removeView(ViewNames.PARSE_CHARNIAK_KBEST);
			textAnnotation.removeView(ViewNames.PSEUDO_PARSE_CHARNIAK);
			textAnnotation.removeView(ViewNames.PARSE_STANFORD);
			textAnnotation.removeView(ViewNames.PARSE_STANFORD_KBEST);
			textAnnotation.removeView(ViewNames.PSEUDO_PARSE_STANFORD);
			textAnnotation.removeView(ViewNames.PARSE_BERKELEY);
			textAnnotation.removeView(ViewNames.PSEUDO_PARSE_BERKELEY);
		}
		return textAnnotations;
	}

}
