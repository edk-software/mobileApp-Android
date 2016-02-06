package pl.org.edk;

import android.content.Context;
import pl.org.edk.database.DbManager;
import pl.org.edk.database.Entities.Area;
import pl.org.edk.database.Entities.Route;
import pl.org.edk.database.Entities.Territory;
import pl.org.edk.managers.HardcodedDataManager;
import pl.org.edk.managers.WebServiceManager;

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

        DbManager.getInstance(context).Init();

        // TEMP: Add fake data to DB
        DbManager.getInstance(context).Reset();
        HardcodedDataManager.CreateTerritoriesAndAreas(context);
        HardcodedDataManager.CreateRoutes(context);

        ArrayList<Territory> territories = WebServiceManager.getInstance(context).getTerritories();
        ArrayList<Area> areas = WebServiceManager.getInstance(context).getAreas();
        ArrayList<Route> routes = WebServiceManager.getInstance(context).getRoutesByTerritory(1);
        Route route = WebServiceManager.getInstance(context).getRoute(1);

        isInitialized = true;
    }
}
