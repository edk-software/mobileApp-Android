package pl.org.edk;

import android.content.Context;
import android.os.Environment;
import pl.org.edk.database.*;
import pl.org.edk.managers.WebServiceManager;

import java.io.File;
import java.util.Calendar;

/**
 * Created by pwawrzynek on 2016-01-28.
 */
public final class BootStrap {
    private BootStrap(){}

    private static boolean mIsInitialized = false;

    public static void initialize(Context context){
        if(mIsInitialized)
            return;

        DbManager.getInstance(context).init();
        WebServiceManager.getInstance(context).init(R.mipmap.ic_launcher);

        Settings.get(context).set(Settings.APP_LANGUAGE, "pl");
        Settings.get(context).set(Settings.CURRENT_EDITION, Calendar.getInstance().get(Calendar.YEAR));
        Settings.get(context).set(Settings.REFLECTIONS_EDITION, Calendar.getInstance().get(Calendar.YEAR));

        initStorage(context);

        mIsInitialized = true;
    }

    private static void initStorage(Context context){
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
