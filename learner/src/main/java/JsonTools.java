import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * Created by ryan on 2/24/16.
 */
public class JsonTools {

    public static JsonArray createJsonArrayFromArray(String[] array){
        JsonConverter<String> converter = new JsonConverter<String>(){
            public JsonElement convertToJson(String object){
                return new JsonPrimitive(object);
            }
        };

        return createJsonArrayFromArray(array, converter);
    }

    private static <T> JsonArray createJsonArrayFromArray(T[] array, JsonConverter<T> converter){
        JsonArray jsonArray = new JsonArray();
        for(int i=0;i<array.length;i++){
            jsonArray.add(converter.convertToJson(array[i]));
        }
        return jsonArray;
    }

    public abstract static class JsonConverter<T> {
        public abstract JsonElement convertToJson(T object);
    }

}
