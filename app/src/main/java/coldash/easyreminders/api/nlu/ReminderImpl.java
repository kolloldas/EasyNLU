package coldash.easyreminders.api.nlu;

import java.text.SimpleDateFormat;
import java.util.Date;

import coldash.easyreminders.model.Reminder;

public class ReminderImpl implements Reminder {

    int id;
    String task, rawText;
    Date startTime, endTime;
    long repeatDuration;

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
        return repeatDuration;
    }

    @Override
    public String getRawText() {
        return rawText;
    }


    ReminderImpl(String text){
        this.rawText = text;
    }
    ReminderImpl(int id, String text){
        this.id = id;
        this.rawText = text;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Reminder: ").append(task).append("\n");
        if(startTime != null)
            sb.append("Date: ").append(new SimpleDateFormat("MMM d, yyyy h:mm a").format(startTime));

        return sb.toString();
    }
}
