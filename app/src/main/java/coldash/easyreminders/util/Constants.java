package coldash.easyreminders.util;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class Constants {

    public final static long MUL_SECOND = 1000;
    public final static long MUL_MINUTE = 60*MUL_SECOND;
    public final static long MUL_HOUR = 60*MUL_MINUTE;
    public final static long MUL_DAY = 24*MUL_HOUR;
    public final static long MUL_YEAR = 365*MUL_DAY; // TODO: Handle leap years
    public final static long MUL_MONTH = 30*MUL_DAY; // TODO: Handle 31, 28 days
    public final static long MUL_WEEK = 7*MUL_DAY;

    public final static int DEFAULT_MORNING_TIME = 900;
    public final static int DEFAULT_AFTERNOON_TIME = 1400;
    public final static int DEFAULT_EVENING_TIME = 1800;
    public final static int DEFAULT_NIGHT_TIME = 2000;

    public final static String KEY_TASK = "task";

    public final static String KEY_START_TIME = "startTime";
    public final static String KEY_END_TIME = "endTime";
    public final static String KEY_REPEAT = "repeat";

    public final static String KEY_YEAR = "year";
    public final static String KEY_MONTH = "month";
    public final static String KEY_WEEK = "week";
    public final static String KEY_DAY = "day";
    public final static String KEY_DOW = "dow";
    public final static String KEY_HOUR = "hour";
    public final static String KEY_MINUTE = "minute";
    public final static String KEY_SECOND = "second";

    public final static String KEY_OFFSET = "offset";
    public final static String KEY_SHIFT = "shift";
    public final static String SHIFT_AM = "am";
    public final static String SHIFT_PM = "pm";
    public final static String SHIFT_MORNING = "morning";
    public final static String SHIFT_NOON = "noon";
    public final static String SHIFT_AFTERNOON = "afternoon";
    public final static String SHIFT_EVENING = "evening";
    public final static String SHIFT_NIGHT = "night";


    public final static List<String> KEYS_TIME = Arrays.asList(
            KEY_YEAR, KEY_MONTH, KEY_WEEK, KEY_DAY, KEY_HOUR, KEY_MINUTE, KEY_SECOND
    );

    public final static List<Long> KEYS_MUL = Arrays.asList(
            MUL_YEAR, MUL_MONTH, MUL_WEEK, MUL_DAY, MUL_HOUR, MUL_MINUTE, MUL_SECOND
    );

    public final static List<Integer> KEYS_CAL_FIELD = Arrays.asList(
            Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.DAY_OF_MONTH,
            Calendar.HOUR, Calendar.MINUTE, Calendar.SECOND
    );
}
