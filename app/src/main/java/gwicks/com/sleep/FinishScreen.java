package gwicks.com.sleep;

import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

    int notiTimeWeekday;
    int notiTimeWeekend;

    int eduTime;

    private PendingIntent sensorUploadIntent;
    PendingIntent startLoggingIntent;
    PendingIntent stopLoggingIntent;

    private PendingIntent decisionPointIntent;

    private PendingIntent qualtrixNotiOne;
    private PendingIntent qualtrixNotiTwo;

    public static final String secureID = Settings.Secure.getString(
            AnyApplication.getInstance().getContentResolver(), Settings.Secure.ANDROID_ID);


    //Intent sensors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_screen);


        Log.d(TAG, "onCreate: the secure device id is: " + secureID);

        // testing calander

        Calendar cal = Calendar.getInstance();
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        //int timeToDoEducationLink

        Log.d(TAG, "onCreate: calander day of week:" + cal.get(Calendar.DAY_OF_WEEK));

        Log.d(TAG, "onCreate: is a weekday: " + isWeekday(dow));

        Log.d(TAG, "onCreate: the date is: " + cal.getTime());
        Log.d(TAG, "onCreate: the date2 is " + Calendar.getInstance().getTime());
        Log.d(TAG, "onCreate: timezone is: " + TimeZone.getDefault());


        eduTime = calculateEduLinkDay(dow);


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






        long timeSinceInstall = prefs.getLong("InstallTime", 0);

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
        // TODO: Need to have the sensors only come on at the right time, instead of all day

//        sensors = new Intent(this, AccGryLgt.class);
//        startService(sensors);

        //Context context = AnyApplication.getInstance();
        //Adding to try and keep services running in background


//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
//
//            Intent sensors = new Intent(this, AccGryLgt.class);
//            startService(sensors);
//        }
//        else{
//            Intent sensors = new Intent(this, AccGryLgt.class);
//            startForegroundService(sensors);
//        }

        startLogging();
        stopLogging();

        startNudgeAlarm();
        qualtrixNotiWeek(morningWeekInt + 6);
        qualtrixNotiWeekend(morningWeekendInt + 6);
        educationNotification(eduTime);
        //startEducationLink();

        //startSensorLoggingAlarm();
        // setStopSensorIntent();
        startSensorUploadAlarm();



        boolean startedBefore = prefs.getBoolean("Finish",false);

        Log.d(TAG, "onCreate: started before finish : " + startedBefore);

        if(startedBefore){
            Log.d(TAG, "onCreate: skipping");
            return;
        }


        launchSendEmailDialog();



        editor.putBoolean("Finish", true);

        editor.apply();
        boolean startedBefore2 = prefs.getBoolean("Finish",false);

        Log.d(TAG, "onCreate: started before2 finish : " + startedBefore2);


    }

    // Pending intent to upload all the data collected to AWS, which should occur once a day just before midnight

    public void startSensorUploadAlarm() {
        Log.d(TAG, "sensor upload in start alarm");

        Calendar cal = Calendar.getInstance();
        long when = cal.getTimeInMillis();
        String timey = Long.toString(when);

        //System.out.println("The time changed into nice format is: " + theTime);

        Log.d("the time is: ", when + " ");

        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 53);

        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, SensorUploadReceiver.class);
        //statsIntent = PendingIntent.getBroadcast(this, 3, intent, 0);
        sensorUploadIntent = PendingIntent.getBroadcast(this, 7, intent, 0);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sensorUploadIntent);

    }

    // Pending intent to start logging data - should start the logging every day around the time they wake up

    // TODO Once per day decision point: 75/25 Nudge or Don't Nudge
    // TODO 50/50 One hour or four hours before reported Bedtime
    // TODO 50/50 Content
    // TODO record whether they get nudge or not, what nudge etc
    // TODO Push a link to Qualtrix, one link before nudges start, another link after nudges start ( 1 for pre, 4 for post Nudge start)
    //

    public void startLogging(){

        Log.d(TAG, "start Logging alarm");

        Calendar cal = Calendar.getInstance();
        long when = cal.getTimeInMillis();
        String timey = Long.toString(when);

        //System.out.println("The time changed into nice format is: " + theTime);

        Log.d("the time is: ", when + " ");

        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, 20);
        cal.set(Calendar.MINUTE, 30);
        cal.set(Calendar.SECOND, 00);

        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, StartLogging.class);
        //statsIntent = PendingIntent.getBroadcast(this, 3, intent, 0);
        startLoggingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, startLoggingIntent);


    }

    public void stopLogging(){

        Log.d(TAG, "stop Logging alarm");

        Calendar cal = Calendar.getInstance();
        long when = cal.getTimeInMillis();
        String timey = Long.toString(when);

        //System.out.println("The time changed into nice format is: " + theTime);

        Log.d("the time is: ", when + " ");

        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 40);
        cal.set(Calendar.SECOND,00);

        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, StopLogging.class);
        //statsIntent = PendingIntent.getBroadcast(this, 3, intent, 0);
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
        ;

        Log.d("the time is: ", when + " ");

        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 05);
        cal.set(Calendar.SECOND, 00);
//        cal.set(Calendar.HOUR_OF_DAY, 12);
//        cal.set(Calendar.MINUTE, 56);

        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, DecisionPointAlarmReceiver.class);
        //statsIntent = PendingIntent.getBroadcast(this, 3, intent, 0);
        decisionPointIntent = PendingIntent.getBroadcast(this, 1, intent, 0);
        //alarmMgr.setExact();

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

        AlarmManager alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, QualtrixNotiOneReceiver.class);

        qualtrixNotiOne = PendingIntent.getBroadcast(this, 2, intent, 0);
        //alarmMgr.setExact();

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
        qualtrixNotiTwo = PendingIntent.getBroadcast(this, 3, intent, 0);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),AlarmManager.INTERVAL_DAY, qualtrixNotiTwo);
    }

    public void educationNotification(int dow){

        Log.d(TAG, "starting edu alarm: ");
        Log.d(TAG, "educationNotification: the dow is: " + dow);
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
        Log.d(TAG, "educationNotification: calset is: " + calSet);
        long oneDay = AlarmManager.INTERVAL_DAY;
        int noOfDays = 3;
        Log.d(TAG, "educationNotification: + oneDay * numberof Days: " + oneDay*noOfDays);
        long reminderTime = calSet + (noOfDays * oneDay);
        Log.d(TAG, "educationNotification: cal is : " + reminderTime);
        AlarmManager alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, EducationNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 4, intent, 0);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);

    }


    public boolean isWeekday(int dayOfWeek){

        return ((dayOfWeek >= Calendar.MONDAY) && dayOfWeek <= Calendar.FRIDAY);
    }


// This is for 5 day
//    public int calculateEduLinkDay(int dow){
//
//        int i;
//        if(dow <= 2){
//            i = dow +5;
//        }else{
//            i = (dow + 5) - 7;
//        }
//
//        return i;
//    }

    // This is for edu link to be displayed in 3 days

    public int calculateEduLinkDay(int dow){

        int i;
        if(dow <= 4){
            i = dow +3;
        }else{
            i = (dow + 3) - 7;
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
