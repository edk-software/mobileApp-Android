package pl.org.edk.database.Entities;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;

/**
 * Created by pwawrzynek on 2016-01-13.
 */
public class Station extends DbEntityBase {
    // ---------------------------------------
    // Constant variables
    // ---------------------------------------
    public static final String TABLE_NAME = "Station";
    public static final String COLUMN_NAME_ROUTE_ID = "RouteID";
    public static final String COLUMN_NAME_STATION_INDEX = "StationIndex";
    public static final String COLUMN_NAME_LATITUDE = "Latitude";
    public static final String COLUMN_NAME_LONGITUDE = "Longitude";
    public static final String COLUMN_NAME_DISTANCE_DONE = "DistanceDone";
    public static final String COLUMN_NAME_DISTANCE_LEFT = "DistanceLeft";

    // ---------------------------------------
    // Class variables
    // ---------------------------------------
    private long routeID;
    private int stationIndex;
    private float latitude;
    private float longitude;
    private float distanceDone;
    private float distanceLeft;
    // External tables
    private Route routeData;
    private ArrayList<StationDesc> descriptions;

    // ---------------------------------------
    // Static methods
    // ---------------------------------------
    public static String getCreateEntries() {
        return "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + INTEGER_TYPE + PRIMARY_KEY + COMMA +
                COLUMN_NAME_ROUTE_ID + INTEGER_TYPE + COMMA +
                COLUMN_NAME_STATION_INDEX + INTEGER_TYPE + COMMA +
                COLUMN_NAME_LATITUDE + FLOAT_TYPE + COMMA +
                COLUMN_NAME_LONGITUDE + FLOAT_TYPE + COMMA +
                COLUMN_NAME_DISTANCE_DONE + FLOAT_TYPE + COMMA +
                COLUMN_NAME_DISTANCE_LEFT + FLOAT_TYPE + ");";
    }

    public static String getDeleteEntries() {
        return "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static String[] getFullProjection(){
        String[] projection = {
                _ID,
                COLUMN_NAME_ROUTE_ID,
                COLUMN_NAME_STATION_INDEX,
                COLUMN_NAME_LATITUDE,
                COLUMN_NAME_LONGITUDE,
                COLUMN_NAME_DISTANCE_DONE,
                COLUMN_NAME_DISTANCE_LEFT
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
        values.put(COLUMN_NAME_STATION_INDEX, stationIndex);
        values.put(COLUMN_NAME_LATITUDE, latitude);
        values.put(COLUMN_NAME_LONGITUDE, longitude);
        values.put(COLUMN_NAME_DISTANCE_DONE, distanceDone);
        values.put(COLUMN_NAME_DISTANCE_LEFT, distanceLeft);

        return values;
    }

    @Override
    public boolean readFromCursor(Cursor cursor) {
        try {
            this.id = cursor.getLong(cursor.getColumnIndexOrThrow(_ID));
            this.routeID = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_NAME_ROUTE_ID));
            this.stationIndex = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_STATION_INDEX));
            this.latitude = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_NAME_LATITUDE));
            this.longitude = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_NAME_LONGITUDE));
            this.distanceDone = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_NAME_DISTANCE_DONE));
            this.distanceLeft = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_NAME_DISTANCE_LEFT));
            return true;
        }catch (Exception ex){
            return false;
        }
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

    public int getStationIndex() {
        return stationIndex;
    }
    public void setStationIndex(int stationIndex) {
        this.stationIndex = stationIndex;
    }

    public float getLatitude() {
        return latitude;
    }
    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }
    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getDistanceDone() {
        return distanceDone;
    }
    public void setDistanceDone(float distanceDone) {
        this.distanceDone = distanceDone;
    }

    public float getDistanceLeft() {
        return distanceLeft;
    }
    public void setDistanceLeft(float distanceLeft) {
        this.distanceLeft = distanceLeft;
    }
}
