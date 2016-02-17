import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.saulexamples.nlp.POSTagger.POSClassifiers;
import edu.illinois.cs.cogcomp.saulexamples.nlp.POSTagger.POSDataModel;
//import edu.illinois.cs.cogcomp.saulexamples.nlp.POSTagger.POSClassifiers;
//import edu.illinois.cs.cogcomp.saulexamples.nlp.POSTagger.POSDataModel;

import java.util.List;

/**
 * Created by rnkelch on 1/24/2016.
 */
public class SaulPosAnnotator extends Annotator
{
    public SaulPosAnnotator()
    {
        super(ViewNames.POS, new String[] {ViewNames.TOKENS});
    }

    @Override
    public void addView(TextAnnotation textAnnotation) throws AnnotatorException
    {
        List<Constituent> constituents = textAnnotation.getView(ViewNames.TOKENS).getConstituents();
        scala.collection.Iterable<Constituent> scalaConstitutes = scala.collection.JavaConversions.asScalaBuffer(constituents);
        POSDataModel.tokens().populate(scalaConstitutes, false);
        POSClassifiers.loadModelsFromPackage();

        View posView = new View(ViewNames.POS,"POS-annotator",textAnnotation,1.0);
        textAnnotation.addView(ViewNames.POS,posView);

        for(int i=0;i<constituents.size();i++){
            String predicted = POSClassifiers.POSClassifier(constituents.get(i));
            posView.addConstituent(new Constituent(predicted,ViewNames.POS,textAnnotation,i,i+1));
        }
    }
}
