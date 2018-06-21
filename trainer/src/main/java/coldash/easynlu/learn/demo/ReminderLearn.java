package coldash.easynlu.learn.demo;

import coldash.easynlu.learn.Dataset;
import coldash.easynlu.learn.Experiment;
import coldash.easynlu.learn.HParams;
import coldash.easynlu.learn.Model;
import coldash.easynlu.learn.SVMOptimizer;
import coldash.easynlu.parse.*;
import coldash.easynlu.parse.annotators.DateTimeAnnotator;
import coldash.easynlu.parse.annotators.NumberAnnotator;
import coldash.easynlu.parse.annotators.PhraseAnnotator;
import coldash.easynlu.parse.annotators.TokenAnnotator;
import coldash.easynlu.parse.tokenizers.BasicTokenizer;

import java.util.*;
import java.util.stream.Collectors;

public class ReminderLearn {
    final static Random random = new Random();

    public static Model makeReminderModel(){
        List<Rule> rules = new LinkedList<>();

        rules.addAll(Rules.BASE);
        rules.addAll(Rules.fromFile("model/reminders.rules"));
        rules.addAll(DateTimeAnnotator.rules());

        List<Annotator> annotators = Arrays.asList(
                TokenAnnotator.INSTANCE,
                PhraseAnnotator.INSTANCE,
                NumberAnnotator.INSTANCE,
                DateTimeAnnotator.INSTANCE
        );

        Grammar grammar = new Grammar(rules, "$ROOT");
        Parser parser = new Parser(grammar, new BasicTokenizer(), annotators);

        return new Model(parser);
    }

    public static void main(String[] args){
        Model model = makeReminderModel();
        Dataset dataset = Dataset.fromText("data/examples-reminders.txt");
        HParams hparams = HParams.hparams()
                .withLearnRate(0.08f)
                .withL2Penalty(0.01f)
                .set(SVMOptimizer.CORRECT_PROB, 0.4f);

        Experiment experiment = new Experiment(model, dataset, hparams,
                                              "model/reminders.weights");

        // Uncomment to train the model
        //experiment.train(120, true);

        experiment.evaluate();

        // Uncomment to run model with user input
        //experiment.interactive();

    }
}
