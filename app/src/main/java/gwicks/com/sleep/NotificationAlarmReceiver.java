package gwicks.com.sleep;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import java.util.Random;

import androidx.core.app.NotificationCompat;

/**
 * Created by gwicks on 20/09/2018.
 */

public class NotificationAlarmReceiver extends BroadcastReceiver{

    private static final String TAG = "NotificationAlarmReceiv";
    private NotificationManager mNotificationManager;
    Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    String CHANNEL_DI = "Nudge_Notification";
    String nudge = "Remember to use your wake up routine tomorrow morning.";

    @Override
    public void onReceive(Context context, Intent intent) {

        if(mNotificationManager == null){
            Log.d(TAG, "onReceive: in notification manager = null");
            mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE) ;
            Log.d(TAG, "onReceive: notificiation manager  = " + mNotificationManager);
        }
        Log.d(TAG, "onReceive: before clear notifications");

        //clearNotfications();
        Log.d(TAG, "onReceive: after clear notifications");


        Random random = new Random();
        int nxt = random.nextInt(99);
        Intent resultIntent = new Intent(context, FinishScreen.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        resultIntent.putExtra("message", nudge + "\n\n" + "\nPress either Back or Home to exit");
        PendingIntent pendingIntent = PendingIntent.getActivity(context, nxt, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            String name = "Oreo_Notificaitons";
            NotificationChannel mChannel = mNotificationManager.getNotificationChannel(CHANNEL_DI);
            if (mChannel == null) {
                mChannel = new NotificationChannel(CHANNEL_DI, name, importance);
                mChannel.setDescription("blah_1");
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                mNotificationManager.createNotificationChannel(mChannel);
            }
        }
        Notification mBuilder =
                new NotificationCompat.Builder(context, CHANNEL_DI)
                        .setSmallIcon(R.drawable.noti_icon)
                        .setContentTitle("Reminder")
                        .setAutoCancel(true)
                        .setContentText(nudge)
                        .setOngoing(true)
                        .setChannelId(CHANNEL_DI)
                        .setSound(uri)
                        .setContentIntent(pendingIntent)
                        //.addAction(action)
                        .build();

        mNotificationManager.notify("nudge",3, mBuilder);
        Log.d(TAG, "onReceive OREO: should be notification built now");
    }
}
