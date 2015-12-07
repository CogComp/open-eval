import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

/**
 * Created by rnkelch on 11/20/2015.
 */
public class TextAnnotatorResult {
    public TextAnnotation textAnnotation;
    public boolean successful;
    public String errorMessage;

    public TextAnnotatorResult(TextAnnotation textAnnotation, boolean successful, String errorMessage){
        this.textAnnotation = textAnnotation;
        this.successful = successful;
        this.errorMessage = errorMessage;
    }
}
