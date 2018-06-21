package coldash.easyreminders.db;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

public class DateConverter {

    @TypeConverter
    public static Long fromDate(Date date) {
        if(date != null)
            return date.getTime();
        else
            return (long) -1;
    }

    @TypeConverter
    public  static Date toDate(Long time) {
        if(time == -1)
            return null;
        else
            return new Date(time);
    }
}
