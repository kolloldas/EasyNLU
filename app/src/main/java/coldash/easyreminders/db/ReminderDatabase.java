package coldash.easyreminders.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import coldash.easyreminders.model.Reminder;

@Database(entities = {ReminderEntity.class}, version = 1)
@TypeConverters(DateConverter.class)
public abstract class ReminderDatabase extends RoomDatabase {
    static final String DB_NAME = "reminder-db";
    public abstract ReminderDao reminderDao();

    public static ReminderDatabase buildDatabase(Context context){
        return Room.databaseBuilder(context, ReminderDatabase.class, DB_NAME).build();
    }

    public void addReminder(Reminder reminder){
        reminderDao().addReminder(new ReminderEntity(reminder));
    }

    public void deleteReminder(Reminder reminder){
        if(reminder instanceof ReminderEntity)
            reminderDao().deleteReminder((ReminderEntity) reminder);
        else
            reminderDao().deleteReminder(new ReminderEntity(reminder));
    }
}
