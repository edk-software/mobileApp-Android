package pl.org.edk.Database.Entities;

import android.content.ContentValues;
import android.database.Cursor;

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
    public static final String COLUMN_NAME_RELEASE_DATE = "ReleaseDate";
    public static final String COLUMN_NAME_DISPLAY_NAME = "DisplayName";
    public static final String COLUMN_NAME_DESCRIPTION = "Description";

    // ---------------------------------------
    // Class variables
    // ---------------------------------------
    private int routeID;
    private String language;
    private String releaseDate;
    private String displayName;
    private String description;

    // ---------------------------------------
    // Static methods
    // ---------------------------------------
    public static String getCreateEntries() {
        return "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + INTEGER_TYPE + PRIMARY_KEY +
                COMMA + COLUMN_NAME_ROUTE_ID + INTEGER_TYPE +
                COMMA + COLUMN_NAME_LANGUAGE + TEXT_TYPE +
                COMMA + COLUMN_NAME_RELEASE_DATE + TEXT_TYPE +
                COMMA + COLUMN_NAME_DISPLAY_NAME + TEXT_TYPE +
                COMMA + COLUMN_NAME_DESCRIPTION + TEXT_TYPE +
                ");";
    }

    public static String getDeleteEntries() {
        return "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static String[] getFullProjection(){
        String[] projection = {
                _ID,
                COLUMN_NAME_ROUTE_ID,
                COLUMN_NAME_LANGUAGE,
                COLUMN_NAME_RELEASE_DATE,
                COLUMN_NAME_DISPLAY_NAME,
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
        values.put(COLUMN_NAME_RELEASE_DATE, releaseDate);
        values.put(COLUMN_NAME_DISPLAY_NAME, displayName);
        values.put(COLUMN_NAME_DESCRIPTION, description);

        return values;
    }

    @Override
    public boolean readFromCursor(Cursor cursor) {
        try {
            this.id = cursor.getLong(cursor.getColumnIndexOrThrow(_ID));
            this.routeID = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_ROUTE_ID));
            this.language = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_LANGUAGE));
            this.releaseDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_RELEASE_DATE));
            this.displayName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_DISPLAY_NAME));
            this.description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_DESCRIPTION));
            return true;
        }catch (Exception ex){
            return false;
        }
    }

    // ---------------------------------------
    // Public methods
    // ---------------------------------------

    public int getRouteID() {
        return routeID;
    }
    public void setRouteID(int routeID) {
        this.routeID = routeID;
    }

    public String getLanguage() {
        return language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }

    public String getReleaseDate() {
        return releaseDate;
    }
    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
