package coldash.easynlu.parse;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import coldash.easynlu.parse.annotators.PhraseAnnotator;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    @Test
    void chartRetrieval(){
        Parser p = new Parser(null, null, null);
        Parser.Chart chart = p.new Chart(10);

        assertEquals(32, chart.mapSpan(2, 3));

        Derivation d = new Derivation(null, null);
        chart.addDerivation(3, 5, d);
        assertEquals(1, chart.getDerivations(3, 5).size());
        assertEquals(d, chart.getDerivations(3, 5).get(0));
    }

    @Test
    void parseSyntactic() {
        Rule r1 = new Rule("$A", "a");
        Rule r2 = new Rule("$B", "b");
        Rule r3 = new Rule("$C", "$A $B");

        Grammar grammar = new Grammar(Arrays.asList(r1, r2, r3), "$C");
        Parser p = new Parser(grammar, s -> Arrays.asList(s.split(" ")), Collections.emptyList());

        Derivation dc1 = new Derivation(r1, null);
        Derivation dc2 = new Derivation(r2, null);

        Derivation expected = new Derivation(r3, Arrays.asList(dc1, dc2));
        Derivation actual = p.parseSyntactic("a b").get(0);

        assertEquals(expected.rule, actual.rule);
        assertEquals(expected.children.get(0).rule, actual.children.get(0).rule);
        assertEquals(expected.children.get(1).rule, actual.children.get(1).rule);

    }

    @Test
    void parse() {
        List<Rule> rules = Arrays.asList(
                new Rule("$A", "a"),
                new Rule("$B", "b"),
                new Rule("$C", "$A $B", "{e:@first, f:@last}")
        );

        Grammar grammar = new Grammar(rules, "$C");

        Parser p = new Parser(grammar, s -> Arrays.asList(s.split(" ")), Collections.emptyList());

        Map<String, Object> expected = new HashMap<String, Object>(){{
            put("e", "a");
            put("f", "b");
        }};

        List<LogicalForm> actual = p.parse("a b");
        assertEquals(1, actual.size());
        assertEquals(expected, actual.get(0).getMap());
    }


    @Test
    void applySemantics() {
        Rule r1 = new Rule("$A", "a");
        Rule r2 = new Rule("$B", "$A $A", "{b:@1}");

        Derivation dc1 = new Derivation(r1, null);
        Derivation dc2 = new Derivation(r1, null);

        Derivation d = new Derivation(r2, Arrays.asList(dc1, dc2));

        Map<String, Object> expected = new HashMap<String, Object>(){{
           put("b", "a");
        }};

        Parser p = new Parser(null, null, null);

        assertEquals(expected, p.applySemantics(d).get(0));
    }

    @Test
    void applyAnnotators() {
        Parser p = new Parser(null, null,
                Collections.singletonList(PhraseAnnotator.INSTANCE));
        Parser.Chart chart = p.new Chart(10);
        List<String> tokens = Arrays.asList("A", "B", "C");
        Rule r = PhraseAnnotator.INSTANCE.annotate(tokens).get(0);

        p.applyAnnotators(chart, tokens, 0, 3);
        assertEquals(r, chart.getDerivations(0, 3).get(0).rule);
    }

    @Test
    void applyLexicalRules() {
        List<Rule> rules = Collections.singletonList(new Rule("$A", "B C"));
        Grammar grammar = new Grammar(rules, "$ROOT");

        Parser p = new Parser(grammar, null, null);
        Parser.Chart chart = p.new Chart(10);
        List<String> tokens = Arrays.asList("A", "B", "C");

        p.applyLexicalRules(chart, tokens, 1, 3);
        assertEquals(rules.get(0), chart.getDerivations(1, 3).get(0).rule);
    }

    @Test
    void applyUnaryRules() {
        List<Rule> rules = Arrays.asList(
                new Rule("$F", "$E"),
                new Rule("$E", "$D"));
        Grammar grammar = new Grammar(rules, "$ROOT");

        Parser p = new Parser(grammar, null, null);
        Parser.Chart chart = p.new Chart(10);

        chart.addDerivation(1, 3, new Derivation(new Rule("$D", "$B $C"),
                                                        null));

        p.applyUnaryRules(chart, 1, 3);

        assertEquals(3, chart.getDerivations(1, 3).size());
        assertEquals(rules.get(0), chart.getDerivations(1, 3).get(2).rule);
        assertEquals(rules.get(1), chart.getDerivations(1, 3).get(1).rule);

    }

    @Test
    void applyBinaryRules() {
        List<Rule> rules = Collections.singletonList(
                new Rule("$C", "$A $B"));
        Grammar grammar = new Grammar(rules, "$ROOT");

        Parser p = new Parser(grammar, null, null);
        Parser.Chart chart = p.new Chart(10);

        chart.addDerivation(0, 1, new Derivation(new Rule("$A", "A"),
                null));
        chart.addDerivation(1, 2, new Derivation(new Rule("$B", "B"),
                null));

        p.applyBinaryRules(chart, 0, 2);
        assertEquals(rules.get(0), chart.getDerivations(0, 2).get(0).rule);
    }
}