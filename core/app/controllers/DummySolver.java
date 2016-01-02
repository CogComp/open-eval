package controllers;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;


import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import play.libs.ws.*;
import play.libs.F.Promise;

/**
 * Temporary class to represent a dummy solver that lives within the evaluation framework
 *
 * @author Joshua Camp
 */

public class DummySolver {
	
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
		String taJson = SerializationHelper.serializeToJson(textAnnotation);
		Promise<String> jsonPromise = sender.post(taJson).map(WSResponse::getBody);
		String result = jsonPromise.get(5000);
        TextAnnotation ta;
        try {
            ta = SerializationHelper.deserializeFromJson(result);
        }
        catch(Exception e){
            ta = null;
        }
		return ta;
	}
	
	public int testURL(){
		int status;
		try{
			Promise<WSResponse> responsePromise = sender.get();
			WSResponse response = responsePromise.get(5000);
			status = response.getStatus();
		}	
		catch(Exception e){
			status = 404;
		}
		return status;
	}
}