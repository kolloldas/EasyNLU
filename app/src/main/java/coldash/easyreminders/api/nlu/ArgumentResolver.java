package coldash.easyreminders.api.nlu;


import android.content.Context;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import static coldash.easyreminders.util.Zip.*;

import coldash.easyreminders.model.Reminder;
import coldash.easyreminders.util.Constants;

public class ArgumentResolver {

    public ArgumentResolver(Context context){

    }

    public Reminder resolve(Map<String, Object> map, String rawText, Calendar now){
        ReminderImpl reminder = new ReminderImpl((int)(now.getTimeInMillis()/1000), rawText);

        resolveTask(map, reminder);
        resolveRepeat(map, reminder, now);
        resolveStartTime(map, reminder, now);
        resolveEndTime(map, reminder, now);

        return reminder;
    }

    void resolveTask(Map<String, Object> map, ReminderImpl reminder){
        reminder.task = getOrDefault(map, Constants.KEY_TASK, "");
    }

    void resolveRepeat(Map<String, Object> map, ReminderImpl reminder, Calendar now){
        if(!map.containsKey(Constants.KEY_REPEAT)){
            reminder.repeatDuration = -1;
            return;
        }
        Map<String, Object> repeatMap = (Map<String, Object>) map.get(Constants.KEY_REPEAT);

        zip(Constants.KEYS_TIME, Constants.KEYS_MUL, Constants.KEYS_CAL_FIELD)
            .forEach((key, mul, field) -> {
                if(repeatMap.containsKey(key)){
                    int duration = ((Number) repeatMap.get(key)).intValue();
                    reminder.repeatDuration += duration * mul;

                    // Calendar cannot add week
                    if(key.equals(Constants.KEY_WEEK))
                        duration *= 7;
                    now.add(field, duration);
                }

                return true;
            });

        if(repeatMap.containsKey(Constants.KEY_DOW))
            resolveRepeatDow(((Number) repeatMap.get(Constants.KEY_DOW)).intValue(), reminder, now);

        if(repeatMap.containsKey(Constants.KEY_SHIFT))
            // Copy to startTime
            getOrDefault(map, Constants.KEY_START_TIME, new HashMap<String, Object>())
                    .put(Constants.KEY_SHIFT, repeatMap.get(Constants.KEY_SHIFT));

        reminder.startTime = now.getTime();

    }

    void resolveRepeatDow(int dow, ReminderImpl reminder, Calendar now){

        // Correct for first day of week
        dow = (dow + Calendar.MONDAY - now.getFirstDayOfWeek() + 6) % 7 + 1;

        reminder.repeatDuration += 7* Constants.MUL_DAY;

        int curDow = now.get(Calendar.DAY_OF_WEEK);
        int dayDiff = dow - curDow;
        if(dayDiff < 0)
            dayDiff += 7;
        now.add(Calendar.DAY_OF_MONTH, dayDiff);

    }

    void resolveStartTime(Map<String, Object> map, ReminderImpl reminder, Calendar now){
        if(!map.containsKey(Constants.KEY_START_TIME))
            return;

        reminder.startTime = resolveTime((Map<String, Object>) map.get(Constants.KEY_START_TIME), now);
    }

    void resolveEndTime(Map<String, Object> map, ReminderImpl reminder, Calendar now){
        if(!map.containsKey(Constants.KEY_END_TIME))
            return;

        reminder.endTime = resolveTime((Map<String, Object>) map.get(Constants.KEY_END_TIME), now);
    }

    Date resolveTime(Map<String, Object> timeMap, Calendar now){
        final boolean[] useDefaults = {false};

        Calendar calendar = Calendar.getInstance();
        calendar.clear();

        if(timeMap.containsKey(Constants.KEY_OFFSET))
            resolveOffsets((Map<String, Object>)timeMap.get(Constants.KEY_OFFSET), now);

        zip(Constants.KEYS_TIME, Constants.KEYS_CAL_FIELD)
            .forEach((key, field) -> {
                if(timeMap.containsKey(key)) {
                    int value = ((Number) timeMap.get(key)).intValue();
                    // Correct for month
                    if(key.equals(Constants.KEY_MONTH))
                        value -= 1;

                    calendar.set(field, value);

                    // Once we've found one datetime field, use defaults for all lower ones
                    useDefaults[0] = true;
                }else {
                    if(useDefaults[0])
                        calendar.set(field, getDefault(field, now));
                    else {
                        calendar.set(field, now.get(field));
                        // Handle AM/PM
                        if(key.equals(Constants.KEY_HOUR))
                            calendar.set(Calendar.AM_PM, now.get(Calendar.AM_PM));
                    }
                }

                return true;
            });

        if(timeMap.containsKey(Constants.KEY_SHIFT))
            resolveShift((String)timeMap.get(Constants.KEY_SHIFT), calendar);

        return calendar.getTime();
    }

    int getDefault(int field, Calendar now){
        switch (field){
            case Calendar.MONTH:
                return Calendar.JANUARY;
            case Calendar.DAY_OF_WEEK:
                return now.getFirstDayOfWeek();
            case Calendar.DATE:
                return 1;
            case Calendar.HOUR_OF_DAY:
                return 12;
            case Calendar.HOUR:
            case Calendar.MINUTE:
            case Calendar.SECOND:
                return 0;
            default:
                return 0;
        }
    }

    void resolveShift(String shift, Calendar calendar){
        switch (shift){
            case Constants.SHIFT_AM:
                calendar.set(Calendar.AM_PM, Calendar.AM);
                break;
            case Constants.SHIFT_PM:
                calendar.set(Calendar.AM_PM, Calendar.PM);
                break;
            case Constants.SHIFT_MORNING:
                calendar.set(Calendar.HOUR_OF_DAY, Constants.DEFAULT_MORNING_TIME/100);
                calendar.set(Calendar.MINUTE, Constants.DEFAULT_MORNING_TIME%100);
                break;
            case Constants.SHIFT_NOON:
                calendar.set(Calendar.HOUR_OF_DAY, 12);
                break;
            case Constants.SHIFT_AFTERNOON:
                calendar.set(Calendar.HOUR_OF_DAY, Constants.DEFAULT_AFTERNOON_TIME/100);
                calendar.set(Calendar.MINUTE, Constants.DEFAULT_AFTERNOON_TIME%100);
                break;
            case Constants.SHIFT_EVENING:
                calendar.set(Calendar.HOUR_OF_DAY, Constants.DEFAULT_EVENING_TIME/100);
                calendar.set(Calendar.MINUTE, Constants.DEFAULT_EVENING_TIME%100);
                break;
            case Constants.SHIFT_NIGHT:
                calendar.set(Calendar.HOUR_OF_DAY, Constants.DEFAULT_NIGHT_TIME/100);
                calendar.set(Calendar.MINUTE, Constants.DEFAULT_NIGHT_TIME%100);
                break;
        }
    }

    void resolveOffsets(Map<String, Object> offsetMap, Calendar calendar){
        zip(Constants.KEYS_TIME, Constants.KEYS_CAL_FIELD)
            .forEach((key, field) -> {
                if(offsetMap.containsKey(key)){
                    int quantity = ((Number) offsetMap.get(key)).intValue();

                    // Calendar cannot add week
                    if(key.equals(Constants.KEY_WEEK))
                        quantity *= 7;
                    calendar.add(field, quantity);
                }

                return true;
            });
    }

    <V> V getOrDefault(Map<String, Object> map, String key, V defaultValue){
        if(map.containsKey(key) && map.get(key).getClass().isAssignableFrom(defaultValue.getClass()))
            return (V) map.get(key);
        else {
            map.put(key, defaultValue);
            return defaultValue;
        }
    }

}
