package gwicks.com.sleep;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.Calendar;

public class FinishScreen extends AppCompatActivity {

    private static final String TAG = "FinishScreen";

    private SharedPreferences prefs;

    String morningWeek;
    String nightWeek;
    String morningWeekend;
    String nightWeekend;

    private PendingIntent sensorUploadIntent;
    private PendingIntent startSensorIntent;
    private PendingIntent stopSensorIntent;

    Intent sensors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_screen);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        morningWeek = prefs.getString("mW",null);
        nightWeek = prefs.getString("nW",null);
        morningWeekend = prefs.getString("mWE", null);
        nightWeekend = prefs.getString("nWE", null);

        Log.d(TAG, "onCreate: " + morningWeek + " , " + nightWeek + " , " + morningWeekend + " , " + nightWeekend);


        // Start the sensors
        // TODO: Need to have the sensors only come on at the right time, instead of all day

//        sensors = new Intent(this, AccGryLgt.class);
//        startService(sensors);

        sensors = new Intent(this, AccGryLgt.class);
        startService(sensors);

        //startSensorLoggingAlarm();
       // setStopSensorIntent();
        //startSensorUploadAlarm();



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
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 53);

        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, SensorUploadReceiver.class);
        //statsIntent = PendingIntent.getBroadcast(this, 3, intent, 0);
        sensorUploadIntent = PendingIntent.getBroadcast(this, 7, intent, 0);
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sensorUploadIntent);

    }

    // Pending intent to start logging data - should start the logging every day around the time they wake up

    public void startSensorLoggingAlarm(){
        Log.d(TAG, "startSensorLoggingAlarm: starteing");

        Calendar cal = Calendar.getInstance();
        long when = cal.getTimeInMillis();

        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, 15);
        cal.set(Calendar.MINUTE, 35);

//        Intent stopSensors = new Intent(this, StopServiceReceiver.class);
//        PendingIntent stop = PendingIntent.getBroadcast(this, 1, stopSensors, 0);

        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        sensors = new Intent(this, AccGryLgt.class);
        startService(sensors);
        stopSensorIntent = PendingIntent.getService(this, 1, sensors, 0);
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, stopSensorIntent);

    }

    // Pending intent to stop the data logging, should occur after they have gone to bed
    // TODO : is this really necessary? I should just leave logging on all the time?

    public void setStopSensorIntent(){
        Log.d(TAG, "setStopSensorIntent: stopping");

        Calendar cal = Calendar.getInstance();
        long when = cal.getTimeInMillis();

        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, 15);
        cal.set(Calendar.MINUTE, 38);

        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        sensors = new Intent(this, AccGryLgt.class);
        startSensorIntent = PendingIntent.getService(this, 1, sensors, 0);
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, startSensorIntent);

    }

    public void stopSensorLoggingAlarm(){

        stopService(sensors);
    }

    public static class StopServiceReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: in recevier");
            Intent service = new Intent(context, AccGryLgt.class);
            context.stopService(service);

        }
    }
}
