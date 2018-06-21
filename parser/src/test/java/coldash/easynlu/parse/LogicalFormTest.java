package coldash.easynlu.parse;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LogicalFormTest {

    @Test
    void fields() {
        Map<String, Object> map = new HashMap<String, Object>(){{
            put("a", 1);
            put("b", 2);
            put("c", new HashMap<String, Object>(){{
                put("d", 4);
                put("e", new HashMap<String, Object>(){{
                    put("f", 5);
                }});
            }});
        }};
        List<String> expected = Arrays.asList(
          "a", "b", "c", "c:d", "c:e", "c:e:f"
        );

        assertEquals(expected, LogicalForm.fields(map));
    }
}