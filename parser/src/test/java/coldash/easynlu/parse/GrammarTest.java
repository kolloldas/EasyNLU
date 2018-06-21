package coldash.easynlu.parse;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class GrammarTest {

	@Test
	void shouldSplitOptionals() {
		Rule[] rules = {
			new Rule("$A", "?$B $C")
		};
		
		Grammar g = new Grammar(Arrays.asList(rules), null);
		assertTrue(g.binaryRules.containsKey(new StringTuple("$B $C")));
		assertTrue(g.unaryRules.containsKey(new StringTuple("$C")));
		
		assertEquals(g.binaryRules.get(new StringTuple("$B $C")).get(0).getLHS(), "$A");
		assertEquals(g.unaryRules.get(new StringTuple("$C")).get(0).getLHS(), "$A");
	}
	
	@Test
	void shouldSplitNaryRule() {
		Rule[] rules = {
				new Rule("$A", "$B $C $D")
			};
		
		Grammar g = new Grammar(Arrays.asList(rules), null);
		assertTrue(g.binaryRules.containsKey(new StringTuple("$C $D")));
		assertTrue(g.binaryRules.containsKey(new StringTuple("$B $A_$B")));
		
		assertEquals(g.binaryRules.get(new StringTuple("$C $D")).get(0).getLHS(), "$A_$B");
		assertEquals(g.binaryRules.get(new StringTuple("$B $A_$B")).get(0).getLHS(), "$A");
	}

	@Test
	void getLexicalRules() {
        List<Rule> rules = Collections.singletonList(new Rule("$A", "B C"));
        Grammar grammar = new Grammar(rules, "$ROOT");

        assertEquals(rules.get(0), grammar.getLexicalRules(Arrays.asList("B", "C")).get(0));
        assertEquals(0, grammar.getBinaryRules("$B", "$C").size());
        assertEquals(0, grammar.getUnaryRules("$B").size());
	}

	@Test
	void getUnaryRules() {
        List<Rule> rules = Collections.singletonList(new Rule("$A", "$B"));
        Grammar grammar = new Grammar(rules, "$ROOT");

        assertEquals(rules.get(0), grammar.getUnaryRules("$B").get(0));
        assertEquals(0, grammar.getBinaryRules("$B", "$C").size());
        assertEquals(0, grammar.getLexicalRules(Arrays.asList("$B", "$C")).size());
	}

	@Test
	void getBinaryRules() {
        List<Rule> rules = Collections.singletonList(new Rule("$A", "$B $C"));
        Grammar grammar = new Grammar(rules, "$ROOT");

        assertEquals(rules.get(0), grammar.getBinaryRules("$B", "$C").get(0));
        assertEquals(0, grammar.getLexicalRules(Arrays.asList("$B", "$C")).size());
        assertEquals(0, grammar.getUnaryRules("$B").size());
	}
}
