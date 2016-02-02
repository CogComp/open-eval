package controllers; 

import edu.illinois.cs.cogcomp.nlp.corpusreaders.PennTreebankPOSReader;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement; 
import java.sql.SQLException;

public class POSReader {
    /*
    public POSReader(String corpusName, String datasetPath) {
        List<TextAnnotation> textAnnotations = getTextAnnotations(corpusName, datasetPath); 
        //storeTextAnnotations(textAnnotations);    
    }
    */
    
    public List<TextAnnotation> getTAs(String corpusName, String datasetPath) {
        List<TextAnnotation> textAnnotations = getTextAnnotations(corpusName, datasetPath); 
        //storeTextAnnotations(textAnnotations);    
        return textAnnotations; 
    }
    
    private List<TextAnnotation> getTextAnnotations(String corpusName, String datasetPath) {
        PennTreebankPOSReader posReader =  new PennTreebankPOSReader(corpusName); 
        posReader.readFile(datasetPath);
        List<TextAnnotation> textAnnotations = posReader.getTextAnnotations(); 
        return textAnnotations; 
    }
    
    private void storeTextAnnotations(List<TextAnnotation> textAnnotations) {
        FrontEndDBInterface f = new FrontEndDBInterface(); 
        Connection conn = f.getConnection(); 
        
        PreparedStatement stmt; 
        String sql; 
        for (TextAnnotation ta : textAnnotations) {
            String jsonTa = SerializationHelper.serializeToJson(ta);
            sql = "INSERT INTO textAnnotations VALUES (?);";
            
            try {
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, jsonTa);
                stmt.executeUpdate();   
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
}