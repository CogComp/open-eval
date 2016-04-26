package controllers.readers; 

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import controllers.FrontEndDBInterface;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.PennTreebankPOSReader; 
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;

public class Reader {
    private int queryOffset = 0;
    
     /** Given a dataset name, this will return a List<TextAnnotation> from the database. 
    */
    public List<TextAnnotation> getTextAnnotationsFromDB(String datasetName) {
        FrontEndDBInterface f = new FrontEndDBInterface();
        Connection conn = f.getConnection();
        
        String sql = "SELECT textAnnotation FROM textannotations WHERE dataset_name = ?";
        ResultSet textAnnotationsRS; 
        List<TextAnnotation> textAnnotations = new ArrayList<>();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql); 
            stmt.setString(1, datasetName);
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
        return textAnnotations; 
    }
    
    
    /** Given a dataset name, this will return a List<TextAnnotation> from the database with the specified number of TextAnnotations.  
    */
    public List<TextAnnotation> getTextAnnotationsFromDBPartial(String datasetName, int queryCount) {
        FrontEndDBInterface f = new FrontEndDBInterface();
        Connection conn = f.getConnection();
        
        String sql = "SELECT textAnnotation FROM textannotations WHERE dataset_name = ? LIMIT ?,?";
        ResultSet textAnnotationsRS; 
        List<TextAnnotation> textAnnotations = new ArrayList<>();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql); 
            stmt.setString(1, datasetName);
            stmt.setInt(2, queryOffset);
            stmt.setInt(3, queryCount);
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
        
        if (textAnnotations.isEmpty()) { //All the text annotations have been returned. 
            return null;
        }
        
        queryOffset += queryCount;
        return textAnnotations; 
    }
    
    /** Specialized function to insert just 20% of the ACE-2005 nw and bn folders for Paul's class. 
    */
    public void insertDatasetACE(String corpusName, String datasetPath) {
        ACEReader aceReaderNW;
        ACEReader aceReaderBN;
        String nw[] = {"nw"};
        String bn[] = {"bn"};
        
        try {
            aceReaderNW = new ACEReader(datasetPath, nw, false);
            aceReaderBN = new ACEReader(datasetPath, bn, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        Iterator taIteratorNW = aceReaderNW.iterator();
        Iterator taIteratorBN = aceReaderBN.iterator();
        
        int sizeNW = 0; //Number of text annotations in NW folder.
        int sizeBN = 0; //Number of text annotations in BN folder.
       
        List<TextAnnotation> textAnnotationsNW = new ArrayList<>();
        while (taIteratorNW.hasNext()) {
            TextAnnotation ta = (TextAnnotation) taIteratorNW.next();
            textAnnotationsNW.add(ta);
            sizeNW++;
        }
        
        List<TextAnnotation> textAnnotationsBN = new ArrayList<>();
        while (taIteratorBN.hasNext()) {
            TextAnnotation ta = (TextAnnotation) taIteratorBN.next();
            textAnnotationsBN.add(ta);
            sizeBN++;
        }
       
        List<TextAnnotation> textAnnotations = new ArrayList<>();
        
        for (int i = 0; i < (int)(sizeNW * .2); i++) {
            textAnnotations.add(textAnnotationsNW.get(i));
        }
        
        for (int i = 0; i < (int)(sizeBN * .2); i++) {
            textAnnotations.add(textAnnotationsBN.get(i));
        }
        
        insertIntoDatasets(corpusName);
        storeTextAnnotations(corpusName, textAnnotations);
    }
    
    /** Inserts a dataset into the MySQL database as a series of JSON TextAnnotations.
    */
    public List<TextAnnotation> insertDatasetIntoDB(String corpusName, String datasetPath, String dType) {
        List<TextAnnotation> textAnnotations = getTextAnnotations(corpusName, datasetPath, dType); 
        insertIntoDatasets(corpusName); 
        storeTextAnnotations(corpusName, textAnnotations);   
        return textAnnotations; 
    }
    
    /** Gets a List of TextAnnotations given the name of the corpus and the path to the dataset. 
    */
    private List<TextAnnotation> getTextAnnotations(String corpusName, String datasetPath, String dType) {
        List<TextAnnotation> textAnnotations = null;
        if (dType.equals("POS")) {
            PennTreebankPOSReader posReader = new PennTreebankPOSReader(corpusName); 
            posReader.readFile(datasetPath);
            textAnnotations = posReader.getTextAnnotations(); 
        }
        else if (dType.equals("ACE")) {
            ACEReader aceReader;
            try {
                aceReader = new ACEReader(datasetPath, false);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Iterator taIterator = aceReader.iterator();
           
            textAnnotations = new ArrayList<>();
            while (taIterator.hasNext()) {
                TextAnnotation ta = (TextAnnotation) taIterator.next();
                textAnnotations.add(ta);
            }
        }
        return textAnnotations; 
    }
    
    /** Inserts the name of the new dataset into the datasets table. 
    */
    private void insertIntoDatasets(String corpusName) {
        FrontEndDBInterface f = new FrontEndDBInterface(); 
        Connection conn = f.getConnection(); 
        String sql = "INSERT INTO datasets VALUES (?, 'Part of Speech Tagging') ;";
        
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