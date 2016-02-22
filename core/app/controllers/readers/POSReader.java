package controllers; 

import edu.illinois.cs.cogcomp.nlp.corpusreaders.PennTreebankPOSReader;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

import java.util.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet; 

public class POSReader {
    private int queryOffset = 0;
    private int queryLimit = 25;
    
    /** Given a dataset name, this will return a List<TextAnnotation> from the database. 
    */
    public List<TextAnnotation> getTextAnnotationsFromDB(String datasetName) {
        FrontEndDBInterface f = new FrontEndDBInterface();
        Connection conn = f.getConnection();
        
        String sql = "SELECT textAnnotation FROM textannotations WHERE dataset_name = ? LIMIT ?,?";
        ResultSet textAnnotationsRS; 
        List<TextAnnotation> textAnnotations = new ArrayList<>();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql); 
            stmt.setString(1, datasetName);
            stmt.setInt(queryOffset);
            stmt.setInt(queryLimit);
            textAnnotationsRS = stmt.executeQuery(); 
         
            while (textAnnotationsRS.next()) {
                String taJson = textAnnotationsRS.getString(1);
                TextAnnotation ta; 
                try {
                    ta = SerializationHelper.deserializeFromJson(taJson);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                textAnnotations.add(ta); 
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        
        queryOffset += queryLimit;
        return textAnnotations; 
    }
    
    
    /** Inserts a dataset into the MySQL database as a series of JSON TextAnnotations.
    */
    public List<TextAnnotation> insertDatasetIntoDB(String corpusName, String datasetPath) {
        List<TextAnnotation> textAnnotations = getTextAnnotations(corpusName, datasetPath); 
        insertIntoDatasets(corpusName); 
        storeTextAnnotations(corpusName, textAnnotations);   
        return textAnnotations; 
    }
    
    /** Gets a List of TextAnnotations given the name of the corpus and the path to the dataset. 
    */
    private List<TextAnnotation> getTextAnnotations(String corpusName, String datasetPath) {
        PennTreebankPOSReader posReader = new PennTreebankPOSReader(corpusName); 
        posReader.readFile(datasetPath);
        List<TextAnnotation> textAnnotations = posReader.getTextAnnotations(); 
        return textAnnotations; 
    }
    
    /** Inserts the name of the new dataset into the datasets table. 
    */
    private void insertIntoDatasets(String corpusName) {
        FrontEndDBInterface f = new FrontEndDBInterface(); 
        Connection conn = f.getConnection(); 
        String sql = "INSERT INTO datasets VALUES (?);";
        
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, corpusName);
            stmt.executeUpdate(); 
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    /** Stores the TextAnnotations into the DB serialized to JSON.
    */
    private void storeTextAnnotations(String corpusName, List<TextAnnotation> textAnnotations) {
        FrontEndDBInterface f = new FrontEndDBInterface(); 
        Connection conn = f.getConnection(); 
        
        PreparedStatement stmt; 
        String sql; 
        for (TextAnnotation ta : textAnnotations) {
            String jsonTa = SerializationHelper.serializeToJson(ta);
            sql = "INSERT INTO textannotations VALUES (?, ?);";
            
            try {
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, jsonTa);
                stmt.setString(2, corpusName);
                stmt.executeUpdate();   
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
}