package coldash.easynlu.learn;


import java.util.HashMap;
import java.util.Map;

public class HParams {

    protected Map<String, Number> params;

    private HParams(){
        params = new HashMap<>();
    }

    public static HParams hparams(){
        return new HParams();
    }

    public HParams withLearnRate(float learnRate){
        params.put("learnRate", learnRate);
        return this;
    }

    public HParams withL2Penalty(float l2Penalty){
        params.put("l2Penalty", l2Penalty);
        return this;
    }

    public HParams withLrDecay(float lrDecay){
        params.put("lrDecay", lrDecay);
        return this;
    }

    public Number learnRate(Number defaultParam){
        return params.getOrDefault("learnRate", defaultParam);
    }

    public Number l2Penalty(Number defaultParam){
        return params.getOrDefault("l2Penalty", defaultParam);
    }

    public Number lrDecay(Number defaultParam){
        return params.getOrDefault("lrDecay", defaultParam);
    }

    public Number get(String paramName, Number defaultParam){
        return params.getOrDefault(paramName, defaultParam);
    }

    public HParams set(String paramName, Number value){
        params.put(paramName, value);
        return this;
    }
}
