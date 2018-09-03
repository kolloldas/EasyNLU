# EasyNLU
EasyNLU is a Natural Language Understanding (NLU) library written in Java for mobile apps. Being grammar based, it is a good fit for domains that are narrow but require tight control. 

The project has a sample Android application that can schedule reminders from natural language input:

![screenshot](screenshot.gif)

EasyNLU is licensed under Apache 2.0.
  
## Overview
At its core EasyNLU is a CCG based semantic parser. A good introduction to semantic parsing can be found [here](http://nbviewer.jupyter.org/github/wcmac/sippycup/blob/master/sippycup-unit-0.ipynb). A semantic parser can parse an input like:
> Go to the dentist at 5pm tomorrow

into a structured form:
```
{task: "Go to the dentist", startTime:{offset:{day:1}, hour:5, shift:"pm"}}
```
EasyNLU provides the structured form as a recursive Java map. This structured form can be then resolved into a task specific object that is 'executed'. E.g. In the sample project the structured form is resolved into a `Reminder` object which has fields like `task`, `startTime` and `repeat` and is used to set up an alarm with the [AlarmManager](https://developer.android.com/reference/android/app/AlarmManager) service.

In general following are the high level steps to set up the NLU capability:
1. Define the rules for the parser
2. Collect labeled samples and train the parser
3. Write a resolver to convert the structured form into task specific objects
4. Integrate into the mobile app

## Parsing
Before writing any rules we should define the scope of the task and the parameters of the structured form. As a toy example let's say our task is to turn on and off phone features like Bluetooh, Wifi and GPS. So the fields are:
* Feature: (bluetooth, wifi, gps)
* Action: (enable/disable)

An example structured form would be:
```
{feature: "bluetooth", action: "enable" }
```
Also it helps to have a few sample inputs to understand the variations:
* turn off Bluetooth
* bt on
* enable wifi
* kill GPS

### Defining a Rule
Continuing the toy example, we can say that at the top level we have a setting action that must have a feature and an action. We then use rules to capture this information:
```java
Rule r1 = new Rule("$Setting", "$Feature $Action");
Rule r2 = new Rule("$Setting", "$Action $Feature");
```
A rule contains a LHS and RHS at a minimum. By convention we prepend a '$' to a word to indicate a category. A category represents a collection of words or other categories. In the rules above `$Feature` represents words like bluetooth, bt, wifi etc which we capture using 'lexical' rules:
```java
List<Rule> lexicals = Arrays.asList(
 new Rule("$Feature", "bluetooth"),
 new Rule("$Feature", "bt"),
 new Rule("$Feature", "wifi"),
 new Rule("$Feature", "gps"),
 new Rule("$Feature", "location"),
);

```
To normalize variations in feature names we structure `$Features` into sub-features:
```java
List<Rule> featureRules = Arrays.asList(
 new Rule("$Feature", "$Bluetooth"),
 new Rule("$Feature", "$Wifi"),
 new Rule("$Feature", "$Gps"),
 
 new Rule("$Bluetooth", "bt"),
 new Rule("$Bluetooth", "bluetooth"),
 new Rule("$Wifi", "wifi"),
 new Rule("$Gps", "gps"),
 new Rule("$Gps", "location")
);

```
Similary for `$Action`:
```java
List<Rule> actionRules = Arrays.asList(
 new Rule("$Action", "$EnableDisable"),
 new Rule("$EnableDisable", "?$Switch $OnOff"),
 new Rule("$EnableDisable", "$Enable"),
 new Rule("$EnableDisable", "$Disable"),
 
 new Rule("$OnOff", "on"),
 new Rule("$OnOff", "off"),
 new Rule("$Switch", "switch"),
 new Rule("$Switch", "turn"),
 
 new Rule("$Enable", "enable"),
 new Rule("$Disable", "disable"),
 new Rule("$Disable", "kill")
);

```
Note the '?' in the third rule; this means that the category `$Switch` is optional. 
To determine if a parse is succesful the parser looks for a special category called the root category. By convention it is denoted as `$ROOT`. We need to add a rule to reach this category:
```java
Rule root = new Rule("$ROOT", "$Setting");
```
With these set of rules our parser should be able to parse the above examples, converting them into so called syntax trees.
### Attaching Semantics
Parsing is no good if we cannot extract the meaning of the sentence. This meaning is captured by the structured form (Logical form in NLP jargon). In EasyNLU we pass a third parameter in the rule definition to define how the semantics will be extracted. We use JSON syntax with special markers to do this:
```java
new Rule("$Action", "$EnableDisable", "{action:@first}"),
```
`@first` tells the parser to pick the value of the first category of the rule RHS. In this case it will be either 'enable' or 'disable' based on the sentence. Other markers include:
* `@identity`: Identity function
* `@last`: Pick the value of the last RHS category
* `@N`: Pick the value of the N<sup>th</sup> RHS catgory, e.g. `@3` will pick the 3rd
* `@merge`: Merge values of all the categories. Only named values (e.g. `{action: enable}`) will be merged
* `@append`: Append values of all the categories into a list. Resulting list must be named. Only named values are allowed

After adding semantic markers our rules become:
```java
List<Rule> rules = Arrays.asList(
  new Rule("$ROOT", "$Setting", "@identity"),
  new Rule("$Setting", "$Feature $Action", "@merge"),
  new Rule("$Setting", "$Action $Feature", "@merge"),
  
  new Rule("$Feature", "$Bluetooth", "{feature: bluetooth}"),
  new Rule("$Feature", "$Wifi", "{feature: wifi}"),
  new Rule("$Feature", "$Gps", "{feature: gps}"),
 
  new Rule("$Bluetooth", "bt"),
  new Rule("$Bluetooth", "bluetooth"),
  new Rule("$Wifi", "wifi"),
  new Rule("$Gps", "gps"),
  new Rule("$Gps", "location"),
  
  new Rule("$Action", "$EnableDisable", "{action: @first}"),
  new Rule("$EnableDisable", "?$Switch $OnOff", "@last"),
  new Rule("$EnableDisable", "$Enable", "enable"),
  new Rule("$EnableDisable", "$Disable", "disable"),
 
  new Rule("$OnOff", "on", "enable"),
  new Rule("$OnOff", "off", "disable"),
  new Rule("$Switch", "switch"),
  new Rule("$Switch", "turn"),
 
  new Rule("$Enable", "enable"),
  new Rule("$Disable", "disable"),
  new Rule("$Disable", "kill")
);
```
If the semantics parameter is not provided the parser will create a default value equal to the RHS.

### Running the Parser
To run the parser clone this repository and import the parser module into your Android Studio/IntelliJ project. The EasyNLU parser takes a `Grammar` object that holds the rules, a `Tokenizer` object to convert the input sentence into words and an optional list of `Annotator` objects to annotate entities like numbers, dates, places etc. Run the following code after defining the rules:
```java
  Grammar grammar = new Grammar(rules, "$ROOT");
  Parser parser = new Parser(grammar, new BasicTokenizer(), Collections.emptyList());

  System.out.println(parser.parse("kill bt"));
  System.out.println(parser.parse("wifi on"));
  System.out.println(parser.parse("enable location"));
  System.out.println(parser.parse("turn off GPS"));
```
You should get the following output:
```
23 rules
[{feature=bluetooth, action=disable}]
[{feature=wifi, action=enable}]
[{feature=gps, action=enable}]
[{feature=gps, action=disable}]
```
Try out other variations. If a parse fails for a sample variant you'll get no output. You can then add or modify the rules and repeat the grammar engineering process. 

### Supporting Lists
EasyNLU now supports lists in the structured form. For the above domain it can handle inputs like
> Disable location bt gps

Add these 3 extra rules to the above grammar:
```
  new Rule("$Setting", "$Action $FeatureGroup", "@merge"),
  new Rule("$FeatureGroup", "$Feature $Feature", "{featureGroup: @append}"),
  new Rule("$FeatureGroup", "$FeatureGroup $Feature", "{featureGroup: @append}"),
```
Run a new query like:
```
System.out.println(parser.parse("disable location bt gps"));
```
You should get this output:
```
[{action=disable, featureGroup=[{feature=gps}, {feature=bluetooth}, {feature=gps}]}]
```
Note that these rules get triggered only if there is more than one feature in the query

### Using annotators
Annotators make it easier to specific types of tokens that would otherwise be cumbersome or downright impossible to handle via rules. For instance take the `NumberAnnotator` class. It will detect and annotate all numbers as `$NUMBER`. You can then directly reference the category in your rules, e.g:
```
Rule r = new Rule("$Conversion", "$Convert $NUMBER $Unit $To $Unit", "{convertFrom: {unit: @2, quantity: @1}, convertTo: {unit: @last}}"
```
EasyNLU currently comes with a few annotators:
* `NumberAnnotator`: Annotates numbers
* `DateTimeAnnotator`: Annotates some date formats. Also provides its own rules that you add using `DateTimeAnnotator.rules()`
* `TokenAnnotator`: Annotates each token of the input as `$TOKEN`
* `PhraseAnnotator`: Annotates each contiguous phrase of input as `$PHRASE`

To use your own custom annotator implement the `Annotator` interface and pass it to the parser. Refer in-built annotators to get an idea how to implement one.

### Defining rules in text files
EasyNLU supports loading rules from text files. Each rule must be in a separate line. The LHS, RHS and the semantics must be separated by tabs:
```
$EnableDisable  ?$Switch $OnOff @last
```
*Be careful not to use IDEs that auto convert tabs to spaces*

## Learning
As more rules are added to the parser you'll find that the parser finds multiple parses for certain inputs. This is due to the general ambiguity of human languages. To determine how accurate the parser is for your task you need to run it through labeled examples.

### Getting the data
Like the rules earlier EasyNLU takes examples defined in plain text. Each line should be a separate example and should contain the raw text and the structured form separated by a tab:
```
take my medicine at 4pm	{task:"take my medicine", startTime:{hour:4, shift:"pm"}}
```

It is important that your cover an acceptable number or variations in the input. You'll get more variety if get different people to do this task. The number of examples depend on the complexity of the domain. The [sample dataset](trainer/data/examples-reminders.txt) provided in this project has 100+ examples.

Once you have the data you can that into a dataset. The learning part is handled by the `trainer` module; import it into your project. Load a dataset like this:
```java
Dataset dataset = Dataset.fromText('filename.txt')
```

### Evaluating the bare parser
We evaluate the parser to determine two types of accuracies
* Oracle accuracy: The fraction of examples where atleast one parse (the structured form) from the parser is correct
* Prediction accuracy: The fraction of examples where the *first* parse from the parser is correct

To run the evaluation we use the `Model` class:
```
Model model = new Model(parser);
model.evaluate(dataset, 2);
```
The 2nd parameter to the evaluate function is the verbose level. Higher the number, more verbose the ouput. The `evaluate()` function with run the parser through each example and show incorrect parses finally displaying the accuracy. If you get both the accuracies in the high 90s then training is unnecessary, you could probably handle those few bad parses with post processing. If oracle accuracy is high but prediction accuracy low then training the system will help. If oracle accuracy is itself low then you need to do more grammar engineering.

### Learning the correct parse
To get the correct parse we score them and pick the one with the highest score. EasyNLU uses a simple linear model to compute the scores. Training is performed using Stochastic Gradient Descent (SGD) with a hinge loss function. Input features are based on rule counts and fields in the structured form. The trained weights are then saved into a text file. 

You can tune some of the model/training parameters to get better accuracy. For the reminders models following parameters were used:
```java
HParams hparams = HParams.hparams()
                .withLearnRate(0.08f)
                .withL2Penalty(0.01f)
                .set(SVMOptimizer.CORRECT_PROB, 0.4f);
```
Run the training code as follows:
```java
Experiment experiment = new Experiment(model, dataset, hparams, "yourdomain.weights");
experiment.train(100, false);
```
This will train the model for 100 epochs with a train-test split of 0.8. It will display the accuracies on the test set and save the model weights into the provided file path. To train on the entire dataset, set the deploy parameter to true. You can run an interactive mode take input from the console:
```
experiement.interactive();
```
*NOTE: that training is not guarranteed to produce high accuracy even with a large number of examples. For certain scenarios the provided features may not be discriminative enough. If you get stuck in such a case please log an issue and we can find additional features.*

### Deploying the model
The model weights is a plain text file. For an Android project you can place it in the `assets` folder and load it using the AssetManager. Please refer [ReminderNlu.java](app/src/main/java/coldash/easyreminders/api/nlu/ReminderNlu.java) for more details. You could even store your weights and rules in the cloud and update your model over the air (Firebase anyone?).

## Post processing
Once the parser is doing a good job at converting natural language input into a structured form you'll probably want that data in task specific object. For some domains like the toy example above it can be pretty straight forward. For others you might have to resolve references to things like dates, places, contacts etc. In the reminder sample the dates are often relative (e.g. 'tomorrow', 'after 2 hours' etc) which need to be converted into absolute values. Please refer [ArgumentResolver.java](app/src/main/java/coldash/easyreminders/api/nlu/ArgumentResolver.java) to see how the resolution is done. 

*TIP: Keep resolution logic at a minimum when defining rules and do most of it in post processing. It will keep the rules simpler.*
