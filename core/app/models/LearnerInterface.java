package models;

import java.io.BufferedReader;
import java.io.FileReader;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;


import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import play.api.libs.ws.*;
import play.libs.ws.*;
import play.libs.F.Promise;
import play.libs.ws.WS;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

/**
 * Temporary class to represent a dummy solver that lives within the evaluation framework
 */

public class LearnerInterface {
	
	WSRequest infoPoster;
	WSRequest instancePoster;
	int timeout;
	
	/**
	 * Constructor. Initiates WSRequest object to send Http requests
	 * @param url - Url of the server to send instances to
	 */
	public LearnerInterface(String url) {
		Config conf = ConfigFactory.load();
		timeout = conf.getInt("learner.default.timeout");
		this.infoPoster = WS.url(url+"info");
		this.instancePoster = WS.url(url+"instance");
		System.out.println(this.infoPoster);
	}

	/**
	 * Sends an instance to the solver server and retrieves a result instance
	 * @param textAnnotation - The unsolved instance to send to the solver
	 * @return The solved TextAnnotation instance retrieved from the solver
	 */
	public Promise<WSResponse> processRequest(TextAnnotation textAnnotation) throws Exception{
		System.out.println("Sending:"+textAnnotation.getText());
		String taJson = SerializationHelper.serializeToJson(textAnnotation);
		Promise<WSResponse> jsonPromise = instancePoster.post(taJson);

		return jsonPromise;
	}
	
	public String getInfo(){
		System.out.println("Pinging server");
		String jsonInfo;
		try{
			Promise<WSResponse> responsePromise = infoPoster.get();
			WSResponse response = responsePromise.get(timeout);
			jsonInfo = response.getBody();
		}	
		catch(Exception e){
			jsonInfo = "err";
		}
		return jsonInfo;
	}
}