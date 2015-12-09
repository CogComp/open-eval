import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

/**
 * Created by rnkelch on 11/20/2015.
 */
public interface TextAnnotator {
    public TextAnnotatorResult run(TextAnnotation partial);
}
