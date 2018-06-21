package coldash.easynlu.parse;

import java.util.*;

public class Parser {

    class Chart {
        final static int MAX_CAPACITY_PER_SPAN = 2000;

        HashMap<Integer, LinkedList<Derivation>> chart;
        int base;

        Chart(int size){
            base = size;
            chart = new HashMap<>(size*size/2);
        }

        LinkedList<Derivation> getDerivations(int start, int end){
            int span = mapSpan(start, end);
            if(!chart.containsKey(span)){
                LinkedList<Derivation> d = new LinkedList<>();
                chart.put(span, d);
                return d;
            }
            return chart.get(span);
        }

        void addDerivation(int start, int end, Derivation derivation) {
            getDerivations(start, end).add(derivation);
        }

        boolean isSpanFull(int start, int end) {
            LinkedList<Derivation> dl =  getDerivations(start, end);
            int size = dl.size();
            if(size > MAX_CAPACITY_PER_SPAN){
                System.err.println(String.format("Exceeded max capacity[%d-%d]: %s", start, end, dl.getLast().rule));
                return true;
            }
            return false;
        }

        int mapSpan(int s, int e) {
            return e*base + s;
        }

    }

    Grammar grammar;
    Tokenizer tokenizer;
    List<Annotator> annotators;
    Map<String, Float> weights;


    public Parser(Grammar grammar, Tokenizer tokenizer, List<Annotator> annotators) {
        this.grammar = grammar;
        this.tokenizer = tokenizer;
        this.annotators = annotators;
        this.weights = new HashMap<>();
    }


    public Map<String, Float> getWeights() {
        return weights;
    }

    public void setWeights(Map<String, Float> weights) {
        this.weights = weights;
    }

    public List<Derivation> parseSyntactic(String input){
        List<String> tokens = tokenizer.tokenize(input);
        List<String> tokensLower = new ArrayList<>(tokens.size());
        for(String token: tokens)
            tokensLower.add(token.toLowerCase());

        int N = tokens.size();
        Chart chart = new Chart(N+1);

        for(int e = 1; e <= N; e++) {
            for(int s = e-1; s >= 0; s--) {

                applyAnnotators(chart, tokens, s, e);
                applyLexicalRules(chart, tokensLower, s, e);
                applyBinaryRules(chart, s, e);
                applyUnaryRules(chart, s, e);
            }
        }

        List<Derivation> derivations = new LinkedList<>();
        for(Derivation d: chart.getDerivations(0, N))
            if(grammar.isRoot(d.rule))
                derivations.add(d);

        return derivations;

    }

    public List<LogicalForm> parse(String input) {

        List<LogicalForm> lfs = new ArrayList<>();

        for(Derivation d: parseSyntactic(input))
            lfs.add(computeLogicalForm(d));

        Collections.sort(lfs, Collections.reverseOrder());
        return lfs;

    }


    public LogicalForm computeLogicalForm(Derivation d){
        LogicalForm lf = new LogicalForm(d, applySemantics(d).get(0));
        lf.updateScore(weights);

        return lf;
    }

    List<Map<String, Object>> applySemantics(Derivation d){
        SemanticFunction fn = d.rule.getSemantics();

        if(d.children == null) {
            if(fn == null)
                return Collections.singletonList(SemanticUtils.value(d.rule.getRHS().toString()));
            else
                return fn.apply(Collections.emptyList());
        }

        String rule = d.rule.toString();
        d.score = weights.containsKey(rule) ? weights.get(rule) : 0f;
        List<Map<String, Object>> params = new LinkedList<>();

        for(Derivation child:  d.children){
            params.addAll(applySemantics(child));
            d.score += child.score;
        }

        return fn.apply(params);
    }

    void applyAnnotators(Chart chart, List<String> tokens, int start, int end) {

        for(Annotator annotator: annotators) {
            for(Rule rule: annotator.annotate(tokens.subList(start, end))) {
                if(chart.isSpanFull(start, end))
                    return;

                chart.addDerivation(start, end, new Derivation(rule, null));
            }
        }
    }

    void applyLexicalRules(Chart chart, List<String> tokens, int start, int end) {
        for(Rule rule: grammar.getLexicalRules(tokens.subList(start, end))) {

            chart.addDerivation(start, end, new Derivation(rule, null));
        }
    }

    void applyUnaryRules(Chart chart, int start, int end) {

        LinkedList<Derivation> queue = new LinkedList<>(chart.getDerivations(start, end));

        while(!queue.isEmpty()) {
            Derivation d = queue.removeFirst();

            for(Rule rule: grammar.getUnaryRules(d.rule.getLHS())) {

                if(chart.isSpanFull(start, end))
                    return;

                //System.out.println(rule);

                Derivation parent = new Derivation(rule, Collections.singletonList(d));

                queue.addLast(parent);
                chart.addDerivation(start, end, parent);
            }
        }
    }

    void applyBinaryRules(Chart chart, int start, int end) {
        if(end > start + 1) {
            for(int mid = start + 1; mid < end; mid++) {
                List<Derivation> left = chart.getDerivations(start, mid);
                List<Derivation> right = chart.getDerivations(mid, end);

                for(Derivation l:left) {
                    for(Derivation r: right) {

                        for(Rule rule: grammar.getBinaryRules(l.rule.getLHS(), r.rule.getLHS())) {

                            if(chart.isSpanFull(start, end))
                                return;

                            //System.out.println(rule);
                            chart.addDerivation(start, end, new Derivation(rule, Arrays.asList(l, r)));
                        }

                    }
                }
            }
        }
    }


}
