package coldash.easynlu.learn;

import coldash.easynlu.parse.Derivation;
import coldash.easynlu.parse.LogicalForm;
import coldash.easynlu.parse.Parser;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SVMOptimizer implements Optimizer {
    public static final String CORRECT_PROB = "correctProb";

    private float learnRate, l2Penalty, lrDecay;
    private float correctProb;
    private Parser parser;
    private Map<String, Float> weights;
    private Random random = new Random();

    public SVMOptimizer(Model model, HParams hparams){
        this.parser = model.getParser();
        this.weights = model.getWeights();

        learnRate = hparams.learnRate(0.1f).floatValue();
        l2Penalty = hparams.l2Penalty(0.0f).floatValue();
        lrDecay = hparams.lrDecay(1.0f).floatValue();
        correctProb = hparams.get(CORRECT_PROB, 0.5f).floatValue();
    }


    @Override
    public void onEpochStart() {

    }

    @Override
    public float optimize(Example e) {
        List<LogicalForm> candidates = parser.parse(e.input);
        if(candidates.isEmpty())
            return 1;

        LogicalForm lf = randomCandidate(e, candidates);
        Derivation d = lf.getDerivation();

        Map<String, Integer> features = d.getRuleFeatures();
        features.putAll(lf.fields().stream().collect(Collectors.toMap(Function.identity(), k -> 1)));

        float target = computeTarget(lf, e);
        float loss = hingeLoss(features, target);

        if(loss > 0)
            updateWeights(features, target, learnRate);

        updateL2Penalty(learnRate, l2Penalty);

        return loss;
    }

    @Override
    public void onEpochComplete(int numExamples) {
        learnRate *= lrDecay;
    }

    float computeScore(Map<String, Integer> features){
        float y = 0;
        for(Map.Entry<String, Integer> entry: features.entrySet()){
            y += weights.computeIfAbsent(entry.getKey(), s -> (float)random.nextGaussian()) * entry.getValue();
        }
        return y;
    }

    float hingeLoss(Map<String, Integer> features, float target){
        float y = computeScore(features);
        return Math.max(0, 1 - target*y);
    }

    float l2Loss(float l2Penalty){
        return (float) weights.values().stream().mapToDouble(v -> v*v).sum();
    }

    float computeTarget(LogicalForm lf, Example e){
        return lf.match(e.label) ? 1 : -1;
    }

    void updateWeights(Map<String, Integer> features, float target, float learnRate){
        features.forEach((k, v) -> weights.compute(k, (wk, wv)-> wv + learnRate * v * target));
    }

    void updateL2Penalty(float learnRate, float l2Penalty){
        weights.replaceAll((k, v) -> v * (1 - learnRate * l2Penalty));
    }

    LogicalForm randomCandidate(Example e, List<LogicalForm> candidates){
        LogicalForm correct = null;
        if(random.nextFloat() < correctProb){
            for(LogicalForm lf: candidates){
                if(lf.match(e.label)){
                    correct = lf;
                    break;
                }
            }
        }
        if(correct != null)
            return correct;
        else
            return candidates.get(random.nextInt(candidates.size()));
    }
}
