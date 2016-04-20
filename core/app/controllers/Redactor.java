package controllers;

import java.util.ArrayList;
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

public class Redactor {
    /**
     * Only publicly visible method in the {@code Redactor} class. Given the task and task variant, 
     * it removes the appropriate Views and/or Relations. For the task variants "Raw Text" and 
     * "Sentence Boundaries", it removes all views except {@code SENTENCE} and {@code TOKENS}.
     * 
     * @param textAnnotations: List of {@code TextAnnotation} objects.
     * @param runConfig: Configuration
     * @return List of redacted instances.
     */
    public static List<TextAnnotation> removeAnnotations(List<TextAnnotation> textAnnotations, Configuration runConfig) {
        List<String> sentenceBoundariesViewNames = new ArrayList<>();
        sentenceBoundariesViewNames.add(ViewNames.SENTENCE);
        sentenceBoundariesViewNames.add(ViewNames.TOKENS);

        if (runConfig.task_variant.equals("Raw Text")) {
            return removeViews(textAnnotations, sentenceBoundariesViewNames);
        } else if (runConfig.task_variant.equals("Sentence Boundaries")) {
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
     * Static method to remove views from `TextAnnotation` objects.  Each task variant has a number of views
     * necessary for the solver to solve it.  All other views should be removed.
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
     * Removes the label from the NER_GOLD_EXTENT_SPAN.
     */
    private static List<TextAnnotation> removeLabelsForNER(List<TextAnnotation> cleansedAnnotations) {
        List<String> nerViews = new ArrayList<>();
        nerViews.add(ViewNames.SENTENCE);
        nerViews.add(ViewNames.TOKENS);
        nerViews.add("NER_GOLD_EXTENT_SPAN");
        List<TextAnnotation> textAnnotations = removeViews(cleansedAnnotations, nerViews);
        for (TextAnnotation textAnnotation : textAnnotations) {
            View view = textAnnotation.getView("NER_GOLD_EXTENT_SPAN");
            List<Constituent> constituents = view.getConstituents();
            for (Constituent c : constituents) {
                view.removeConstituent(c);
                int start = c.getStartSpan();
                int end = c.getEndSpan();
                view.addConstituent(new Constituent("", "NER_GOLD_EXTENT_SPAN", textAnnotation, start, end));
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
        relationExtractionViews.add("RELATIONVIEW");
        List<TextAnnotation> textAnnotations = removeViews(uncleansedAnnotations, relationExtractionViews);
        for (TextAnnotation textAnnotation : textAnnotations) {
            Set<String> viewNames = textAnnotation.getAvailableViews();
            for (String viewName : viewNames) {
                View view = textAnnotation.getView(viewName);
                if (view instanceof PredicateArgumentView) {
                    PredicateArgumentView predicateArgumentView = (PredicateArgumentView) view;
                    predicateArgumentView.removeAllRelations();
                    for (Constituent c : predicateArgumentView.getConstituents()) {
                        predicateArgumentView.removeConstituent(c);
                        int start = c.getStartSpan();
                        int end = c.getEndSpan();
                        view.addConstituent(new Constituent("", "RELATIONVIEW", textAnnotation, start, end));
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