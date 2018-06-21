package coldash.easynlu.learn;

import coldash.easynlu.parse.LogicalForm;
import coldash.easynlu.parse.Parser;

import java.io.*;
import java.util.*;

public class Model {
    Parser parser;
    Map<String, Float> weights;


    public Model(Parser parser){
        this.parser = parser;
        weights = parser.getWeights();
    }

    public Parser getParser() {
        return parser;
    }

    public Map<String, Float> getWeights() {
        return weights;
    }

    void loadWeights(String filePath){
        weights = parseWeights(filePath);
        parser.setWeights(weights);
    }

    static Map<String, Float> parseWeights(String filePath){
        File file = new File(filePath);
        Map<String, Float> weights = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            String line;
            while((line = reader.readLine()) != null){
                String[] items = line.split("\t");
                assert items.length == 2: "Malformed input";

                weights.put(items[0].trim(), Float.valueOf(items[1].trim()));
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        return weights;
    }

    void saveWeights(String filePath){
        File file = new File(filePath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

            for(Map.Entry<String, Float> entry: weights.entrySet())
                writer.write(String.format("%s\t%f\n", entry.getKey(), entry.getValue()));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void train(Dataset d, Optimizer optimizer, int epochs){
        float loss;
        float maxAcc = 0;
        int count;
        Map<String, Float> bestWeights = null;
        for(int i = 0; i < epochs; i++){
            loss = 0;
            count = 0;

            optimizer.onEpochStart();
            for(Example e: d.shuffle()){
                loss += optimizer.optimize(e);
                count++;
            }
            optimizer.onEpochComplete(count);

            System.out.println(String.format("Epoch %d: Train loss = %f", i+1, loss/count));
            float acc = evaluate(d, 0);
            if(acc > maxAcc){
                maxAcc = acc;
                bestWeights = new HashMap<>(parser.getWeights());
            }

            System.out.println();
        }

        System.out.println("Max accuracy: " + maxAcc);
        weights = bestWeights;
        parser.setWeights(bestWeights);
    }

    float evaluate(Dataset dataset, int verboseLevel){
        float acc = 0, accOracle = 0;
        float score = 0;
        int count = 0;

        for(Example e: dataset){
            boolean first = true;
            boolean correct = false;
            boolean firstCorrect = false;

            List<LogicalForm> lfs = parser.parse(e.input);
            for(LogicalForm lf: lfs){
                if(first)
                    score += lf.getDerivation().getScore();
                if(lf.match(e.label)){
                    if(first) {
                        acc++;
                        firstCorrect = true;
                    }
                    accOracle++;
                    correct = true;
                    break;
                }
                first = false;
            }

            if(!correct && verboseLevel > 0 ){
                System.out.println("Failed to parse: " + e.input);
                System.out.println("Predictions:");
                lfs.forEach(System.out::println);
                System.out.println("Target:");
                System.out.println(e.label);
                System.out.println();
            }else if(!firstCorrect && verboseLevel > 1){
                System.out.println("Wrong prediction for: " + e.input);
                System.out.println("Prediction");
                System.out.println(lfs.get(0));
                System.out.println("Target:");
                System.out.println(e.label);
                System.out.println();
            }

            count++;
        }

        if(count > 0) {
            acc /= count;
            score /= count;
            accOracle /= count;
        }

        if(verboseLevel > 0) {
            System.out.println(count + " examples");
            System.out.println("Average score: " + score);
        }
        System.out.println("Accuracy: " + acc);
        System.out.println("Oracle accuracy: " + accOracle);

        return acc;
    }

}
