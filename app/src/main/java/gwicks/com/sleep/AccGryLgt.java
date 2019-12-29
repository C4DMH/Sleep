package gwicks.com.sleep;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.List;

import androidx.core.app.NotificationCompat;

/**
 * Created by gwicks on 9/07/2018.
 * This is a service that starts recording the Accelertomter, Gyroscope and Light Sensor
 */

public class AccGryLgt extends Service implements SensorEventListener {

    private static final String TAG = "AccGryLgt";

    private SensorManager sensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private Sensor mLight;
    String path;
    String path2;

    private static long LAST_TS_ACC = 0; // Time stamps to prevent too many readings
    private static long LAST_TS_GYRO = 0;
    private static long LAST_SAVE = 0;

    private static PowerManager.WakeLock wakeLock = null;


    // Buffers to prevent too many writes to file
    StringBuilder accelBuffer;
    StringBuilder gryoBuffer;
    StringBuilder lightBuffer;

    // Boolean which prevents the file from being accessed etc while a write is going on
    private boolean writingAccelToFile = false;
    private boolean writingGyroToFile = false;
    private boolean writingLightToFile = false;

    float previousLightReading;

    private static Float[] LAST_VALUES_ACC = null;
    private static Float[] LAST_VALUES_GRYO = null;

    // Thresholds to prevent too many values being written ( ie a filter for small changes)
    double THRESHOLD = 0.01;
    double ACCEL_THRESHOLD = 0.05;
    float lightReading = 0;
    long timeStampLight = 0;

    File AccelFile;
    File GyroFile;
    File LightFile;
    File DestroyFile;

    private NotificationManager mNotificationManager;
    String CHANNEL_DI = "Blah";

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "onTaskRemoved: ");
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: ");
        return super.onUnbind(intent);
    }

    @Override
    public boolean stopService(Intent name) {
        Log.d(TAG, "stopService: ");
        return super.stopService(name);
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        Log.d(TAG, "unbindService: ");
        super.unbindService(conn);
    }

    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {
        Log.d(TAG, "unregisterReceiver: ");
        super.unregisterReceiver(receiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand: ");

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        
        if( sensorManager == null){
            Log.d(TAG, "onStartCommand: hello");
        }
        
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(mAccelerometer == null){
            Log.d(TAG, "onStartCommand: Acc null");
        }
        mGyroscope =  sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if(mGyroscope== null){
            Log.d(TAG, "onStartCommand: gyro null");
        }
        mLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if(mLight == null){
            Log.d(TAG, "onStartCommand: light null");
        }
        //return super.onStartCommand(intent, flags, startId);
        wakeLock.acquire();

        if(mNotificationManager == null){
            Log.d(TAG, "onReceive: in notification manager = null");
            mNotificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE) ;
            Log.d(TAG, "onReceive: notificiation manager  = " + mNotificationManager);
        }

        // Since Oreo the service must be run as a foreground services with a notification, hence:

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            Log.d(TAG, "onStartCommand: bigger than O");
            NotificationChannel channel = mNotificationManager.getNotificationChannel(CHANNEL_DI);
            if(channel == null){
                channel = new NotificationChannel(CHANNEL_DI, "Oreo", NotificationManager.IMPORTANCE_LOW);

//                channel.enableVibration(true);
//                channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                mNotificationManager.createNotificationChannel(channel);
            }

            Notification mBuilder =
                    new NotificationCompat.Builder(this, CHANNEL_DI)
                            .setSmallIcon(R.drawable.not_icon_clear)
                            .setAutoCancel(true)
                            .setContentTitle("SLEEP")
                            .setContentText("Sleep app is running")
                            .setOngoing(false)
                            .setChannelId(CHANNEL_DI)
                            .build();
            //mNotificationManager.notify("first",1, mBuilder);
            Log.d(TAG, "onStartCommand: notification should be built");
            startForeground(1, mBuilder);
        }

        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : sensors) {
            Log.d("Sensors", "" + sensor.getName());

        }

        if(accelBuffer == null){
            Log.d(TAG, "onStartCommand: accel null, building");
            accelBuffer = new StringBuilder();
        }

        if(gryoBuffer == null){
            Log.d(TAG, "onStartCommand: gyro null, building");
            gryoBuffer = new StringBuilder();
        }
        if(lightBuffer == null){
            Log.d(TAG, "onStartCommand: light null, building");
            lightBuffer = new StringBuilder();
        }

//        accelBuffer = new StringBuilder();
//        gryoBuffer = new StringBuilder();
//        lightBuffer = new StringBuilder();

        if(mAccelerometer != null){
            sensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "onStartCommand: acc listener registered");

        }
        if(mGyroscope != null){
            sensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "onStartCommand: gyro listener registered");

        }
        if(mLight != null){
            sensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "onStartCommand: light listener registed");

        }

//        sensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
//        //sensorManager.registerListener(this, head, SensorManager.SENSOR_DELAY_GAME);
//        sensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
//        sensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: what is this?");
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "AccelGyroLight: starting accelgyrolight constructor");

        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AccGryLgt:Service");

        path2 = (getExternalFilesDir(null) + "/Sensors");
        Log.d(TAG, "AccGryLgt:  the path to externalfilesdir is: " + path2);

        File directory2 = new File(path2);

        if(!directory2.exists()){
            Log.d(TAG, "onCreate: making directory");
            directory2.mkdirs();
        }

        Log.d(TAG, "onCreate: before new transferservice");

        //getApplicationContext().startService(new Intent(getApplicationContext(), TransferService.class));

        Log.d(TAG, "onCreate: after new transferservice");
        
        accelBuffer = new StringBuilder();
        gryoBuffer = new StringBuilder();
        lightBuffer = new StringBuilder();
    }


    @Override
    public void onDestroy() {

        // Write anything in the buffer to file, unregister the listeners, and cancel the wakelock

        Log.d(TAG, "onDestroy: the sensor service has been destroyed");
        writeAccBufferToFile();
        writeGryoBufferToFile();
        writeLightBufferToFile();
        sensorManager.unregisterListener(this);
        wakeLock.release();
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            long TS = System.currentTimeMillis();
            //Log.d(TAG, "onSensorChanged: The time stamp check is:  " + TS +" + " + LAST_TS_ACC );

            // Filter to remove readings that come too often
            if (TS < LAST_TS_ACC + 100) {
                //Log.d(TAG, "onSensorChanged: skipping");
                return;
            }

            // Filter to remove small changes

            if(LAST_VALUES_ACC != null && Math.abs(event.values[0] - LAST_VALUES_ACC[0]) < ACCEL_THRESHOLD
                    && Math.abs(event.values[1] - LAST_VALUES_ACC[1]) < ACCEL_THRESHOLD
                    && Math.abs(event.values[2] - LAST_VALUES_ACC[2]) < ACCEL_THRESHOLD) {
                return;
            }

            LAST_VALUES_ACC = new Float[]{event.values[0], event.values[1], event.values[2]};

            LAST_TS_ACC = System.currentTimeMillis();

            accelBuffer.append(LAST_TS_ACC + "," + event.values[0] + "," + event.values[1] + "," + event.values[2] + "\n");
            //Log.d(TAG, "onSensorChanged: \n the acc buffer length is: " + accelBuffer.length());
            //Log.d(TAG, "onSensorChanged: the buffer is: " + accelBuffer.toString());
            if((accelBuffer.length() > 500000) && (writingAccelToFile == false) ){
                writingAccelToFile = true;

                AccelFile = new File(path2 +"/Acc/"  + LAST_TS_ACC +"_Service.txt");
                Log.d(TAG, "onSensorChanged: accelfile created at : " + AccelFile.getPath());

                File parent = AccelFile.getParentFile();
                if(!parent.exists() && !parent.mkdirs()){
                    throw new IllegalStateException("Couldn't create directory: " + parent);
                }

                //Try threading to take of UI thread

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //Log.d(TAG, "onSensorChanged: in accelbuffer");
                        // Log.d(TAG, "run: in runnable");
                        //writeToStream(accelBuffer);
                        writeStringBuilderToFile(AccelFile, accelBuffer);
                        accelBuffer.setLength(0);
                        writingAccelToFile = false;

                    }
                }).start();

            }
        }

        else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE){

            //Log.d(TAG, "onSensorChanged: gyro");


            long TS = System.currentTimeMillis();
            if (TS < LAST_TS_GYRO + 100) {
                //Log.d(TAG, "onSensorChanged: skipping");
                return;
            }
            // Filter to remove readings that have too small a change from previous reading.
            if(LAST_VALUES_GRYO != null && Math.abs(event.values[0] - LAST_VALUES_GRYO[0]) < THRESHOLD
                    && Math.abs(event.values[1] - LAST_VALUES_GRYO[1]) < THRESHOLD
                    && Math.abs(event.values[2] - LAST_VALUES_GRYO[2]) < THRESHOLD) {
                return;
            }

            LAST_VALUES_GRYO = new Float[]{event.values[0], event.values[1], event.values[2]};


            LAST_TS_GYRO = System.currentTimeMillis();


            gryoBuffer.append(LAST_TS_GYRO + "," + event.values[0] + "," + event.values[1] + "," + event.values[2] + "\n");
            //Log.d(TAG, "onSensorChanged: \n the gryo buffer length is: " + gryoBuffer.length());
            //Log.d(TAG, "onSensorChanged: the buffer is: " + accelBuffer.toString());
            //Log.d(TAG, "onSensorChanged: wrtringgryotofile = " + writingGyroToFile);
            if((gryoBuffer.length() > 500000) && (writingGyroToFile == false) ){
                Log.d(TAG, "onSensorChanged: 1");
                writingGyroToFile = true;

                GyroFile = new File(path2 +"/Gyro/"  + LAST_TS_GYRO +"_Service.txt");
                Log.d(TAG, "onSensorChanged: file created at: "+ GyroFile.getPath());

                File parent = GyroFile.getParentFile();
                if(!parent.exists() && !parent.mkdirs()){
                    throw new IllegalStateException("Couldn't create directory: " + parent);
                }


                //Try threading to take of UI thread

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //Log.d(TAG, "onSensorChanged: in accelbuffer");
                        //Log.d(TAG, "run: in runnable");
                        //writeToStream(accelBuffer);
                        writeStringBuilderToFile(GyroFile, gryoBuffer);
                        gryoBuffer.setLength(0);
                        writingGyroToFile = false;

                    }
                }).start();

            }
            //Log.d(TAG, "onSensorChanged: after buffer length check");

        }

        else if(event.sensor.getType() == Sensor.TYPE_LIGHT){

            //Log.d(TAG, "onSensorChanged: light");
            long lightTime = System.currentTimeMillis();

            lightReading = event.values[0];
            //Log.d(TAG, "onSensorChanged: \n the buffer length is: " + lightBuffer.length());

            if(lightReading > (previousLightReading + 3) || lightReading < (previousLightReading -3)){
                timeStampLight = System.currentTimeMillis();
                lightBuffer.append(lightTime + "," + event.values[0] + "\n");
                Log.d(TAG, "onSensorChanged: appending to light buffer");
                Log.d(TAG, "onSensorChanged: the light buffer length is: " + lightBuffer.length());
                previousLightReading = lightReading;
            }


            if((lightBuffer.length() > 50000) && (writingLightToFile == false) ){

                timeStampLight = System.currentTimeMillis();
                LightFile = new File(path2 +"/Light/"  + timeStampLight +"_Service.txt");
                Log.d(TAG, "onSensorChanged: ligtfile created at: " + LightFile.getPath());
                File parent = LightFile.getParentFile();
                if(!parent.exists() && !parent.mkdirs()){
                    throw new IllegalStateException("Couldn't create directory: " + parent);
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //Log.d(TAG, "onSensorChanged: in accelbuffer");
                        //Log.d(TAG, "run: in runnable");
                        //writeToStream(accelBuffer);
                        writeStringBuilderToFile(LightFile, lightBuffer);
                        lightBuffer.setLength(0);
                        writingLightToFile = false;

                    }
                }).start();

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void writeStringBuilderToFile(File file, StringBuilder builder){
        Log.d(TAG, "writeStringBuilderToFile: in stringbuilder to file");
        BufferedWriter writer = null;


        try {
            writer = new BufferedWriter(new java.io.FileWriter((file)));
            Log.d(TAG, "writeStringBuilderToFile: writiting");
            writer.append(builder);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(writer != null){
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // Write what is left in buffer to file before onDestroy called.

    public void writeAccBufferToFile(){

        writingAccelToFile = true;

        AccelFile = new File(path2 +"/Acc/"  + LAST_TS_ACC +"_Service.txt");
        Log.d(TAG, "onSensorChanged: accelfile created at : " + AccelFile.getPath());

        File parent = AccelFile.getParentFile();
        if(!parent.exists() && !parent.mkdirs()){
            throw new IllegalStateException("Couldn't create directory: " + parent);
        }

        //Try threading to take of UI thread

        new Thread(new Runnable() {
            @Override
            public void run() {
                //Log.d(TAG, "onSensorChanged: in accelbuffer");
                // Log.d(TAG, "run: in runnable");
                //writeToStream(accelBuffer);
                writeStringBuilderToFile(AccelFile, accelBuffer);
                accelBuffer.setLength(0);
                writingAccelToFile = false;

            }
        }).start();

    }

    public void writeGryoBufferToFile(){

        writingGyroToFile = true;

        GyroFile = new File(path2 +"/Gyro/"  + LAST_TS_GYRO +"_Service.txt");
        Log.d(TAG, "onSensorChanged: file created at: "+ GyroFile.getPath());

        File parent = GyroFile.getParentFile();
        if(!parent.exists() && !parent.mkdirs()){
            throw new IllegalStateException("Couldn't create directory: " + parent);
        }


        //Try threading to take of UI thread

        new Thread(new Runnable() {
            @Override
            public void run() {
                //Log.d(TAG, "onSensorChanged: in accelbuffer");
                //Log.d(TAG, "run: in runnable");
                //writeToStream(accelBuffer);
                writeStringBuilderToFile(GyroFile, gryoBuffer);
                gryoBuffer.setLength(0);
                writingGyroToFile = false;

            }
        }).start();

    }

    public void writeLightBufferToFile(){

        timeStampLight = System.currentTimeMillis();
        LightFile = new File(path2 +"/Light/"  + timeStampLight +"_Service.txt");
        Log.d(TAG, "onSensorChanged: ligtfile created at: " + LightFile.getPath());
        File parent = LightFile.getParentFile();
        if(!parent.exists() && !parent.mkdirs()){
            throw new IllegalStateException("Couldn't create directory: " + parent);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Log.d(TAG, "onSensorChanged: in accelbuffer");
                //Log.d(TAG, "run: in runnable");
                //writeToStream(accelBuffer);
                writeStringBuilderToFile(LightFile, lightBuffer);
                lightBuffer.setLength(0);
                writingLightToFile = false;

            }
        }).start();

    }

}
