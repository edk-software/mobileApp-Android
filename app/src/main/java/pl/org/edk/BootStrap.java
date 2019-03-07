package pl.org.edk;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import pl.org.edk.database.*;
import pl.org.edk.managers.WebServiceManager;

import java.io.File;

/**
 * Created by pwawrzynek on 2016-01-28.
 */
public final class BootStrap {
    private BootStrap(){}

    private static boolean mIsInitialized = false;

    public static void initialize(Context context){
        if(mIsInitialized)
            return;

        initNotificationChannel(context);

        DbManager.getInstance(context).init();
        WebServiceManager.getInstance(context).init(R.mipmap.ic_launcher);
        Settings.get(context).init();

        initStorage(context);

        mIsInitialized = true;
    }

    private static void initNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            /* Create or update. */
            NotificationChannel channel = new NotificationChannel(Settings.NOTIFICATION_MAIN_CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationChannel channel2 = new NotificationChannel(Settings.NOTIFICATION_LOW_PRIORITY_CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_LOW);
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.createNotificationChannel(channel);
            mNotificationManager.createNotificationChannel(channel2);
        }
    }

    public static void initStorage(Context context){
        String path = Environment.getExternalStorageDirectory() + "/edk";
        File folder = new File(path);
        if(!folder.exists()) {
            folder.mkdir();
        }

        File kmlFolder = new File(path + "/kml");
        if(!kmlFolder.exists()){
            kmlFolder.mkdir();
        }
        Settings.get(context).set(Settings.APP_DIRECTORY_KML, kmlFolder.getAbsolutePath());

        File audioFolder = new File(path + "/audio");
        if(!audioFolder.exists()){
            audioFolder.mkdir();
        }
        Settings.get(context).set(Settings.APP_DIRECTORY_AUDIO, audioFolder.getAbsolutePath());
    }
}
