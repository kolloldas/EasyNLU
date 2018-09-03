package coldash.easynlu.parse.annotators;

import coldash.easynlu.parse.Annotator;
import coldash.easynlu.parse.Rule;
import coldash.easynlu.parse.SemanticUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Date Annotator
 * 1/12/17 -> $Date
 * 01/02/2018 -> $Date
 * 1-2-2018 -> $Date
 * 3rd May -> $DateDay $DateMonth
 * today -> $DateDay
 * yesterday -> $DateDay
 * 2 days back/ago -> $Number $Day $Ago
 * last Sunday -> $Previous $DateDow 
 * wednesday -> $DateDow
 * 1st of March -> $DateDay $Of $DateMonth
 * 10th June -> $DateDay $DateMonth
 * June 10 2016 -> $DateMonth $Number $Number
 * 2.3.96 -> $Date
 * 29 June 2003 -> $Number $DateMonth $NUmber
 * 4th of July -> $DateDay $Of $DateMonth
 * Mon -> $DateDow
 * May the 13th -> $DateMonth $The $DateDay
 * Nov 7 1972 -> $DateMonth $Number $Number
 * 
 * @author bobroo_2
 *
 */
public class DateTimeAnnotator implements Annotator {

    final static String CAT_DATE = "$DATE";


    static final List<String> MONTHS = Arrays.asList(
                "january", "february", "march", "april", "may", "june",
                "july", "august", "september", "october", "november", "december",
                "jan", "feb", "mar", "apr", "jun",
                "jul", "aug", "sep", "oct", "nov", "dec"
            );


    static final List<String> DOW = Arrays.asList(
            "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday",
            "mon", "tue", "wed", "thu", "fri", "sat", "sun"
        );


    static final Map<String, String> SHIFT_MAP = new HashMap<String, String>(){{
        put("morning", "morning"); put("noon", "noon"); put("afternoon", "afternoon");
        put("evening", "evening"); put("tonight", "night"); put("night", "night");
        put("am", "am"); put("pm", "pm");
    }};

    static final Pattern PATTERN_NUMBER = Pattern.compile("(\\d+)(st$|rd$|th$)?");

    static final List<Rule> DATE_RULES = Arrays.asList(
            new Rule("$Hour", "hour"),
            new Rule("$Hour", "hours"),
            new Rule("$Hour", "hr"),
            new Rule("$Hour", "h"),
            new Rule("$Hour", "hrs"),
            new Rule("$Hourly", "hourly"),

            new Rule("$Second", "second"),
            new Rule("$Second", "seconds"),
            new Rule("$Second", "sec"),
            new Rule("$Second", "secs"),
            new Rule("$Second", "s"),

            new Rule("$Minute", "minute"),
            new Rule("$Minute", "minutes"),
            new Rule("$Minute", "min"),
            new Rule("$Minute", "m"),
            new Rule("$Minute", "mins"),

            new Rule("$Morning", "morning"),
            new Rule("$Noon", "noon"),
            new Rule("$Afternoon", "afternoon"),
            new Rule("$Evening", "evening"),
            new Rule("$Night", "night"),

            new Rule("$Day", "day"),
            new Rule("$Day", "days"),
            new Rule("$Daily", "daily"),

            new Rule("$Week", "week"),
            new Rule("$Week", "weeks"),
            new Rule("$Weekly", "weekly"),

            new Rule("$Month", "month"),
            new Rule("$Month", "months"),
            new Rule("$Monthly", "monthly"),

            new Rule("$Year", "year"),
            new Rule("$Year", "years"),
            new Rule("$Yearly", "yearly"),

            new Rule("$AtBy", "$At", SemanticUtils::first),
            new Rule("$AtBy", "$By", SemanticUtils::first),

            new Rule(CAT_DATE, "?$In $DATE_MONTH", SemanticUtils::last),
            new Rule(CAT_DATE, "?$On ?$The $DATE_DAY", SemanticUtils::last),
            new Rule(CAT_DATE, "?$On ?$The $DATE_DAY ?$Of $DATE_MONTH", SemanticUtils::merge),
            new Rule(CAT_DATE, "?$On $DATE_MONTH $DATE_DAY", SemanticUtils::merge),
            new Rule(CAT_DATE, "?$On ?$The $DATE_DAY ?$Of $DATE_MONTH $DATE_YEAR", SemanticUtils::merge),
            new Rule(CAT_DATE, "?$On $DATE_MONTH $DATE_DAY $DATE_YEAR", SemanticUtils::merge),

            new Rule(CAT_DATE, "$DATE_ELEMENT ?$DATE", SemanticUtils::merge),
            new Rule("$DATE_ELEMENT", "?$In ?$The $DATE_SHIFT", SemanticUtils::last),
            new Rule("$DATE_ELEMENT", "?$AtBy $NUMBER", SemanticUtils.named("hour", SemanticUtils._LAST)),
            new Rule("$DATE_ELEMENT", "?$AtBy $Noon", SemanticUtils.named("shift", "noon")),
            new Rule("$DATE_ELEMENT", "?$AtBy $Night", SemanticUtils.named("shift", "night")),
            new Rule("$DATE_ELEMENT", "?$AtBy $DATE_TIME", SemanticUtils::last),
            new Rule("$DATE_ELEMENT", "$DATE_OFFSET", SemanticUtils::first),
            new Rule("$DATE_ELEMENT", "?$On $DATE_DOW", SemanticUtils::last),
            new Rule("$DATE_ELEMENT", "$NextDate", SemanticUtils::first),
            new Rule("$DATE_ELEMENT", "$AfterDuration", SemanticUtils::first),

            new Rule("$NextDate", "$Next $Week", SemanticUtils.named("offset", SemanticUtils.named("week", 1.0))),
            new Rule("$NextDate", "$Next $Day", SemanticUtils.named("offset", SemanticUtils.named("day", 1.0))),
            new Rule("$NextDate", "$Next $Month", SemanticUtils.named("offset", SemanticUtils.named("month", 1.0))),
            new Rule("$NextDate", "$Next $Hour", SemanticUtils.named("offset", SemanticUtils.named("hour", 1.0))),
            new Rule("$NextDate", "$Next $Minute", SemanticUtils.named("offset", SemanticUtils.named("minute", 1.0))),

            new Rule("$AfterDuration", "$After $DATE_DURATION", SemanticUtils.named("offset", SemanticUtils._LAST)),
            new Rule("$AfterDuration", "$In $DATE_DURATION", SemanticUtils.named("offset", SemanticUtils._LAST)),

            new Rule("$DATE_DURATION", "$NUMBER $Second", SemanticUtils.named("second", SemanticUtils._FIRST)),
            new Rule("$DATE_DURATION", "$A $Second", SemanticUtils.named("second", 1.0)),

            new Rule("$DATE_DURATION", "$NUMBER $Minute", SemanticUtils.named("minute", SemanticUtils._FIRST)),
            new Rule("$DATE_DURATION", "$A $Minute", SemanticUtils.named("minute", 1.0)),

            new Rule("$DATE_DURATION", "$NUMBER $Hour", SemanticUtils.named("hour", SemanticUtils._FIRST)),
            new Rule("$DATE_DURATION", "$An $Hour", SemanticUtils.named("hour", 1.0)),

            new Rule("$DATE_DURATION", "$NUMBER $Day", SemanticUtils.named("day", SemanticUtils._FIRST)),
            new Rule("$DATE_DURATION", "$A $Day", SemanticUtils.named("day", 1.0)),

            new Rule("$DATE_DURATION", "$NUMBER $Week", SemanticUtils.named("week", SemanticUtils._FIRST)),
            new Rule("$DATE_DURATION", "$A $Week", SemanticUtils.named("week", 1.0)),

            new Rule("$DATE_DURATION", "$NUMBER $Month", SemanticUtils.named("month", SemanticUtils._FIRST)),
            new Rule("$DATE_DURATION", "$A $Month", SemanticUtils.named("month", 1.0)),

            new Rule("$DATE_DURATION", "$NUMBER $Year", SemanticUtils.named("year", SemanticUtils._FIRST)),
            new Rule("$DATE_DURATION", "$A $Year", SemanticUtils.named("year", 1.0))
            );

    public static final Annotator INSTANCE = new DateTimeAnnotator();

    private Map<String, Number> mapMonths, mapDow;

    private DateTimeAnnotator() {
        mapMonths = new HashMap<>();
        mapDow = new HashMap<>();

        for(int i = 0; i < MONTHS.size(); i++)
            mapMonths.put(MONTHS.get(i), (i%12)+1.0);

        for(int i = 0; i < DOW.size(); i++)
            mapDow.put(DOW.get(i), (i%7)+1.0);

    }

    @Override
    public List<Rule> annotate(List<String> tokens) {
        if(tokens.size() == 1) {
            String s = tokens.get(0).toLowerCase();
            Rule r = parseMonth(s);
            if(r == null)
                r = parseDay(s);
            if(r == null)
                r = parseTime(s);
            if(r != null)
                return Collections.singletonList(r);
            else
                return parseNumber(s);
        }

        return Collections.emptyList();
    }


    private Rule parseMonth(String s) {
        Rule r = null;

        if(mapMonths.containsKey(s.toLowerCase()))
            r = new Rule("$DATE_MONTH", s, SemanticUtils.named("month", mapMonths.get(s)));


        return r;
    }


    private Rule parseDay(String s) {
        Rule r = null;
        if(s.equals("today"))
            r = new Rule("$DATE_OFFSET", s, SemanticUtils.named("offset", SemanticUtils.named("day", 0.0)));
        else if(s.equals("tomorrow"))
            r = new Rule("$DATE_OFFSET", s, SemanticUtils.named("offset", SemanticUtils.named("day", 1.0)));
        else if(mapDow.containsKey(s))
            r = new Rule("$DATE_DOW", s, SemanticUtils.named("dow", mapDow.get(s)));

        return r;
    }

    private List<Rule> parseNumber(String s) {
        List<Rule> rules = new LinkedList<>();

        Matcher m = PATTERN_NUMBER.matcher(s);
        if(m.matches()) {
            Integer num = Integer.valueOf(m.group(1));
            if(num >= 1 && num <= 31)
                rules.add(new Rule("$DATE_DAY", s, SemanticUtils.named("day", num.doubleValue())));

            if(num >= 1 && num <= 12)
                rules.add(new Rule("$DATE_MONTH", s, SemanticUtils.named("month", num.doubleValue())));

            if((num >= 1 && num <= 99) || (num >= 1970 && num <= 2100))
                rules.add(new Rule("$DATE_YEAR", s, SemanticUtils.named("year", num.doubleValue())));

            if(num >= 100 && num <= 1259) {
                Integer min = num % 100;
                Integer hr = num / 100;
                final Map<String, Object> template = SemanticUtils.named("hour", hr.doubleValue());
                if(min > 0)
                    template.put("minute", min.doubleValue());
                rules.add(new Rule("$DATE_TIME", s, template));
            }
        }

        return rules;
    }

    private Rule parseTime(String s) {
        Rule rule = null;

        if(SHIFT_MAP.containsKey(s)){
            rule = new Rule("$DATE_SHIFT", s, SemanticUtils.named("shift", SHIFT_MAP.get(s)));
        }

        return rule;
    }

    public static List<Rule> rules(){

        return DATE_RULES;
    }

}
