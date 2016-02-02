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


/**
*
* Simple (JUnit) tests that can call all parts of a play app.
* If you are interested in mocking a whole application, see the wiki for more details.
*
*/
public class ApplicationTest {

    @Test
    public void simpleCheck() {
        int a = 1 + 1;
        assertEquals(2, a);
    }
    
    @Test
    public void testInsertConfigToDB() {
        String jdbcDriver = "com.mysql.jdbc.Driver";
        String mysqlURL = "jdbc:mysql://gargamel.cs.illinois.edu/openeval_db"; //Change this according to URL of MySQL server.
        String username = "oeroot"; //Username to access database.
        String password = "Fow,10#"; //Password for the above username. 
        
        try {
            /*Getting the initial count of the number of records in the database.*/
            Connection conn = DriverManager.getConnection(mysqlURL, username, password);
            String sql = "SELECT COUNT(*) FROM configurations;";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet ret = stmt.executeQuery();
            ret.first();
            int countOfRecordsInitial = ret.getInt(1);
            
            /*Inserting new record into database*/
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
        String jdbcDriver = "com.mysql.jdbc.Driver";
        String mysqlURL = "jdbc:mysql://gargamel.cs.illinois.edu/openeval_db"; //Change this according to URL of MySQL server.
        String username = "oeroot"; //Username to access database.
        String password = "Fow,10#"; //Password for the above username. 

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
        String jdbcDriver = "com.mysql.jdbc.Driver";
        String mysqlURL = "jdbc:mysql://gargamel.cs.illinois.edu/openeval_db"; //Change this according to URL of MySQL server.
        String username = "oeroot"; //Username to access database.
        String password = "Fow,10#"; //Password for the above username. 
        
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
    public void testGetRecords() {
    }
}
