import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import controllers.Redactor;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import models.Configuration;

/**
 * A collection of tests for the {@link Redactor}.
 * 
 * TODO: Fetch ACE data from database, not a json file.
 * 
 */
public class RedactorTest {
    
    private List<TextAnnotation> dummyTextAnnotations;
    private List<TextAnnotation> aceTextAnnotations;
    private final int NUM_ANNOTATIONS = 2;
    private final boolean WITH_NOISY_LABELS = true;
    private final String ACE_DATA_FILENAME = "sampleDocument.json";
    
    @Before
    public void setUpTextAnnotations() {
        // Instantiate dummy TA list.
        dummyTextAnnotations = new ArrayList<>();
        
        // Generate some dummy text annotations with noisy labels
        for (int i = 0; i < NUM_ANNOTATIONS; i++) {
            TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(WITH_NOISY_LABELS);
            dummyTextAnnotations.add(ta);
        }
        // Read in the ACE dataset as a list of text annotations.
        aceTextAnnotations = readInAceData();
    }
    
    /**
     * Fails if the {@code Redactor} left any views other than {@code SENTENCE} and {@code TOKENS}.
     */
    @Test
    public void rawTextAndSentenceBoundariesRedactionTest() {
        Configuration runConfig = new Configuration("", "", "", "Part of Speech Tagging", "Raw Text", "", "");
        List<TextAnnotation> cleansedAnnotations = Redactor.removeAnnotations(dummyTextAnnotations, runConfig);
        for (TextAnnotation ta : cleansedAnnotations) {
            for (String viewName : ta.getAvailableViews()) {
                String message = "View " + viewName + " should have been removed but was not";
                Assert.assertTrue(message, viewName.equals(ViewNames.SENTENCE) || viewName.equals(ViewNames.TOKENS));
            }
        }
    }
    
    /**
     * Fails if the {@code Redactor.NER_VIEW_NAME} does not exist, it was removed by the {@code Redactor},
     * or the {@code Constituent} labels were not removed.
     */
    @Test
    public void removeNerLabelsTest() {
        // First, make sure the supplied file actually contains the NER View.
        for (TextAnnotation ta : aceTextAnnotations) {
            String message = Redactor.NER_VIEW_NAME + " not found in " + ACE_DATA_FILENAME  + ".";
            Assert.assertNotNull(message, ta.getView(Redactor.NER_VIEW_NAME));
        }
        
        Configuration runConfig = new Configuration("", "", "", "Named Entity Recognition", "Gold Tokens", "", "");
        List<TextAnnotation> cleansedAnnotations = Redactor.removeAnnotations(aceTextAnnotations, runConfig);
        for (TextAnnotation ta: cleansedAnnotations) {
            // Ensure the NER view name was not removed.
            View nerView = ta.getView(Redactor.NER_VIEW_NAME);
            Assert.assertNotNull(Redactor.NER_VIEW_NAME + " erroneously removed", nerView);

            // Make sure all constituent labels have been removed.
            for (Constituent c : nerView.getConstituents()) {
                Assert.assertEquals("", c.getLabel());
            }
        }
    }
    
    /**
     * Fails if the {@code Redactor.RELATION_EXTRACTION_VIEW_NAME} does not exist, it was removed by the {@code Redactor},
     * the {@code Relation}s were not removed, or the {@code Constituent} labels were not removed.
     */
    @Test
    public void relationExtractionTest() {
        // First, make sure the supplied file actually contains the relation extraction view.
        for (TextAnnotation ta : aceTextAnnotations) {
            String message = Redactor.RELATION_EXTRACTION_VIEW_NAME + " not found in " + ACE_DATA_FILENAME + ".";
            Assert.assertNotNull(message, ta.getView(Redactor.RELATION_EXTRACTION_VIEW_NAME));
        }
        
        Configuration runConfig = new Configuration("", "", "", "Relation Extraction", "Gold Tokens", "", "");
        List<TextAnnotation> cleansedAnnotations = Redactor.removeAnnotations(aceTextAnnotations, runConfig);
        for (TextAnnotation ta: cleansedAnnotations) {
            // Ensure the relation extraction view name was not removed.
            View reView = ta.getView(Redactor.RELATION_EXTRACTION_VIEW_NAME);
            Assert.assertNotNull(Redactor.RELATION_EXTRACTION_VIEW_NAME + " erroneously removed", reView);
            
            // Make sure all relations are removed.
            Assert.assertEquals("Not all relations removed", Collections.EMPTY_LIST, reView.getRelations());
            
            // Make sure all constituent labels have been removed.
            for (Constituent c : reView.getConstituents()) {
                Assert.assertEquals("", c.getLabel());
            }
        }
    }
    
    /**
     * Fails if the {@code ViewNames.COREF} view name does not exist, it was removed by the {@code Redactor},
     * or the {@code Relation}s were not removed.
     */
    @Test
    public void corefTest() {
        // First, make sure the supplied file actually contains the coref view.
        for (TextAnnotation ta : aceTextAnnotations) {
            String message = ViewNames.COREF + " not found in " + ACE_DATA_FILENAME + ".";
            Assert.assertNotNull(message, ta.getView(ViewNames.COREF));
        }
        
        Configuration runConfig = new Configuration("", "", "", "Co-reference", "Gold Tokens", "", "");
        List<TextAnnotation> cleansedAnnotations = Redactor.removeAnnotations(aceTextAnnotations, runConfig);
        for (TextAnnotation ta: cleansedAnnotations) {
            // Ensure the coref view name was not removed.
            View corefView = ta.getView(ViewNames.COREF);
            Assert.assertNotNull(ViewNames.COREF + " erroneously removed", corefView);
            
            // Make sure all relations are removed.
            Assert.assertEquals("Not all relations removed", Collections.EMPTY_LIST, corefView.getRelations());
        }
    }
    
    private List<TextAnnotation> readInAceData() {
        String fileContents = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(ACE_DATA_FILENAME));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            fileContents = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            TextAnnotation ta = SerializationHelper.deserializeFromJson(fileContents);
            List<TextAnnotation> tas = new ArrayList<>();
            tas.add(ta);
            return tas;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
