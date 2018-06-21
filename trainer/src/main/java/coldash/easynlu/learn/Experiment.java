package coldash.easynlu.learn;

import java.util.List;
import java.util.Scanner;

import coldash.easynlu.parse.LogicalForm;
import coldash.easynlu.parse.Parser;

public class Experiment {
    final static String KEY_SPLIT_RATIO = "split_ratio";

    Model model;
    Dataset dataset;
    HParams hparams;
    String weightsPath;

    public Experiment(Model model, Dataset dataset, HParams hparams, String weightsPath){
        this.model = model;
        this.dataset = dataset;
        this.hparams = hparams;
        this.weightsPath = weightsPath;
    }

    public void train(int numEpochs, boolean deploy){
        Optimizer optimizer = new SVMOptimizer(model, hparams);

        Dataset train, test;
        if(!deploy) {
            Dataset[] partitions = dataset.split(
                    hparams.get(KEY_SPLIT_RATIO, 0.8f).floatValue(), true);
            train = partitions[0];
            test = partitions[1];

        }else{
            train = test = dataset;
        }

        model.train(train, optimizer, numEpochs);

        System.out.println("Evaluate test set:");
        model.evaluate(test, 2);
        model.saveWeights(weightsPath);
    }

    public void evaluate(){
        model.loadWeights(weightsPath);
        model.evaluate(dataset, 2);
    }

    public void interactive(){

        model.loadWeights(weightsPath);

        Parser parser = model.getParser();
        Scanner scanner = new Scanner(System.in);

        while(true){
            System.out.print(">> ");
            String input = scanner.nextLine();

            List<LogicalForm> results = parser.parse(input);

            if(results.size() > 0)
                System.out.println(results.get(0));
            else
                System.err.println("Failed to parse input\n");
        }
    }
}
