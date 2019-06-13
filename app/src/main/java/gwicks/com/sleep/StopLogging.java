package gwicks.com.sleep;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by gwicks on 28/09/2018.
 */
public class StopLogging extends BroadcastReceiver {

    private static final String TAG = "StopLogging";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: at start");

        Calendar cal = Calendar.getInstance();
        long when = cal.getTimeInMillis();
        int currentHour = cal.get(Calendar.HOUR_OF_DAY);
        Log.d(TAG, "stopLogging: current hour: " + currentHour);
        if(currentHour < 9 || currentHour > 19){
            Log.d(TAG, "stopLogging: skipping stop logging");
            return;
        }



        //sensors.stopService(sensors);
        context.stopService(new Intent(context, AccGryLgt.class));
        Log.d(TAG, "onReceive: logging should be stopped");
    }
}