package coldash.easyreminders.ui;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import static coldash.easyreminders.util.Zip.*;
import static coldash.easyreminders.util.Constants.*;

public class BindingAdapters {
    public interface OnGoListener {
        void apply();
    }
    final static DateFormat START_TIME_FORMAT = new SimpleDateFormat("MMM d, yyyy h:mm a");

    final static long MUL_SECOND = 1000;
    final static long MUL_MINUTE = 60*MUL_SECOND;
    final static long MUL_HOUR = 60*MUL_MINUTE;
    final static long MUL_DAY = 24*MUL_HOUR;
    final static long MUL_WEEK = 7*MUL_DAY;
    final static long MUL_MONTH = 30*MUL_DAY; // TODO: Handle 31, 28 days
    final static long MUL_YEAR = 365*MUL_DAY; // TODO: Handle leap years

    @BindingAdapter("visibleGone")
    public static void visibleGone(View view, boolean visible){
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @BindingAdapter("textDate")
    public static void textDate(TextView view, Date date){
        view.setText(START_TIME_FORMAT.format(date));
    }

    @BindingAdapter("textRepeat")
    public static void textRepeat(TextView view, long repeatDuration){
        if(repeatDuration != -1){
            final long[] duration = {0};
            final String[] unit = {""};

            zip(KEYS_MUL, KEYS_TIME)
                .forEach((mul, time) ->{
                    if(repeatDuration % mul == 0){
                        duration[0] = repeatDuration / mul;
                        unit[0] = time;
                        return false;
                    }

                    return true;
                });

            if(duration[0] > 0){
                if(duration[0] == 1)
                    view.setText(String.format("Every %s", unit[0]));
                else
                    view.setText(String.format("Every %d %ss", (int)duration[0], unit[0]));
            }
        }
    }

    @BindingAdapter("onGo")
    public static void onGo(TextView view, OnGoListener listener){
        if(listener == null)
            view.setOnEditorActionListener(null);
        else
            view.setOnEditorActionListener((v, actionId, event) ->{
                if(actionId == EditorInfo.IME_ACTION_GO) {
                    //view.clearFocus();
                    listener.apply();
                    view.setText("");

                    /* FIXME: Have to close keyboard forcefully because RecylerView seems to
                     * be interfering with the normal process */
                    InputMethodManager imm = (InputMethodManager)view.getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                return false;
            });
    }
}
