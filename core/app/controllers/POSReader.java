package controllers; 

import edu.illinois.cs.cogcomp.nlp.corpusreaders.PennTreebankPOSReader;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class POSReader {
    
    public List<TextAnnotation> insertDatasetIntoDB(String corpusName, String datasetPath) {
        List<TextAnnotation> textAnnotations = getTextAnnotations(corpusName, datasetPath); 
        insertIntoDatasets(corpusName); 
        storeTextAnnotations(corpusName, textAnnotations);   
        return textAnnotations; 
    }
    
    /** Gets a List of TextAnnotations given the name of the corpus and the path to the dataset. 
    */
    private List<TextAnnotation> getTextAnnotations(String corpusName, String datasetPath) {
        PennTreebankPOSReader posReader =  new PennTreebankPOSReader(corpusName); 
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