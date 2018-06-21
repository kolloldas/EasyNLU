package coldash.easyreminders;

import android.app.Application;

import coldash.easyreminders.api.ReminderApi;


public class ReminderApplication extends Application {

    @Override
    public void onCreate(){
        super.onCreate();
    }

    public ReminderApi getApi(){
        return ReminderApi.getsInstance(this);
    }
}
