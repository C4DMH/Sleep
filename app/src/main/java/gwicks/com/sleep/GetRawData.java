package gwicks.com.sleep;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

enum DownloadStatus { IDLE, PROCESSING, NOT_INITIALISED, FAILED_OR_EMPTY, OK}




class GetRawData extends AsyncTask<String, Void, String> {

    //private StudyCodeVerification.MyInterface mListenr;

    private static final String TAG = "GetRawData";
    private DownloadStatus mDownloadStatus;
    private final OnDownloadComplete mCallback;

    interface OnDownloadComplete {

        void onDownloadComplete(String data, DownloadStatus status);
    }

    public GetRawData(OnDownloadComplete callback) {
        mDownloadStatus = DownloadStatus.IDLE;
        mCallback = callback;
    }

    @Override
    protected String doInBackground(String... strings) {

        Log.d(TAG, "doInBackground: mdownloadstatus 1: " + mDownloadStatus);

        HttpsURLConnection connection = null;
        BufferedReader reader = null;
        if(strings == null){
            mDownloadStatus = DownloadStatus.NOT_INITIALISED;
        }

        try{
            mDownloadStatus = DownloadStatus.PROCESSING;
            Log.d(TAG, "doInBackground: mdownloadstatus 2: " + mDownloadStatus);
            URL url = new URL(strings[0]);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int response = connection.getResponseCode();
            Log.d(TAG, "doInBackground: The response code was: " + response);

            StringBuilder result = new StringBuilder();

            reader = new BufferedReader((new InputStreamReader(connection.getInputStream())));

//            String line;
//            while(null!= (line= reader.readLine())){

            for(String line  =reader.readLine(); line!= null; line = reader.readLine()){
                result.append(line).append("\n");
            }
            mDownloadStatus = DownloadStatus.OK;
            Log.d(TAG, "doInBackground: 1");
            Log.d(TAG, "doInBackground: result = " + result);
            if(result.toString().contains("claimed")){
                Log.d(TAG, "doInBackground: true");
                //return true;
            }
            Log.d(TAG, "doInBackground: mdownloadstatus 3 " + mDownloadStatus);
            return result.toString();
            //return false;

        }catch(MalformedURLException e){
            Log.e(TAG, "doInBackground: Invalid URL " + e.getMessage() );
        }catch(IOException e){
            Log.e(TAG, "doInBackground: IO Exception reading data " + e.getMessage() );
        }catch(SecurityException e){
            Log.e(TAG, "doInBackground: Security Exception " + e.getMessage() );
        }finally {
            if(connection != null){
                connection.disconnect();
            }
            if(reader != null){
                try{
                    reader.close();
                }catch(IOException e){
                    Log.e(TAG, "doInBackground: Error closing stream " + e.getMessage() );
                }
            }
            Log.d(TAG, "doInBackground: 2");
        }
        Log.d(TAG, "doInBackground: 3");
        Log.d(TAG, "doInBackground: mdownloadstatus 4: " + mDownloadStatus);
        return null;
    }

    void runInSameThread(String s){
        Log.d(TAG, "runInSameThread: starts");
        onPostExecute(doInBackground(s));
        Log.d(TAG, "runInSameThread: ends");
    }

    @Override
    protected void onPostExecute(String s) {
        Log.d(TAG, "onPostExecute: parmater = " + s);
        Log.d(TAG, "doInBackground: mdownloadstatus 5: " + mDownloadStatus);
//        if(s.contains("claimed")){
//            Log.d(TAG, "onPostExecute: contrains false");
//           // return true;
//        }
        if(mCallback != null){
            Log.d(TAG, "onPostExecute: mcallback to ondownloadcomplete");
            Log.d(TAG, "onPostExecute: downloadstatus: " + mDownloadStatus);
            mCallback.onDownloadComplete(s, mDownloadStatus);
        }
        Log.d(TAG, "onPostExecute: ends");
        Log.d(TAG, "onPostExecute: does not contain claimed false");
        //return false;

    }


}