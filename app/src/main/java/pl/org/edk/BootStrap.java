package pl.org.edk;

import android.content.Context;
import pl.org.edk.Database.DbHelper;
import pl.org.edk.Database.DbManager;
import pl.org.edk.Database.Entities.Area;
import pl.org.edk.Database.Entities.Route;

/**
 * Created by Admin on 2016-01-28.
 */
public final class Bootstrap {
    private Bootstrap(){}

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
