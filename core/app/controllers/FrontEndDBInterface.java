package controllers; 

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import play.*;
import play.mvc.*;
import play.Logger;

import org.json.*;
import com.mysql.jdbc.Driver;

import edu.illinois.cs.cogcomp.core.experiments.EvaluationRecord;

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
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setNull(1, Types.INTEGER); //Set null so MySQL can auto-increment the primary key (id).
            stmt.executeUpdate();
            
            /*Storing info on the task-variants.*/
            /*Have to get the ID of the newly created configuration.*/ 
            ResultSet newID = stmt.getGeneratedKeys();
            newID.first();
            int id = newID.getInt(1);
            
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
            Connection conn = getConnection();
            
            String sql = "SELECT teamName, description, datasetName, evaluator, id FROM configurations;";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet configList = stmt.executeQuery();
            
            List<models.Configuration> configs = new ArrayList<>();
            
            while (configList.next()) {
                /*Getting task variant for this configuration.*/
                int configID = configList.getInt(5);
                sql = "SELECT taskVariant FROM taskvariants WHERE configurations_id = ?;";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, configID);
                ResultSet taskVariantRS = stmt.executeQuery();
                taskVariantRS.first();
                String taskVariant = taskVariantRS.getString(1);
                
                models.Configuration config = new models.Configuration(configList.getString(1), configList.getString(2), configList.getString(3), taskVariant, configList.getString(4), Integer.toString(configList.getInt(5))); 
                configs.add(config);
            }
        
            conn.close();
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
    
    public void deleteConfigAndRecords(int configuration_id) {
        try {
            Connection conn = getConnection();
            String sql = "DELETE FROM configurations WHERE id = " + configuration_id + ";";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.executeUpdate();
            
            sql = "DELETE FROM records WHERE configuration_id = " + configuration_id + ";";
            stmt = conn.prepareStatement(sql);
            stmt.executeUpdate();
            
            conn.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }
    
    /** Stores information at the start of a particular run.
    Returns the id of the record inserted. 
    */
    public String storeRunInfo(int configuration_id, String url, String author, String repo, String comment) {
        try {
            Connection conn = getConnection();
            
            String sql = "INSERT INTO records (configuration_id, url, author, repo, comment) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, configuration_id);
            stmt.setString(2, url);
            stmt.setString(3, author);
            stmt.setString(4, repo);
            stmt.setString(5, comment);
            stmt.executeUpdate(); 
            
            ResultSet newID = stmt.getGeneratedKeys();
            newID.first();
            int record_id = newID.getInt(1);
            
            conn.close();
            return Integer.toString(record_id);
        } catch (Exception e) {
            throw new RuntimeException(e); 
        }
    }
    
    /** Retrives the records of a configuration.*/
    public List<models.Record> getRecordsFromConfID(int configuration_id) {
        try {
            Connection conn = getConnection(); 
        
            String sql = "SELECT record_id, date, comment, repo, author FROM records WHERE configuration_id = " + configuration_id + ";";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet recordsRS = stmt.executeQuery();
            
            List<models.Record> records = new ArrayList<>();
            
            while (recordsRS.next()) {
                int record_id = recordsRS.getInt(1);
                models.Metrics metrics = getMetricsFromRecordID(record_id);
                models.Record record = new models.Record(Integer.toString(record_id), recordsRS.getTimestamp(2).toString(), recordsRS.getString(3), 
                    recordsRS.getString(4), recordsRS.getString(5), metrics); 
                records.add(record);
            }
            
            conn.close();
            return records;
        }
        catch (Exception e) {
            throw new RuntimeException(e); 
        }
    }
    
    private models.Metrics getMetricsFromRecordID(int record_id) {
        try {
            Connection conn = getConnection();
            String sql = "SELECT precision_score, recall, f1, gold_count, correct_count, predicted_count, missed_count, extra_count FROM records WHERE record_id = " + record_id + ";";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet metricsRS = stmt.executeQuery();
            metricsRS.first();
            models.Metrics metrics = new models.Metrics(metricsRS.getDouble(1), metricsRS.getDouble(2), metricsRS.getDouble(3), metricsRS.getInt(4), 
                metricsRS.getInt(5), metricsRS.getInt(6), metricsRS.getInt(7), metricsRS.getInt(8));   
                
            conn.close();
            return metrics;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public models.Record getRecordFromRecordID(int record_id) {
        try {
            Connection conn = getConnection();
            
            models.Metrics metrics = getMetricsFromRecordID(record_id);
           
            String sql = "SELECT date, comment, repo, author FROM records WHERE record_id = " + record_id + ";";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet recordsRS = stmt.executeQuery();
            recordsRS.first();
            
            models.Record record = new models.Record(Integer.toString(record_id), recordsRS.getTimestamp(1).toString(), recordsRS.getString(2), 
                recordsRS.getString(3), recordsRS.getString(4), metrics);
                
            conn.close();
            return record;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public void deleteRecordFromRecordID(int record_id) {
        try { 
            Connection conn = getConnection();
            
            String sql = "DELETE FROM records WHERE record_id = " + record_id + ";";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.executeUpdate();
            
            conn.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public void insertEvaluationIntoDB(EvaluationRecord evalRecord, int record_id) {
        try {
            Connection conn = getConnection();
            
            String sql = "UPDATE records SET f1=?, precision_score=?, recall=?, gold_count=?, correct_count=?, predicted_count=?, missed_count=?, extra_count=?";
            sql += " WHERE record_id = ?;";
     
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setDouble(1, evalRecord.getF1());
            stmt.setDouble(2, evalRecord.getPrecision());
            stmt.setDouble(3, evalRecord.getRecall());
            stmt.setInt(4, evalRecord.getGoldCount());
            stmt.setInt(5, evalRecord.getCorrectCount());
            stmt.setInt(6, evalRecord.getPredictedCount());
            stmt.setInt(7, evalRecord.getMissedCount());
            stmt.setInt(8, evalRecord.getExtraCount());
            stmt.setInt(9, record_id);
            
            stmt.executeUpdate();
            conn.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /** Get's all the datasets for a specific task. 
    */
    public List<String> getDatasetsForTask(String taskName) {
        try {
            Connection conn = getConnection();
            
            String sql = "SELECT name FROM datasets WHERE task = ?;";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, taskName);
            ResultSet datasetNamesRS = stmt.executeQuery();
            
            List<String> datasets = new ArrayList<>();
            while (datasetNamesRS.next()) {
                datasets.add(datasetNamesRS.getString(1));
            }
            return datasets; 
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    
    /** Returns a connection to the Gargamel database.*/
    public Connection getConnection() {
        try {
            Class.forName(jdbcDriver);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        
        try { 
            DriverManager.setLoginTimeout(2);
            Connection conn = DriverManager.getConnection(mysqlURL, username, password);
            return conn;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
}