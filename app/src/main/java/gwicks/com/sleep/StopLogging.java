package gwicks.com.sleep;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by gwicks on 28/09/2018.
 */
public class StopLogging extends BroadcastReceiver {

    private static final String TAG = "StopLogging";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: at start");



        //sensors.stopService(sensors);
        context.stopService(new Intent(context, AccGryLgt.class));
        Log.d(TAG, "onReceive: logging should be stopped");
    }
}