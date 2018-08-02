package gwicks.com.sleep;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Spinner morningSpinnerWeek;
    Spinner morningSpinnerWeekend;
    Spinner nightSpinnerWeek;
    Spinner nightSpinnerWeekend;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        prefs = PreferenceManager.getDefaultSharedPreferences(this);





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

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();





        String morningWeek = morningSpinnerWeek.getSelectedItem().toString();
        String nightWeek = nightSpinnerWeek.getSelectedItem().toString();
        String morningWeekend = morningSpinnerWeekend.getSelectedItem().toString();
        String nightWeekend = nightSpinnerWeekend.getSelectedItem().toString();

        editor.putString("mW", morningWeek);
        editor.putString("nW", nightWeek );
        editor.putString("mWE", morningWeekend);
        editor.putString("nWE", nightWeekend);

        editor.apply();

        Toast.makeText(this, "The value is: " + morningWeek,Toast.LENGTH_LONG).show();

        Intent finishIntent = new Intent(MainActivity.this, FinishScreen.class);
        MainActivity.this.startActivity(finishIntent);

    }
}
