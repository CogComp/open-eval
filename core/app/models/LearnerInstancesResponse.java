package models;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

/**
 * Created by ryan on 3/13/16.
 */
public class LearnerInstancesResponse {
    public TextAnnotation[] textAnnotations;
    public String[] errors;
    public String requestWideError;

    public LearnerInstancesResponse(TextAnnotation[] textAnnotations, String[] errors, String requestWideError){
        this.textAnnotations = textAnnotations;
        this.errors = errors;
        this.requestWideError = requestWideError;
    }
}
