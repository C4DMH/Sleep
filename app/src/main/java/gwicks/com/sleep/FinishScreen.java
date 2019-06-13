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
import android.widget.TextView;

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
    PendingIntent restartAppIntent;

    private PendingIntent decisionPointIntent;

    private PendingIntent qualtrixNotiOne;
    private PendingIntent qualtrixNotiTwo;

    public static final String secureID = Settings.Secure.getString(
            AnyApplication.getInstance().getContentResolver(), Settings.Secure.ANDROID_ID);

    int eduLinkDay;

    TextView placeholder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_screen);

        Log.d(TAG, "onCreate: the secure device id is: " + secureID);

        // testing calander

        Calendar cal = Calendar.getInstance();
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        Log.d(TAG, "onCreate: before battery");

        placeholder = (TextView)findViewById(R.id.textView5);
        placeholder.setText(getIntent().getStringExtra("message"));

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "onCreate: starting battery optimization");
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

        int dayOfYear = prefs.getInt("doy",1);

        int eduAlarmDay = calculateEduLinkDay(dayOfYear);


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

        startNudgeAlarm();
        qualtrixNotiWeek(morningWeekInt + 6);
        qualtrixNotiWeekend(morningWeekendInt + 6);
        educationNotification(eduAlarmDay);
        startSensorUploadAlarm();
        stopLogging();
        startLogging();
        startLoggingBackup();
        //restartApp();

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
        cal.set(Calendar.HOUR_OF_DAY, 15);
        cal.set(Calendar.MINUTE, 20);

        boolean alarmUp = (PendingIntent.getBroadcast(this, 7,
                new Intent(FinishScreen.this, SensorUploadReceiver.class),
                PendingIntent.FLAG_NO_CREATE) != null);

        Log.d(TAG, "startSensorUpload: boolean alarm up is: " + alarmUp);

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

        boolean alarmUp2 = (PendingIntent.getBroadcast(this, 7,
                new Intent(FinishScreen.this, SensorUploadReceiver.class),
                PendingIntent.FLAG_NO_CREATE) != null);

        Log.d(TAG, "startSensorUpload: boolean alarm2 up is: " + alarmUp2);

    }

    // Pending intent to start logging data - should start the logging every day around the time they wake up

    public void startLogging(){

        Log.d(TAG, "start Logging alarm");

        Calendar cal = Calendar.getInstance();
        long when = cal.getTimeInMillis();

        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, 20);
        cal.set(Calendar.MINUTE, 30);
        cal.set(Calendar.SECOND, 00);

        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, StartLogging.class);
        startLoggingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, startLoggingIntent);

    }

    public void restartApp(){

        Log.d(TAG, "start restart app alarm");

        Calendar cal = Calendar.getInstance();
        long when = cal.getTimeInMillis();

        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, 11);
        cal.set(Calendar.MINUTE, 27);
        cal.set(Calendar.SECOND, 00);

        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, RestartApp.class);
        restartAppIntent = PendingIntent.getBroadcast(this, 22, intent, 0);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, restartAppIntent);

    }


    public void startLoggingBackup(){

        Log.d(TAG, "start backup Logging alarm");

        Calendar cal = Calendar.getInstance();
        long when = cal.getTimeInMillis();
        int currentHour = cal.get(Calendar.HOUR_OF_DAY);
        Log.d(TAG, "stopLogging: current hour: " + currentHour);

        Log.d(TAG, "startLoggingBackup: must be before 9am or after 9pm");
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 30);
        cal.set(Calendar.SECOND, 00);

        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, StartLoggingBackup.class);
        PendingIntent startBackupPendingIntent = PendingIntent.getBroadcast(this, 99, intent, 0);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 1000*60*60 , startBackupPendingIntent);

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


        boolean alarmUp = (PendingIntent.getBroadcast(this, 3,
                new Intent(FinishScreen.this, DecisionPointAlarmReceiver.class),
                PendingIntent.FLAG_NO_CREATE) != null);

        Log.d(TAG, "startNudgeAlarm: boolean alarm up is: " + alarmUp);

        if(alarmUp){
            return;
        }

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

        boolean alarmUp2 = (PendingIntent.getBroadcast(this, 3,
                new Intent(FinishScreen.this, DecisionPointAlarmReceiver.class),
                PendingIntent.FLAG_NO_CREATE) != null);

        Log.d(TAG, "startNudge: boolean alarm2 up is: " + alarmUp2);

    }


    // Alarm to push Qualtrix link 1

    public void qualtrixNotiWeek(int time){

        Log.d(TAG, "qualtrixNotiWeek: ");

        boolean alarmUp = (PendingIntent.getBroadcast(this, 4,
                new Intent(FinishScreen.this, QualtrixNotiOneReceiver.class),
                PendingIntent.FLAG_NO_CREATE) != null);

        Log.d(TAG, "qualtrix one: boolean alarm up is: " + alarmUp);

        if(alarmUp){
            return;
        }

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

        boolean alarmUp2 = (PendingIntent.getBroadcast(this, 4,
                new Intent(FinishScreen.this, QualtrixNotiOneReceiver.class),
                PendingIntent.FLAG_NO_CREATE) != null);

        Log.d(TAG, "qualtrix one: boolean alarm 2 up is: " + alarmUp2);


    }


    public void qualtrixNotiWeekend(int time){

        Log.d(TAG, "qualtrixNotiWeekend: ");
        boolean alarmUp = (PendingIntent.getBroadcast(this, 5,
                new Intent(FinishScreen.this, QualtrixNotiTwoReceiver.class),
                PendingIntent.FLAG_NO_CREATE) != null);

        Log.d(TAG, "squaltrix two: boolean alarm up is: " + alarmUp);

        if(alarmUp){
            return;
        }

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

        boolean alarmUp2 = (PendingIntent.getBroadcast(this, 5,
                new Intent(FinishScreen.this, QualtrixNotiTwoReceiver.class),
                PendingIntent.FLAG_NO_CREATE) != null);

        Log.d(TAG, "squaltrix two: boolean alarm 2 up is: " + alarmUp2);


    }

    public void educationNotification(int doy){

        Log.d(TAG, "starting edu alarm: ");

        boolean alarmUp = (PendingIntent.getBroadcast(this, 6,
                new Intent(FinishScreen.this, EducationNotificationReceiver.class),
                PendingIntent.FLAG_NO_CREATE) != null);

        Log.d(TAG, "Educations: boolean alarm up is: " + alarmUp);

        if(alarmUp){
            return;
        }

        Calendar cal = Calendar.getInstance();
        long when = cal.getTimeInMillis();
        Log.d("the time is: ", when + " ");
        //cal.setTimeInMillis(System.currentTimeMillis());
//        int day = cal.get(Calendar.DAY_OF_WEEK);
//        cal.set(Calendar.DAY_OF_WEEK, day);
//        cal.set(Calendar.HOUR_OF_DAY,18);
//        cal.set(Calendar.MINUTE, 30);
//        cal.set(Calendar.SECOND, 00);


        cal.set(Calendar.DAY_OF_YEAR, doy);
        cal.set(Calendar.HOUR_OF_DAY,18);
        cal.set(Calendar.MINUTE, 30);
        cal.set(Calendar.SECOND, 00);


        long calSet = cal.getTimeInMillis();
        Log.d(TAG, "educationNotification: day of year is: " + doy);
        Log.d(TAG, "educationNotification: calset is: " + calSet);
        //long oneDay = AlarmManager.INTERVAL_DAY;
        //int noOfDays = 5;
       // Log.d(TAG, "educationNotification: + oneDay * numberof Days: " + oneDay*noOfDays);
        ///long reminderTime = calSet + (noOfDays * oneDay);
        ///Log.d(TAG, "educationNotification: cal is : " + reminderTime);
        AlarmManager alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, EducationNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 6, intent, 0);
//        alarmMgr.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, calSet, pendingIntent);

        boolean alarmUp2 = (PendingIntent.getBroadcast(this, 6,
                new Intent(FinishScreen.this, EducationNotificationReceiver.class),
                PendingIntent.FLAG_NO_CREATE) != null);

        Log.d(TAG, "Educations: boolean alarm 2 up is: " + alarmUp2);

    }


    public boolean isWeekday(int dayOfWeek){

        return ((dayOfWeek >= Calendar.MONDAY) && dayOfWeek <= Calendar.FRIDAY);
    }



    public int calculateEduLinkDay(int doy){

        int i;
        if(doy <= 359){
            i = doy +5;
        }else{
            i = (doy - 365) + 5;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy: on destory called");
    }
}
