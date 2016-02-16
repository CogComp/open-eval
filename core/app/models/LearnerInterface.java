package models;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;


import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import play.libs.ws.*;
import play.libs.F.Promise;

/**
 * Temporary class to represent a dummy solver that lives within the evaluation framework
 *
 * @author Joshua Camp
 */

public class LearnerInterface {
	
	WSRequest infoPoster;
	WSRequest instancePoster;
	
	/**
	 * Constructor. Initiates WSRequest object to send Http requests
	 * @param url - Url of the server to send instances to
	 */
	public LearnerInterface(String url) {
		this.infoPoster = WS.url(url+"info");
		this.instancePoster = WS.url(url+"instance");
		System.out.println(this.infoPoster);
	}

	/**
	 * Sends an instance to the solver server and retrieves a result instance
	 * @param textAnnotation - The unsolved instance to send to the solver
	 * @return The solved TextAnnotation instance retrieved from the solver
	 */
	public WSResponse processRequest(TextAnnotation textAnnotation) {
		System.out.println("Sending:"+textAnnotation.getText());
		String taJson = SerializationHelper.serializeToJson(textAnnotation);
		Promise<WSResponse> jsonPromise = instancePoster.post(taJson);

		return jsonPromise.get(50000);
	}
	
	public String getInfo(){
		System.out.println("Pinging server");
		String jsonInfo;
		try{
			Promise<WSResponse> responsePromise = infoPoster.get();
			WSResponse response = responsePromise.get(5000);
			jsonInfo = response.getBody();
		}	
		catch(Exception e){
			jsonInfo = "err";
		}
		return jsonInfo;
	}
}