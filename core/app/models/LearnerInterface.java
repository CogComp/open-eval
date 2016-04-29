package models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;


import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import play.api.libs.ws.*;
import play.libs.F;
import play.libs.ws.*;
import play.libs.F.Promise;
import play.mvc.Http;
import play.libs.ws.WS;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

import models.LearnerSettings;

/**
 * Temporary class to represent a dummy solver that lives within the evaluation framework
 */
public class LearnerInterface {
	
	WSRequest infoPoster;
	WSRequest instancePoster;

	private static final String TEXT_ANNOTATION_PROPERTY = "textAnnotation";
	private static final String ERROR_PROPERTY = "error";

	int timeout;
	
	/**
	 * Constructor. Initiates WSRequest object to send Http requests
	 * @param url - Url of the server to send instances to
	 */
	public LearnerInterface(String url) throws Exception{
		if(!url.startsWith("http"))
			url = "http://"+url;
		if(!url.endsWith("/"))
			url+="/";
		this.infoPoster = WS.url(url+"info");
		this.instancePoster = WS.url(url+"instance");
		System.out.println(this.infoPoster);
		Config conf = ConfigFactory.load();
		timeout = conf.getInt("learner.default.timeout");
	}

	/**
	 * Sends an instance to the solver server and retrieves a result instance
	 * @param textAnnotations - The unsolved instances to send to the solver
	 * @return The solved TextAnnotation instance retrieved from the solver
	 */
	public Promise<LearnerInstancesResponse> processRequests(List<TextAnnotation> textAnnotations) {
		System.out.println(String.format("Sending %n TextAnnotations", textAnnotations.size()));

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("[");
		for(TextAnnotation textAnnotation : textAnnotations){
			String taJson = SerializationHelper.serializeToJson(textAnnotation);
			stringBuilder.append(taJson);
			stringBuilder.append(",");
		}
		stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), "]");

		Promise<LearnerInstancesResponse> promise = Promise.promise(new F.Function0<LearnerInstancesResponse>() {
			@Override
			public LearnerInstancesResponse apply() throws Throwable {
				Promise<WSResponse> jsonPromise = instancePoster.post(stringBuilder.toString());
				WSResponse response = jsonPromise.get(50000);

				TextAnnotation[] results = new TextAnnotation[textAnnotations.size()];
				String[] errors = new String[textAnnotations.size()];
				String requestWideError = null;

				if (response.getStatus() == Http.Status.OK) {
					JsonNode json = response.asJson();

					for(int i=0;i<json.size();i++){
						JsonNode instanceInfo = json.get(i);

						if(instanceInfo.has(TEXT_ANNOTATION_PROPERTY)) {
							String textAnnotationJson = instanceInfo.get(TEXT_ANNOTATION_PROPERTY).toString();
							try {
								results[i] = SerializationHelper.deserializeFromJson(textAnnotationJson);
							} catch (Exception e) {
								errors[i] = "Server side error parsing Text Annotation: " + e.getLocalizedMessage();
							}
						}
						else if (instanceInfo.has(ERROR_PROPERTY)){
							errors[i] = instanceInfo.get(ERROR_PROPERTY).asText();
						}
					}
				}
				else {
					requestWideError = response.getBody();
				}
				return new LearnerInstancesResponse(results, errors, requestWideError);
			}
		});

		return promise;
	}
	
	public LearnerSettings getInfo(){
		System.out.println("Pinging server");
		LearnerSettings settings;
		JsonNode response;
		try {
			Promise<WSResponse> responsePromise = infoPoster.get();
			response = responsePromise.get(timeout).asJson();
		}
		catch(Exception e){
			return new LearnerSettings("Server at url not found");
		}
		try {
			String addedView = response.get("addedView").asText();
			int maxInstancesToSend = response.get("maxNumInstancesAccepted").asInt();
			JsonNode requiredViewsJson = response.get("requiredViews");
			ObjectMapper mapper = new ObjectMapper();
			String[] viewArray = mapper.readValue(requiredViewsJson.toString(), String[].class);
			settings = new LearnerSettings(addedView, Arrays.asList(viewArray), maxInstancesToSend);
		}
		catch(Exception e){
			return new LearnerSettings("Received invalid response from url");
		}

		return settings;
	}
}