package coldash.easyreminders.ui;

import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;
import java.util.Objects;

import coldash.easyreminders.databinding.ReminderItemBinding;
import coldash.easyreminders.model.Reminder;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>{

    public interface OnDeleteListener {
        void onDelete(Reminder reminder);
    }

    List<? extends Reminder> reminders;
    OnDeleteListener listener;

    public ReminderAdapter(OnDeleteListener listener){
        this.listener = listener;
    }

    public void updateReminders(final List<? extends Reminder> newReminders){

        if(reminders == null){
            reminders = newReminders;
            notifyItemRangeInserted(0, newReminders.size());

        }else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return reminders.size();
                }

                @Override
                public int getNewListSize() {
                    return newReminders.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return reminders.get(oldItemPosition).getId() ==
                            newReminders.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Reminder newReminder = newReminders.get(newItemPosition);
                    Reminder oldReminder = reminders.get(oldItemPosition);
                    return newReminder.getId() == oldReminder.getId()
                            && Objects.equals(newReminder.getTask(), oldReminder.getTask())
                            && Objects.equals(newReminder.getStartTime(), oldReminder.getStartTime())
                            && newReminder.getRepeat() == oldReminder.getRepeat();
                }
            });

            reminders = newReminders;
            result.dispatchUpdatesTo(this);
        }
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ReminderItemBinding binding = ReminderItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent,false);
        binding.setListener(listener);
        return new ReminderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        holder.binding.setReminder(reminders.get(position));
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return reminders == null ? 0 : reminders.size();
    }

    static class ReminderViewHolder extends RecyclerView.ViewHolder {

        final ReminderItemBinding binding;

        public ReminderViewHolder(ReminderItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
