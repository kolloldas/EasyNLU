package coldash.easynlu.learn;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

public class Example {
    final static Gson GSON = new Gson();
    String input;
    Map<String, Object> label;

    public Example(String input, String json){
        this.input = input;
        this.label = parseJson(json);
    }

    private Map<String, Object> parseJson(String json){
        return GSON.fromJson(
                json, new TypeToken<HashMap<String, Object>>() {}.getType()
        );
    }

}
