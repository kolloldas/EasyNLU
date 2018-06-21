package coldash.easyreminders.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import java.util.List;

import coldash.easyreminders.ReminderApplication;
import coldash.easyreminders.api.ReminderApi;
import coldash.easyreminders.model.Reminder;

public class ReminderViewModel extends AndroidViewModel {

    MediatorLiveData<Boolean> busyStatus;
    MediatorLiveData<String> errorMsg;
    MutableLiveData<String> reminderText;
    MediatorLiveData<List<? extends Reminder>> reminders;
    ReminderApi api;

    public ReminderViewModel(@NonNull Application application) {
        super(application);

        busyStatus = new MediatorLiveData<>();
        errorMsg = new MediatorLiveData<>();
        reminderText = new MutableLiveData<>();
        reminders = new MediatorLiveData<>();
        api = ((ReminderApplication)application).getApi();

        busyStatus.addSource(api.getBusyStatus(), busyStatus::setValue);
        reminders.addSource(api.getReminders(), reminders::setValue);
        errorMsg.addSource(api.getErrorMessages(), errorMsg::setValue);
    }

    public void newReminder(){
        String text = reminderText.getValue();
        if(text != null && !text.isEmpty()) {
            api.newReminder(text);
            reminderText.setValue("");
        }
    }

    public void deleteReminder(Reminder reminder){
        if(reminder != null)
            api.deleteReminder(reminder);
    }

    public LiveData<Boolean> getBusyStatus() { return busyStatus; }
    public LiveData<String> getErrorMessages() { return errorMsg; }
    public MutableLiveData<String> getReminderText() { return reminderText; }
    public LiveData<List<? extends Reminder>> getReminders() { return reminders; }
}
