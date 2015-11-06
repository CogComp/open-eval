package controllers.edu.illinois.cs.cogcomp;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import java.util.*;

/**
 * Temporary class to represent a dummy solver that lives within the evaluation framework
 *
 * @author Joshua Camp
 */

public class DummySolver {
	
	public DummySolver() {

	}

	public TextAnnotation processRequest(TextAnnotation textAnnotation) {
		return textAnnotation;
	}
}