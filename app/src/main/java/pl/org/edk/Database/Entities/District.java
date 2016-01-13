package pl.org.edk.Database.Entities;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by pwawrzynek on 2015-12-15.
 */
public class District extends DbEntityBase {
    // ---------------------------------------
    // Constant variables
    // ---------------------------------------
    public static final String TABLE_NAME = "District";
    public static final String COLUMN_NAME_DISPLAY_NAME = "DisplayName";

    // ---------------------------------------
    // Class variables
    // ---------------------------------------
    private String displayName;

    // ---------------------------------------
    // Static methods
    // ---------------------------------------
    public static String getCreateEntries() {
        return "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + INTEGER_TYPE + PRIMARY_KEY +
                COMMA + COLUMN_NAME_DISPLAY_NAME + TEXT_TYPE +
                ");";
    }

    public static String getDeleteEntries() {
        return "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static String[] getFullProjection(){
        String[] projection = {
                _ID,
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

        values.put(COLUMN_NAME_DISPLAY_NAME, displayName);

        return values;
    }

    @Override
    public boolean readFromCursor(Cursor cursor) {
        try {
            this.id = cursor.getLong(cursor.getColumnIndexOrThrow(_ID));
            this.displayName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_DISPLAY_NAME));
            return true;
        }catch (Exception ex){
            return false;
        }
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
}
