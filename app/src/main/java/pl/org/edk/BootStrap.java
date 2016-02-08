package pl.org.edk;

import android.content.Context;
import pl.org.edk.database.*;
import pl.org.edk.managers.HardcodedDataManager;

/**
 * Created by Admin on 2016-01-28.
 */
public final class BootStrap {
    private BootStrap(){}

    private static boolean mIsInitialized = false;

    public static void initialize(Context context){
        if(mIsInitialized)
            return;

        DbManager.getInstance(context).Init();

        // TEMP: Add fake data to DB
        DbManager.getInstance(context).Reset();
        HardcodedDataManager.CreateTerritoriesAndAreas(context);
        HardcodedDataManager.CreateRoutes(context);

        mIsInitialized = true;
    }
}
