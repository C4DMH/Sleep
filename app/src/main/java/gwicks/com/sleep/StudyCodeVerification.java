package gwicks.com.sleep;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.view.View.GONE;

public class StudyCodeVerification extends AppCompatActivity implements GetRawData.OnDownloadComplete, GetRawDataTwo.OnDownloadCompleteTwo {

    private static final String TAG = "StudyCodeVerification";
    EditText studyCode;
    String finalCode;
    String creationDate = "";

    ImageView qrCodeButton;

    String informedConsent;
    SharedPreferences mSharedPreferences;

    private static final String LOG_TAG = "Barcode Scanner API";
    private static final int PHOTO_REQUEST = 10;

    //private BarcodeDetector mBarcodeDetector;
    private TextView scanResults;
    private Uri imageUri;

    private static final int REQUEST_WRITE_PERMISSION = 20;
    private static final int REQUEST_CAMERA_PERMISSION = 31;
    private static final String SAVED_INSTANCE_URI = "uri";
    private static final String SAVED_INSTANCE_RESULT = "result";
    private Boolean skip = false;

    int count = 0;

    SurfaceView sv;
    CameraSource mCameraSource;
    TextView mTextView;

    BarcodeDetector mBarcodeDetector;

    int height;
    int width;
    String result = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.study_code);
        updateStatusBarColor("#1281e8");
        studyCode = (EditText) findViewById(R.id.studyCode);
        mTextView = findViewById(R.id.imageView10);

        qrCodeButton = findViewById(R.id.imageView6);


        studyCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (count <= studyCode.getText().toString().length()
                        && (studyCode.getText().toString().length() == 4
                        || studyCode.getText().toString().length() == 9
                        || studyCode.getText().toString().length() == 14)) {
                    studyCode.setText(studyCode.getText().toString() + " ");
                    int pos = studyCode.getText().length();
                    studyCode.setSelection(pos);
                } else if (count >= studyCode.getText().toString().length()
                        && (studyCode.getText().toString().length() == 4
                        || studyCode.getText().toString().length() == 9
                        || studyCode.getText().toString().length() == 14)) {
                    studyCode.setText(studyCode.getText().toString().substring(0, studyCode.getText().toString().length() - 1));
                    int pos = studyCode.getText().length();
                    studyCode.setSelection(pos);
                }
                count = studyCode.getText().toString().length();

            }
        });


        informedConsent = getString(R.string.informed_consent);


        if (!checkPermissionForWriteExtertalStorage()) {
            ActivityCompat.requestPermissions(StudyCodeVerification.this, new
                    String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
        }

        if (skip) {
            Intent installIntent = new Intent(StudyCodeVerification.this, SetupStepTwo.class);
            StudyCodeVerification.this.startActivity(installIntent);
            finish();
        }
    }



    public void updateStatusBarColor(String color){// Color must be in hexadecimal fromat
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.d(TAG, "updateStatusBarColor: color change being called!");
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(color));
        }
    }

    public void requestCameraPermission(View v){
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                qrcode();
                Log.d(TAG, "requestCameraPermission: camera permission OK");
            } else {
                Log.d(TAG, "requestCameraPermission: rewquesting permission camera");

                ActivityCompat.requestPermissions(StudyCodeVerification.this, new
                        String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }




    }

    public void qrcode() {

        Log.d(TAG, "qrcode: qrcode clicked");
        Log.d(TAG, "onCreate: camera permissions");
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity)
                    this, Manifest.permission.CAMERA)) {


            } else {
                ActivityCompat.requestPermissions((Activity) this,
                        new String[]{Manifest.permission.CAMERA},
                        11);
            }
        }


        height = mTextView.getHeight();
        Log.d(TAG, "on: height: " + height);
        width = mTextView.getWidth();
        Log.d(TAG, "on: width: " + width + " next" );

        mTextView.setVisibility(GONE);
        studyCode.setVisibility(GONE);
        qrCodeButton.setVisibility(GONE);


        sv = (SurfaceView) findViewById(R.id.cameraView);

        sv.setVisibility(View.VISIBLE);

        final View bar = findViewById(R.id.bar);
        final Animation animation = AnimationUtils.loadAnimation(StudyCodeVerification.this, R.anim.anim);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                bar.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        bar.setVisibility(View.VISIBLE);
        bar.startAnimation(animation);


        mBarcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE).build();
//
        mCameraSource = new CameraSource.Builder(this, mBarcodeDetector)
                .setRequestedPreviewSize(1280, 720).build();

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity)
                    this, Manifest.permission.CAMERA)) {


            } else {
                ActivityCompat.requestPermissions((Activity) this,
                        new String[]{Manifest.permission.CAMERA},
                        11);
            }
        }
        Log.d(TAG, "preview size " + mCameraSource.getPreviewSize());


        sv.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                try{
                    mCameraSource.start(holder);
                }catch(IOException e){
                    Log.d(TAG, "surfaceCreated: error");
                }

            }



            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.d(TAG, "surfaceChanged: ");
                Log.d(TAG, "preview size " + mCameraSource.getPreviewSize());
                Log.d(TAG, "surfaceChanged: surface view : " + sv.getWidth() + " height: " + sv.getHeight() );

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d(TAG, "surfaceDestroyed: ");
                mCameraSource.stop();

            }
        });

        mBarcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {

                //Log.d(TAG, "receiveDetections: detected");
                final SparseArray<Barcode> qrCodes = detections.getDetectedItems();

                if(qrCodes.size() == 1){


                    mTextView.post(new Runnable() {
                        @Override
                        public void run() {
                            Vibrator vibrator = (Vibrator)getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(1000);
                            //mTextView.setText(qrCodes.valueAt(0).displayValue);
                            result = qrCodes.valueAt(0).displayValue;
                            bar.setVisibility(GONE);
                            bar.clearAnimation();
                            studyCode.setVisibility(View.VISIBLE);
                            qrCodeButton.setVisibility(View.VISIBLE);
                            setString(result);
                        }
                    });
                }
            }
        });
    }


    public void setString(String string){

        String s = string;
        try {
            String s1 = s.substring(0,4);
            String s2 = s.substring(4,8);
            String s3 = s.substring(8,12);
            String s4 = s.substring(12,16);

            String finalString = s1.toUpperCase() + " " + s2.toUpperCase() + " " + s3.toUpperCase() + " " + s4.toUpperCase();
            studyCode.setText(finalString);
        }catch(Exception e){
            Log.d(TAG, "setString: error " + e);
            Toast.makeText(this, "Incorrect study code, please try again", Toast.LENGTH_LONG).show();
        }

        sv.setVisibility(View.GONE);
        mTextView.setVisibility(View.VISIBLE);
    }


    public void informedConsent(View v) {
//
        String finalText = studyCode.getText().toString().trim();
        finalText = finalText.replace(" ", "");
        finalCode = finalText.toLowerCase();
        validateStudyCode(finalCode);


    }

    public void validateStudyCode(String code){
//        String url = "http://date.jsontest.com";
//        new MyAsyncTask().execute(url);
        Log.d(TAG, "validateStudyCode: ");
        Log.d(TAG, "validateStudyCode: code entered: " + code);

        GetRawData getRawData = new GetRawData(this);
        //getRawData.execute("https://u8x53zf4i5.execute-api.us-east-1.amazonaws.com/default/EARS-study-code-verification?code=" + code);
        Log.d(TAG, "validateStudyCode: EXECUTE!!!!!!!!!!!!!!!!");
        getRawData.execute("https://aufpk43i29.execute-api.us-west-2.amazonaws.com/default/EARS-study-code-verification?code=" + code);
        Log.d(TAG, "validateStudyCode: returning false");

    }


    public void onDownloadComplete(String data, DownloadStatus status){

        Log.d(TAG, "onDownloadComplete: step2 == false");
        if(status == DownloadStatus.OK){
            Log.d(TAG, "onDownloadComplete: data is : " + data);

            if(data.contains("study code has already been claimed!")){
                Toast.makeText(this, "That study code has already been claimed, please contact your study coordinator",Toast.LENGTH_LONG).show();
                return;
            }

            Boolean claimed = null;
            String study = "";

            // 29th March 2019 JSON attempt
            try{
                JSONObject jsonData = new JSONObject(data);
                claimed = jsonData.getBoolean("claimed");
                study = jsonData.getString("study");
                creationDate = jsonData.getString("studyCodeCreationDate");
                Log.d(TAG, "onDownloadComplete: claimed: " + claimed + ", study: " + study + ", creationDate: " + creationDate);
            }catch(JSONException e){
                Log.d(TAG, "onDownloadComplete:  error processing json: " + e.getMessage());
            }
            Log.d(TAG, "onDownloadComplete: 3");

            //TODO add a check here in case of error to prevent crash

            if(claimed == null){
                Toast.makeText(this, "That study code does not exist, please contact your study coordinator",Toast.LENGTH_LONG).show();
                return;
            }

            if(claimed.equals(false)){
                showDialog();
                createStudy(study);

            }else{
                Toast.makeText(this, "You have entered an incorrect study code. This app is only for approved participants", Toast.LENGTH_LONG).show();
                Toast.makeText(this, "You have entered an incorrect study code. This app is only for approved participants", Toast.LENGTH_LONG).show();

            }
        }else{
            Log.d(TAG, "onDownloadComplete: failed with status: " + status);
            Toast.makeText(this, "Your study code is incorrect",Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onDownloadCompleteTwo(String data, DownloadStatus2 status) {
        if(status == DownloadStatus2.OK2){

            Log.d(TAG, "onDownloadComplete: step 2 == true");
            String study = "";
            String emaDailyEnd;
            String emaDailyStart;
            int emHoursBetween;
            //String[] emaMoodIdentifiers;
            int emaPhaseBreak;
            int emaPhaseFrequency;
            Boolean emaVariesDuringWeek = null;
            //String[] emaWeekDay;
//            String[] emaWeekDays;
//            String[] includedSensors;
            Boolean phaseAutoScheduled = null;
            String awsBucket;
            //String studySite;

            Log.d(TAG, "onDownloadComplete: in the step2 = true part");
            Log.d(TAG, "onDownloadComplete: the study complete data is : " + data);

            try{
                JSONObject studyJsonObject = new JSONObject(data);
                study = studyJsonObject.getString("studyName");
                //emaPhaseFrequency = studyJsonObject.getInt("emaPhaseFrequency");
                emaDailyEnd = studyJsonObject.getString("emaDailyEnd");
                emaDailyStart = studyJsonObject.getString("emaDailyStart");
                emHoursBetween = studyJsonObject.getInt("emaHoursBetween");

                JSONArray moodIdentifers = studyJsonObject.getJSONArray("emaMoodIdentifiers");
                String[] emaMoodIdentifiers = new String[moodIdentifers.length()];
                for(int i = 0; i<moodIdentifers.length(); i++){
                    emaMoodIdentifiers[i] = moodIdentifers.getString(i);
                }
                emaPhaseBreak = studyJsonObject.getInt("emaPhaseBreak");
                emaPhaseFrequency = studyJsonObject.getInt("emaPhaseFrequency");
                emaVariesDuringWeek = studyJsonObject.getBoolean("emaVariesDuringWeek");
                JSONArray weekDay = studyJsonObject.getJSONArray("emaWeekDay");
                String[] emaWeekDay = new String[weekDay.length()];
                for(int i =0;i<weekDay.length();i++){
                    emaWeekDay[i] = weekDay.getString(i);
                }
                JSONArray weekDays = studyJsonObject.getJSONArray("emaWeekDays");
                String[] emaWeekDays = new String[weekDays.length()];
                for(int i =0;i<weekDays.length();i++){
                    emaWeekDays[i] = weekDays.getString(i);
                }
                JSONArray sensors = studyJsonObject.getJSONArray("includedSensors");
                String[] includedSensors = new String[sensors.length()];
                for(int i =0; i<sensors.length();i++){
                    includedSensors[i] = sensors.getString(i);
                }
                phaseAutoScheduled = studyJsonObject.getBoolean("phaseAutoScheduled");
                awsBucket = studyJsonObject.getString("s3BucketName");
                //studySite = studyJsonObject.getString("studySites");

                Log.d(TAG, "onDownloadComplete: study: " + study + " emaPhasa: " + emaPhaseFrequency);

                Study thisStudy = new Study(study, emaDailyEnd, emaDailyStart, emHoursBetween, emaMoodIdentifiers, emaPhaseBreak, emaPhaseFrequency, emaVariesDuringWeek,emaWeekDay,emaWeekDays, includedSensors,phaseAutoScheduled,awsBucket);

                // Set all the constants, may not need
                Constants.studyName = study;
                Constants.study = study;
                Constants.emaDailyEnd = emaDailyEnd;
                Constants.emaDailyStart = emaDailyStart;
                Constants.emHoursBetween = emHoursBetween;
                Constants.emaPhaseBreak = emaPhaseBreak;
                Constants.emaPhaseFrequency = emaPhaseFrequency;
                Constants.emaVariesDuringWeek = emaVariesDuringWeek;
                Constants.phaseAutoScheduled = phaseAutoScheduled;
                Constants.awsBucket = awsBucket;
                Constants.emaMoodIdentifiers = emaMoodIdentifiers;
                Constants.emaWeekDay = emaWeekDay;
                Constants.emaWeekDays = emaWeekDays;
                Constants.includedSensors = includedSensors;


                mSharedPreferences =  PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString("bucket", awsBucket);
                editor.putString("studyName", study);
                editor.putString("study", study);
                editor.putString("emaDailyStart", emaDailyStart);
                editor.putString("emaDailyEnd", emaDailyEnd);
                editor.putInt("emaHoursBetween", emHoursBetween);
                editor.putInt("emaPhaseBreak", emaPhaseBreak);
                editor.putInt("emaPhaseFrequency", emaPhaseFrequency);
                editor.putBoolean("StudyCodeOK", true);
                //editor.putString("studySite", studySite);

                editor.apply();

                Log.d(TAG, "onDownloadComplete: toString: " + thisStudy.toString());

            }catch(JSONException e){
                Log.d(TAG, "onDownloadComplete: error processing study JSON: " + e.getMessage());
            }
        }
    }


    public void startInstall()

    {
        updateStudyCode();
        Log.d(TAG, "startInstall: ");
    }

    public void showDialog(){
        AlertDialog alertDialog = new AlertDialog.Builder(StudyCodeVerification.this).create();
        //alertDialog.setTitle("7 Cups EARS: Informed Consent & Terms of Service Agreement");
        alertDialog.setTitle("EARS: Informed Consent & Terms of Service Agreement");
        alertDialog.setMessage(Html.fromHtml(informedConsent));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "I Disagree",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,"I Agree",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startInstall();
            }
        });
        alertDialog.show();

    }

    public void createStudy(String study){

        Log.d(TAG, "createStudy: begin");

        GetRawDataTwo createStudyGetRawData = new GetRawDataTwo(this);
        Log.d(TAG, "createStudy: EXECUTE@!@!@!@!@!@!@!@@!@!@");
        createStudyGetRawData.execute("https://7ocx4sxhze.execute-api.us-west-2.amazonaws.com/default/get-study-variables?study=" + study);
        Log.d(TAG, "createStudy: end");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.d(TAG, "onRequestPermissionsResult: 11");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: why am i here?");
                    //takePicture();
                } else {
                    Toast.makeText(StudyCodeVerification.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
            case REQUEST_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: camera permission granted");
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        try {
                            qrcode();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        }
    }


    public void updateStudyCode(){

        Log.d(TAG, "updateStudyCode: 1");
        PostAWS postAWS = new PostAWS();
        postAWS.execute();
        Log.d(TAG, "updateStudyCode: 2");
    }


    private class PostAWS extends AsyncTask<String, String, String> {

        private static final String TAG = "PostAWS";


        @Override
        protected void onPreExecute() {
            Log.d(TAG, "onPreExecute: 3");
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, "doInBackground: 4");
            String postData = "";
            HttpsURLConnection urlConnection = null;

            try{
                //URL url = new URL("https://aufpk43i29.execute-api.us-west-2.amazonaws.com/default/EARS-study-code-verification");
                String stringUrl = createUri();
                URL finalURL = new URL(stringUrl);
                //URL url = new URL("https://aufpk43i29.execute-api.us-west-2.amazonaws.com/default/EARS-study-code-verification?code=3719026908f5412f" + "&study=test"  + "&studyCodeCreationDate=1554720022297" + "&OS=android&deviceID=" +Constants.deviceID);
                //Log.d(TAG, "doInBackground: url: " + url.toString());
                urlConnection = (HttpsURLConnection)finalURL.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                urlConnection.setRequestProperty("Accept","application/json");
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);



                InputStream in = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);
                int inputStreamData = inputStreamReader.read();
                while(inputStreamData != -1){
                    char currentData = (char) inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    postData += currentData;
                    Log.d(TAG, "doInBackground: postdata: " + postData + "\n");
                }



                Log.i("STATUS", String.valueOf(urlConnection.getResponseCode()));
                Log.i("MSG" , urlConnection.getResponseMessage());
                Log.d(TAG, "doInBackground: " + urlConnection.getContentEncoding());
                Log.d(TAG, "doInBackground:  " + urlConnection.getContentType());
                Log.d(TAG, "doInBackground: " + urlConnection.getHeaderFields());
                Log.d(TAG, "doInBackground: " + urlConnection.getOutputStream());

                urlConnection.disconnect();

            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.d(TAG, "doInBackground: error");
            }catch(Exception e){
                Log.d(TAG, "doInBackground: error");
            }finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                return postData;
            }
        }

        @Override
        protected void onPostExecute(String s) {

            Boolean success = false;
            Log.d(TAG, "onPostExecute: string s = " + s);
            try{
                JSONObject studyJsonObject = new JSONObject(s);
                success = studyJsonObject.getBoolean("success");
                Log.d(TAG, "onPostExecute: success: " + success);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(success){

                Intent installIntent = new Intent(StudyCodeVerification.this, SetupStepTwo.class);
                StudyCodeVerification.this.startActivity(installIntent);
                finish();

            }else{
                Toast.makeText(StudyCodeVerification.this, "Error connecting to server, please try again", Toast.LENGTH_LONG).show();
            }

            super.onPostExecute(s);
        }
    }

    private String createUri(){
        String baseURL = "https://aufpk43i29.execute-api.us-west-2.amazonaws.com/default/EARS-study-code-verification";
        Log.d(TAG, "createUri: uri builder");

        Log.d(TAG, "createUri: constants.study: " + Constants.study);
        Log.d(TAG, "createUri: finalCode: " + finalCode + " helloworld");


        String myUri =  Uri.parse(baseURL).buildUpon()
                .appendQueryParameter("code", finalCode)
                .appendQueryParameter("study", Constants.study)
                .appendQueryParameter("studyCodeCreationDate",creationDate)
                .appendQueryParameter("OS","android")
                .appendQueryParameter("deviceID", Constants.deviceID)
                .build().toString();

        Log.d(TAG, "createUri: string is: " + myUri);
        return myUri;
    }

    public boolean checkPermissionForWriteExtertalStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }


}
