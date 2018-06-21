package coldash.easyreminders.api.nlu;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import coldash.easyreminders.model.Reminder;
import coldash.easynlu.parse.Annotator;
import coldash.easynlu.parse.Grammar;
import coldash.easynlu.parse.LogicalForm;
import coldash.easynlu.parse.Parser;
import coldash.easynlu.parse.Rule;
import coldash.easynlu.parse.Rules;
import coldash.easynlu.parse.Weights;
import coldash.easynlu.parse.annotators.DateTimeAnnotator;
import coldash.easynlu.parse.annotators.NumberAnnotator;
import coldash.easynlu.parse.annotators.PhraseAnnotator;
import coldash.easynlu.parse.annotators.TokenAnnotator;
import coldash.easynlu.parse.tokenizers.BasicTokenizer;

public class ReminderNlu {
    final static String RULES_FILE = "reminders.rules";
    final static String WEIGHTS_FILE = "reminders.weights";

    Parser parser;
    ArgumentResolver resolver;

    public ReminderNlu(final Context context){

        List<Rule> rules = new LinkedList<Rule>(){{
           addAll(Rules.BASE);
           addAll(loadRules(context.getAssets()));
           addAll(DateTimeAnnotator.rules());
        }};

        List<Annotator> annotators = Arrays.asList(
                TokenAnnotator.INSTANCE,
                PhraseAnnotator.INSTANCE,
                NumberAnnotator.INSTANCE,
                DateTimeAnnotator.INSTANCE
        );

        Grammar grammar = new Grammar(rules, "$ROOT");
        parser = new Parser(grammar, new BasicTokenizer(), annotators);
        parser.setWeights(loadWeights(context.getAssets()));

        resolver = new ArgumentResolver(context);
    }

    public Reminder parseText(String text){
        List<LogicalForm> lfs = parser.parse(text);
        if(!lfs.isEmpty()){
            return resolver.resolve(lfs.get(0).getMap(), text, Calendar.getInstance());
        }else{
            return null;
        }
    }

    private List<Rule> loadRules(AssetManager assets){
        List<Rule> rules = Collections.emptyList();
        try {
            Reader reader = new InputStreamReader(assets.open(RULES_FILE));
            rules = Rules.fromText(reader);
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return rules;
    }

    private Map<String, Float> loadWeights(AssetManager assets){
        Map<String, Float> weights = Collections.emptyMap();

        try {
            Reader reader = new InputStreamReader(assets.open(WEIGHTS_FILE));
            weights = Weights.fromText(reader);
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return weights;
    }
}
