package coldash.easynlu.parse;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DerivationTest {

    @Test
    void getRuleFeatures() {
        Rule r1 = new Rule("$A", "a");
        Rule r2 = new Rule("$B", "$A $A");

        Derivation dc1 = new Derivation(r1, Collections.emptyList());
        Derivation dc2 = new Derivation(r1, Collections.emptyList());
        Derivation d = new Derivation(r2, Arrays.asList(dc1, dc2));

        Map<String, Integer> expected = new HashMap<String, Integer>(){{
            put(r1.toString(), 2);
            put(r2.toString(), 1);
        }};

        assertEquals(expected, d.getRuleFeatures());
    }
}