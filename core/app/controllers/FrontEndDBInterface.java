package controllers; 

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import java.util.ArrayList;
import java.util.List;

import play.*;
import play.mvc.*;

import org.json.*;
import com.mysql.jdbc.Driver;


/**
 * Class to store and retrieve configurations and history. 
 * 
 *
 */

public class FrontEndDBInterface {    
    private String jdbcDriver = "com.mysql.jdbc.Driver";
    private String mysqlURL = "jdbc:mysql://gargamel.cs.illinois.edu/openeval_db"; //Change this according to URL of MySQL server.
    private String username = "oeroot"; //Username to access database.
    private String password = "Fow,10#"; //Password for the above username. 
   
    /** Stores the received configuration in the MySQL configurations table and taskvariants table. */
    public void insertConfigToDB(String datasetName, String teamName, String description, String evaluator, String taskType, List<String> taskVariants) {
        try {            
            Connection connection = getConnection();
            
            /*Storing basic configuration info.*/
            String sql = "INSERT INTO configurations VALUES (?, '"+datasetName+"', '"+teamName+"', '"+description+"', '"+evaluator+"', '"+taskType+"');";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setNull(1, Types.INTEGER); //Set null so MySQL can auto-increment the primary key (id).
            stmt.execute();
            
            /*Storing info on the task-variants.*/
            /*Have to get the ID of the newly created configuration.*/ 
            sql = "SELECT MAX(id) from configurations;";
            stmt = connection.prepareStatement(sql);
            ResultSet idOfConfig = stmt.executeQuery();
            idOfConfig.first();
            int id = idOfConfig.getInt(1);
            
            /*Inserting all the task variants in to taskVariants table.*/
            sql = "INSERT INTO taskvariants VALUES (?, ?);";
            stmt = connection.prepareStatement(sql);
            for (String taskVariant : taskVariants){
                stmt.setInt(1, id);
                stmt.setString(2, taskVariant);
                stmt.execute();
            }
            
            connection.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /** Returns a list of all the configurations in the database to be displayed on landing page. */
    public List<models.Configuration> getConfigList() {
        try {
            Connection connection = getConnection();
            
            String sql = "SELECT teamName, description, datasetName, evaluator, id FROM configurations;";
            PreparedStatement insertStmt = connection.prepareStatement(sql);
            ResultSet configList = insertStmt.executeQuery();
            
            List<models.Configuration> configs = new ArrayList<>();
            
            while (configList.next()) {
                models.Configuration config = new models.Configuration(configList.getString(1), configList.getString(2), configList.getString(3), "We're displaying task variants?", configList.getString(4), Integer.toString(configList.getInt(5))); 
                configs.add(config);
            }
        
            connection.close();
            return configs; 
        } catch (Exception e) {
			throw new RuntimeException(e);
        }
    }
    
    /** Returns all the information about a single configuration as a configuration object.*/
    public models.Configuration getConfigInformation(int id) { 
        try {       
            Connection connection = getConnection();
            
            String sql = "SELECT teamName, description, datasetName, evaluator, id FROM configurations WHERE id = " + id + ";"; 
            PreparedStatement insertStmt = connection.prepareStatement(sql);
            ResultSet configInfoList = insertStmt.executeQuery();
            configInfoList.next();
            
            /*Return information about configuration.*/
            models.Configuration config = new models.Configuration(configInfoList.getString(1), configInfoList.getString(2), configInfoList.getString(3),
                "task_variant_b", configInfoList.getString(4), Integer.toString(configInfoList.getInt(5))); 
            connection.close(); 
            return config; 
        } catch (Exception e) {
			throw new RuntimeException(e);
        }
    }
	
	/** Stores information at the start of a particular run. - INCOMPLETE*/
	public void storeRunInfo() {
		Connection conn = getConnection();
	}
	
	/** Retrives the records of a configuration - INCOMPLETE*/
	/*
	public List<models.Record> getRecords(int configuration_id) {
		Connection conn = getConnection(); 
		
		String sql = "SELECT date, comment, repo, author, score FROM records WHERE id = " + configuration_id + ";";
		PreparedStatement stmt = conn.prepareStatement(sql);
		ResultSet recordsRS = stmt.executeQuery();
		return null;
	}
	*/
	
	/** Returns a connection to the Gargamel database.*/
	private Connection getConnection() {
		try {
			Class.forName(jdbcDriver);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		
        try { 
            Connection conn = DriverManager.getConnection(mysqlURL, username, password);
			return conn;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
    
}