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
import android.util.Log;

import java.util.Calendar;

import androidx.core.app.NotificationCompat;

/**
 * Created by gwicks on 24/09/2018.
 *
 * Weekday Qualtrix notification link, link will change depending on whether or not the nudges have started
 * as shown by the boolean (started)
 */

public class QualtrixNotiOneReceiver extends BroadcastReceiver {

    private static final String TAG = "QualtrixNotiOneReceiver";
    private NotificationManager mNotificationManager;
    Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    String CHANNEL_DI = "QualtrixOne";
    Context mContext;

    String qualtrixURL;
    String header;
    public static String firstQualtrixLink = "https://oregon.qualtrics.com/jfe/form/SV_6zoaJMOHop0Q0AZ";
    public static String secondQualtrixLink = "https://oregon.qualtrics.com/jfe/form/SV_aXazAkwvhEUPdR3";

//    public static String firstQualtrixLink = "https://oregon.qualtrics.com/jfe/form/SV_aXazAkwvhEUPdR3";
//    public static String secondQualtrixLink = "https://oregon.qualtrics.com/jfe/form/SV_aXazAkwvhEUPdR3";

    public static String firstHeader = "Qualtrix Survey One";
    public static String secondHeader = "Qualtrix Survey Two";

    @Override
    public void onReceive(Context context, Intent intent) {

        mContext = context;
        Log.d(TAG, "onReceive: in onreceive");

        Calendar cal = Calendar.getInstance();
        int dow = cal.get(Calendar.DAY_OF_WEEK);

        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean started = mSharedPreferences.getBoolean("NudgesStarted", false); // Have the nudges started, if so change link
        Log.d(TAG, "onReceive: started = " + started);

        if(started){
            qualtrixURL = secondQualtrixLink;
            header = secondHeader;
        }else{
            qualtrixURL = firstQualtrixLink;
            header = firstHeader;
        }

        if(!isWeekday(dow)){
            Log.d(TAG, "onReceive: weekend!!");
            return;
        }

        int hour = cal.get(Calendar.HOUR_OF_DAY);
        Log.d(TAG, "onReceive: hour : " + hour);
        if(hour >= 12){
            Log.d(TAG, "onReceive: after 11 skipping");
            return;
        }


        if(mNotificationManager == null){
            Log.d(TAG, "onReceive: in notification manager = null");
            mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE) ;
            Log.d(TAG, "onReceive: notificiation manager  = " + mNotificationManager);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            String name = "Oreo_Notificaitons";
            NotificationChannel mChannel = mNotificationManager.getNotificationChannel(CHANNEL_DI);
            if (mChannel == null) {
                mChannel = new NotificationChannel(CHANNEL_DI, name, importance);
                mChannel.setDescription("QualtrixOne");
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                mNotificationManager.createNotificationChannel(mChannel);
            }
        }

        Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(qualtrixURL));
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 1, notificationIntent, 0);

        Notification mBuilder =
                new NotificationCompat.Builder(context, CHANNEL_DI)
                        .setSmallIcon(R.drawable.noti_icon)
                        .setContentTitle(header)
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

    public boolean isWeekday(int dayOfWeek){
        return ((dayOfWeek >= Calendar.MONDAY) && dayOfWeek <= Calendar.FRIDAY);
    }
}
