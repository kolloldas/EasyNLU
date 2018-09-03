package coldash.easynlu.parse;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static coldash.easynlu.parse.SemanticUtils.*;

class SemanticUtilsTest {

    @Test
    void namedChain(){
        Map<String, Object> target = new HashMap<String, Object>(){{
            put("a", 1); put("b", new HashMap<String, Object>(){{
                put("c", 3);
            }});
        }};

        Map<String, Object> map = named("a", 1)
                .named("b", named("c", 3));

        assertEquals(target, map);
    }

    @Test
    void merge(){
        List<Map<String, Object>> params = Arrays.asList(
                named("a", 1),
                named("b",2),
                named("c", 3)
        );

        List<Map<String, Object>> merged = Collections.singletonList(
                new HashMap<String, Object>() {{
                    put("a", 1);
                    put("b", 2);
                    put("c", 3);
                }}
        );

        assertEquals(merged, SemanticUtils.merge(params));
    }

    @Test
    void first(){
        List<Map<String, Object>> params = Arrays.asList(
                named("a", 1),
                named("b",2),
                named("c", 3)
        );

        List<Map<String, Object>> first = Collections.singletonList(
                named("a", 1)
        );


        assertEquals(first, SemanticUtils.first(params));
    }

    @Test
    void last(){
        List<Map<String, Object>> params = Arrays.asList(
                named("a", 1),
                named("b",2),
                named("c", 3)
        );

        List<Map<String, Object>> last = Collections.singletonList(
                named("c", 3)
        );

        assertEquals(last, SemanticUtils.last(params));
    }


    @Test
    void appendSingle(){
        List<Map<String, Object>> params = Collections.singletonList(named("a", 1));

        List<Object> expected = Collections.singletonList(
                                    named("a", 1));

        assertEquals(expected, SemanticUtils.append(params, "x"));
    }

    @Test
    void appendTwo(){
        List<Map<String, Object>> params = Arrays.asList(
                named("x", Collections.singletonList(named("a", 1))),
                named("b",2)
        );

        List<Object> expected = Arrays.asList(
                        named("a", 1),
                        named("b",2)
                    );

        assertEquals(expected, SemanticUtils.append(params, "x"));
    }

    @Test
    void appendMulti(){
        List<Map<String, Object>> params = Arrays.asList(
                named("x", Collections.singletonList(named("a", 1))),
                named("b",2), named(KEY_UNNAMED, 4),
                named("c",3)
        );

        List<Object> expected = Arrays.asList(
                        named("a", 1),
                        named("b",2),
                        named("c",3)
                );

        assertEquals(expected, SemanticUtils.append(params, "x"));
    }

    @Test
    void parseSemantics() {
        List<Map<String, Object>> params = Arrays.asList(
                named("a", 1),
                named("b",2),
                named("c", 3)
        );


        List<Map<String, Object>> first = Collections.singletonList(
                named("a", 1)
        );

        List<Map<String, Object>> mid = Collections.singletonList(
                named("b", 2)
        );

        assertEquals(first, SemanticUtils.parseSemantics("@first").apply(params));
        assertEquals(mid, SemanticUtils.parseSemantics("@1").apply(params));
    }

    @Test
    void parseSemanticsValue() {

        List<Map<String, Object>> expected = Collections.singletonList(
                value("test")
        );

        assertEquals(expected, SemanticUtils.parseSemantics("test").apply(null));

    }

    @Test
    void parseSemanticsJson() {
        List<Map<String, Object>> params = Arrays.asList(
                value(1),
                named("x", 10),
                value(3)
        );

        List<Map<String, Object>> params2 = Arrays.asList(
                value("hello"),
                named("z", -1),
                value(30)
        );

        Map<String, Object> expected = new HashMap<String, Object>(){{
            put("a", 1);
            put("b", new HashMap<String, Object>() {{put("x", 10);}});
            put("c", new HashMap<String, Object>() {{put("d", 3);}});
            put("e", "hello");
        }};

        Map<String, Object> expected2 = new HashMap<String, Object>(){{
            put("a", "hello");
            put("b", new HashMap<String, Object>() {{put("z", -1);}});
            put("c", new HashMap<String, Object>() {{put("d", 30);}});
            put("e", "hello");
        }};

        String json = "{a:@first, b:@1, c:{d:@last}, e:hello}";


        SemanticFunction fn = SemanticUtils.parseSemantics(json);
        assertEquals(Collections.singletonList(expected), fn.apply(params));
        assertEquals(Collections.singletonList(expected2), fn.apply(params2));

    }

    @Test
    void parseSemanticsMerge() {
        Map<String, Object> expected = new HashMap<String, Object>(){{
            put("a", 1);
            put("x", 10);
            put("y", 20);
        }};

        List<Map<String, Object>> params = Arrays.asList(
                value(1),
                named("x", 10),
                named("y", 20)
        );

        String json = "{a:@first, @merge:[1,2]}";
        SemanticFunction fn = SemanticUtils.parseSemantics(json);

        assertEquals(Collections.singletonList(expected), fn.apply(params));
    }

    @Test
    void parseTemplate() {
        Map<String, Object> template = named("a", _FIRST).named("b", _N(1))
                .named("c", named("d", _LAST));

        Map<String, Object> expected = new HashMap<String, Object>(){{
            put("a", 1);
            put("b", new HashMap<String, Object>() {{put("x", 10);}});
            put("c", new HashMap<String, Object>() {{put("d", 3);}});
        }};

        List<Map<String, Object>> params = Arrays.asList(
                value(1),
                named("x", 10),
                value(3)
        );

        SemanticFunction fn = SemanticUtils.parseTemplate(template);
        assertEquals(Collections.singletonList(expected), fn.apply(params));
    }

    @Test
    void parseTemplateMerge() {
        Map<String, Object> template = named("a", _FIRST)
                .named("@merge", Arrays.asList(1, 2));

        Map<String, Object> expected = new HashMap<String, Object>(){{
            put("a", 1);
            put("x", 10);
            put("y", 20);
        }};

        List<Map<String, Object>> params = Arrays.asList(
                value(1),
                named("x", 10),
                named("y", 20)
        );

        SemanticFunction fn = SemanticUtils.parseTemplate(template);
        assertEquals(Collections.singletonList(expected), fn.apply(params));
    }

    @Test
    void parseTemplateAppend(){
        Map<String, Object> template = named("x", _APPEND);

        Map<String, Object> expected = named("x", Arrays.asList(
                named("a", 10),
                named("b",20),
                named("c",3)
        ));

        List<Map<String, Object>> params = Arrays.asList(
                value(1),
                named("x", Arrays.asList(named("a", 10), named("b", 20))),
                named("c",3)
        );

        SemanticFunction fn = SemanticUtils.parseTemplate(template);
        assertEquals(Collections.singletonList(expected), fn.apply(params));
    }

    @Test
    void processNumberParam(){
        Map<String, Object> actual = new HashMap<>();


        Map<String, Object> expected = new HashMap<String, Object>(){{
            put("a", 10.0);
            put("b", 20L);
        }};

        List<Map<String, Object>> params = Arrays.asList(
                value(10),
                value(20)
        );

        SemanticUtils.processNumberParam(actual, "a", _N_DOUBLE(0), params);
        SemanticUtils.processNumberParam(actual, "b", _N_LONG(1), params);

        assertEquals(expected, actual);
    }

}