package coldash.easyreminders.api;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.util.Log;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import coldash.easyreminders.api.nlu.ReminderNlu;
import coldash.easyreminders.api.scheduler.ReminderScheduler;
import coldash.easyreminders.db.ReminderDatabase;
import coldash.easyreminders.model.Reminder;

public class ReminderApi {
    final static String TAG = ReminderApi.class.getSimpleName();
    private static ReminderApi sInstance;

    private MutableLiveData<Boolean> busyStatus;
    private MutableLiveData<String> errorMsg;
    private MediatorLiveData<List<? extends Reminder>> reminders;

    private Executor executor;

    private ReminderNlu nlu;
    private ReminderScheduler scheduler;

    private ReminderDatabase database;

    private ReminderApi() {}

    public static ReminderApi getsInstance(Context context) {
        if(sInstance == null){
            sInstance = new ReminderApi();
            sInstance.initialize(context);
        }
        return sInstance;
    }

    private void initialize(final Context context){
        executor = Executors.newSingleThreadExecutor();
        busyStatus = new MutableLiveData<>();
        errorMsg = new MutableLiveData<>();
        reminders = new MediatorLiveData<>();

        database = ReminderDatabase.buildDatabase(context);
        reminders.addSource(database.reminderDao().loadAllReminders(), reminders::postValue);

        executor.execute(() -> {
            busyStatus.postValue(true);
            nlu = new ReminderNlu(context);
            scheduler = new ReminderScheduler(context);
            busyStatus.postValue(false);

            Log.i(TAG, "Initialization complete");
        });
    }

    public LiveData<Boolean> getBusyStatus(){
        return busyStatus;
    }

    public LiveData<String> getErrorMessages() { return errorMsg; }

    public LiveData<List<? extends Reminder>> getReminders(){
        return reminders;
    }

    public void newReminder(String text){
        executor.execute(() -> {
            busyStatus.postValue(true);
            Reminder reminder = nlu.parseText(text);
            if(reminder != null) {
                database.addReminder(reminder);
                scheduler.schedule(reminder);

            }else{
                errorMsg.postValue("Failed to parse input");
            }
            busyStatus.postValue(false);
        });
    }

    public void deleteReminder(Reminder reminder){
        executor.execute(() -> {
            busyStatus.postValue(true);
            scheduler.cancel(reminder);
            database.deleteReminder(reminder);
            busyStatus.postValue(false);
        });
    }
}
