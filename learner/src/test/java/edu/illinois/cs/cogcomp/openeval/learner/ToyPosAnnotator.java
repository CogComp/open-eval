package edu.illinois.cs.cogcomp.openeval.learner;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.nlp.utilities.POSUtils;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class ToyPosAnnotator extends Annotator
{
    public ToyPosAnnotator()
    {
        // The problem we are trying to solve is parts of speech (POS)
        // The only view needed by use is the tokens of the document
        super(ViewNames.POS, new String[] {ViewNames.TOKENS, ViewNames.SENTENCE});
    }

    @Override
    public void addView(TextAnnotation textAnnotation) throws AnnotatorException
    {
        System.out.println("Received:"+textAnnotation.getText());
        String[] tokens = textAnnotation.getTokens();
        List<String> tags = POSUtils.allPOS;

        // Create a new view with our view name (the other fields are unimportant for this example)
        View posView = new View(ViewNames.POS,"POS-annotator",textAnnotation,1.0);
        textAnnotation.addView(ViewNames.POS,posView);

        Random random = new Random();

        for(int i=0;i<tokens.length;i++){
            // For this example we will just randomly assigning tags.
            int randomTagIndex = random.nextInt(tags.size());
            // Add the tag to the view for the specified token
            posView.addConstituent(new Constituent(tags.get(randomTagIndex),ViewNames.POS,textAnnotation,i,i+1));
        }
    }

    public static void main(String args[]) throws IOException {

        // Do any training
        Annotator annotator = new ToyPosAnnotator();

        // We will have our server listen on port 5757 and pass it our trained annotator
        ServerPreferences serverPreferences = new ServerPreferences(1000000, 25);
        Server server = new Server(5757, serverPreferences, annotator);

        // We have no more work to do, so we will use the executeInstance method to start and keep our edu.illinois.cs.cogcomp.openeval.learner.Server alive
        fi.iki.elonen.util.ServerRunner.executeInstance(server);
    }
}