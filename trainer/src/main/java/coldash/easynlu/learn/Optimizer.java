package coldash.easynlu.learn;

public interface Optimizer {
    void onEpochStart();
    float optimize(Example e);
    void onEpochComplete(int numExamples);
}
