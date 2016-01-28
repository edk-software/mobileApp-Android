package pl.org.edk.Database.Services;

import android.content.ContentValues;
import android.database.Cursor;
import pl.org.edk.Database.Entities.Route;

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

    // ---------------------------------------
    // Get
    // ---------------------------------------
    public Route GetRoute(long routeId, boolean includeStations){
        Cursor cursor = executeQueryWhere(Route.TABLE_NAME, Route.getFullProjection(), Route._ID, String.valueOf(routeId));

        // No Routes with this id found
        if(cursor.getCount() == 0)
            return null;

        // Get this Route info
        cursor.moveToFirst();
        Route route = new Route();
        return route.readFromCursor(cursor) ? route : null;
    }
}
