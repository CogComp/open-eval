package edu.illinois.cs.cogcomp.openeval.learner;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.JsonTools;
import fi.iki.elonen.NanoHTTPD;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by ryan on 3/13/16.
 */
public class RequestResponseBuilder {
    private static JsonParser parser = new JsonParser();

    public static String getMultipleAnnotationRequestBody(){
        TextAnnotation[] textAnnotations = new TextAnnotation[] {getBasicTextAnnotation(), getBasicTextAnnotation()};
        JsonArray jAnnotations = JsonTools.createJsonArrayFromArray(textAnnotations);

        return jAnnotations.toString();
    }

    public static JsonArray getInstancesFromJson(NanoHTTPD.Response response) throws IOException {
        String responseBody = IOUtils.toString(response.getData());
        return parser.parse(responseBody).getAsJsonArray();
    }

    public static TextAnnotation getBasicTextAnnotation()
    {
        String[] sentences = {"The dog runs"};
        ArrayList<String[]> list = new ArrayList<>();
        list.add(sentences);
        return BasicTextAnnotationBuilder.createTextAnnotationFromTokens(list);
    }
}
