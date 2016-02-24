import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;

/**
 * Created by ryan on 2/24/16.
 */
public class JsonTools {

    private static <T> JsonArray createFromArray(T[] array, JsonConverter<T> converter){
        JsonArray jsonArray = new JsonArray();
        for(int i=0;i<array.length;i++){
            jsonArray.add(converter.convertToJson(array[i]));
        }
        return jsonArray;
    }

    public abstract class JsonConverter<T> {
        public abstract JsonElement convertToJson(T object);
    }

}
