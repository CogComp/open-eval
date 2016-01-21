package controllers;

import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

public class ToyTextAnnotationGenerator {
    static String documentString = "Saul or Soul; that is the question";

    public static TextAnnotation generateToyTextAnnotation(int numDocs) {
        int i = 0;
        List<String[]> docs = new ArrayList<>();
        while (i < numDocs) {
            docs.add(documentString.split(" "));
            i++;
        }
        return BasicTextAnnotationBuilder.createTextAnnotationFromTokens(docs);
    }
}
