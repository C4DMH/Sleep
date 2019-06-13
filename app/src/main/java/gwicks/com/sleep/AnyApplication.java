package gwicks.com.sleep;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferService;

/**
 * Created by gwicks on 25/09/2018.
 */

public class AnyApplication extends Application {

    //private static Context context;
    private static final String TAG = "AnyApplication";
    private static AnyApplication instance;

    public void onCreate(){
        Log.d(TAG, "onCreate: anyapplication oncreate");

        super.onCreate();
        //context = this ;
        instance = this;
        Log.d(TAG, "onCreate: instance = " + instance);

        getApplicationContext().startService(new Intent(getApplicationContext(), TransferService.class));

    }

    //    //public static Context getAppContext(){
//        return context;
//    }
    public static AnyApplication getInstance() {
        Log.d(TAG, "getInstance: getting instance");
        Log.d(TAG, "getInstance: instance = " + instance);
        return instance;
    }
}
