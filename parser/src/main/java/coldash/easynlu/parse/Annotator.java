package coldash.easynlu.parse;

import java.util.List;

public interface Annotator {
	List<Rule> annotate(List<String> tokens);
}
