package pl.org.edk.webServices;

import android.provider.CalendarContract;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by pwawrzynek on 2016-03-12.
 */
public class RequestLogger
{
    // ---------------------------------------
    // Inner classes
    // ---------------------------------------
    class WebServiceLog
    {
        public WebServiceLog(String methodName, Date requestTime) {
            this.methodName = methodName;
            this.requestTime = requestTime;

            mLogs = new ArrayList<>();
        }

        String methodName;
        Date requestTime;
        boolean allowed;
        String responseBody;

        public String toString()
        {
            return "[" + requestTime.toString() + "]: " + methodName;
        }
    }

    // ---------------------------------------
    // Members
    // ---------------------------------------
    private int mTimePeriod;
    private int mRequestLimit;
    private ArrayList<WebServiceLog> mLogs;

    // ---------------------------------------
    // Members
    // ---------------------------------------
    public RequestLogger(int timePeriod, int requestLimit) {
        this.mTimePeriod = timePeriod;
        this.mRequestLimit = requestLimit;
    }

    // ---------------------------------------
    // Public methods
    // ---------------------------------------
    public boolean validateMethod(String methodName){
        WebServiceLog newLog = new WebServiceLog(methodName, Calendar.getInstance().getTime());

        // Check if the request counter is not too high
        newLog.allowed = getLastMinuteCount() < mRequestLimit;
        mLogs.add(newLog);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -3);
        clearOldLogs(calendar.getTime());

        return newLog.allowed;
    }

    public String getLogs(){
        String logString = "";
        for(WebServiceLog log : mLogs)
            logString += log + "\n";
        return logString;
    }

    public void addResponse(String methodName, String response){
        for(int i = mLogs.size()-1; i > 0; i--){
            WebServiceLog log = mLogs.get(i);
            if(log.methodName == methodName && log.responseBody == null){
                log.responseBody = response;
                break;
            }
        }
    }
    // ---------------------------------------
    // Private methods
    // ---------------------------------------

    /**
     * Check how many requests has been sent during the last TIME_PERIOD
     * @return
     */
    private int getLastMinuteCount(){
        long currentTime = Calendar.getInstance().getTime().getTime();
        int count = 0;
        for(WebServiceLog log : mLogs){
            if(currentTime - log.requestTime.getTime() <= mTimePeriod) {
                count++;
            }
        }

        return count;
    }

    /**
     * Remove logs older than the specified date
     * @param date
     */
    private void clearOldLogs(Date date){
        for(int i = 0; i < mLogs.size(); i++){
            WebServiceLog log = mLogs.get(i);
            if(log.requestTime.compareTo(date) < 0) {
                mLogs.remove(log);
                i--;
            }
        }
    }
}
