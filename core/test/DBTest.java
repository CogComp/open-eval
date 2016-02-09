import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.*;

import play.mvc.*;
import play.test.*;
import play.data.DynamicForm;
import play.data.validation.ValidationError;
import play.data.validation.Constraints.RequiredValidator;
import play.i18n.Lang;
import play.libs.F;
import play.libs.F.*;
import play.twirl.api.Content;

import static play.test.Helpers.*;
import static org.junit.Assert.*;

import controllers.FrontEndDBInterface; 

import edu.illinois.cs.cogcomp.core.experiments.EvaluationRecord;


/**
*JUnit tests for the database.
*/    
public class DBTest {
    String jdbcDriver = "com.mysql.jdbc.Driver";
    String mysqlURL = "jdbc:mysql://gargamel.cs.illinois.edu/openeval_db"; //Change this according to URL of MySQL server.
    String username = "oeroot"; //Username to access database.
    String password = "Fow,10#"; //Password for the above username. 

    
    @Test
    public void testInsertConfigToDB() {    
        try {
            /*Getting the initial count of the number of records in the database.*/
            Connection conn = DriverManager.getConnection(mysqlURL, username, password);
            String sql = "SELECT COUNT(*) FROM configurations;";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet ret = stmt.executeQuery();
            ret.first();
            int countOfRecordsInitial = ret.getInt(1);
            
            /*Inserting new configuration into database*/
            FrontEndDBInterface f = new FrontEndDBInterface();
            List<String> taskVariants = new ArrayList<>();
            taskVariants.add("testTskVar"); 
            f.insertConfigToDB("testDataset", "testTeamName", "testDescription", "testEvaluator", "testTaskType", taskVariants); 
        
            /*Checking to see if new count in database is old count + 1*/
            sql = "SELECT COUNT(*) FROM configurations;";
            stmt = conn.prepareStatement(sql);
            ret = stmt.executeQuery();
            ret.first();
            int countOfRecordsNew = ret.getInt(1); 
            assertEquals(countOfRecordsInitial + 1, countOfRecordsNew);

            /*Cleaning - Getting rid of the test record.*/  
            sql = "DELETE FROM configurations WHERE datasetName = 'testDataset'";
            stmt = conn.prepareStatement(sql); 
            stmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            if (e.getMessage().contains("The driver has not received any packets from the server.")) //In the case where we cannot connect to the DB. 
                return;
        }
    }
    
    @Test
    public void testGetConfigList() {
        try {
            /*Putting in a test configuration to see if it shows up in our configuration list.*/
            Connection conn = DriverManager.getConnection(mysqlURL, username, password);
            String sql = "INSERT INTO configurations (datasetName, teamName, description, evaluator, taskType) VALUES ('testDataset', 'testTeamName', 'testDescription', 'testEvaluator', 'testTaskType')";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.executeUpdate();
            
            /*Getting ID of newly added configuration.*/
            sql = "SELECT MAX(id) from configurations;";
            stmt = conn.prepareStatement(sql);
            ResultSet idOfConfig = stmt.executeQuery();
            idOfConfig.first();
            String id = Integer.toString(idOfConfig.getInt(1));
            
            
            /*Seeing if the newly inserted configuration shows up in the configuration list.*/
            boolean inConfigList = false; 
            FrontEndDBInterface f = new FrontEndDBInterface();
            List<models.Configuration> configList = f.getConfigList(); 
            if (configList != null) {
                 for (int i = 0; i < configList.size(); i++) {
                    if (configList.get(i).configuration_id.equals(id)) {
                        inConfigList = true; 
                        break;
                    }
                }
            }
            assertTrue(inConfigList);
            
            /*Cleaning - Getting rid of the test record.*/
            sql = "DELETE FROM configurations WHERE datasetName = 'testDataset'";
            stmt = conn.prepareStatement(sql); 
            stmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
             if (e.getMessage().contains("The driver has not received any packets from the server.")) //In the case where we cannot connect to the DB. 
                return;
        }
    } 
    
    @Test
    public void testGetConfigInformation() {
        try {
            /*Inserting a test configuration into the database that we will check to get the information of.*/
            Connection conn = DriverManager.getConnection(mysqlURL, username, password);
            String sql = "INSERT INTO configurations (datasetName, teamName, description, evaluator, taskType) VALUES ('testDataset', 'testTeamName', 'testDescription', 'testEvaluator', 'testTaskType')";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.executeUpdate();
            
            /*Checking to see if we can get the information that was within that configuration by ID.*/
            sql = "SELECT MAX(id) from configurations;";
            stmt = conn.prepareStatement(sql);
            ResultSet idOfConfig = stmt.executeQuery();
            idOfConfig.first();
            int id = idOfConfig.getInt(1);
            
            sql = "SELECT datasetName FROM configurations WHERE id = ?;";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet ret = stmt.executeQuery(); 
            ret.first();
            String checkDataset = ret.getString(1);
            assertEquals(checkDataset, "testDataset");
            
            /*Cleaning - Getting rid of the test record.*/
            sql = "DELETE FROM configurations WHERE datasetName = 'testDataset'";
            stmt = conn.prepareStatement(sql); 
            stmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
             if (e.getMessage().contains("The driver has not received any packets from the server.")) //In the case where we cannot connect to the DB. 
                return;
        }
    } 
    
    @Test
    public void testStoreRunInfo() {
        try {
            Connection conn = DriverManager.getConnection(mysqlURL, username, password);
            
            /*Inserting new configuration into database.*/
            FrontEndDBInterface f = new FrontEndDBInterface();
            List<String> taskVariants = new ArrayList<>();
            taskVariants.add("testTskVar"); 
            f.insertConfigToDB("testDataset", "testTeamName", "testDescription", "testEvaluator", "testTaskType", taskVariants); 
            
            /*Figuring out the configuration_id of the configuration we just inserted.*/
            String sql = "SELECT MAX(id) FROM configurations;";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet ret = stmt.executeQuery();
            ret.first();
            int configID = ret.getInt(1); 
            
            /*Inserting new run info into database.*/
            f.storeRunInfo(configID, "www.test.com", "J.R.R. Tolkien", "https://github.com/IllinoisCogComp/open-eval/", "This is a comment.");
            
            /*Testing to see if run info is in database.*/
            sql = "SELECT author FROM records WHERE configuration_id = " + configID + ";";
            stmt = conn.prepareStatement(sql);
            ret = stmt.executeQuery();
            ret.first();
            assertEquals("J.R.R. Tolkien", ret.getString(1));
            
            /*Cleaning - Getting rid of the test record and configuration.*/
            sql = "DELETE FROM configurations WHERE id = " + configID + ";";
            stmt = conn.prepareStatement(sql); 
            stmt.executeUpdate();
            
            sql = "DELETE FROM records WHERE configuration_id = " + configID + ";";
            stmt = conn.prepareStatement(sql);
            stmt.executeUpdate();
            conn.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Test
    public void testInsertEvaluationIntoDB() {
        try {
            Connection conn = DriverManager.getConnection(mysqlURL, username, password);
                
            /*Inserting new configuration into database.*/
            FrontEndDBInterface f = new FrontEndDBInterface();
            List<String> taskVariants = new ArrayList<>();
            taskVariants.add("testTskVar"); 
            f.insertConfigToDB("testDataset", "testTeamName", "testDescription", "testEvaluator", "testTaskType", taskVariants); 
            
            /*Figuring out the configuration_id of the configuration we just inserted.*/
            String sql = "SELECT MAX(id) FROM configurations;";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet ret = stmt.executeQuery();
            ret.first();
            int configID = ret.getInt(1); 
            
            /*Inserting new run info into database.*/
            String record_id = f.storeRunInfo(configID, "www.test.com", "J.R.R. Tolkien", "https://github.com/IllinoisCogComp/open-eval/", "This is a comment.");
            
            /*Inserting evaluation record into DB.*/
            EvaluationRecord evalRecord = new EvaluationRecord();
            evalRecord.incrementGold(8);
            evalRecord.incrementPredicted(4);
            evalRecord.incrementCorrect(4);
            f.insertEvaluationIntoDB(evalRecord, Integer.parseInt(record_id));
            
            /*Testing to see if record was updated in database.*/
            sql = "SELECT gold_count FROM records WHERE record_id = " + record_id + ";";
            stmt = conn.prepareStatement(sql);
            ret = stmt.executeQuery();
            ret.first();
            assertEquals(8, ret.getInt(1));
            
            /*Deleting test records from DB.*/
            sql = "DELETE FROM configurations WHERE id = " + configID + ";";
            stmt = conn.prepareStatement(sql); 
            stmt.executeUpdate();
            
            sql = "DELETE FROM records WHERE configuration_id = " + configID + ";";
            stmt = conn.prepareStatement(sql);
            stmt.executeUpdate();
            conn.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }        
    }
    
    @Test 
    public void testGetRecordFromRecordID() {
         try {
        Connection conn = DriverManager.getConnection(mysqlURL, username, password);
        
        /*Inserting new configuration into database.*/
        FrontEndDBInterface f = new FrontEndDBInterface();
        List<String> taskVariants = new ArrayList<>();
        taskVariants.add("testTskVar"); 
        f.insertConfigToDB("testDataset", "testTeamName", "testDescription", "testEvaluator", "testTaskType", taskVariants); 
        
        /*Figuring out the configuration_id of the configuration we just inserted.*/
        String sql = "SELECT MAX(id) FROM configurations;";
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet ret = stmt.executeQuery();
        ret.first();
        int configID = ret.getInt(1); 
        
        /*Inserting new run info into database.*/
        String record_id = f.storeRunInfo(configID, "www.test.com", "J.R.R. Tolkien", "https://github.com/IllinoisCogComp/open-eval/", "This is a comment.");
        
        EvaluationRecord evalRecord = new EvaluationRecord();
        evalRecord.incrementGold(8);
        evalRecord.incrementPredicted(4);
        evalRecord.incrementCorrect(4);
        f.insertEvaluationIntoDB(evalRecord, Integer.parseInt(record_id));
        
        models.Record record = f.getRecordFromRecordID(Integer.parseInt(record_id));
        models.Metrics metrics = record.metrics;
        assertEquals(metrics.gold_count, 8);
       
        
        /*Cleaning - Getting rid of the test record and configuration.*/
        sql = "DELETE FROM configurations WHERE id = " + configID + ";";
        stmt = conn.prepareStatement(sql); 
        stmt.executeUpdate();
        
        sql = "DELETE FROM records WHERE record_id = " + record_id + ";";
        stmt = conn.prepareStatement(sql);
        stmt.executeUpdate();
        conn.close();
    } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
}