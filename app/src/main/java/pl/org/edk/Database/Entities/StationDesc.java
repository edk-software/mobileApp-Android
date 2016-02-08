package pl.org.edk.database.entities;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by pwawrzynek on 2016-01-13.
 */
public class StationDesc extends DbEntityBase {
    // ---------------------------------------
    // Constant variables
    // ---------------------------------------
    public static final String TABLE_NAME = "StationDesc";
    public static final String COLUMN_NAME_STATION_ID = "StationID";
    public static final String COLUMN_NAME_LANGUAGE = "Language";
    public static final String COLUMN_NAME_DISPLAY_NAME = "DisplayName";
    public static final String COLUMN_NAME_TITLE = "Title";
    public static final String COLUMN_NAME_DESCRIPTION = "Description";

    // ---------------------------------------
    // Class variables
    // ---------------------------------------
    private long stationID;
    private String language;
    private String displayName;
    private String title;
    private String description;
    // External tables
    private Station stationData;
    private RouteDesc routeDesc;

    // ---------------------------------------
    // Static methods
    // ---------------------------------------
    public static String getCreateEntries() {
        return "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + INTEGER_TYPE + PRIMARY_KEY + COMMA +
                COLUMN_NAME_STATION_ID + INTEGER_TYPE + COMMA +
                COLUMN_NAME_LANGUAGE + TEXT_TYPE + COMMA +
                COLUMN_NAME_DISPLAY_NAME + TEXT_TYPE + COMMA +
                COLUMN_NAME_TITLE + TEXT_TYPE + COMMA +
                COLUMN_NAME_DESCRIPTION + TEXT_TYPE + ");";
    }

    public static String getDeleteEntries() {
        return "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static String[] getFullProjection(){
        String[] projection = {
                _ID,
                COLUMN_NAME_STATION_ID,
                COLUMN_NAME_LANGUAGE,
                COLUMN_NAME_DISPLAY_NAME,
                COLUMN_NAME_TITLE,
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

        values.put(COLUMN_NAME_STATION_ID, stationID);
        values.put(COLUMN_NAME_LANGUAGE, language);
        values.put(COLUMN_NAME_DISPLAY_NAME, displayName);
        values.put(COLUMN_NAME_TITLE, title);
        values.put(COLUMN_NAME_DESCRIPTION, description);

        return values;
    }

    @Override
    public boolean readFromCursor(Cursor cursor) {
        try {
            this.id = cursor.getLong(cursor.getColumnIndexOrThrow(_ID));
            this.stationID = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_NAME_STATION_ID));
            this.language = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_LANGUAGE));
            this.displayName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_DISPLAY_NAME));
            this.title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_TITLE));
            this.description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_DESCRIPTION));
            return true;
        }catch (Exception ex){
            return false;
        }
    }

    // ---------------------------------------
    // Public methods
    // ---------------------------------------
    public long getStationID() {
        return stationID;
    }
    public void setStationID(long stationID) {
        this.stationID = stationID;
    }

    public String getLanguage() {
        return language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }

    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
