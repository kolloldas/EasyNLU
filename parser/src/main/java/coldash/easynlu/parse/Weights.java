package coldash.easynlu.parse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class Weights {
    public static Map<String, Float> fromText(Reader reader) throws IOException {

        Map<String, Float> weights = new HashMap<>();
        BufferedReader bufferedReader = new BufferedReader(reader);

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            String[] items = line.split("\t");
            assert items.length == 2 : "Malformed input";

            weights.put(items[0].trim(), Float.valueOf(items[1].trim()));
        }

        return weights;
    }
}
