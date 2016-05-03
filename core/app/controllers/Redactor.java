package controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.CoreferenceView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import models.Configuration;

/**
 * This class contains several static methods for removing information (e.g. views or labels)
 * from {@code TextAnnotations} objects.
 * 
 *  TODO: Extract tasks and task variants to enumerators.
 *  TODO: Generalize. Much of this was hard-coded to fit the specific needs of the CS546 class.
 *  
 */
public class Redactor {
    
    /**
     * List of view names to be kept when the task variant is "Sentence Boundaries".
     */
    private static final List<String> sentenceBoundariesViewNames = Arrays.asList(ViewNames.SENTENCE, ViewNames.TOKENS);
    
    public static final String NER_VIEW_NAME = "NER_GOLD_EXTENT_SPAN";
    
    public static final String RELATION_EXTRACTION_VIEW_NAME = "RELATIONVIEW";
    
    public static final String RAW_TEXT = "Raw Text";
    
    public static final String SENTENCE_BOUNDARIES = "Sentence Boundaries";
        
    /**
     * Only publicly visible method in the {@code Redactor} class. Given the task and task variant
     * (members of the {@code Configuration} object, it removes the appropriate Views and/or Relations.
     * For the task variants "Raw Text" and "Sentence Boundaries", it removes all views except 
     * {@code SENTENCE} and {@code TOKENS}.
     * 
     * @param textAnnotations: List of {@code TextAnnotation} objects.
     * @param runConfig: Configuration
     * @return List of redacted instances.
     */
    public static List<TextAnnotation> removeAnnotations(List<TextAnnotation> textAnnotations, Configuration runConfig) {
        // TODO: Send raw text without any views. This is currently not possible because serializaing and
        // deserializing text annotations requires the SENTENCE and TOKEN views.
        if (runConfig.task_variant.equals(RAW_TEXT) || runConfig.task_variant.equals(SENTENCE_BOUNDARIES)) {
            return removeViews(textAnnotations, sentenceBoundariesViewNames);
        } 
        switch (runConfig.task) {
            case "Part of Speech Tagging":
                   return removeViews(textAnnotations, sentenceBoundariesViewNames);
            case "Parsing":
                return removeViews(textAnnotations, sentenceBoundariesViewNames);
            case "Named Entity Recognition":
                   return removeLabelsForNER(textAnnotations);
            case "Relation Extraction":
                   return removeRelationsFromPredicateArgumentView(textAnnotations);
            case "Co-reference":
                   return removeCoreferenceRelations(textAnnotations);
            default:
                throw new RuntimeException("Task " + runConfig.task + " not yet implemented");
        }
    }
    /**
     * Removes views from {@code TextAnnotation} objects.  Each task variant has a number of views
     * necessary for the solver to solve it.  All other views are removed.
     */
    private static List<TextAnnotation> removeViews(List<TextAnnotation> textAnnotations, List<String> viewsToKeep) {
        if (viewsToKeep == null) {
            viewsToKeep = new ArrayList<String>();
        }
        viewsToKeep = new ArrayList<>(viewsToKeep);
        List<String> viewsToRemove = new ArrayList<>();
        List<TextAnnotation> cleansed = new ArrayList<>();
        for (TextAnnotation textAnnotation : textAnnotations) {
            for (String viewName : textAnnotation.getAvailableViews()) {
                if (!viewsToKeep.contains(viewName)) {
                    viewsToRemove.add(viewName);
                }
            }
            TextAnnotation cleansedAnnotation;
            try {
                cleansedAnnotation = (TextAnnotation)textAnnotation.clone();
            }
            catch(CloneNotSupportedException ce){
                cleansed.add(null);
                continue;
            }
            for(String viewName: viewsToRemove){
                cleansedAnnotation.removeView(viewName);
            }
            cleansed.add(cleansedAnnotation);
        }
        return cleansed;
    }
    /**
     * Removes the label from the {@code NER_VIEW_NAME} view.
     */
    private static List<TextAnnotation> removeLabelsForNER(List<TextAnnotation> cleansedAnnotations) {
        List<String> nerViews = new ArrayList<>();
        nerViews.add(ViewNames.SENTENCE);
        nerViews.add(ViewNames.TOKENS);
        nerViews.add(NER_VIEW_NAME);
        
        List<TextAnnotation> textAnnotations = removeViews(cleansedAnnotations, nerViews);
        for (TextAnnotation textAnnotation : textAnnotations) {
            View view = textAnnotation.getView(NER_VIEW_NAME);
            List<Constituent> constituents = view.getConstituents();
            // Each constituent is removed and re-added with an empty label
            for (Constituent c : constituents) {
                view.removeConstituent(c);
                int start = c.getStartSpan();
                int end = c.getEndSpan();
                view.addConstituent(new Constituent("", NER_VIEW_NAME, textAnnotation, start, end));
            }
            textAnnotation.addView(view.getViewName(), view);
        }
        return textAnnotations;
    }
    
    /**
     * Removes every {@code Relation} from the {@code RELATIONVIEW} in the list of text annotations.
     */
    private static List<TextAnnotation> removeRelationsFromPredicateArgumentView(List<TextAnnotation> uncleansedAnnotations) {
        List<String> relationExtractionViews = new ArrayList<>();
        relationExtractionViews.add(ViewNames.SENTENCE);
        relationExtractionViews.add(ViewNames.TOKENS);
        relationExtractionViews.add(RELATION_EXTRACTION_VIEW_NAME);
        
        List<TextAnnotation> textAnnotations = removeViews(uncleansedAnnotations, relationExtractionViews);
        for (TextAnnotation textAnnotation : textAnnotations) {
            Set<String> viewNames = textAnnotation.getAvailableViews();
            for (String viewName : viewNames) {
                View view = textAnnotation.getView(viewName);
                if (view instanceof PredicateArgumentView) {
                    PredicateArgumentView predicateArgumentView = (PredicateArgumentView) view;
                    predicateArgumentView.removeAllRelations();
                    
                    // Each constituent is removed and re-added with an empty label
                    for (Constituent c : predicateArgumentView.getConstituents()) {
                        predicateArgumentView.removeConstituent(c);
                        int start = c.getStartSpan();
                        int end = c.getEndSpan();
                        view.addConstituent(new Constituent("", RELATION_EXTRACTION_VIEW_NAME, textAnnotation, start, end));
                    }
                }
            }
        }
        return textAnnotations;
    }
    
    /**
     * Removes all coreference relations from {@code COREF} View.
     */
    private static List<TextAnnotation> removeCoreferenceRelations(List<TextAnnotation> uncleansedAnnotations) {
        List<String> coreferenceViews = new ArrayList<>();
        coreferenceViews.add(ViewNames.SENTENCE);
        coreferenceViews.add(ViewNames.TOKENS);
        coreferenceViews.add(ViewNames.COREF);
        
        List<TextAnnotation> textAnnotations = removeViews(uncleansedAnnotations, coreferenceViews);
        for (TextAnnotation textAnnotation : textAnnotations) {
            Set<String> viewNames = textAnnotation.getAvailableViews();
            for (String viewName : viewNames) {
                View view = textAnnotation.getView(viewName);
                if (view instanceof CoreferenceView) {
                    CoreferenceView coreferenceView = (CoreferenceView) view;
                    coreferenceView.removeAllRelations();
                    textAnnotation.addView(viewName, coreferenceView);
                    
                }
            }
        }
        return textAnnotations;
    }
}