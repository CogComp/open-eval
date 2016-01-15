import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import controllers.ToyTextAnnotationGenerator;
import controllers.cleansers.TokenLabelCleanser;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

public class CleanserTest {

	/** Generates a toy annotation with a TOKEN_LABEL_VIEW and a SPAN_LABEL_VIEW and removes
	 * the TOKEN_LABEL_VIEW.
	 */
	@Test
	public void cleanserTest() {
		List<TextAnnotation> toyAnnotations = new ArrayList<>();
		for (int i = 0; i < 3 ; i++) {
			TextAnnotation ta = ToyTextAnnotationGenerator.generateToyTextAnnotation(3);
			toyAnnotations.add(ta);
		}
		Set<String> uncleansed = toyAnnotations.get(0).getAvailableViews();
		Set<String> uncleansedViews = new HashSet<>();
		uncleansedViews.addAll(uncleansed);
		List<TextAnnotation> cleansedAnnotations;
		TokenLabelCleanser cleanser = new TokenLabelCleanser();
		cleansedAnnotations = cleanser.removeAnnotations(toyAnnotations);
		for (TextAnnotation cleansedAnnotation : cleansedAnnotations) {
			Set<String> views = cleansedAnnotation.getAvailableViews();
			Assert.assertEquals(1, views.size());
			Assert.assertEquals(2, uncleansedViews.size());
			Assert.assertTrue(uncleansedViews.contains(ViewNames.TOKENS));
			Assert.assertFalse(views.contains(ViewNames.TOKENS));
		}
	}

}
