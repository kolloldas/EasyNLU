package coldash.easynlu.parse;


import java.util.List;
import java.util.Map;

public interface SemanticFunction {
	List<Map<String, Object>> apply(List<Map<String, Object>> params);
}
