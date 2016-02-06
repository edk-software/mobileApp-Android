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

        DbManager.getInstance().Init(context);

        // TEMP: Add fake data to DB
        DbManager.getInstance().Reset(context);
        HardcodedDataManager.CreateTerritoriesAndAreas();
        HardcodedDataManager.CreateRoutes();

        //ArrayList<Territory> territories = WebServiceManager.getInstance().getTerritories();
        //ArrayList<Area> areas = WebServiceManager.getInstance().getAreas();
        //ArrayList<Route> routes = WebServiceManager.getInstance().getRoutesByTerritory(1);
        ArrayList<Route> routes = WebServiceManager.getInstance().getRoutesByArea(1);
        Route route = WebServiceManager.getInstance().getRoute(1);

        isInitialized = true;
    }
}
