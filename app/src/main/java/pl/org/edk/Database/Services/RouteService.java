package pl.org.edk.database.services;

import android.content.ContentValues;
import android.database.Cursor;
import pl.org.edk.database.entities.Route;
import pl.org.edk.database.entities.RouteDesc;
import pl.org.edk.database.entities.Station;

import java.util.ArrayList;

/**
 * Created by Admin on 2015-12-16.
 */
public class RouteService extends DbServiceBase {
    // ---------------------------------------
    // Insert
    // ---------------------------------------
    public boolean InsertRoute(Route route){
        ContentValues routeValues = route.getContentValues();
        long newId = dbWrite().insert(Route.TABLE_NAME, null, routeValues);
        if(newId <= 0)
            return false;

        route.setId(newId);
        return true;
    }

    public boolean InsertStation(Station station, long routeId){
        station.setRouteID(routeId);

        ContentValues stationValues = station.getContentValues();
        long newId = dbWrite().insert(Station.TABLE_NAME, null, stationValues);
        if(newId <= 0)
            return false;

        station.setId(newId);
        return true;
    }

    public boolean InsertRouteWithStations(Route route){
        boolean result = InsertRoute(route);
        if(!result)
            return false;

        for(Station station : route.getStations()){
            result &= InsertStation(station, route.getId());
        }
        return result;
    }

    // ---------------------------------------
    // Get
    // ---------------------------------------
    public Route GetRoute(long routeId){
        Cursor cursor = executeQueryWhere(Route.TABLE_NAME, Route.getFullProjection(), Route._ID, String.valueOf(routeId));

        // No Routes with this id found
        if(cursor.getCount() == 0)
            return null;

        // Get this Route info
        cursor.moveToFirst();
        Route route = new Route();
        return route.readFromCursor(cursor) ? route : null;
    }

    public Route GetRouteWithStations(long routeId){
        Route route = GetRoute(routeId);
        if(route == null)
            return null;

        GetStationsForRoute(route);
        return route;
    }

    public void GetStationsForRoute(Route route){
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

    public RouteDesc GetDescForRoute(long routeId, String language){
        ArrayList<String> whereColumns = new ArrayList<>();
        whereColumns.add(RouteDesc.COLUMN_NAME_ROUTE_ID);
        whereColumns.add(RouteDesc.COLUMN_NAME_LANGUAGE);
        String[] whereValues = {String.valueOf(routeId), language};

        Cursor cursor = executeQueryWhere(RouteDesc.TABLE_NAME, RouteDesc.getFullProjection(), whereColumns, whereValues);

        // No Routes with this id found
        if(cursor.getCount() == 0)
            return null;

        // Get this Route info
        cursor.moveToFirst();
        RouteDesc routeDesc = new RouteDesc();
        return routeDesc.readFromCursor(cursor) ? routeDesc : null;
    }

    public ArrayList<Route> GetRoutesForArea(long areaId, boolean includeStations){
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
                GetStationsForRoute(route);
        }

        return routes;
    }
}
