package controllers; 

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.security.MessageDigest;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import edu.illinois.cs.cogcomp.core.experiments.EvaluationRecord;

/**
 * Class to store and retrieve configurations and history. 
 * 
 *
 */

public class FrontEndDBInterface {    
    private String jdbcDriver;
    private String mysqlURL;
    private String username;
    private String password;
    private Integer timeout;
   
    Config conf;
    
    public FrontEndDBInterface() {
    	
    	conf = ConfigFactory.load();
    	jdbcDriver = conf.getString("db.default.driver");
    	mysqlURL = conf.getString("db.default.url");
    	username = conf.getString("db.default.username");
    	password = conf.getString("db.default.password");
    	timeout = conf.getInt("db.default.timeout");
    }
    
    /**----------------------USER & TEAM DB FUNCTIONS-------------------------------------------*/
    public void insertNewUserToDB(String username, String password, String team) {
        try {
            Connection conn = getConnection();
            
            /*Hashing user's password.*/
            String userPassHash = hash(password);
            
            String sql = "INSERT INTO users(username, password, teamName) VALUES (?, ?, ?);";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, userPassHash);
            stmt.setString(3, team);
            
            stmt.executeUpdate();
            conn.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public boolean authenticateUser(String userName, String password) {
        try {
            Connection conn = getConnection();
            
            String passHashUser = hash(password);
            
            String sql = "SELECT password FROM users WHERE username = ?;";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userName);
            ResultSet passRS = stmt.executeQuery();
            if (!passRS.isBeforeFirst()) { //If there is not user with the specified username. 
                conn.close();
                return false;
            }
            passRS.first();
            String passHashDB = passRS.getString(1);
            
            boolean passVerify = passHashUser.equals(passHashDB);
            conn.close();
            if (passVerify) 
                return true;
            else
                return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public boolean checkTeamPassword(String teamName, String teamPassword) {
        try {
            Connection conn = getConnection();
            
            String sql = "SELECT password FROM teams WHERE name = ?;";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, teamName);
            
            ResultSet passwordRS = stmt.executeQuery();
            passwordRS.first();
            String teamPassHashDB = passwordRS.getString(1);
            
            /*Hashing the users password.*/
            String teamPassHashUser = hash(teamPassword);
            
            /*Comparing the user's password with the one in the database.*/
            boolean samePass = teamPassHashUser.equals(teamPassHashDB);
            
            conn.close();
            return samePass;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public boolean userCanAccessConfig(String userName, int configID) {
        try {
            Connection conn = getConnection();
            
            /*Checking to see if this is a super user.*/
            String sql = "SELECT isSuper FROM users WHERE username = ?;";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userName);
            ResultSet isSuperRS = stmt.executeQuery();
            isSuperRS.first();
            
            if (isSuperRS.getBoolean(1)) {
                conn.close();
                return true;
            }
            
            sql = "SELECT teamName FROM users WHERE userName = ?;";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, userName);
            ResultSet teamNameRS = stmt.executeQuery();
            teamNameRS.first();
            String teamNameUser = teamNameRS.getString(1);
            
            sql = "SELECT team_name FROM configurations WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, configID);
            teamNameRS = stmt.executeQuery();
            teamNameRS.first();
            String teamNameConfig = teamNameRS.getString(1);
            
            boolean isSame = teamNameUser.equals(teamNameConfig);
            
            conn.close();
            return isSame;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public String getTeamnameFromUsername(String username) {
        try {
            Connection conn = getConnection();
            
            String sql = "SELECT teamName FROM users WHERE username = ?;";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            
            ResultSet teamnameRS = stmt.executeQuery();
            teamnameRS.first();
            String teamname = teamnameRS.getString(1);
            
            conn.close();
            return teamname;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**----------------------CONFIGURATION DB FUNCTIONS----------------------------------*/    
    
    
    /** Stores the received configuration in the MySQL configurations table and taskvariants table. */
    public long insertConfigToDB(String datasetName, String configName, String description, String evaluator, String taskType, String taskVariant, String teamName) {
        try {            
            Connection conn = getConnection();
            
            /*Storing basic configuration info.*/
            String sql = "INSERT INTO configurations(id, datasetName, teamName, description, evaluator, taskType, taskVariant, team_name) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setNull(1, Types.INTEGER); //Set null so MySQL can auto-increment the primary key (id).
            stmt.setString(2, datasetName);
            stmt.setString(3, configName);
            stmt.setString(4, description);
            stmt.setString(5, evaluator);
            stmt.setString(6, taskType);
            stmt.setString(7, taskVariant);
            stmt.setString(8, teamName);
            stmt.executeUpdate();
            
            ResultSet idRS = stmt.getGeneratedKeys();
            idRS.first();
            long id = idRS.getLong(1);
           
            conn.close();
            return id;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /** Returns a list of all the configurations in the database to be displayed on landing page. */
    public List<models.Configuration> getConfigList(String teamName) {
        try {
            Connection conn = getConnection();
            
            String sql = "SELECT teamName, description, datasetName, taskType, taskVariant, evaluator, id FROM configurations WHERE team_name = ? ORDER BY lastUpdated DESC;";
           
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, teamName);
            ResultSet configList = stmt.executeQuery();
            
            List<models.Configuration> configs = new ArrayList<>();
            
            while (configList.next()) {
                models.Configuration config = new models.Configuration(configList.getString(1), configList.getString(2), configList.getString(3), configList.getString(4), configList.getString(5), configList.getString(6), Integer.toString(configList.getInt(7))); 
                config.records = getLatestRecordFromConfID(configList.getInt(7));
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
            
            String sql = "SELECT teamName, description, datasetName, taskType, taskVariant, evaluator, id FROM configurations WHERE id = " + id + ";";
            PreparedStatement insertStmt = connection.prepareStatement(sql);
            ResultSet configInfoList = insertStmt.executeQuery();
            configInfoList.next();
            
            /*Return information about configuration.*/
            models.Configuration config = new models.Configuration(configInfoList.getString(1), configInfoList.getString(2), configInfoList.getString(3), configInfoList.getString(4),
                configInfoList.getString(5), configInfoList.getString(6), Integer.toString(configInfoList.getInt(7)));
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
    
    /**----------------------RECORD DB FUNCTION------------------------------------*/
    
    /** Stores information at the start of a particular run.
    Returns the id of the record inserted. 
    */
    public String storeRunInfo(int configuration_id, String url, String author, String repo, String comment) {
        try {
            Connection conn = getConnection();
            
            String sql = "INSERT INTO records (configuration_id, url, author, repo, comment, isRunning) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, configuration_id);
            stmt.setString(2, url);
            stmt.setString(3, author);
            stmt.setString(4, repo);
            stmt.setString(5, comment);
            stmt.setBoolean(6, true);
            stmt.executeUpdate(); 
            
            ResultSet newID = stmt.getGeneratedKeys();
            newID.first();
            int record_id = newID.getInt(1);
            
            /*Updating configurations with time of last run.*/
            sql = "SELECT date FROM records WHERE record_id = ?;";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, record_id);
            ResultSet newDateRS = stmt.executeQuery();
            newDateRS.first();
            String newDate = newDateRS.getTimestamp(1).toString();
            sql = "UPDATE configurations SET lastUpdated = ? WHERE id = ?;";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, newDate);
            stmt.setInt(2, configuration_id);
            stmt.executeUpdate();
            
            conn.close();
            return Integer.toString(record_id);
        } catch (Exception e) {
            throw new RuntimeException(e); 
        }
    }
    
    public List<models.Record> getLatestRecordFromConfID(int configuration_id) {
        try {
            Connection conn = getConnection();
            
            String sql = "SELECT record_id, date, comment, repo, author, isRunning FROM records WHERE configuration_id = ? ORDER BY date DESC LIMIT 1;";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, configuration_id);
            ResultSet recordsRS = stmt.executeQuery();
           
            List<models.Record> record = new ArrayList<>();
            
            if (recordsRS.isBeforeFirst()) {   
                recordsRS.first();
                int record_id = recordsRS.getInt(1);
                models.Metrics metrics = getMetricsFromRecordID(record_id);
                models.Record latestRecord = new models.Record(Integer.toString(record_id), recordsRS.getTimestamp(2).toString(), recordsRS.getString(3), 
                    recordsRS.getString(4), recordsRS.getString(5), metrics, Integer.toString(configuration_id)); 
                latestRecord.isRunning = recordsRS.getBoolean(6);
                record.add(latestRecord);
            }
            
            conn.close();
            return record;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /** Retrives the records of a configuration.*/
    public List<models.Record> getRecordsFromConfID(int configuration_id) {
        try {
            Connection conn = getConnection(); 
        
            String sql = "SELECT record_id, date, comment, repo, author, isRunning FROM records WHERE configuration_id = ? ORDER BY date DESC;";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, configuration_id);
            ResultSet recordsRS = stmt.executeQuery();
            
            List<models.Record> records = new ArrayList<>();
            
            while (recordsRS.next()) {
                int record_id = recordsRS.getInt(1);
                models.Metrics metrics = getMetricsFromRecordID(record_id);
                models.Record record = new models.Record(Integer.toString(record_id), recordsRS.getTimestamp(2).toString(), recordsRS.getString(3), 
                    recordsRS.getString(4), recordsRS.getString(5), metrics, Integer.toString(configuration_id)); 
                record.isRunning = recordsRS.getBoolean(6);
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
            // Need to make average time right & reflected in db
            models.Metrics metrics = new models.Metrics(metricsRS.getDouble(1), metricsRS.getDouble(2), metricsRS.getDouble(3), "average solve time", metricsRS.getInt(4), 
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
           
            String sql = "SELECT date, comment, repo, author, configuration_id, isRunning FROM records WHERE record_id = " + record_id + ";";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet recordsRS = stmt.executeQuery();
            recordsRS.first();
            
            models.Record record = new models.Record(Integer.toString(record_id), recordsRS.getTimestamp(1).toString(), recordsRS.getString(2), 
                recordsRS.getString(3), recordsRS.getString(4), metrics, recordsRS.getString(5), recordsRS.getBoolean(6));
                
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
    
    /**--------------------EVALUTATION DB FUNCTIONS-------------------------------------*/
    
    public void insertEvaluationIntoDB(EvaluationRecord evalRecord, int record_id, boolean isRunning) {
        try {
            Connection conn = getConnection();
            String sql = "UPDATE records SET f1=?, precision_score=?, recall=?, gold_count=?, correct_count=?, predicted_count=?, missed_count=?, extra_count=?, isRunning=?";
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
            stmt.setBoolean(9, isRunning);
            stmt.setInt(10, record_id);
            
            stmt.executeUpdate();
            conn.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    
    /**------------------------TASK DB FUNCTIONS---------------------------------*/
    
    /**Gets all the tasks that Open Eval can do.*/
    public List<String> getTasks() {
        try {
            Connection conn = getConnection();
            
            String sql = "SELECT name FROM tasks;";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet tasksRS = stmt.executeQuery();
            
            List<String> tasks = new ArrayList<>();
            while (tasksRS.next()) {
                tasks.add(tasksRS.getString(1));
            }
            
            conn.close();
            return tasks;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /** Gets all the datasets for a specific task. 
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
            
            conn.close();
            return datasets; 
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**Gets all the task variants of a task.
    */
    public List<String> getTaskVariantsForTask(String taskName) {
        try {
            Connection conn = getConnection();
            
            String sql = "SELECT name FROM taskvariants WHERE task_name = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, taskName);
            ResultSet taskVariantsRS =  stmt.executeQuery();
            
            List<String> taskVariants = new ArrayList<>();
            while (taskVariantsRS.next()) {
                taskVariants.add(taskVariantsRS.getString(1));
            }
            
            conn.close();
            return taskVariants;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**Gets the evaluator, which is determined by a task and a task-variant.*/
    public String getEvaluator(String taskName, String taskVariant) {
        try {
            Connection conn = getConnection();
            System.out.println(taskName+", "+taskVariant);
            String sql = "SELECT name FROM taskMappings WHERE task_name = ? AND task_variant = ?;";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, taskName);
            stmt.setString(2, taskVariant);
            ResultSet evaluatorRS = stmt.executeQuery();
            evaluatorRS.first();
            String evaluator = evaluatorRS.getString(1);
            
            conn.close();
            return evaluator;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
   
    /**Gets the evaluator for a task. - DON'T USE THIS ONE.*/
    public String getEvaluatorForTask (String taskName) {
        try {
            Connection conn = getConnection();
            
            String sql = "SELECT name FROM evaluators WHERE task_name = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, taskName);
            ResultSet evaluatorRS = stmt.executeQuery();
            evaluatorRS.first();
            String evaluator = evaluatorRS.getString(1);
            
            conn.close();
            return evaluator;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /** TASK VARIANT DB FUNCTIONS. */
    public List<String> getViewsForTaskVariant(String taskVariant) {
        try {
            Connection conn = getConnection();
            
            String sql = "SELECT view FROM views WHERE task_variant = ?;";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet viewsRS = stmt.executeQuery();
            
            List<String> views = new ArrayList<>();
            while (viewsRS.next()) {
                views.add(viewsRS.getString(1));
            }
            
            conn.close();
            return views;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        
    }
    
    public String getEvaluatorView(String task) {
        try {
            Connection conn = getConnection();
            
            String sql = "SELECT evaluatorView FROM tasks WHERE name = ?;";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, task);
            ResultSet evaluatorViewRS = stmt.executeQuery();
            evaluatorViewRS.first();
            String evaluatorView = evaluatorViewRS.getString(1);
            
            conn.close();
            return evaluatorView;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**---------------------HELPER FUNCTIONS-----------------------------------*/
    final protected static char[] hexArray = "0123456789abcdef".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    
    private String hash(String strToHash) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(strToHash.getBytes("UTF-8"));
            byte[] hashByteArr = md.digest();
            String hashHex = bytesToHex(hashByteArr);    
            return hashHex;
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
            DriverManager.setLoginTimeout(timeout);
            Connection conn = DriverManager.getConnection(mysqlURL, username, password);
            return conn;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
}