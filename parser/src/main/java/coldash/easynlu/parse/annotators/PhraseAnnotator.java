package coldash.easynlu.parse.annotators;

import coldash.easynlu.parse.Annotator;
import coldash.easynlu.parse.Rule;
import coldash.easynlu.parse.SemanticUtils;
import coldash.easynlu.parse.StringTuple;

import java.util.Collections;
import java.util.List;

public class PhraseAnnotator implements Annotator {
	final static String SYMBOL = "$PHRASE";
	public static final Annotator INSTANCE = new PhraseAnnotator();
	
	@Override
	public List<Rule> annotate(List<String> tokens) {
		String phrase = StringTuple.joinList(" ", tokens);
		return Collections.singletonList(
				new Rule(SYMBOL, StringTuple.fromList(tokens), SemanticUtils.valueFn(phrase)));
	}

}
