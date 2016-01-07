import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;

public class ToyPosAnnotator extends Annotator
{
    public ToyPosAnnotator()
    {
        super(ViewNames.POS, new String[] {"TOKENS"});
    }

    @Override
    public void addView(TextAnnotation textAnnotation) throws AnnotatorException
    {
        String[] tokens = textAnnotation.getTokens();
        String[] tags = {"DT","NN","IN","DT","NN","VBD","IN","NN","."};
        View posView = new View(ViewNames.POS,"POS-annotator",textAnnotation,1.0);
        textAnnotation.addView(ViewNames.POS,posView);
        for(int i=0;i<tokens.length;i++){
            posView.addConstituent(new Constituent(tags[i],ViewNames.POS,textAnnotation,i,i+1));
        }
    }
}
