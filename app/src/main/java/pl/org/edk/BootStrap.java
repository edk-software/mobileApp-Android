package pl.org.edk;

import android.content.Context;
import pl.org.edk.Database.DbManager;
import pl.org.edk.Database.Entities.Route;

import java.util.ArrayList;

/**
 * Created by Admin on 2016-01-28.
 */
public final class BootStrap {
    private BootStrap(){}

    private static boolean isInitialized = false;

    public static void Initialize(Context context){
        if(isInitialized)
            return;

        DbManager.getInstance().Init(context);

        // TEMP: Add fake data to DB
        DbManager.getInstance().Reset(context);
        HardcodedDataManager.CreateTerritoriesAndAreas();
        HardcodedDataManager.CreateRoutes();

        isInitialized = true;
    }
}
