import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import controllers.ToyTextAnnotationGenerator;
import controllers.io.DatabaseCommunication;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

public class DatabaseTest {

	private final Integer NUM_DOCS = 3;
	
	@Test
	public void test() {
		DatabaseCommunication dbComm = new DatabaseCommunication();
		TextAnnotation toyTextAnnotation = ToyTextAnnotationGenerator.generateToyTextAnnotation(NUM_DOCS);
		List<TextAnnotation> toyTextAnnotations = new ArrayList<>();
		dbComm.storeDataset("toyDataset", toyTextAnnotations);
		List<TextAnnotation> textAnnotations = dbComm.retrieveDataset("toyDataset");
		Assert.assertNotNull(textAnnotations);
		for (TextAnnotation ta : textAnnotations) {
			System.out.println(ta.getText());
		}
	}

}
