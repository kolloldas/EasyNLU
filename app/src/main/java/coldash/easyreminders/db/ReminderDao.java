package coldash.easyreminders.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY id DESC")
    LiveData<List<ReminderEntity>> loadAllReminders();

    @Query("SELECT * from reminders where id = :id")
    LiveData<ReminderEntity> getReminder(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addReminder(ReminderEntity reminder);

    @Delete
    void deleteReminder(ReminderEntity reminder);
}
