package coldash.easynlu.parse.annotators;

import coldash.easynlu.parse.Annotator;
import coldash.easynlu.parse.Rule;
import coldash.easynlu.parse.SemanticUtils;
import coldash.easynlu.parse.StringTuple;

import java.util.Collections;
import java.util.List;

public class TokenAnnotator implements Annotator {
	final static String SYMBOL = "$TOKEN";
	public static final Annotator INSTANCE = new TokenAnnotator();
	
	@Override
	public List<Rule> annotate(List<String> tokens) {
		if(tokens.size() == 1)
			return Collections.singletonList(
					new Rule(SYMBOL, StringTuple.fromList(tokens), SemanticUtils.valueFn(tokens.get(0))));
		
		return Collections.emptyList();
	}

}
