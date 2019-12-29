package gwicks.com.sleep;

/**
 * Created by gwicks on 9/07/2018.
 */


public class Constants {

    /*
     * You should replace these values with your own. See the README for details
     * on what to fill in.
     */
    public static final String COGNITO_POOL_ID = "";


    /*
     * Note, you must first create a bucket using the S3 console before running
     * the sample (https://console.aws.amazon.com/s3/). After creating a bucket,
     * put it's name in the field below.
     */

    public static final String BUCKET_NAME = "earstest";

    public static String deviceID;
    public static String modelName;
    public static String modelNumber;
    public static int androidVersion;
    public static String earsVersion;

    public static String studyName = "default";
    public static String study;
    public static String emaDailyEnd;
    public static String emaDailyStart;
    public static int emHoursBetween;
    public static int emaPhaseBreak;
    public static int emaPhaseFrequency;
    public static Boolean emaVariesDuringWeek = null;
    public static Boolean phaseAutoScheduled = null;
    //public static final String awsBucket = "columbia-study";
    public static String awsBucket;

    public static String[] emaMoodIdentifiers;
    public static String[] emaWeekDay;
    public static String[] emaWeekDays;
    public static String[] includedSensors;

    public static String secureID;
    public static String site;
    public static String phoneNumberOne;
    public static String phoneNumberTwo;

    public static Boolean encryption;

    public static Boolean deactivated = false;






}
