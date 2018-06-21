# EasyNLU
EasyNLU is a Natural Language Understanding (NLU) library written in Java for mobile apps. Being grammar based, it is a good fit for domains that are narrow but require tight control. The project has a sample Android application that can schedule reminders from natural language input:
<Insert GIF>
EasyNLU is licensed with Apache 2.0.
  
## Overview
At its core EasyNLU is a CCG based semantic parser. A good introduction to semantic parsing can be found [here](http://nbviewer.jupyter.org/github/wcmac/sippycup/blob/master/sippycup-unit-0.ipynb). A semantic parser can parse an input like:
> Go to the dentist at 5pm tomorrow

into a structured form:
```
{task: "Go to the dentist", startTime:{offset:{day:1}, hour:5, shift:"pm"}}
```
EasyNLU provides the structured form as a recursive Java map. This structured form can be then resolved into a task specific object that is 'executed'. E.g. In the sample project the structured form is resolved into a `Reminder` object which has fields like `task`, `startTime` and `repeat` and is used to set up an alarm with the [AlarmManager](https://developer.android.com/reference/android/app/AlarmManager) service.

In general following are the high steps to set up the NLU:
1. Define the rules for the parser
2. Collect labeled samples and train the parser
3. Write a resolver to convert the structured form into task specific objects
4. Integrate into the mobile app

## Parsing
Before writing the rules we should define the scope of the task and the parameters of the structured form. As a toy example let's say our task is to turn on and off phone features like Bluetooh, Wifi and GPS. So the fields are:
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
Continuing the toy example, we can say that at the top level we have a setting action that must have a feature and an action. We use rules to capture this information:
```
Rule r1 = new Rule("$Setting", "$Feature $Action");
Rule r2 = new Rule("$Setting", "$Action $Feature");
```
A rule contains a LHS and RHS at a minimum. By convention we prepend a '$' to a word to indicate a category. A category represents a collection of words or other categories. In this rule `$Feature` represents words like bluetooth, bt, wifi etc which we capture using 'lexical' rules:
```
List<Rule> lexicals = Arrays.asList(
 new Rule("$Feature", "bluetooth"),
 new Rule("$Feature", "bt"),
 new Rule("$Feature", "wifi"),
 new Rule("$Feature", "gps"),
 new Rule("$Feature", "location"),
);

```
To normalize variations in feature names we structure `$Features` into sub-features:
```
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
```
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
```
Rule root = new Rule("$ROOT", "$Setting");
```
With these set of rules our parser should be able to parse the above examples, converting them into so called syntax trees.
### Attaching Semantics
Parsing is no good if we cannot extract the meaning of the sentence. This meaning is captured by the structured form (Logical form in NLP jargon). In EasyNLU we pass a third parameter in the rule definition to define how the semantics will be captured. We use JSON syntax with special markers to do this:
```
new Rule("$Action", "$EnableDisable", "{action:@first}"),
```
`@first` tells the parser to pick the value of the first category of the rule RHS. In this case it will be either 'enable' or 'disable' based on the sentence. Other markers are:
* `@last`: Pick the value of the last RHS category
* `@N`: Pick the value of the Nth RHS catgory, e.g. `@3` will pick the 3rd
* `@merge`: Merge values of all the categories. Only named values (e.g. `{action: enable}`) will be merged
After adding semantic markers our rules become:
```
List<Rule> rules = Arrays.asList(
  new Rule("$ROOT", "$Setting", "@first"),
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
To run the parser clone this repository and import the parser module into your Android Studio/IntelliJ project. The EasyNLU takes a `Grammar` object that holds the rules, a `Tokenizer` object to convert the input sentence into words and an optional list of `Annotator` objects to annotate entities like numbers, dates, places etc. Run the following code after defining the rules:
```
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
### Using annotators
Annotators make it easier to specific types of tokens that would otherwise be cumbersome or downright impossible to handle via rules. For instance take the `NumberAnnotator` class. It will detect and annotate all numbers as `$NUMBER`. You can then directly reference the category in your rules, e.g:
```
Rule r = new Rule("$Conversion", "$Convert $NUMBER $Unit $To $Unit", "{convertFrom: {unit: @2, quantity: @1}, convertTo: {unit: @last}}"
```
### Defining rules in text files
EasyNLU supports loading rules from text files. Each rule must be in a separate line. The LHS, RHS and the semantics must be separated by tabs:
```
$EnableDisable  ?$Switch $OnOff @last
```
*Be careful not to use IDEs that auto convert tabs to spaces*
