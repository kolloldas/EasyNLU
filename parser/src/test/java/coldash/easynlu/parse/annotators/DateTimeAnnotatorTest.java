package coldash.easynlu.parse.annotators;

import coldash.easynlu.parse.Rule;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeAnnotatorTest {

    @Test
    void annotateMonth() {
        List<String> tokens = Collections.singletonList("May");
        List<Rule> rules = DateTimeAnnotator.INSTANCE.annotate(tokens);

        assertEquals(rules.get(0).getLHS(), "$DATE_MONTH");
        List<Map<String, Object>> result = rules.get(0).getSemantics().apply(Collections.emptyList());

        assertTrue(result.get(0).containsKey("month"));
        assertEquals(result.get(0).get("month"), 5.0);

    }

    @Test
    void annotateDow() {
        List<String> tokens = Collections.singletonList("Sunday");
        List<Rule> rules = DateTimeAnnotator.INSTANCE.annotate(tokens);

        assertEquals(rules.get(0).getLHS(), "$DATE_DOW");
        List<Map<String, Object>> result = rules.get(0).getSemantics().apply(Collections.emptyList());

        assertTrue(result.get(0).containsKey("dow"));
        assertEquals(result.get(0).get("dow"), 7.0);
    }
}