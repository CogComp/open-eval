package controllers;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import java.util.*;

import javax.inject.Inject;

import play.mvc.*;
import play.libs.ws.*;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.Json;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Temporary class to represent a dummy solver that lives within the evaluation framework
 *
 * @author Joshua Camp
 */

public class DummySolver {
	
	@Inject WSClient ws;
	
	WSRequest sender;
	
	/**
	 * Constructor. Initiates WSRequest object to send Http requests
	 * @param url - Url of the server to send instances to
	 */
	public DummySolver(String url) {
		this.sender = ws.url(url);
	}

	/**
	 * Sends an instance to the solver server and retrieves a result instance
	 * @param textAnnotation - The unsolved instance to send to the solver
	 * @return The solved TextAnnotation instance retrieved from the solver
	 */
	public TextAnnotation processRequest(TextAnnotation textAnnotation) {
		Promise<JsonNode> jsonPromise = sender.post(Json.toJson(textAnnotation)).map(response -> {
											return response.asJson();
										});
		TextAnnotation result = Json.fromJson(jsonPromise.get(5000), TextAnnotation.class);
		return result;
	}
}