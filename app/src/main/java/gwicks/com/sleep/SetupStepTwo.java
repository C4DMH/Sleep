package gwicks.com.sleep;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Created by gwicks on 20/09/2018.
 *
 * First / Launcher activity. takes input from user as to their weekday and weekend wake
 * and sleep times, stores in shared preferences.
 */

public class SetupStepTwo extends AppCompatActivity {

    Spinner morningSpinnerWeek;
    Spinner morningSpinnerWeekend;
    Spinner nightSpinnerWeek;
    Spinner nightSpinnerWeekend;

    private SharedPreferences prefs;

    private static final String TAG = "SetupStepTwo";
    public static final int REQUEST_WRITE_STORAGE_REQUEST_CODE = 101;
    TransferUtility mTransferUtility;

    String model;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //requestAppPermissions();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean startedBefore = prefs.getBoolean("Finish",false);
        Log.d(TAG, "onCreate: started before : " + startedBefore);
        if(startedBefore){
            Log.d(TAG, "onCreate: skipping");
            Intent finishIntent = new Intent(SetupStepTwo.this, FinishScreen.class);
            SetupStepTwo.this.startActivity(finishIntent);

        }

        Log.d(TAG, "onCreate: ");
        mTransferUtility = Util.getTransferUtility(this);





        morningSpinnerWeek = (Spinner)findViewById(R.id.spinner);

        ArrayAdapter<CharSequence> adapterMorning = ArrayAdapter.createFromResource(this,R.array.wake_time, android.R.layout.simple_spinner_item);
        adapterMorning.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        morningSpinnerWeek.setAdapter(adapterMorning);




        morningSpinnerWeekend = (Spinner)findViewById(R.id.spinner3);

        ArrayAdapter<CharSequence> adapterMorningWeekend = ArrayAdapter.createFromResource(this,R.array.wake_time, android.R.layout.simple_spinner_item);
        adapterMorning.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        morningSpinnerWeekend.setAdapter(adapterMorningWeekend);


        nightSpinnerWeek = (Spinner)findViewById(R.id.spinner2);

        ArrayAdapter<CharSequence> adapterNight = ArrayAdapter.createFromResource(this,R.array.sleep_time, android.R.layout.simple_spinner_item);
        adapterMorning.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nightSpinnerWeek.setAdapter(adapterNight);


        nightSpinnerWeekend = (Spinner)findViewById(R.id.spinner4);

        ArrayAdapter<CharSequence> adapterNightWeekend = ArrayAdapter.createFromResource(this,R.array.sleep_time, android.R.layout.simple_spinner_item);
        adapterMorning.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nightSpinnerWeekend.setAdapter(adapterNightWeekend);

    }


    public void buttonClick(View view){

        model = "";

        try{
            model = android.os.Build.MODEL;
        }catch(Exception e){
            Log.d(TAG, "buttonClick: caught exception");
        }


        Calendar cal = Calendar.getInstance();
        int doy = cal.get(Calendar.DAY_OF_YEAR);

        Date date = new Date(System.currentTimeMillis());
        long millis = date.getTime();


        Log.d(TAG, "buttonClick: current time is: " + millis);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();

        String path = getExternalFilesDir(null) + "/SleepTimes/";

        File directory = new File(path);

        if(!directory.exists()){
            directory.mkdirs();
        }

        //TODO this should be removed, not sure why the file is writing the data

//        String desination = Environment.getExternalStorageDirectory().getAbsolutePath() + "/sleep/";
//        File destination = new File(desination);
//
//        if(!destination.exists()){
//            destination.mkdirs();
//        }


        editor.putBoolean("NudgesStarted", false);
        editor.putLong("InstallTime", millis);
        editor.putInt("doy", doy);


        String morningWeek = morningSpinnerWeek.getSelectedItem().toString();
        int morningWeekInt = morningSpinnerWeek.getSelectedItemPosition();
        String nightWeek = nightSpinnerWeek.getSelectedItem().toString();
        int nightWeekInt = nightSpinnerWeek.getSelectedItemPosition();
        String morningWeekend = morningSpinnerWeekend.getSelectedItem().toString();
        int morningWeekendInt = morningSpinnerWeekend.getSelectedItemPosition();
        String nightWeekend = nightSpinnerWeekend.getSelectedItem().toString();
        int nightWeekendInt = nightSpinnerWeekend.getSelectedItemPosition();

        editor.putString("mW", morningWeek);
        editor.putInt("mWInt", morningWeekInt);
        editor.putString("nW", nightWeek );
        editor.putInt("nWInt", nightWeekInt);
        editor.putString("mWE", morningWeekend);
        editor.putInt("mWEInt", morningWeekendInt);
        editor.putString("nWE", nightWeekend);
        editor.putInt("nWEInt", nightWeekendInt);

        editor.apply();

        //Toast.makeText(this, "The value is: " + morningWeek,Toast.LENGTH_LONG).show();
        File sleepFile = new File(path+"Sleeptime.txt");
        writeToFile(sleepFile, model + "\n");
        writeToFile(sleepFile, morningWeekInt + "," + nightWeekInt + "," + morningWeekendInt +"," + nightWeekendInt + "\n");
        Util.uploadFileToBucket(sleepFile,"input.txt",false,logUploadCallback, "/Input/");
        Intent finishIntent = new Intent(SetupStepTwo.this, FinishScreen.class);
        finishIntent.putExtra("message", "Thanks, you're done\nPress either Back or Home to exit");
        SetupStepTwo.this.startActivity(finishIntent);

        //TODO Need to strip out all the recurring log.d writes, as a debug build will keep them!!!!!

    }

    public static void writeToFile(File file, String data) {

        FileOutputStream stream = null;

        try {
            stream = new FileOutputStream(file, true);
            stream.write(data.getBytes());
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
        }
    }

    private void requestAppPermissions() {

        Log.d(TAG, "requestAppPermissions: 1");
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        if (hasReadPermissions() && hasWritePermissions()) {
            return;
        }

        Log.d(TAG, "requestAppPermissions: 2");

        ActivityCompat.requestPermissions(this,
                new String[] {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQUEST_WRITE_STORAGE_REQUEST_CODE); // your request code

        Log.d(TAG, "requestAppPermissions: 3");
    }

    private boolean hasReadPermissions() {
        return (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private boolean hasWritePermissions() {
        return (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    final Util.FileTransferCallback logUploadCallback = new Util.FileTransferCallback() {
        @SuppressLint("DefaultLocale")

        private String makeLogLine(final String name, final int id, final TransferState state) {
            Log.d("LogUploadTask", "This is AWSBIT");
            return String.format("%s | ID: %d | State: %s", name, id, state.toString());
        }

        @Override
        public void onCancel(int id, TransferState state) {
            Log.d(TAG, makeLogLine("Callback onCancel()", id, state));
        }

        @Override
        public void onStart(int id, TransferState state) {
            Log.d(TAG, makeLogLine("Callback onStart()", id, state));

        }

        @Override
        public void onComplete(int id, TransferState state) {
            Log.d(TAG, makeLogLine("Callback onComplete()", id, state));
        }

        @Override
        public void onError(int id, Exception e) {
            Log.d(TAG, makeLogLine("Callback onError()", id, TransferState.FAILED), e);
        }
    };
}
