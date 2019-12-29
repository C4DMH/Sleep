package gwicks.com.sleep;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Arrays;

public class Study {

    SharedPreferences mSharedPreferences;

    private static final String TAG = "Study";

    private String mStudy;
    private String emaDailyEnd;
    private String emaDailyStart;
    private int emaHoursBetween;
    private String[] emaMoodIdentifiers;
    private int emaPhaseBreak;
    private int emaPhaseFrequency;
    private Boolean emaVariesDuringWeek;
    private String[] emaWeekDay;
    private String[] emaWeekDays;
    private String[] includedSensors;
    private Boolean phaseAutoScheduled;
    private String awsBucket;
    private String studySite;


//    Study(String study, String emaDailyEnd, String emaDailyStart, String emaHoursBetween, int emaPhaseBreak, int emaPhaseFrequency, Boolean emaVariesDuringWeek, Boolean phaseAutoScheduled){
//        mStudy
//    }


    Study(String study, String emaDailyEnd, String emaDailyStart, int emaHoursBetween, String[] emaMoodIdentifiers, int emaPhaseBreak, int emaPhaseFrequency, Boolean emaVariesDuringWeek, String[] emaWeekDay, String[] emaWeekDays, String[] includedSensors, Boolean phaseAutoScheduled, String awsBucket) {
        mStudy = study;
        Constants.studyName = study;
        this.emaDailyEnd = emaDailyEnd;
        this.emaDailyStart = emaDailyStart;
        this.emaHoursBetween = emaHoursBetween;
        this.emaMoodIdentifiers = emaMoodIdentifiers;
        this.emaPhaseBreak = emaPhaseBreak;
        this.emaPhaseFrequency = emaPhaseFrequency;
        this.emaVariesDuringWeek = emaVariesDuringWeek;
        this.emaWeekDay = emaWeekDay;
        this.emaWeekDays = emaWeekDays;
        this.includedSensors = includedSensors;
        this.phaseAutoScheduled = phaseAutoScheduled;
        this.awsBucket = awsBucket;
        this.studySite = "null";
    }

    @Override
    public String toString() {
        return "Study{" +
                "mStudy='" + mStudy + '\'' +
                ", emaDailyEnd='" + emaDailyEnd + '\'' +
                ", emaDailyStart='" + emaDailyStart + '\'' +
                ", emaHoursBetween='" + emaHoursBetween + '\'' +
                ", emaMoodIdentifiers=" + Arrays.toString(emaMoodIdentifiers) +
                ", emaPhaseBreak=" + emaPhaseBreak +
                ", emaPhaseFrequency=" + emaPhaseFrequency +
                ", emaVariesDuringWeek=" + emaVariesDuringWeek +
                ", emaWeekDay=" + emaWeekDay +
                ", emaWeekDays=" + emaWeekDays +
                ", includedSensors=" + includedSensors +
                ", studySite = " + studySite +
                ", phaseAutoScheduled=" + phaseAutoScheduled +
                '}';
    }

    String getStudy() {
        return mStudy;
    }

    String getEmaDailyEnd() {
        return emaDailyEnd;
    }

    String getEmaDailyStart() {
        return emaDailyStart;
    }

    int getEmaHoursBetween() {
        return emaHoursBetween;
    }

    String[] getEmaMoodIdentifiers() {
        return emaMoodIdentifiers;
    }

    int getEmaPhaseBreak() {
        return emaPhaseBreak;
    }

    int getEmaPhaseFrequency() {
        return emaPhaseFrequency;
    }

    Boolean getEmaVariesDuringWeek() {
        return emaVariesDuringWeek;
    }

    String[] getEmaWeekDay() {
        return emaWeekDay;
    }

    String[] getEmaWeekDays() {
        return emaWeekDays;
    }

    String[] getIncludedSensors() {
        return includedSensors;
    }

    Boolean getPhaseAutoScheduled() {
        return phaseAutoScheduled;
    }

    String getAwsBucket() {
        if(awsBucket == null){
            Log.d(TAG, "getAwsBucket: it was null!");
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(AnyApplication.getInstance());
            String s = mSharedPreferences.getString("bucket","default");
            return s;
        }
        return awsBucket;
    }
}