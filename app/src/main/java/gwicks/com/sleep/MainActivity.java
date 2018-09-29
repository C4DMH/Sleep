package gwicks.com.sleep;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    Spinner morningSpinnerWeek;
    Spinner morningSpinnerWeekend;
    Spinner nightSpinnerWeek;
    Spinner nightSpinnerWeekend;

    private SharedPreferences prefs;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean startedBefore = prefs.getBoolean("Finish",false);
        Log.d(TAG, "onCreate: started before : " + startedBefore);
        if(startedBefore){
            Log.d(TAG, "onCreate: skipping");
            Intent finishIntent = new Intent(MainActivity.this, FinishScreen.class);
            MainActivity.this.startActivity(finishIntent);

        }

        Log.d(TAG, "onCreate: ");





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

        Date date = new Date(System.currentTimeMillis());
        long millis = date.getTime();

        Log.d(TAG, "buttonClick: current time is: " + millis);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();


        editor.putBoolean("NudgesStarted", false);
        editor.putLong("InstallTime", millis);


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

        Intent finishIntent = new Intent(MainActivity.this, FinishScreen.class);
        MainActivity.this.startActivity(finishIntent);

    }
}
