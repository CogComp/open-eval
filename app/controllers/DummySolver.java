package controllers;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import java.util.*;

import javax.inject.Inject;

import play.mvc.*;
import play.libs.ws.*;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.Json;
/**
 * Temporary class to represent a dummy solver that lives within the evaluation framework
 *
 * @author Joshua Camp
 */

public class DummySolver {
	
	@Inject WSClient ws;
	
	WSRequest sender;
	
	public DummySolver(String url) {
		this.sender = ws.url(url);
	}

	public TextAnnotation processRequest(TextAnnotation textAnnotation) {
	/*
		Promise<Json> jsonPromise = sender.post(textAnnotation.json()).map(response -> {
											return response.asJson();
										});
		return TextAnnotation.decode(jsonPromise.get(5000));
	*/ 
		return textAnnotation;
	}
}