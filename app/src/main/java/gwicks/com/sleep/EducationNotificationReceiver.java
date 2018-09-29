package gwicks.com.sleep;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Date;

/**
 * Created by gwicks on 24/09/2018.
 *
 * One off alarm to send a link to the Education thingamajig
 */

public class EducationNotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "EducationNotificationRe";
    Context mContext;
    private NotificationManager mNotificationManager;
    Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    String CHANNEL_DI = "EduLink";



    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "onReceive: edu receiver, why am i here?");

        mContext = context;

        if(mNotificationManager == null){
            Log.d(TAG, "onReceive: in notification manager = null");
            //mNotificationManager = FinishInstallScreen.notificationManager;
            mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE) ;
            Log.d(TAG, "onReceive: notificiation manager  = " + mNotificationManager);
            //mNotificationManager = EMA.
            //mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }



        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);


        long timeSinceInstall = mSharedPreferences.getLong("InstallTime", 0);
        Date date = new Date(System.currentTimeMillis());
        long millis = date.getTime();

        Log.d(TAG, "buttonClick: current time is: " + millis);
//
//        //TODO what is this? Is this for the education link? OK it's because nudges dont start for 7 days
//        //TODO, but I should not need it, because of the calculate eduday method in finishinstallscreen
        //TODO it fires only once, so do the logic on install!!
//
//        if(millis - timeSinceInstall < 200000 ){
//            Log.d(TAG, "onReceive: not yet time for the edu link, skipping");
//            return;
//        }
////
//        if(mSharedPreferences.getBoolean("EduDone", false) == true){
//            Log.d(TAG, "onReceive: we have already done this link, so skip");
//            return;
//        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            String name = "Oreo_Notificaitons";
            NotificationChannel mChannel = mNotificationManager.getNotificationChannel(CHANNEL_DI);
            if (mChannel == null) {
                mChannel = new NotificationChannel(CHANNEL_DI, name, importance);
                mChannel.setDescription("EduLink");
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                mNotificationManager.createNotificationChannel(mChannel);

            }
        }

        Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://oregon.qualtrics.com/jfe/form/SV_5jQzxiIhiTKfJad"));
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 1, notificationIntent, 0);

        Notification mBuilder =
                new NotificationCompat.Builder(context, CHANNEL_DI)
                        .setSmallIcon(R.drawable.noti_icon)
                        .setContentTitle("Qualtrix Survey")
                        .setAutoCancel(true)
                        .setContentText("Please click this to complete Qualtrix Notification")
                        .setOngoing(true)
                        .setChannelId(CHANNEL_DI)
                        .setSound(uri)
                        .setContentIntent(pendingIntent)
                        .build();


        mNotificationManager.notify("first",1, mBuilder);
        Log.d(TAG, "onReceive OREO: should be notification built now");

    }
}
