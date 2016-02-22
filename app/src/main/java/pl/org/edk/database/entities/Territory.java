package pl.org.edk.database.entities;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;

/**
 * Created by pwawrzynek on 2015-12-15.
 */
public class Territory extends DbEntityBase {
    // ---------------------------------------
    // Constant variables
    // ---------------------------------------
    public static final String TABLE_NAME = "Territory";
    public static final String COLUMN_NAME_DISPLAY_NAME = "DisplayName";

    // ---------------------------------------
    // Constructors
    // ---------------------------------------
    public Territory() {
        areas = new ArrayList<>();
    }

    public Territory(String displayName) {
        this();
        this.displayName = displayName;
    }

    // ---------------------------------------
    // Class variables
    // ---------------------------------------
    private String displayName;
    // External tables
    private ArrayList<Area> areas;

    // ---------------------------------------
    // Static methods
    // ---------------------------------------
    public static String getCreateEntries() {
        return "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + INTEGER_TYPE + PRIMARY_KEY + COMMA +
                COLUMN_NAME_SERVER_ID + INTEGER_TYPE + COMMA +
                COLUMN_NAME_DISPLAY_NAME + TEXT_TYPE + ");";
    }

    public static String getDeleteEntries() {
        return "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static String[] getFullProjection(){
        String[] projection = {
                _ID,
                COLUMN_NAME_SERVER_ID,
                COLUMN_NAME_DISPLAY_NAME
        };
        return projection;
    }

    // ---------------------------------------
    // Base class methods
    // ---------------------------------------

    @Override
    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME_SERVER_ID, serverID);
        values.put(COLUMN_NAME_DISPLAY_NAME, displayName);

        return values;
    }

    @Override
    public boolean readFromCursor(Cursor cursor) {
        try {
            this.id = cursor.getLong(cursor.getColumnIndexOrThrow(_ID));
            this.serverID = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_NAME_SERVER_ID));
            this.displayName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_DISPLAY_NAME));
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
    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public ArrayList<Area> getAreas() {
        return areas;
    }
    public void setAreas(ArrayList<Area> areas) {
        this.areas = areas;
    }
}
