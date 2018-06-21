package coldash.easynlu.parse.tokenizers;

import coldash.easynlu.parse.Tokenizer;

import java.util.Arrays;
import java.util.List;

public class BasicTokenizer implements Tokenizer {

	@Override
	public List<String> tokenize(String input) {
		String cleaned = input.trim()
				.replaceAll("[:,]", "")
				.replaceAll("[/\\-]", " ")
                .replaceAll("(\\d)(st|nd|rd|th|ST|ND|RD|TH)?", "$1")
                .replaceAll("(\\d)([a-zA-Z])", "$1 $2");
		
		return Arrays.asList(cleaned.split(" "));
	}

}
