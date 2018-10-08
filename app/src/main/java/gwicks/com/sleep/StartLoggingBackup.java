package gwicks.com.sleep;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by gwicks on 5/10/2018.
 */

public class StartLoggingBackup extends BroadcastReceiver {

    private static final String TAG = "StartLoggingBackup";

    @Override
    public void onReceive(Context context, Intent intent) {


        Calendar cal = Calendar.getInstance();
        long when = cal.getTimeInMillis();
        int currentHour = cal.get(Calendar.HOUR_OF_DAY);
        Log.d(TAG, "stopLogging: current hour: " + currentHour);
        if(currentHour < 9 || currentHour > 21) {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                context.startService((new Intent(context, AccGryLgt.class)));
                Log.d(TAG, "onReceive: logging");
            }
            else{
                context.startForegroundService((new Intent(context, AccGryLgt.class)));
                Log.d(TAG, "onReceive: logging");
            }
            Log.d(TAG, "onReceive: logging should have started!");
        }else{
            Log.d(TAG, "onReceive: skipping logging backup start as not the right time");
        }
    }
}
