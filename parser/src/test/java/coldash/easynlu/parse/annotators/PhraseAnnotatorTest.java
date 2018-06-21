package coldash.easynlu.parse.annotators;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import coldash.easynlu.parse.Annotator;
import coldash.easynlu.parse.Rule;
import coldash.easynlu.parse.SemanticUtils;

import static org.junit.jupiter.api.Assertions.*;

class PhraseAnnotatorTest {

    @Test
    void annotate() {
        Annotator annotator = new PhraseAnnotator();
        List<String> tokens = Arrays.asList(
          "The", "boy", "who", "cried"
        );
        String expected = "The boy who cried";
        List<Rule> actual = annotator.annotate(tokens);

        assertEquals(1, actual.size());
        assertEquals("$PHRASE", actual.get(0).getLHS());
        assertEquals(expected, actual.get(0).getRHS().toString());
        assertEquals(expected, actual.get(0)
                .getSemantics().apply(null)
                .get(0).get(SemanticUtils.KEY_UNNAMED));;
    }
}