package pl.org.edk.Database.Entities;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.Date;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;

/**
 * Created by Admin on 2015-12-15.
 */
public class Route extends DbEntityBase {
    // ---------------------------------------
    // Constant variables
    // ---------------------------------------
    public static final String TABLE_NAME = "Route";
    public static final String COLUMN_NAME_TERRITORY_ID = "TerritoryID";
    public static final String COLUMN_NAME_AREA_ID = "RegionID";
    public static final String COLUMN_NAME_RELEASE_DATE = "ReleaseDate";
    public static final String COLUMN_NAME_DISPLAY_NAME = "Name";
    public static final String COLUMN_NAME_DISPLAY_KML_DATA = "KmlData";

    // ---------------------------------------
    // Constructors
    // ---------------------------------------
    public Route(){}

    public Route(int territoryId, int areaId, String releaseDate, String name) {
        this.territoryId = territoryId;
        this.areaId = areaId;
        this.releaseDate = releaseDate;
        this.name = name;
    }

    // ---------------------------------------
    // Class variables
    // ---------------------------------------
    private long territoryId;
    private long areaId;
    private String releaseDate;
    private String name;
    private String kmlData;
    // External tables
    private ArrayList<RouteDesc> descriptions;
    private ArrayList<Station> stations;

    // ---------------------------------------
    // Static methods
    // ---------------------------------------
    public static String getCreateEntries() {
        return "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + INTEGER_TYPE + PRIMARY_KEY + COMMA +
                COLUMN_NAME_TERRITORY_ID + INTEGER_TYPE + COMMA +
                COLUMN_NAME_AREA_ID + INTEGER_TYPE + COMMA +
                COLUMN_NAME_RELEASE_DATE + TEXT_TYPE + COMMA +
                COLUMN_NAME_DISPLAY_NAME + TEXT_TYPE + COMMA +
                COLUMN_NAME_DISPLAY_KML_DATA + TEXT_TYPE + ");";
    }

    public static String getDeleteEntries() {
        return "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static String[] getFullProjection(){
        String[] projection = {
                _ID,
                COLUMN_NAME_TERRITORY_ID,
                COLUMN_NAME_AREA_ID,
                COLUMN_NAME_RELEASE_DATE,
                COLUMN_NAME_DISPLAY_NAME,
                COLUMN_NAME_DISPLAY_KML_DATA
        };
        return projection;
    }

    // ---------------------------------------
    // Base class methods
    // ---------------------------------------

    @Override
    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME_TERRITORY_ID, territoryId);
        values.put(COLUMN_NAME_AREA_ID, areaId);
        values.put(COLUMN_NAME_RELEASE_DATE, releaseDate);
        values.put(COLUMN_NAME_DISPLAY_NAME, name);
        values.put(COLUMN_NAME_DISPLAY_KML_DATA, kmlData);

        return values;
    }

    @Override
    public boolean readFromCursor(Cursor cursor) {
        try {
            this.id = cursor.getLong(cursor.getColumnIndexOrThrow(_ID));
            this.territoryId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_NAME_TERRITORY_ID));
            this.areaId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_NAME_AREA_ID));
            this.releaseDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_RELEASE_DATE));
            this.name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_DISPLAY_NAME));
            this.kmlData = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_DISPLAY_KML_DATA));
            return true;
        }catch (Exception ex){
            return false;
        }
    }

    // ---------------------------------------
    // Getters and setters
    // ---------------------------------------
    public long getTerritoryId() {
        return territoryId;
    }
    public void setTerritoryId(long territoryId) {
        this.territoryId = territoryId;
    }

    public long getAreaId() {
        return areaId;
    }
    public void setAreaId(long areaId) {
        this.areaId = areaId;
    }

    public Date getReleaseDate(){
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
        return format.parse(this.releaseDate, new ParsePosition(0));
    }
    public void setReleaseDate(Date date){
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
        releaseDate = format.format(date);
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getKmlData() {
        return kmlData;
    }
    public void setKmlData(String kmlData) {
        this.kmlData = kmlData;
    }
}
