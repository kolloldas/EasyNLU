package coldash.easynlu.parse;

import java.util.*;
import java.util.List;

public class Grammar {
	
	HashMap<StringTuple, List<Rule>> lexicalRules;
	HashMap<StringTuple, List<Rule>> unaryRules;
	HashMap<StringTuple, List<Rule>> binaryRules;
	HashSet<String> combinedCategories;
	HashSet<Rule> nonTerminals;
	
	private String rootCategory;
	
	public Grammar(List<Rule> rules, String rootCategory) {
		lexicalRules = new HashMap<>();
		unaryRules = new HashMap<>();
		binaryRules = new HashMap<>();
		combinedCategories = new HashSet<>();
		nonTerminals = new HashSet<>();
		this.rootCategory = rootCategory;

		for(Rule rule: rules) addRule(rule);
		System.out.println((lexicalRules.size() + unaryRules.size() + binaryRules.size()) + " rules");
	}
	
	public boolean isRoot(Rule rule) {
		return rule.getLHS().equals(rootCategory);
	}
	
	public List<Rule> getLexicalRules(List<String> rhs){
	    StringTuple tuple = StringTuple.fromList(rhs);
	    return lexicalRules.containsKey(tuple) ? lexicalRules.get(tuple) : Collections.emptyList();
	}
	
	public List<Rule> getUnaryRules(String rhs){
        StringTuple tuple = StringTuple.fromParts(rhs);
        return unaryRules.containsKey(tuple) ? unaryRules.get(tuple) : Collections.emptyList();
	}
	
	public List<Rule> getBinaryRules(String left, String right){
        StringTuple tuple = StringTuple.fromParts(left, right);
        return binaryRules.containsKey(tuple) ? binaryRules.get(tuple) : Collections.emptyList();
	}

	public Set<Rule> getNonTerminalRules(){
	    return nonTerminals;
    }
	
	private void addRule(Rule rule) throws UnsupportedOperationException{
		if(rule.hasOptionals())
			processOptionals(rule);
		else if(rule.isLexical()) {
			addLexicalRule(rule);
		}else if(rule.isCategorial()) {
			if(rule.isUnary())
				addUnaryRule(rule);
			else if(rule.isBinary())
				addBinaryRule(rule);
			else
				processNAryRule(rule);
		}else {
			throw new UnsupportedOperationException("Cannot mix terminals and non-terminals");
		}
	}
	
	private void addLexicalRule(Rule rule) {
        computeIfAbsent(lexicalRules, rule.getRHS(), new LinkedList<>()).add(rule);
	}
	
	private void addUnaryRule(Rule rule) {
		nonTerminals.add(rule);
        computeIfAbsent(unaryRules, rule.getRHS(), new LinkedList<>()).add(rule);
	}

	private void addBinaryRule(Rule rule) {
	    nonTerminals.add(rule);
	    computeIfAbsent(binaryRules, rule.getRHS(), new LinkedList<>()).add(rule);
	}


    private void processOptionals(Rule rule) {
		StringTuple rhs = rule.getRHS();
		int index = rhs.find(rule::isOptional);
		assert index != -1;
		
		StringTuple withOptional = rhs.modify(index, s->s.substring(1));
		StringTuple withoutOptional = rhs.remove(index);
		
		assert withoutOptional.size() > 0: String.format("Cannot have all optionals in rule: %s", rule);
		
		addRule(new Rule(rule.getLHS(), withOptional, rule.getSemantics()));
		
		// Insert blank argument for the removed item
		SemanticFunction fn = params -> {
			params.add(index, new HashMap<>());
		    return rule.getSemantics().apply(params);
		};
		addRule(new Rule(rule.getLHS(), withoutOptional, fn));
	}
	
	private void processNAryRule(Rule rule) {
		StringTuple rhs = rule.getRHS();
		
		String lhsUpper = rule.getLHS();
		String lhsLower = String.format("%s_%s", lhsUpper, rhs.get(0));
		// Check for existing names
		while(combinedCategories.contains(lhsLower)) {
			lhsLower = lhsLower.concat("_");
		}
		combinedCategories.add(lhsLower);
		
		StringTuple rhsLower = rhs.remove(0);
		StringTuple rhsUpper = StringTuple.fromParts(rhs.get(0), lhsLower);
		
		addRule(new Rule(lhsLower, rhsLower, SemanticUtils::identity));
		addRule(new Rule(lhsUpper, rhsUpper, rule.getSemantics())); 
	}

	private <K,V> V computeIfAbsent(Map<K, V> map, K key, V initialValue){
	    if(!map.containsKey(key)){
	        map.put(key, initialValue);
	        return initialValue;
        }

        return map.get(key);

    }
}
