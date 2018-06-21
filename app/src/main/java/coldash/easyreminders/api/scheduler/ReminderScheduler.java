package coldash.easyreminders.api.scheduler;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import coldash.easyreminders.model.Reminder;

public class ReminderScheduler {
    public final static String CHANNEL_ID = "coldash.easyreminders";
    Context context;

    public ReminderScheduler(Context context){
        this.context = context;

        createNotificationChannel();
    }

    public void schedule(Reminder reminder){
        if(reminder.getTask() != null && reminder.getStartTime() != null) {
            if(reminder.getRepeat() != -1)
                scheduleRepeating(reminder);
            else
                scheduleOneShot(reminder);
        }
    }

    public void cancel(Reminder reminder){
        PendingIntent pendingIntent = getReminderIntent(reminder);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    void scheduleOneShot(Reminder reminder){
        PendingIntent pendingIntent = getReminderIntent(reminder);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminder.getStartTime().getTime(), pendingIntent);
    }

    void scheduleRepeating(Reminder reminder){
        PendingIntent pendingIntent = getReminderIntent(reminder);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, reminder.getStartTime().getTime(),
                                    reminder.getRepeat(), pendingIntent);
    }

    PendingIntent getReminderIntent(Reminder reminder){
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra(Intent.EXTRA_TEXT, reminder.getTask());
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_UID, reminder.getId());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                reminder.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    private void createNotificationChannel(){
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Easy Reminders";
            String description = "Show reminders";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


}
