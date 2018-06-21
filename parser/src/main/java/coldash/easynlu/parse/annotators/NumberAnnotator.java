package coldash.easynlu.parse.annotators;

import coldash.easynlu.parse.Annotator;
import coldash.easynlu.parse.Rule;
import coldash.easynlu.parse.SemanticUtils;

import java.util.Collections;
import java.util.List;

public class NumberAnnotator implements Annotator {
	final static String SYMBOL = "$NUMBER";
	public static final Annotator INSTANCE = new NumberAnnotator();


	@Override
	public List<Rule> annotate(List<String> tokens) {
		
		if(tokens.size() == 1) {
			try {
				String n = tokens.get(0);
				Object f = Double.valueOf(n);
				return Collections.singletonList(new Rule(SYMBOL, n, SemanticUtils.value(f)));
				
			}catch(NumberFormatException e) {
				
			}
		}
		
		return Collections.emptyList();
		
	}

}
