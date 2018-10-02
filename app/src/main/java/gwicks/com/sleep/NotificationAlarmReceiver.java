package gwicks.com.sleep;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by gwicks on 20/09/2018.
 */

public class NotificationAlarmReceiver extends BroadcastReceiver{

    private static final String TAG = "NotificationAlarmReceiv";

    private NotificationManager mNotificationManager;

    Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    String CHANNEL_DI = "Nudge_Notification";

    File nudgetext;

    @Override
    public void onReceive(Context context, Intent intent) {

        String nudge = "";

        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String fileName = dt.format(date);

        if(Math.random() < 0.5){
            Log.d(TAG, "onReceive: nudge < 0.5");
            nudge = "Make sure to set your alarm for your normal wakeup time.";
        }else{
            Log.d(TAG, "onReceive: nudge > 0.5");
            nudge = "Remember to use your wake up routine tomorrow morning.";
        }

        String path2 = (context.getExternalFilesDir(null) + "/Sensors/Nudges/");
        File directory = new File(path2);

        if(!directory.exists()){
            Log.d(TAG, "onCreate: making directory");
            directory.mkdirs();
        }

        nudgetext = new File(directory, fileName + ".txt");

        writeToFile(nudgetext, nudge + "\n");

        if(mNotificationManager == null){
            Log.d(TAG, "onReceive: in notification manager = null");
            //mNotificationManager = FinishInstallScreen.notificationManager;
            mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE) ;
            Log.d(TAG, "onReceive: notificiation manager  = " + mNotificationManager);
            //mNotificationManager = EMA.
            //mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        Log.d(TAG, "onReceive: before clear notifications");

        //clearNotfications();
        Log.d(TAG, "onReceive: after clear notifications");

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
                        //.setOngoing(true)
                        .setChannelId(CHANNEL_DI)
                        .setSound(uri)
                        //.setContentIntent(pendingIntent)
                        //.addAction(action)
                        .build();


        mNotificationManager.notify("nudge",3, mBuilder);
        Log.d(TAG, "onReceive OREO: should be notification built now");








    }


    public static void writeToFile(File file, String data) {

        FileOutputStream stream = null;
        System.out.println("The state of the media is: " + Environment.getExternalStorageState());
        Log.d(TAG, "writeToFile: file location is:" + file.getAbsolutePath());



        //File nudgetext = new File(directory, "NudgeFile.txt");

        //OutputStreamWriter stream = new OutputStreamWriter(openFileOutput(file), Context.MODE_APPEND);
        try {
            Log.e("History", "In try");
            Log.d(TAG, "writeToFile: ");
            stream = new FileOutputStream(file, true);
            Log.d(TAG, "writeToFile: 2");
            stream.write(data.getBytes());
            Log.d(TAG, "writeToFile: 3");
        } catch (FileNotFoundException e) {
            Log.e("History", "In catch");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }catch(NullPointerException e){
            e.printStackTrace();
        }

    }
}
