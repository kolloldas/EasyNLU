package coldash.easyreminders.model;


import java.util.Date;

public interface Reminder {
    int getId();
    String getTask();
    Date getStartTime();
    Date getEndTime();
    long getRepeat();
    String getRawText();
}
