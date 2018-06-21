package coldash.easynlu.parse.annotators;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import coldash.easynlu.parse.Annotator;
import coldash.easynlu.parse.Rule;
import coldash.easynlu.parse.SemanticUtils;

import static org.junit.jupiter.api.Assertions.*;

class NumberAnnotatorTest {

    @Test
    void annotate() {
        Annotator annotator = new NumberAnnotator();
        List<String> tokens = Collections.singletonList("123");
        List<Rule> actual = annotator.annotate(tokens);

        assertEquals(1, actual.size());
        assertEquals("$NUMBER", actual.get(0).getLHS());
        assertEquals("123", actual.get(0).getRHS().toString());
        assertEquals(123.0, actual.get(0)
                        .getSemantics().apply(null)
                        .get(0).get(SemanticUtils.KEY_UNNAMED));
    }
}