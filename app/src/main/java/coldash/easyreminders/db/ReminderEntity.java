package coldash.easyreminders.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

import coldash.easyreminders.model.Reminder;

@Entity(tableName = "reminders")
public class ReminderEntity implements Reminder {

    @PrimaryKey
    private int id;
    private String task;
    private String rawText;
    private Date startTime;
    private Date endTime;
    private long repeat;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getTask() {
        return task;
    }

    @Override
    public Date getStartTime() {
        return startTime;
    }

    @Override
    public Date getEndTime() {
        return endTime;
    }

    @Override
    public long getRepeat() {
        return repeat;
    }

    @Override
    public String getRawText() {
        return rawText;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }


    public ReminderEntity(int id, String task, String rawText, Date startTime,
                          Date endTime, long repeat) {
        this.id = id;
        this.task = task;
        this.rawText = rawText;
        this.startTime = startTime;
        this.endTime = endTime;
        this.repeat = repeat;
    }

    public ReminderEntity(Reminder reminder){
        id = reminder.getId();
        task = reminder.getTask();
        rawText = reminder.getRawText();
        startTime = reminder.getStartTime();
        endTime = reminder.getEndTime();
        repeat = reminder.getRepeat();
    }
}
