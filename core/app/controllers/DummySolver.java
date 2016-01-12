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
		this.sender = WS.url(url);
	}

	/**
	 * Sends an instance to the solver server and retrieves a result instance
	 * @param textAnnotation - The unsolved instance to send to the solver
	 * @return The solved TextAnnotation instance retrieved from the solver
	 */
	public TextAnnotation processRequest(TextAnnotation textAnnotation) {
		Promise<JsonNode> jsonPromise = sender.post(Json.toJson(textAnnotation)).map(response -> {
											if(response.getStatus()==200)
												return response.asJson();
											else
												return Json.parse(""+response.getStatus());
										});
		JsonNode result = jsonPromise.get(5000);
		int error = result.asInt();
		TextAnnotation ta;
		if(error == 0)
			ta = Json.fromJson(result, TextAnnotation.class);
		else	
			ta = null;
		return ta;
	}
	
	public int testURL(){
		int status=200; 
		try{
			Promise<WSResponse> responsePromise = sender.get();
			WSResponse response = responsePromise.get(5000);
			status = response.getStatus();
		}	
		catch(Exception e){
			status = 404;
			System.out.println(e);
		}
		return status;
	}
}