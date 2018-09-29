package gwicks.com.sleep;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

/**
 * Created by gwicks on 28/09/2018.
 */

public class StartLogging extends BroadcastReceiver {

    private static final String TAG = "StartLogging";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: ");

        try {

            //sleep 5 seconds
            Thread.sleep(5000);

            System.out.println("Testing..." );

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            public void run() {
//
//            }
//        }, 6000);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {


            context.startService((new Intent(context, AccGryLgt.class)));
        }
        else{

            context.startForegroundService((new Intent(context, AccGryLgt.class)));
        }
        Log.d(TAG, "onReceive: logging should have started!");

    }
}
