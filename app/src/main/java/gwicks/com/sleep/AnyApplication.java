package gwicks.com.sleep;

import android.app.Application;
import android.util.Log;

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
