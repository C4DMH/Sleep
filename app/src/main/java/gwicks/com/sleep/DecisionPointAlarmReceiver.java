package gwicks.com.sleep;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by gwicks on 20/09/2018.
 *
 * This receiver is where the logic for when the nudge will arrive is done
 */

public class DecisionPointAlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "DecisionPointAlarmRecei";
    File nudgetext;
    Context mContext;
    private PendingIntent notificationIntent;

    @Override
    public void onReceive(Context context, Intent intent) {

        int sleepTime;
        int sleepActualTime = 21;
        mContext = context;
        int alarmTime;

        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String fileName = dt.format(date);

        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        long timeSinceInstall = mSharedPreferences.getLong("InstallTime", 0);
        Date date2 = new Date(System.currentTimeMillis());
        long millis = date2.getTime();
        Log.d(TAG, "onReceive: the timesinceinstall variable is : " + timeSinceInstall);

        Log.d(TAG, "buttonClick: current time is: " + millis);

        //what is this? Is this for the education link? OK it's because nudges dont start for 7 days

        // OK time in Millis is dah, in milliseconds, need to add 1000
        // 6.77 days

        //172800000 = 2 days
        // 585200000 previeus
//
        if(millis - timeSinceInstall < 585200000 ){
            Log.d(TAG, "onReceive: millis - timesniceinstall: " +(millis - timeSinceInstall) );
            Log.d(TAG, "onReceive: skipping first week");
            //return;
        }

        if(millis - timeSinceInstall > 1210000000  ){
            Log.d(TAG, "onReceive: millis - timesniceinstall: " +(millis - timeSinceInstall) );
            Log.d(TAG, "onReceive:  skipping last week");
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean("NudgesStarted", false);
            editor.apply();
            return;
        }

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean("NudgesStarted", true);
        editor.apply();

        //Log.d(TAG, "onReceive: shared prefs sleep time exists: " + mSharedPreferences.contains());
        Calendar cal = Calendar.getInstance();
        int dow = cal.get(Calendar.DAY_OF_WEEK);

        if(isWeekday(dow)){
            sleepTime = mSharedPreferences.getInt("nWInt",0);
            Log.d(TAG, "onReceive: its a weekday");
        }else{
            sleepTime = mSharedPreferences.getInt("nWEInt",0);
            Log.d(TAG, "onReceive: its a weekend");
        }


        if(sleepTime == 0){
            sleepActualTime = 20;
        }else if(sleepTime == 1){
            sleepActualTime = 21;
        }else if(sleepTime == 2){
            sleepActualTime = 22;
        }else if(sleepTime == 3){
            sleepActualTime = 23;
        }else if(sleepTime == 4){
            sleepActualTime = 0;
        }else if(sleepTime == 5){
            sleepActualTime = 1;
        }

        String path2 = (context.getExternalFilesDir(null) + "/Sensors/Nudges/");
        File directory = new File(path2);

        if(!directory.exists()){
            Log.d(TAG, "onCreate: making directory");
            directory.mkdirs();
        }
        if(!directory.exists()){
            Log.d(TAG, "onCreate: directory still not fucking existing, why the fuck not?");

        }

        nudgetext = new File(directory, fileName + ".txt");

        if (!nudgetext.exists()) {
            try {
                nudgetext.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd");

        String currentDate = formatter.format(new Date());
        Log.d(TAG, "onReceive: the current date is: " + currentDate);

        int delay;


        if(Math.random() < 0.25){ // No nudge given
            Log.d(TAG, "onReceive: skipping the nudge");
            writeToFile(nudgetext,currentDate + ", No Nudge given on this date\n" );

        }else{
            // Set alarm for notification for either 1hour or 4 hours before bedtime.
            Log.d(TAG, "onReceive: starting nudgeNotfication");

            if(Math.random() < 0.5){
                Log.d(TAG, "onReceive: delay < 0.5");
                writeToFile(nudgetext, currentDate + ", 1 hour,");
                delay = 1;// * 60 *60 *1000; // delay 1 hour
                if(sleepActualTime == 0){
                    alarmTime = 23;
                }else if(sleepActualTime == 1){
                    alarmTime = 23;
                }else{
                    alarmTime = sleepActualTime -1;
                }

                //TODO if time is after 1am, and delay is 1 hour, we get midnight, which android assumes is prior midnight, not next one!

                Log.d(TAG, "onReceive: alarm time is: " + alarmTime + " and actual sleep time is: " + sleepActualTime + " because sleeptime variable is: " + sleepTime);
            }else{
                Log.d(TAG, "onReceive: delay > 0.5");
                delay = 4;// * 60 * 60 * 1000; //delay 4 hours
                writeToFile(nudgetext, currentDate + ", 4 hour,");
                if(sleepActualTime == 0){
                    alarmTime = 20;
                }else if(sleepActualTime == 1){
                    alarmTime = 21;
                }else{
                    alarmTime = sleepActualTime -4;
                }


               // alarmTime = sleepActualTime - 4;
                Log.d(TAG, "onReceive: alarm time is: " + alarmTime + " and actual sleep time is: " + sleepActualTime + " because sleeptime variable is: " + sleepTime);
            }

            startNudgeNotificationAlarm(alarmTime);


        }
    }



    public void startNudgeNotificationAlarm(int alarmTime) {
        Log.d(TAG, "startNudgeAlarm: in start alarm");

        Calendar cal = Calendar.getInstance();
        long when = cal.getTimeInMillis();

        Log.d("the time is: ", when + " ");

        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, alarmTime);
        //cal.set(Calendar.HOUR_OF_DAY, 23);

        cal.set(Calendar.MINUTE, 00);
        cal.set(Calendar.SECOND,00);
//        cal.set(Calendar.HOUR_OF_DAY, 12);
//        cal.set(Calendar.MINUTE, 56);

        Log.d(TAG, "startNudgeNotificationAlarm: the notification alarm is set fir: " + cal.getTimeInMillis());

        AlarmManager alarmMgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(mContext, NotificationAlarmReceiver.class);
        //statsIntent = PendingIntent.getBroadcast(this, 3, intent, 0);
        notificationIntent = PendingIntent.getBroadcast(mContext, 9, intent, 0);
        //alarmMgr.setExact();

        alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), notificationIntent);


    }





    public static void writeToFile(File file, String data) {

        FileOutputStream stream = null;
        System.out.println("The state of the media is: " + Environment.getExternalStorageState());
        Log.d(TAG, "writeToFile: file location is:" + file.getAbsolutePath());
        Log.d(TAG, "writeToFile: the data being written is:: " + data);



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

    public boolean isWeekday(int dayOfWeek){

        return ((dayOfWeek >= Calendar.MONDAY) && dayOfWeek <= Calendar.FRIDAY);
    }
}
