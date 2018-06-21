package coldash.easynlu.parse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Derivation {
    Rule rule;
    float score;
    List<Derivation> children;

    Derivation(Rule rule, List<Derivation> children){
        this.rule = rule;
        this.children = children;
    }

    public float getScore() {
        return score;
    }

    public Map<String, Integer> getRuleFeatures(){
        return getRuleFeatures(this);
    }

    private Map<String, Integer> getRuleFeatures(Derivation d){
        Map<String, Integer> features = new HashMap<>();
        if(d.children == null)
            return features;

        features.put(d.rule.toString(), 1);

        for(Derivation child: d.children){
            for(Map.Entry<String, Integer> entry: getRuleFeatures(child).entrySet()){
                int count = features.containsKey(entry.getKey()) ? features.get(entry.getKey()) : 0;
                features.put(entry.getKey(), count + entry.getValue());
            }
        }

        return features;
    }
}
