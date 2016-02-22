package pl.org.edk.database.services;

import android.database.Cursor;
import pl.org.edk.database.entities.DbEntityBase;
import pl.org.edk.database.entities.Route;
import pl.org.edk.database.entities.RouteDesc;
import pl.org.edk.database.entities.Station;

import java.security.InvalidParameterException;
import java.util.ArrayList;

/**
 * Created by pwawrzynek on 2015-12-16.
 */
public class RouteService extends DbServiceBase {
    // ---------------------------------------
    // Insert
    // ---------------------------------------
    public boolean insertRoute(Route route){
        if(route.getAreaId() <= 0){
            throw new InvalidParameterException("Specified Route is not linked with any Area!");
        }

        long newId = executeQueryInsert(route);
        if(newId <= 0)
            return false;

        route.setId(newId);

        // Insert descriptions
        if(route.getDescriptions() != null){
            for(RouteDesc routeDesc : route.getDescriptions()){
                routeDesc.setRouteID(route.getId());
                insertRouteDesc(routeDesc);
            }
        }

        return true;
    }

    private boolean insertRouteDesc(RouteDesc routeDesc) {
        if(routeDesc.getRouteID() <= 0){
            throw new InvalidParameterException("Specified Area is not linked with any Territory!");
        }

        long newId = executeQueryInsert(routeDesc);
        if(newId <= 0)
            return false;

        routeDesc.setId(newId);
        return true;
    }

    public boolean insertStation(Station station, long routeId){
        station.setRouteID(routeId);

        long newId = executeQueryInsert(station);
        if(newId <= 0)
            return false;

        station.setId(newId);
        return true;
    }

    public boolean insertRouteWithStations(Route route){
        boolean result = insertRoute(route);
        if(!result)
            return false;

        for(Station station : route.getStations()){
            result &= insertStation(station, route.getId());
        }
        return result;
    }

    // ---------------------------------------
    // Update
    // ---------------------------------------
    public boolean updateRoute(Route route){
        int count = executeQueryUpdate(route);
        // Route updated or inserted correctly
        if(count > 0 ||  insertRoute(route)){
            // Update descriptions
            if(route.getDescriptions() != null){
                for (RouteDesc routeDesc : route.getDescriptions()){
                    routeDesc.setRouteID(route.getId());
                    updateRouteDesc(routeDesc);
                }
            }
            return true;
        }
        else {
            return false;
        }
    }

    public boolean updateRouteDesc(RouteDesc routeDesc){
        ArrayList<String> whereColumns = new ArrayList<>();
        whereColumns.add(RouteDesc.COLUMN_NAME_ROUTE_ID);
        whereColumns.add(RouteDesc.COLUMN_NAME_LANGUAGE);

        String[] whereArgs = new String[]{
                String.valueOf(routeDesc.getRouteID()), routeDesc.getLanguage()
        };

        int count = executeQueryUpdate(routeDesc, whereColumns, whereArgs);
        if(count > 0){
            return true;
        }
        else {
            return insertRouteDesc(routeDesc);
        }
    }

    // ---------------------------------------
    // Get
    // ---------------------------------------
    public Route getRoute(long routeId){
        Cursor cursor = executeQueryWhere(Route.TABLE_NAME, Route.getFullProjection(), Route._ID, String.valueOf(routeId));

        // No Routes with this id found
        if(cursor.getCount() == 0)
            return null;

        // Get this Route info
        cursor.moveToFirst();
        Route route = new Route();
        return route.readFromCursor(cursor) ? route : null;
    }

    public Route getRoute(long routeId, String language){
        Route route = getRoute(routeId);
        if(route == null){
            return null;
        }

        // Fetch language-dependent data
        RouteDesc routeDesc = getDescForRoute(routeId, language);
        if(routeDesc != null){
            route.getDescriptions().add(routeDesc);
        }

        return route;
    }

    public Route getRouteByServerID(long routeServerID){
        Cursor cursor = executeQueryWhere(Route.TABLE_NAME, Route.getFullProjection(),
                DbEntityBase.COLUMN_NAME_SERVER_ID, String.valueOf(routeServerID));

        // No Routes with this id found
        if(cursor.getCount() == 0)
            return null;

        // Get this Route info
        cursor.moveToFirst();
        Route route = new Route();
        return route.readFromCursor(cursor) ? route : null;
    }

    public Route getRouteWithStations(long routeId){
        Route route = getRoute(routeId);
        if(route == null)
            return null;

        getStationsForRoute(route);
        return route;
    }

    public void getStationsForRoute(Route route){
        Cursor cursor = executeQueryWhere(Station.TABLE_NAME, Station.getFullProjection(),
                Station.COLUMN_NAME_ROUTE_ID, String.valueOf(route.getId()));

        ArrayList<Station> stations = new ArrayList<>();
        for (int i=0; i < cursor.getCount(); i++){
            cursor.moveToPosition(i);
            Station nextStation = new Station();
            if(nextStation.readFromCursor(cursor))
                stations.add(nextStation);
        }
        route.setStations(stations);
    }

    public RouteDesc getDescForRoute(long routeId, String language){
        ArrayList<String> whereColumns = new ArrayList<>();
        whereColumns.add(RouteDesc.COLUMN_NAME_ROUTE_ID);
        whereColumns.add(RouteDesc.COLUMN_NAME_LANGUAGE);
        String[] whereValues = {String.valueOf(routeId), language};

        Cursor cursor = executeQueryWhere(RouteDesc.TABLE_NAME, RouteDesc.getFullProjection(), whereColumns, whereValues);

        // No Routes with this id found
        if(cursor.getCount() == 0) {
            return null;
        }

        // Get this Route info
        cursor.moveToFirst();
        RouteDesc routeDesc = new RouteDesc();
        return routeDesc.readFromCursor(cursor) ? routeDesc : null;
    }

    public ArrayList<Route> getRoutesForArea(long areaId, boolean includeStations){
        Cursor cursor = executeQueryWhere(Route.TABLE_NAME, Route.getFullProjection(),
                Route.COLUMN_NAME_AREA_ID, String.valueOf(areaId));

        ArrayList<Route> routes = new ArrayList<>();
        for(int i=0; i < cursor.getCount(); i++){
            cursor.moveToPosition(i);
            Route route = new Route();
            if(route.readFromCursor(cursor))
                routes.add(route);
        }

        // Fetch Stations
        if(includeStations){
            for (Route route : routes)
                getStationsForRoute(route);
        }

        return routes;
    }
}
