package gwicks.com.sleep;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by gwicks on 9/07/2018.
 *
 * Basic class to capture power plugged and un-plugged events and record to text file
 */
public class PowerConnectionReceiver extends BroadcastReceiver {

    private static final String TAG = "PowerConnectionReceiver";

    @Override

    public void onReceive(Context context , Intent intent) {
        String action = intent.getAction();

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("ddMMyyyy_HHmmssSSS");
        SimpleDateFormat dateOnly = new SimpleDateFormat("ddMMyyyy");
        String theTime = df.format(cal.getTime());
        String theDate = dateOnly.format(cal.getTime());
//        long when = cal.getTimeInMillis();
//        String timey = Long.toString(when);
//        String theTime = convertDate(timey, "dd/MM/yyyy hh:mm:ss");

        String path = context.getExternalFilesDir(null) + "/Sensors/CHARGING/";
        File directory = new File(path);
        if(!directory.exists()){
            Log.d(TAG, "onStartJob: making directory");
            directory.mkdirs();
        }

        File location = new File(directory, theDate +".txt");


        //Toast.makeText(context, "The time is nice format is: " + theTime, Toast.LENGTH_LONG ).show();

        if(action.equals(Intent.ACTION_POWER_CONNECTED)) {
            // Do something when power connected
            writeToFile(location, "TIME," + theTime + ",CHARGING STATUS, Charging\n");
        }
        else if(action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
            // Do something when power disconnected
            writeToFile(location, "TIME," + theTime + ",CHARGING STATUS, Disconnected\n");

        }
    }

    private static void writeToFile(File file, String data) {

        FileOutputStream stream = null;
        Log.d(TAG, "writeToFile: file location is:" + file.getAbsolutePath());

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

