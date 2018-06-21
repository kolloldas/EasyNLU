package coldash.easynlu.parse.annotators;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import coldash.easynlu.parse.Annotator;
import coldash.easynlu.parse.Rule;
import coldash.easynlu.parse.SemanticUtils;

import static org.junit.jupiter.api.Assertions.*;

class TokenAnnotatorTest {

    @Test
    void annotate() {
        Annotator annotator = new TokenAnnotator();
        List<String> tokens = Collections.singletonList("Single");
        String expected = "Single";
        List<Rule> actual = annotator.annotate(tokens);

        assertEquals(1, actual.size());
        assertEquals("$TOKEN", actual.get(0).getLHS());
        assertEquals(expected, actual.get(0).getRHS().toString());
        assertEquals(expected, actual.get(0)
                .getSemantics().apply(null)
                .get(0).get(SemanticUtils.KEY_UNNAMED));;
    }
}