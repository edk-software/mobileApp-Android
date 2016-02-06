package pl.org.edk.managers;

/**
 * Created by Admin on 2016-02-01.
 */
public class LogManager {
    public static void LogInfo(String infoMessage){
        System.out.println("INFO - " + infoMessage);
    }

    public static void LogError(String errorMessage){
        System.out.println("ERROR - " + errorMessage);
    }
}
