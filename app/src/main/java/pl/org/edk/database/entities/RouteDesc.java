package pl.org.edk.database.entities;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;

/**
 * Created by pwawrzynek on 2016-01-13.
 */
public class RouteDesc extends DbEntityBase {
    // ---------------------------------------
    // Constant variables
    // ---------------------------------------
    public static final String TABLE_NAME = "RouteDesc";
    public static final String COLUMN_NAME_ROUTE_ID = "RouteID";
    public static final String COLUMN_NAME_LANGUAGE = "Language";
    public static final String COLUMN_NAME_DESCRIPTION = "Description";

    // ---------------------------------------
    // Class variables
    // ---------------------------------------
    private long routeID;
    private String language;
    private String description;
    // External tables
    private ArrayList<StationDesc> stationDescs;

    // ---------------------------------------
    // Static methods
    // ---------------------------------------
    public static String getCreateEntries() {
        return "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + INTEGER_TYPE + PRIMARY_KEY + COMMA +
                COLUMN_NAME_ROUTE_ID + INTEGER_TYPE + COMMA +
                COLUMN_NAME_LANGUAGE + TEXT_TYPE + COMMA +
                COLUMN_NAME_DESCRIPTION + TEXT_TYPE + ");";
    }

    public static String getDeleteEntries() {
        return "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static String[] getFullProjection(){
        String[] projection = {
                _ID,
                COLUMN_NAME_ROUTE_ID,
                COLUMN_NAME_LANGUAGE,
                COLUMN_NAME_DESCRIPTION
        };
        return projection;
    }

    // ---------------------------------------
    // Base class methods
    // ---------------------------------------

    @Override
    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME_ROUTE_ID, routeID);
        values.put(COLUMN_NAME_LANGUAGE, language);
        values.put(COLUMN_NAME_DESCRIPTION, description);

        return values;
    }

    @Override
    public boolean readFromCursor(Cursor cursor) {
        try {
            this.id = cursor.getLong(cursor.getColumnIndexOrThrow(_ID));
            this.routeID = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_NAME_ROUTE_ID));
            this.language = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_LANGUAGE));
            this.description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_DESCRIPTION));
            return true;
        }catch (Exception ex){
            return false;
        }
    }

    @Override
    public String getTableName(){
        return TABLE_NAME;
    }

    // ---------------------------------------
    // Public methods
    // ---------------------------------------

    public long getRouteID() {
        return routeID;
    }
    public void setRouteID(long routeID) {
        this.routeID = routeID;
    }

    public String getLanguage() {
        return language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
