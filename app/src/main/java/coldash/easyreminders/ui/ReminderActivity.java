package coldash.easyreminders.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import coldash.easyreminders.R;
import coldash.easyreminders.databinding.ActivityReminderBinding;
import coldash.easyreminders.viewmodel.ReminderViewModel;

public class ReminderActivity extends AppCompatActivity {

    ActivityReminderBinding layoutBinding;
    ReminderAdapter remindersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layoutBinding = DataBindingUtil.setContentView(this, R.layout.activity_reminder);

        final ReminderViewModel reminderViewModel = ViewModelProviders.of(this)
                .get(ReminderViewModel.class);

        subscribeUi(reminderViewModel);

        remindersAdapter = new ReminderAdapter(reminderViewModel::deleteReminder);
        layoutBinding.remindersList.setAdapter(remindersAdapter);
        //layoutBinding.remindersList.setHasFixedSize(true);

    }

    private void subscribeUi(ReminderViewModel reminderViewModel){
        layoutBinding.setViewModel(reminderViewModel);

        reminderViewModel.getReminders().observe(this, reminders -> {
            if(reminders != null){
                remindersAdapter.updateReminders(reminders);
            }
        });

        reminderViewModel.getErrorMessages().observe(this, message -> {
            if(message != null && !message.isEmpty())
                Toast.makeText(ReminderActivity.this, message, Toast.LENGTH_SHORT).show();
        });
    }
}
