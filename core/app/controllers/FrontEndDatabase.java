package controllers; 

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import play.*;
import play.mvc.*;

import org.json.*;

/**
 * Class to store and retrieve configurations and history. 
 * 
 *
 */

public class FrontEndDatabase {
	private String datasetName;
	private String teamName; 
	private String description; 
	private String evaluator; 
	private String taskType; 
	private ArrayList<String> taskVariants;
	
	/** Reads in an HTTP POST request containg the JSON with configuration information */
	public void storeConfig() {
		Map<String, String[]> params = request().body().asFormUrlEncoded();
		
		//Temporary JSON that may be used for testing. 
		String json = "{"
				+ "\"configuration\" : {"
				+ "\"datasetName\": \"Sports Articles\","
				+ "\"teamName\" : \"Open Eval!\","
				+ "\"description\" : \"My description.\","
				+ "\"evaluator\" : \"F2\","
				+ "\"taskType\" : \"Text Annotation\""
				+ "},"
				+ "\"taskVariants\" : ["
				+ "\"tskVar1\","
				+ "\"tskVar2\","
				+ "\"tskVar3\""
				+ "]"
				+ "}";
		
		readJSON(json); 
		insertConfigToDB(); 
	}
	
	/** Reads and Parses the received JSON that contains new configuration info. */
	private void readJSON(String json) {
		JSONObject jsonConfig = new JSONObject(json);
		taskVariants = new ArrayList<>();
		
		datasetName = jsonConfig.getJSONObject("configuration").getString("datasetName");
		teamName = jsonConfig.getJSONObject("configuration").getString("teamName");
		description = jsonConfig.getJSONObject("configuration").getString("description");
		evaluator = jsonConfig.getJSONObject("configuration").getString("evaluator");
		taskType = jsonConfig.getJSONObject("configuration").getString("taskType");
		
		JSONArray jsonTaskVarArr = jsonConfig.getJSONArray("taskVariants");
		for (int i = 0; i < jsonTaskVarArr.length(); i++) {
			taskVariants.add((String)jsonTaskVarArr.get(i));
		}
	}
	
	/** Stores the received configuration in the MySQL configurations table. */
	private void insertConfigToDB() {
		try {
			String jdbcDriver = "com.mysql.jdbc.Driver";
		    String mysqlURL = "jdbc:mysql://localhost/test"; //Change this according to URL of MySQL server.
		    
		    try {
				Class.forName(jdbcDriver);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		    
			Connection connection = DriverManager.getConnection(mysqlURL, "root", "");
			
			PreparedStatement insertStmt = connection.prepareStatement("insert into `configurations` values ('"+datasetName+"', '"+teamName+"', '"+description+"', '"+evaluator+"', '"+taskType+"');");
			insertStmt.execute();
			connection.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	/** Returns a list of all the configurations in the database. */
	public void getConfigList() {
		
	}
	
	/** Returns all the information about a single configuration, to be used displayed on the configuration page. */
	public void getConfigInformation() {
		
	}
		
}
