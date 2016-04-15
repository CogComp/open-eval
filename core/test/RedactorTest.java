import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import controllers.Redactor;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import models.Configuration;

public class RedactorTest {
    @Test
    public void sentenceboundariesRedactorTest() {
        List<TextAnnotation> toyAnnotations = new ArrayList<>();
        for (int i = 0; i < 1 ; i++) {
            TextAnnotation ta =  DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(true); //ToyTextAnnotationGenerator.generateToyTextAnnotation(3);
            toyAnnotations.add(ta);
        }
        Configuration runConfig = new Configuration("", "", "", "Part of Speech Tagging", "Sentence Boundaries", "", "");
        List<TextAnnotation> cleansedAnnotations = Redactor.removeAnnotations(toyAnnotations, runConfig);
    }
    
    @Test
    public void rawTextRedactionTest() {
        List<TextAnnotation> toyAnnotations = new ArrayList<>();
        for (int i = 0; i < 1 ; i++) {
            TextAnnotation ta =  DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(true); //ToyTextAnnotationGenerator.generateToyTextAnnotation(3);
            toyAnnotations.add(ta);
        }
        String orijson = SerializationHelper.serializeToJson(toyAnnotations.get(0));
//        System.out.println(orijson);
        Configuration runConfig = new Configuration("", "", "", "Part of Speech Tagging", "Raw text", "", "");
        List<TextAnnotation> cleansedAnnotations = Redactor.removeAnnotations(toyAnnotations, runConfig);
        String json = SerializationHelper.serializeToJson(cleansedAnnotations.get(0));
  //      System.out.println(json);
    }
    /*
    @Test
    public void aceDataTest() {
        String everything = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader("sampleDocument.json"));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            everything = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            TextAnnotation ta = SerializationHelper.deserializeFromJson(everything);
            List<TextAnnotation> tas = new ArrayList<>();
            tas.add(ta);
            Configuration runConfig = new Configuration("", "", "", "Co-reference", "Gold Token", "", "");
            List<TextAnnotation> cleansedAnnotations = Redactor.removeAnnotations(tas, runConfig);
            String jsonS = SerializationHelper.serializeToJson(cleansedAnnotations.get(0));
            PrintWriter writer = new PrintWriter("the-file-name.txt", "UTF-8");
            writer.write(jsonS);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */
}
