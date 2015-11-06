package controllers;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import java.util.*;
import org.apache.http.client.methods.HttpPost;

/**
 * Temporary class to represent a dummy solver that lives within the evaluation framework
 *
 * @author Joshua Camp
 */

public class DummySolver {
	
	HttpPost post;
	
	public DummySolver(String url) {
		this.post = new HttpPost(url);
	}

	public TextAnnotation processRequest(TextAnnotation textAnnotation) {
		return textAnnotation;
	}
}