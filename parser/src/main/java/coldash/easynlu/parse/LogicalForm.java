package coldash.easynlu.parse;

import java.util.*;

public class LogicalForm implements Comparable<LogicalForm>{

	Map<String, Object> map;
	Derivation derivation;
    List<String> fields;
    float score;

	public LogicalForm(Derivation derivation, Map<String, Object> map) {
        this.derivation = derivation;
	    this.map = map;

	    fields = fields(map);
	    score = derivation.score;
	}

    public int fieldCount() {
		return fields.size();
	}

    public Map<String, Object> getMap() {return map; }

    public Derivation getDerivation() {
        return derivation;
    }

    public boolean match(Map<String, Object> map){
	    return Objects.deepEquals(this.map, map);
    }

    public List<String> fields(){
	    return fields;
    }

    public void updateScore(Map<String, Float> weights){
        for(String field: fields) {
            score += weights.containsKey(field) ? weights.get(field) : 0f;
        }
    }

    static List<String> fields(Map<String, Object> hashMap){
	    List<String> keys = new ArrayList<>(hashMap.keySet());
        for(Map.Entry<String, Object> entry: hashMap.entrySet()) {
            if(entry.getValue() instanceof HashMap<?, ?>)
                for(String f: fields((HashMap<String, Object>)entry.getValue()))
                    keys.add(String.format("%s:%s", entry.getKey(), f));

        }
	    return keys;
    }

    @Override
	public String toString() {
		return map.toString();
	}


    @Override
    public int compareTo(LogicalForm o) {
        return Float.compare(score, o.score);
    }
}
