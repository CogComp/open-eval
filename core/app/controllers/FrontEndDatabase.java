package controllers; 

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Map;

import play.*;
import play.mvc.*;

import org.json.*;
import com.mysql.jdbc.Driver;


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
	
	String jdbcDriver = "com.mysql.jdbc.Driver";
    String mysqlURL = "jdbc:mysql://localhost/test"; //Change this according to URL of MySQL server.
    String username = "root"; //Username to access database.
    String password = ""; //Password for the above username. 
	
	/** Reads in an HTTP POST request containg the JSON with configuration information */
	public void storeConfig(String json) {
		//Temporary JSON that may be used for testing. 
		String json2 = "{"
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
	
	/** Stores the received configuration in the MySQL configurations table and taskvariants table. */
	private void insertConfigToDB() {
		try {
		    try {
				Class.forName(jdbcDriver);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		    
			Connection connection = DriverManager.getConnection(mysqlURL, username, password);
			
			//Storing basic configuration info. 
			String sql = "INSERT INTO configurations VALUES (?, '"+datasetName+"', '"+teamName+"', '"+description+"', '"+evaluator+"', '"+taskType+"');";
			PreparedStatement insertStmt = connection.prepareStatement(sql);
			insertStmt.setNull(1, Types.INTEGER); //Set null so MySQL can auto-increment the primary key (id).
			insertStmt.execute();
			
			//Storing info on the task-variants.
			//Have to get the ID of the newly created configuration. 
			sql = "SELECT MAX(id) from configurations;";
			insertStmt = connection.prepareStatement(sql);
			ResultSet idOfConfig = insertStmt.executeQuery();
			idOfConfig.first();
			int id = idOfConfig.getInt(1);
			
			//Inserting all the task variants in to taskVariants table.
			sql = "INSERT INTO taskvariants VALUES (?, ?);";
			insertStmt = connection.prepareStatement(sql);
			for (String taskVariant : taskVariants){
				insertStmt.setInt(1, id);
				insertStmt.setString(2, taskVariant);
				insertStmt.execute();
			}
			
			connection.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	/** Returns a list of all the configurations in the database to be displayed on landing page. */
	public ArrayList<String[]> getConfigList() {
		try {
		    try {
				Class.forName(jdbcDriver);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		    
			Connection connection = DriverManager.getConnection(mysqlURL, username, password);
			
			String sql = "SELECT teamName, description, datasetName, evaluator, id FROM configurations;";
			PreparedStatement insertStmt = connection.prepareStatement(sql);
			ResultSet configList = insertStmt.executeQuery();
			
			//Return Configuration List as an ArrayList of Arrays. 
			ArrayList<String[]> configs = new ArrayList<>();
			
			while (configList.next()) {
				String config[] = {configList.getString(1), configList.getString(2), configList.getString(3), "We're displaying task variants?", configList.getString(4), Integer.toString(configList.getInt(5))};  
				configs.add(config);
			}
		
			connection.close();
			return configs; 
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/** Returns all the information about a single configuration, to be used displayed on the configuration page. */
	public String[] getConfigInformation(int id) {
		//id = 30; //Temporary id to be used for testing purposes.  
		try {
		    try {
				Class.forName(jdbcDriver);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		    
			Connection connection = DriverManager.getConnection(mysqlURL, username, password);
			
			String sql = "SELECT teamName, description, datasetName, evaluator, id FROM configurations WHERE id = " + id + ";"; 
			PreparedStatement insertStmt = connection.prepareStatement(sql);
			ResultSet configInfoList = insertStmt.executeQuery();
			configInfoList.next();
			
			//Return List of information for configuration.
			String configInfo[] = new String[6];
			configInfo[0] = configInfoList.getString(1);
			configInfo[1] = configInfoList.getString(2);
			configInfo[2] = configInfoList.getString(3);
			configInfo[3] = "task_variant_b";
			configInfo[4] = configInfoList.getString(4);
			configInfo[5] = Integer.toString(configInfoList.getInt(5));	
			connection.close();	
			return configInfo; 
			
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
