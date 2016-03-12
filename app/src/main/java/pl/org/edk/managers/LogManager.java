package pl.org.edk.managers;

/**
 * Created by pwawrzynek on 2016-02-01.
 */
public class LogManager {
    public static void logInfo(String infoMessage){
        System.out.println("INFO - " + infoMessage);
    }

    public static void logError(String errorMessage){
        System.out.println("ERROR - " + errorMessage);
    }
}
