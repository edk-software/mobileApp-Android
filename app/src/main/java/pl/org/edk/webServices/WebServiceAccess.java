package pl.org.edk.webServices;

import android.content.Context;
import com.google.gson.reflect.TypeToken;
import pl.org.edk.database.entities.*;
import pl.org.edk.managers.LogManager;
import pl.org.edk.util.JsonHelper;
import pl.org.edk.util.NumConverter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.*;

/**
 * Created by pwawrzynek on 2016-01-31.
 */
public class WebServiceAccess {
    // ---------------------------------------
    // Constants
    // ---------------------------------------
    private static final String METHOD_GET_TERRITORIES = "get-territories.php";
    private static final String METHOD_GET_AREAS = "get-areas.php";
    private static final String METHOD_GET_ROUTES = "get-routes.php";
    private static final String METHOD_GET_REFLECTIONS = "get-reflections.php";
    private static final String METHOD_CHECK_REFLECTIONS = "check-reflections.php";

    // ---------------------------------------
    // Class members
    // ---------------------------------------
    private Context mContext;
    private static WebServiceAccess mInstance;

    private final HttpManager mRestManager;

    // ---------------------------------------
    // Constructors
    // ---------------------------------------
    public WebServiceAccess(Context context){
        this.mContext = context.getApplicationContext();
        mRestManager = new HttpManager("http://panel.edk.org.pl");
    }

    // ---------------------------------------
    // Public methods
    // ---------------------------------------
    public ArrayList<Territory> getTerritories(){
        String response = callMethod(METHOD_GET_TERRITORIES);

        if(response == null || response.length() == 0)
            return new ArrayList<>();

        // Deserialize and rewrite the serverIDs
        ArrayList<Territory> territories = JsonHelper.deserializeFromJson(response, new TypeToken<ArrayList<Territory>>(){}.getType());
        for(Territory territory : territories) {
            territory.setServerID(territory.getId());
            territory.setId(0);
        }

        LogManager.LogInfo("WebServiceAccess.getTerritories success - " + territories.size() + " items downloaded");
        return territories;
    }

    public ArrayList<Area> getAreas(){
        String response = callMethod(METHOD_GET_AREAS);

        if(response == null)
            return new ArrayList<>();

        // Deserialize and rewrite the serverIDs
        ArrayList<Area> areas = JsonHelper.deserializeFromJson(response, new TypeToken<ArrayList<Area>>(){}.getType());
        for(Area area : areas){
            area.setServerID(area.getId());
            area.setId(0);
        }

        LogManager.LogInfo("WebServiceAccess.getAreas success - " + areas.size() + " items downloaded");
        return areas;
    }

    public Route getRoute(long routeServerId){
        String response = callMethod(METHOD_GET_ROUTES, "route", String.valueOf(routeServerId));

        if(response == null)
            return null;

        // Deserialize and rewrite the serverIDs
        Route route = JsonHelper.deserializeFromJson(response, Route.class);
        route.setServerID(route.getId());
        route.setId(0);

        LogManager.LogInfo("WebServiceAccess.getRoute success.");
        return route;
    }

    public ArrayList<Route> getRoutesByTerritory(long territoryServerId){
        String response = callMethod(METHOD_GET_ROUTES, "territory", String.valueOf(territoryServerId));

        if(response == null)
            return new ArrayList<>();

        // Deserialize and rewrite the serverIDs
        ArrayList<Route> routes = JsonHelper.deserializeFromJson(response, new TypeToken<ArrayList<Route>>(){}.getType());
        for(Route route : routes){
            route.setServerID(route.getId());
            route.setId(0);
        }

        LogManager.LogInfo("WebServiceAccess.getRoutesByTerritory success - " + routes.size() + " items downloaded");
        return routes;
    }

    public ArrayList<Route> getRoutesByArea(long areaId){
        String response = callMethod(METHOD_GET_ROUTES, "area", String.valueOf(areaId));

        if(response == null)
            return new ArrayList<>();

        // Deserialize and rewrite the serverIDs
        ArrayList<Route> routes = JsonHelper.deserializeFromJson(response, new TypeToken<ArrayList<Route>>(){}.getType());
        for(Route route : routes){
            route.setServerID(route.getId());
            route.setId(0);
        }

        LogManager.LogInfo("WebServiceAccess.getRoutesByArea success - " + routes.size() + " items downloaded");
        return routes;
    }

    public ArrayList<Reflection> getReflections(String lang){
        String response = callMethod(METHOD_GET_REFLECTIONS, "language", lang);

        if(response == null)
            return new ArrayList<>();

        // Deserialize
        ArrayList<Reflection> reflections = JsonHelper.deserializeFromJson(response, new TypeToken<ArrayList<Reflection>>(){}.getType());
        for(Reflection reflection : reflections){
            reflection.setLanguage(lang);
        }

        LogManager.LogInfo("WebServiceAccess.getReflections success - " + reflections.size() + " items downloaded");
        return reflections;
    }

    public Date checkReflectionsReleaseDate(String lang){
        String response = callMethod(METHOD_CHECK_REFLECTIONS, "language", lang);

        if(response == null)
            return null;

        return NumConverter.stringToDate(response);
    }

    // ---------------------------------------
    // Private methods
    // ---------------------------------------
    private String callMethod(final String methodName, final HashMap<String, String> parameters){
        String response;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            response = executor.submit(new Callable<String>() {
                @Override
                public String call(){
                    return mRestManager.callMethod(methodName, parameters);
                }
            }).get();
            return response;
        } catch (Exception e) {
            LogManager.LogError("WebServiceAccess - calling method " + methodName + " failed: " + e.getMessage());
            return null;
        } finally {
            executor.shutdown();
        }
    }

    private String callMethod(String methodName){
        return callMethod(methodName, null);
    }

    private String callMethod(String methodName, String paramKey, String paramValue){
        final HashMap<String, String> params = new HashMap<>(1);
        params.put(paramKey, paramValue);
        return callMethod(methodName, params);
    }
}
