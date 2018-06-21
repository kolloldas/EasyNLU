package coldash.easynlu.parse.tokenizers;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import coldash.easynlu.parse.Tokenizer;

import static org.junit.jupiter.api.Assertions.*;

class BasicTokenizerTest {

    @Test
    void tokenize() {
        Tokenizer tokenizer = new BasicTokenizer();

        String example = "$100, 10:45 1/2/3 4-5-6 1st 2nd 3RD 4th 10pm 3May";
        List<String> expected = Arrays.asList(
          "$100", "1045", "1", "2", "3", "4", "5", "6", "1", "2", "3", "4", "10", "pm", "3", "May"
        );

        assertEquals(expected, tokenizer.tokenize(example));
    }
}