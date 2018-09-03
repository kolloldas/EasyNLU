package coldash.easynlu.parse;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SemanticUtils {

    public static class ChainMap<K, V> extends HashMap<K, V> {
        public ChainMap<K, V> named(K name, V value){
            put(name, value);
            return this;
        }
    }

    public final static String _IDENTITY = "@identity";
    public final static String _FIRST = "@first";
    public final static String _LAST = "@last";
    public final static String _MERGE = "@merge";
    public final static String _APPEND = "@append";
    public final static String KEY_UNNAMED = "__unnamed";

    private final static Gson GSON = new Gson();
    private final static Pattern PATTERN_PARAM = Pattern.compile("^@(\\d+)([LF])?");

    public static String _N(int item){
        return String.format("@%d", item);
    }

    public static String _N_LONG(int item){
        return String.format("@%dL", item);
    }
    public static String _N_DOUBLE(int item){
        return String.format("@%dF", item);
    }

    public static ChainMap<String, Object> named(String name, Object value){
        ChainMap<String, Object> map = new ChainMap<>();
        map.put(name, value);

        return map;
    }

    public static ChainMap<String, Object> value(Object value){
        return named(KEY_UNNAMED, value);
    }

    public static SemanticFunction valueFn(Object value){
        return params -> Collections.singletonList(value(value));
    }

    public static List<Map<String, Object>> identity(List<Map<String, Object>> params){
        return params;
    }

    public static List<Map<String, Object>> merge(List<Map<String, Object>> params){
        Map<String, Object> map = new HashMap<>();
        for(Map<String, Object> p: params){
            if(!p.containsKey(KEY_UNNAMED))
                map.putAll(p);
        }
        return Collections.singletonList(map);
    }

    public static List<Map<String, Object>> first(List<Map<String, Object>> params){
        return Collections.singletonList(params.get(0));
    }

    public static List<Map<String, Object>> last(List<Map<String, Object>> params){
        return Collections.singletonList(params.get(params.size()-1));
    }


    public static SemanticFunction parseSemantics(String semantics){
        SemanticFunction fn = null;

        semantics = semantics.trim();
        if(semantics.startsWith("@")) {
            switch (semantics.toLowerCase()) {
                case _IDENTITY:
                    fn = SemanticUtils::identity;
                    break;
                case _FIRST:
                    fn = SemanticUtils::first;
                    break;
                case _LAST:
                    fn = SemanticUtils::last;
                    break;
                case _MERGE:
                    fn = SemanticUtils::merge;
                    break;
                default:
                    Matcher m = PATTERN_PARAM.matcher(semantics);
                    if (m.matches()) {
                        int index = Integer.valueOf(m.group(1));
                        fn = params -> Collections.singletonList(params.get(index));
                    }
                    break;
            }
        }else if(semantics.startsWith("{")){
            final Map<String, Object> template = GSON.fromJson(
                    semantics, new TypeToken<HashMap<String, Object>>() {
                    }.getType()
            );
            fn = parseTemplate(template);
        }else {
            fn = valueFn(semantics);
        }

        return fn;
    }

    public static SemanticFunction parseTemplate(Map<String, Object> template){

        return params -> {
            Map<String, Object> result = new HashMap<>();
            LinkedList<Map<String, Object>> queueIn = new LinkedList<>();
            LinkedList<Map<String, Object>> queueOut = new LinkedList<>();

            queueIn.addLast(template);
            queueOut.addLast(result);

            while (!queueIn.isEmpty()) {
                final Map<String, Object> mapIn = queueIn.removeFirst();
                final Map<String, Object> mapOut = queueOut.removeFirst();

                for (Map.Entry<String, Object> entry : mapIn.entrySet()) {
                    mapOut.put(entry.getKey(), entry.getValue());

                    if(entry.getKey().equals(_MERGE)) {
                        // Merge the parameters
                        if(entry.getValue() instanceof List){
                            List<Number> indices = (List<Number>)entry.getValue();
                            for(Number index: indices){
                                mapOut.putAll(params.get(index.intValue()));
                            }
                        }
                        // Remove merge key
                        mapOut.remove(_MERGE);

                    } else if (entry.getValue() instanceof Map) {
                        Map<String, Object> child = new HashMap<>();
                        mapOut.put(entry.getKey(), child);

                        queueIn.addLast((Map<String, Object>) entry.getValue());
                        queueOut.addLast(child);


                    } else if (entry.getValue() instanceof String) {
                        String value = (String) entry.getValue();
                        value = value.trim();

                        if(value.startsWith("@")) {

                            switch (value) {
                                case _FIRST:
                                    subsume(mapOut, params.get(0), entry.getKey());
                                    break;
                                case _LAST:
                                    subsume(mapOut, params.get(params.size() - 1), entry.getKey());
                                    break;
                                case _APPEND:
                                    mapOut.put(entry.getKey(), append(params, entry.getKey()));
                                    break;
                                default:
                                    processNumberParam(mapOut, entry.getKey(), value, params);
                                    break;
                            }
                        }
                    }

                }
            }
            return Collections.singletonList(result);

        };

    }

    static boolean processNumberParam(Map<String, Object> map, String key, String value, List<Map<String, Object>> params){
        Matcher m = PATTERN_PARAM.matcher(value);
        if(m.matches()){
            int index = Integer.valueOf(m.group(1));
            String numberType = m.group(2);
            Map<String, Object> param = params.get(index);

            if(numberType == null)
                subsume(map, params.get(index), key);
            else if(param.containsKey(KEY_UNNAMED) && param.get(KEY_UNNAMED) instanceof Number){
                Number num = (Number)(param.get(KEY_UNNAMED));
                if(numberType.equals("L"))
                    map.put(key, num.longValue());
                else if(numberType.equals("F"))
                    map.put(key, num.doubleValue());
            }
            return true;
        }
        return false;
    }

    static void subsume(Map<String, Object> parent, Map<String, Object> child, String key){
        parent.put(key, child.containsKey(KEY_UNNAMED) ? child.get(KEY_UNNAMED) : child);
    }

    static List<Object> append(List<Map<String, Object>> params, String key){
        List<Object> list = new LinkedList<>();
        for(Map<String, Object> p: params){
            if(p.containsKey(key)){
                Object v = p.get(key);
                if(v instanceof List){
                    list.addAll((List<Object>)v);
                }
            }else if(!p.isEmpty() && !p.containsKey(KEY_UNNAMED)){
                list.add(p);
            }
        }

        return list;
    }

}
