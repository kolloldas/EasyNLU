package coldash.easynlu.parse;


import java.util.Map;
import java.util.Objects;

public class Rule {
	
	private String lhs;
	private StringTuple rhs;
	private SemanticFunction semantics;
	
	public Rule(String lhs, StringTuple rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
        validate();
	}

    public Rule(String lhs, String rhs) {
        this(lhs, new StringTuple(rhs));
    }

    public Rule(String lhs, String rhs, SemanticFunction semantics) {
        this(lhs, rhs);
        this.semantics = semantics;
    }

    public Rule(String lhs, StringTuple rhs, SemanticFunction semantics) {
        this(lhs, rhs);
        this.semantics = semantics;
    }

    public Rule(String lhs, String rhs, Map<String, Object> semantics) {
        this(lhs, rhs);
        this.semantics = SemanticUtils.parseTemplate(semantics);
    }

    public Rule(String lhs, String rhs, String semantics) {
        this(lhs, rhs);
        this.semantics = SemanticUtils.parseSemantics(semantics);
    }
	
	public String getLHS() {
		return lhs;
	}
	
	public StringTuple getRHS() {
		return rhs;
	}
	
	public SemanticFunction getSemantics() {
		return semantics;
	}
	
	public boolean isLexical() {
		for(String item: rhs)
		    if(item.startsWith("$"))
		        return false;
		return true;
	}
	
	public boolean isUnary() {
		return rhs.size() == 1;
	}
	
	public boolean isBinary() {
		return rhs.size() == 2;
	}
	
	public boolean isCategorial() {
        for(String item: rhs)
            if(!isCat(item))
                return false;
	    return true;
	}
	
	public boolean hasOptionals() {
        for(String item: rhs)
            if(isOptional(item))
                return true;
	    return false;
	}
	
	public void validate() {
		assert isCat(lhs): String.format("Invalid Rule: %s->%s", lhs, rhs);
	}
	
	public boolean isCat(String label) {
		return label.startsWith("$");
	}
	
	public boolean isOptional(String label) {
		return label.startsWith("?") && label.length() > 1;
	}
	
	@Override
	public String toString() {
		return String.format("Rule(%s->%s)", lhs, rhs);
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rule rule = (Rule) o;
        return Objects.equals(lhs, rule.lhs) &&
                Objects.equals(rhs, rule.rhs);
    }

    @Override
    public int hashCode() {

        return Objects.hash(lhs, rhs);
    }
}
