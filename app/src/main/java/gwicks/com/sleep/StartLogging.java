package gwicks.com.sleep;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

/**
 * Created by gwicks on 28/09/2018.
 *
 * Start the sensor service
 */

public class StartLogging extends BroadcastReceiver {

    private static final String TAG = "StartLogging";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: ");

        // sleep for 5 seconds to ensure on install that start logging is called after stopping logging. Should only
        // make a difference on the first day of the study, immediately after install
        try {

            //sleep 5 seconds
            Thread.sleep(5000);

            System.out.println("Testing..." );

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // foreground service is phone is oreo or later

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {


            context.startService((new Intent(context, AccGryLgt.class)));
        }
        else{

            context.startForegroundService((new Intent(context, AccGryLgt.class)));
        }
        Log.d(TAG, "onReceive: logging should have started!");

    }
}
