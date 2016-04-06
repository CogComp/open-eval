package controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;

public class Redactor {

    /**
     * Calls removeAnnotations without the need for the user to create a singleton list.
     * @param textAnnotations
     * @param viewToKeep
     * @return
     */
    public static List<TextAnnotation> removeAnnotations(List<TextAnnotation> textAnnotations, String viewToKeep) {
        List<String> viewsToKeep = new ArrayList<>();
        viewsToKeep.add(viewToKeep);
        return removeAnnotations(textAnnotations, viewsToKeep);
    }
    /**
     * Static method to remove views from `TextAnnotation` objects.  Each task variant has a number of views
     * necessary for the learner to solve it.  All other views should be removed.
     * @param textAnnotations
     * @param viewsToKeep
     * @return
     */
    public static List<TextAnnotation> removeAnnotations(List<TextAnnotation> textAnnotations, List<String> viewsToKeep) {
        if (viewsToKeep == null) {
            viewsToKeep = new ArrayList<String>();
        }
        viewsToKeep = new ArrayList<>(viewsToKeep);
        viewsToKeep.add(ViewNames.SENTENCE);
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
     * Removes the token label from all views for which the {@code ViewType} is {@code TokenLabelView}.
     * @param textAnnotation
     */
    public static List<TextAnnotation> removeTokenLabels(List<TextAnnotation> textAnnotations) {
        for (TextAnnotation textAnnotation : textAnnotations) {
            View view = textAnnotation.getView(ViewNames.TOKENS);
            if (view instanceof TokenLabelView) {
                TokenLabelView tokenLabelView = (TokenLabelView) view;
                int start = tokenLabelView.getStartSpan();
                int end = tokenLabelView.getEndSpan();
                List<Constituent> constituents = tokenLabelView.getConstituents();
                for (Constituent c : constituents) {
                    tokenLabelView.removeConstituent(c);
                }
                for (int i = start; i < end; i++) {
                    tokenLabelView.addTokenLabel(i, "", 1.0);
                }
                textAnnotation.addView(view.getViewName(), view);
            }
        }
        return textAnnotations;
    }
}
