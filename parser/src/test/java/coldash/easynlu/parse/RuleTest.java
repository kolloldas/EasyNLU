package coldash.easynlu.parse;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RuleTest {

    @Test
    void getRHS() {
        Rule r = new Rule("$A", "B C D");
        assertEquals(r.getRHS(), StringTuple.fromParts("B", "C", "D"));
    }

    @Test
    void isLexical() {
        Rule r1 = new Rule("$A", "B C D");
        Rule r2 = new Rule("$A", "B C $D");
        Rule r3 = new Rule("$A", "B");
        Rule r4 = new Rule("$A", "$B");

        assertTrue(r1.isLexical());
        assertFalse(r2.isLexical());
        assertTrue(r3.isLexical());
        assertFalse(r4.isLexical());
    }

    @Test
    void isUnary() {
        Rule r1 = new Rule("$A", "B C D");
        Rule r2 = new Rule("$A", "B C $D");
        Rule r3 = new Rule("$A", "B");
        Rule r4 = new Rule("$A", "$B");

        assertFalse(r1.isUnary());
        assertFalse(r2.isUnary());
        assertTrue(r3.isUnary());
        assertTrue(r4.isUnary());
    }

    @Test
    void isBinary() {
        Rule r1 = new Rule("$A", "B C");
        Rule r2 = new Rule("$A", "B C $D");
        Rule r3 = new Rule("$A", "B");

        assertTrue(r1.isBinary());
        assertFalse(r2.isBinary());
        assertFalse(r3.isBinary());
    }

    @Test
    void isCategorial() {
        Rule r1 = new Rule("$A", "B C D");
        Rule r2 = new Rule("$A", "B C $D");
        Rule r3 = new Rule("$A", "$B $C");
        Rule r4 = new Rule("$A", "$B");

        assertFalse(r1.isCategorial());
        assertFalse(r2.isCategorial());
        assertTrue(r3.isCategorial());
        assertTrue(r4.isCategorial());
    }

    @Test
    void hasOptionals() {
        Rule r1 = new Rule("$A", "?B C");
        Rule r2 = new Rule("$A", "B C ?$D");
        Rule r3 = new Rule("$A", "?B");
        Rule r4 = new Rule("$A", "$B $D");

        assertTrue(r1.hasOptionals());
        assertTrue(r2.hasOptionals());
        assertTrue(r3.hasOptionals());
        assertFalse(r4.hasOptionals());

    }
}