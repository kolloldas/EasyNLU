package coldash.easyreminders.api.nlu;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ArgumentResolverTest {
    Gson gson = new Gson();

    ReminderImpl reminder;
    ArgumentResolver resolver;
    Calendar now;

    @Before
    public void setUp(){
        resolver = new ArgumentResolver(null);
        reminder = new ReminderImpl("");
        now = Calendar.getInstance();
    }


    @Test
    public void resolveRepeatYear() {
        Map<String, Object> map = make("{repeat:{year:2}}");
        Calendar c = Calendar.getInstance();
        c.setTime(now.getTime());
        c.add(Calendar.YEAR, 2);

        long expectedDur = 2L * 365 * 24 * 3600 * 1000;

        resolver.resolveRepeat(map, reminder, now);
        assertEquals(expectedDur, reminder.repeatDuration, 100);
        assertNotNull(reminder.startTime);
        assertEquals(c.getTimeInMillis(), reminder.startTime.getTime(), 100);

    }

    @Test
    public void resolveRepeatWeek() {
        Map<String, Object> map = make("{repeat:{week:2}}");
        Calendar c = Calendar.getInstance();
        c.setTime(now.getTime());
        c.add(Calendar.DAY_OF_MONTH, 2*7);

        long expectedDur = 2L * 7 * 24 * 3600 * 1000;

        resolver.resolveRepeat(map, reminder, now);
        assertEquals(expectedDur, reminder.repeatDuration, 100);
        assertNotNull(reminder.startTime);
        assertEquals(c.getTimeInMillis(), reminder.startTime.getTime(), 100);

    }

    @Test
    public void resolveRepeatSecond() {
        Map<String, Object> map = make("{repeat:{second:200}}");
        Calendar c = Calendar.getInstance();
        c.setTime(now.getTime());
        c.add(Calendar.SECOND, 200);

        long expectedDur = 200L * 1000;

        resolver.resolveRepeat(map, reminder, now);
        assertEquals(expectedDur, reminder.repeatDuration, 100);
        assertNotNull(reminder.startTime);
        assertEquals(c.getTimeInMillis(), reminder.startTime.getTime(), 100);

    }

    @Test
    public void resolveRepeatDow() {
        now.set(2018, 5, 22);

        long expectedDur = 7 * 24 * 3600 * 1000;

        resolver.resolveRepeatDow(3, reminder, now);

        assertEquals(expectedDur, reminder.repeatDuration, 100);
        assertEquals(27, now.get(Calendar.DAY_OF_MONTH));

    }


    @Test
    public void resolveStartTime() {
        Calendar input = Calendar.getInstance();
        Calendar expected = Calendar.getInstance();

        input.clear();
        expected.clear();

        input.set(2018, 5, 29);
        expected.set(2018, 6, 13, 20, 0, 0);

        Map<String, Object> map = make("{startTime:{offset:{week:2}, hour:8, shift:pm}}");
        resolver.resolveStartTime(map, reminder, input);

        assertNotNull(reminder.startTime);
        assertEquals(expected.getTimeInMillis(), reminder.startTime.getTime());

    }

    @Test
    public void resolveEndTime() {
        Calendar input = Calendar.getInstance();
        Calendar expected = Calendar.getInstance();

        input.clear();
        expected.clear();

        input.set(2018, 5, 29);
        expected.set(2018, 6, 1, 20, 0, 0);

        Map<String, Object> map = make("{endTime:{offset:{day:2}, hour:8, shift:pm}}");
        resolver.resolveEndTime(map, reminder, input);

        assertNotNull(reminder.endTime);
        assertEquals(expected.getTimeInMillis(), reminder.endTime.getTime());

    }

    @Test
    public void resolveTime() {
        Calendar c = Calendar.getInstance();
        c.set(2018, 4, 22, 9, 15, 30);

        Map<String, Object> map = make("{year:2018, month:5, day:22, hour:9, minute:15, second:30}");
        assertEquals(c.getTimeInMillis(), resolver.resolveTime(map, now).getTime(), 100);

    }

    @Test
    public void resolveTimeWithOffsets() {
        now.set(2018, 4, 22, 9, 15, 30);
        Calendar c = Calendar.getInstance();
        c.setTime(now.getTime());
        c.set(2018, 4, 23, 9, 15, 0);

        Map<String, Object> map = make("{offset:{day:1}, hour:9, minute:15}");
        assertEquals(c.getTimeInMillis(), resolver.resolveTime(map, now).getTime(), 100);

    }

    @Test
    public void resolveTimePm() {
        Calendar c = Calendar.getInstance();
        c.set(2018, 4, 22, 21, 15, 30);

        Map<String, Object> map = make("{year:2018, month:5, day:22, hour:9, minute:15, second:30, shift:pm}");
        assertEquals(c.getTimeInMillis(), resolver.resolveTime(map, now).getTime(), 100);

    }

    @Test
    public void resolveTimeAfterPm() {
        Calendar input = Calendar.getInstance();
        Calendar expected = Calendar.getInstance();

        input.clear();
        expected.clear();

        input.set(2018, 5, 29, 12, 20, 0);
        expected.set(2018, 5, 29, 12, 30, 0);

        Map<String, Object> map = make("{offset:{minute:10}}");
        assertEquals(expected.getTimeInMillis(), resolver.resolveTime(map, input).getTime(), 100);

    }

    @Test
    public void resolveTimeWithTimeDefaults() {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(2018, 4, 22);

        Map<String, Object> map = make("{year:2018, month:5, day:22}");
        assertEquals(c.getTimeInMillis(), resolver.resolveTime(map, now).getTime(), 100);

    }

    @Test
    public void resolveTimeWithDateDefaults() {
        now.set(2010, 6, 20);
        Calendar c = Calendar.getInstance();

        c.clear();
        c.set(2010, 6, 20);
        c.set(Calendar.HOUR, 2);
        c.set(Calendar.MINUTE, 11);
        c.clear(Calendar.SECOND);
        c.clear(Calendar.MILLISECOND);

        Map<String, Object> map = make("{hour:2, minute:11}");
        assertEquals(c.getTimeInMillis(), resolver.resolveTime(map, now).getTime(), 100);

    }

    @Test
    public void resolveShift() {
        Calendar c = Calendar.getInstance();

        c.clear();
        resolver.resolveShift("morning", c);
        assertEquals(9, c.get(Calendar.HOUR_OF_DAY));

        c.clear();
        resolver.resolveShift("noon", c);
        assertEquals(12, c.get(Calendar.HOUR_OF_DAY));

        c.clear();
        resolver.resolveShift("afternoon", c);
        assertEquals(14, c.get(Calendar.HOUR_OF_DAY));

        c.clear();
        resolver.resolveShift("evening", c);
        assertEquals(18, c.get(Calendar.HOUR_OF_DAY));

        c.clear();
        resolver.resolveShift("night", c);
        assertEquals(20, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, c.get(Calendar.MINUTE));
        assertEquals(1970, c.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, c.get(Calendar.MONTH));
        assertEquals(1, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, c.get(Calendar.SECOND));
    }

    @Test
    public void resolveOffsets(){
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(2018, 4, 22);

        Map<String, Object> map = make("{year:2}");
        resolver.resolveOffsets(map, c);
        assertEquals(2020, c.get(Calendar.YEAR));
        assertEquals(4, c.get(Calendar.MONTH));
        assertEquals(22, c.get(Calendar.DAY_OF_MONTH));

    }

    @Test
    public void resolveOffsetsPm(){
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(2018, 4, 22, 12, 20, 30);

        Map<String, Object> map = make("{minute:10}");
        resolver.resolveOffsets(map, c);
        assertEquals(2018, c.get(Calendar.YEAR));
        assertEquals(4, c.get(Calendar.MONTH));
        assertEquals(22, c.get(Calendar.DAY_OF_MONTH));

        assertEquals(0, c.get(Calendar.HOUR));
        assertEquals(30, c.get(Calendar.MINUTE));
        assertEquals(Calendar.PM, c.get(Calendar.AM_PM));

    }

    @Test
    public void resolveOffsetsWeek(){
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(2018, 5, 29);

        Map<String, Object> map = make("{week:2}");
        resolver.resolveOffsets(map, c);

        assertEquals(2018, c.get(Calendar.YEAR));
        assertEquals(6, c.get(Calendar.MONTH));
        assertEquals(13, c.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void getOrDefault() {
        Map<String, Object> map = make("{}");
        resolver.getOrDefault(map, "a", "b");

        assertTrue(map.containsKey("a"));
        assertEquals(map.get("a"), "b");
    }

    Map<String, Object> make(String json){
        return gson.fromJson(json, new TypeToken<HashMap<String, Object>>(){}.getType());
    }
}