package gwicks.com.sleep;

import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by gwicks on 20/09/2018.
 *
 * The final activity. A shit load of logic is done here, all the alarms are set etc
 */

public class FinishScreen extends AppCompatActivity {

    private static final String TAG = "FinishScreen";

    private SharedPreferences prefs;

    String morningWeek;
    int morningWeekInt;
    String nightWeek;
    int nightWeekInt;
    String morningWeekend;
    int morningWeekendInt;
    String nightWeekend;
    int nightWeekendInt;

    private PendingIntent sensorUploadIntent;
    PendingIntent startLoggingIntent;
    PendingIntent stopLoggingIntent;

    private PendingIntent decisionPointIntent;

    private PendingIntent qualtrixNotiOne;
    private PendingIntent qualtrixNotiTwo;

    public static final String secureID = Settings.Secure.getString(
            AnyApplication.getInstance().getContentResolver(), Settings.Secure.ANDROID_ID);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_screen);

        Log.d(TAG, "onCreate: the secure device id is: " + secureID);

        // testing calander

        Calendar cal = Calendar.getInstance();
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        Log.d(TAG, "onCreate: before battery");

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }

        Log.d(TAG, "onCreate: after battery");

        //int timeToDoEducationLink

        Log.d(TAG, "onCreate: calander day of week:" + cal.get(Calendar.DAY_OF_WEEK));

        Log.d(TAG, "onCreate: is a weekday: " + isWeekday(dow));
        Log.d(TAG, "onCreate: dow is: " + dow);

        Log.d(TAG, "onCreate: the date is: " + cal.getTime());
        Log.d(TAG, "onCreate: the date2 is " + Calendar.getInstance().getTime());
        Log.d(TAG, "onCreate: timezone is: " + TimeZone.getDefault());


        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
//
//        boolean startedBefore = prefs.getBoolean("Finish",false);
//
//        Log.d(TAG, "onCreate: started before finish : " + startedBefore);
//
//        if(startedBefore){
//            Log.d(TAG, "onCreate: skipping");
//            return;
//        }
//
//
//        launchSendEmailDialog();


        morningWeek = prefs.getString("mW", null);
        Log.d(TAG, "onCreate: morning week is: " + morningWeek);
        morningWeekInt = prefs.getInt("mWInt",0);
        Log.d(TAG, "onCreate: moring weekInt is: " + morningWeekInt);

        nightWeek = prefs.getString("nW", null);
        Log.d(TAG, "onCreate: nightweek is: " + nightWeek);
        nightWeekInt = prefs.getInt("nWInt",0);
        Log.d(TAG, "onCreate: nightweek int is: " + nightWeekInt);

        morningWeekend = prefs.getString("mWE", null);
        morningWeekendInt = prefs.getInt("mWEInt", 0);
        Log.d(TAG, "onCreate: morning weekend int is: " + morningWeekendInt);

        nightWeekend = prefs.getString("nWE", null);
        nightWeekendInt = prefs.getInt("nWEInt", 0);

        Log.d(TAG, "onCreate: night weekend int is: " + nightWeekendInt);

        Log.d(TAG, "onCreate: " + morningWeek + " , " + nightWeek + " , " + morningWeekend + " , " + nightWeekend + "the nightweek int is: " + nightWeekInt );


        // Start the sensors

        startLogging();

        //prevent logging on install

//        try {
//
//            //sleep 5 seconds
//            Thread.sleep(5000);
//
//            System.out.println("Testing..." );
//
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }






        startNudgeAlarm();
        qualtrixNotiWeek(morningWeekInt + 6);
        qualtrixNotiWeekend(morningWeekendInt + 6);
        educationNotification();
        startSensorUploadAlarm();

        stopLogging();

        boolean startedBefore = prefs.getBoolean("Finish",false);

        Log.d(TAG, "onCreate: started before finish : " + startedBefore);

        if(startedBefore){
            Log.d(TAG, "onCreate: skipping");
            return;
        }

        launchSendEmailDialog();

        editor.putBoolean("Finish", true);
        editor.apply();

    }

    // Pending intent to upload all the data collected to AWS, which should occur once a day just before midnight

    public void startSensorUploadAlarm() {
        Log.d(TAG, "sensor upload in start alarm");

        Calendar cal = Calendar.getInstance();
        long when = cal.getTimeInMillis();

        Log.d("the time is: ", when + " ");

        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, 11);
        cal.set(Calendar.MINUTE, 00);

        // This will prevent premature firing

//        Calendar now = Calendar.getInstance();
//        now.setTimeInMillis(System.currentTimeMillis());
//        if(cal.before(now)){
//            cal.add(Calendar.DAY_OF_MONTH,1);
//        }

        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, SensorUploadReceiver.class);
        sensorUploadIntent = PendingIntent.getBroadcast(this, 7, intent, 0);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sensorUploadIntent);

    }

    // Pending intent to start logging data - should start the logging every day around the time they wake up

    public void startLogging(){

        Log.d(TAG, "start Logging alarm");

        Calendar cal = Calendar.getInstance();
        long when = cal.getTimeInMillis();
        Log.d("the time is: ", when + " ");

        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, 20);
        cal.set(Calendar.MINUTE, 30);
        cal.set(Calendar.SECOND, 00);

        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, StartLogging.class);
        startLoggingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, startLoggingIntent);


    }

    public void stopLogging(){

        Log.d(TAG, "stop Logging alarm");

        Calendar cal = Calendar.getInstance();
        long when = cal.getTimeInMillis();
        Log.d("the time is: ", when + " ");

        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 40);
        cal.set(Calendar.SECOND,00);

        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, StopLogging.class);
        stopLoggingIntent = PendingIntent.getBroadcast(this, 2, intent, 0);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, stopLoggingIntent);


    }


    public static class StopServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: in recevier");
            Log.d(TAG, "onReceive: stop service receiver called");
            Intent service = new Intent(context, AccGryLgt.class);
            context.stopService(service);
        }
    }

    // The different alarms for the nudge

    public void startNudgeAlarm() {
        Log.d(TAG, "startNudgeAlarm: in start alarm");

        Calendar cal = Calendar.getInstance();
        long when = cal.getTimeInMillis();
        Log.d("the time is: ", when + " ");

        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 05);
        cal.set(Calendar.SECOND, 00);
//        cal.set(Calendar.HOUR_OF_DAY, 12);
//        cal.set(Calendar.MINUTE, 56);

        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, DecisionPointAlarmReceiver.class);
        decisionPointIntent = PendingIntent.getBroadcast(this, 3, intent, 0);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, decisionPointIntent);
    }


    // Alarm to push Qualtrix link 1

    public void qualtrixNotiWeek(int time){

        Log.d(TAG, "qualtrixNotiWeek: ");

        Calendar cal = Calendar.getInstance();
        long when = cal.getTimeInMillis();
        Log.d("the time is: ", when + " ");
        cal.setTimeInMillis(System.currentTimeMillis());

        cal.set(Calendar.HOUR_OF_DAY, time);
        cal.set(Calendar.MINUTE, 30);
        cal.set(Calendar.SECOND, 00);

        // This will prevent premature firing

//        Calendar now = Calendar.getInstance();
//        now.setTimeInMillis(System.currentTimeMillis());
//        if(cal.before(now)){
//            Log.d(TAG, "qualtrixNotiWeek: skipping today");
//            cal.add(Calendar.DAY_OF_MONTH,1);
//            Log.d(TAG, "qualtrixNotiWeek: qualtrix alarm set repeating for " + cal.getTimeInMillis());
//        }

        AlarmManager alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, QualtrixNotiOneReceiver.class);
        qualtrixNotiOne = PendingIntent.getBroadcast(this, 4, intent, 0);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),AlarmManager.INTERVAL_DAY, qualtrixNotiOne);
    }


    public void qualtrixNotiWeekend(int time){

        Log.d(TAG, "qualtrixNotiWeekend: ");
        Calendar cal = Calendar.getInstance();
        long when = cal.getTimeInMillis();
        Log.d("the time is: ", when + " ");
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, time);
        cal.set(Calendar.MINUTE, 30);
        cal.set(Calendar.SECOND, 00);
        AlarmManager alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, QualtrixNotiTwoReceiver.class);
        qualtrixNotiTwo = PendingIntent.getBroadcast(this, 5, intent, 0);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),AlarmManager.INTERVAL_DAY, qualtrixNotiTwo);
    }

    public void educationNotification(){

        Log.d(TAG, "starting edu alarm: ");

        Calendar cal = Calendar.getInstance();
        long when = cal.getTimeInMillis();
        Log.d("the time is: ", when + " ");
        //cal.setTimeInMillis(System.currentTimeMillis());
        int day = cal.get(Calendar.DAY_OF_WEEK);
        cal.set(Calendar.DAY_OF_WEEK, day);
        cal.set(Calendar.HOUR_OF_DAY,18);
        cal.set(Calendar.MINUTE, 30);
        cal.set(Calendar.SECOND, 00);
        long calSet = cal.getTimeInMillis();
        Log.d(TAG, "educationNotification: day of week is: " + day);
        Log.d(TAG, "educationNotification: calset is: " + calSet);
        long oneDay = AlarmManager.INTERVAL_DAY;
        int noOfDays = 5;
        Log.d(TAG, "educationNotification: + oneDay * numberof Days: " + oneDay*noOfDays);
        long reminderTime = calSet + (noOfDays * oneDay);
        Log.d(TAG, "educationNotification: cal is : " + reminderTime);
        AlarmManager alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, EducationNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 6, intent, 0);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);

    }


    public boolean isWeekday(int dayOfWeek){

        return ((dayOfWeek >= Calendar.MONDAY) && dayOfWeek <= Calendar.FRIDAY);
    }



    public int calculateEduLinkDay(int dow){

        int i;
        if(dow <= 6){
            i = dow +1;
        }else{
            i = (dow + 1) - 1;
        }
        Log.d(TAG, "calculateEduLinkDay: so calculated dow is: " + i);

        return i;
    }

    public void launchSendEmailDialog(){
        DialogFragment newFragment = new EmailSecureDeviceID();
        newFragment.setCancelable(false);

        newFragment.show(getFragmentManager(), "email");
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }


}
