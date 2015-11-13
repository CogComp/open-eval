import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by rnkelch on 11/8/2015.
 */
public class StreamReader {

    public String readAll(InputStream inputStream) throws IOException
    {
        StringBuilder result = new StringBuilder();
        BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));

        String line = null;

        while ((line = input.readLine()) != null) {
            result.append(line);
        }

        return result.toString();
    }
}
