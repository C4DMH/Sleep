package gwicks.com.sleep;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RestartApp extends BroadcastReceiver {


    private static final String TAG = "RestartApp";
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "onReceive: ");
        Intent intent1 = new Intent(context, FinishScreen.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent1);


    }
}
