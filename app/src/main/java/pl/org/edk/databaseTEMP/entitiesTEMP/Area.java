package pl.org.edk.databaseTEMP.entitiesTEMP;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by Admin on 2015-12-15.
 */
public class Area extends DbEntityBase {
    // ---------------------------------------
    // Constant variables
    // ---------------------------------------
    public static final String TABLE_NAME = "Area";
    public static final String COLUMN_NAME_TERRITORY_ID = "TerritoryID";
    public static final String COLUMN_NAME_DISPLAY_NAME = "DisplayName";

    // ---------------------------------------
    // Constructors
    // ---------------------------------------
    public Area() {
    }

    public Area(String displayName) {
        this.displayName = displayName;
    }

    // ---------------------------------------
    // Class variables
    // ---------------------------------------
    private long territoryId;
    private String displayName;

    // ---------------------------------------
    // Static methods
    // ---------------------------------------
    public static String getCreateEntries() {
        return "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + INTEGER_TYPE + PRIMARY_KEY + COMMA +
                COLUMN_NAME_SERVER_ID + INTEGER_TYPE + COMMA +
                COLUMN_NAME_TERRITORY_ID + INTEGER_TYPE + COMMA +
                COLUMN_NAME_DISPLAY_NAME + TEXT_TYPE + ");";
    }

    public static String getDeleteEntries() {
        return "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static String[] getFullProjection(){
        String[] projection = {
                _ID,
                COLUMN_NAME_SERVER_ID,
                COLUMN_NAME_TERRITORY_ID,
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
        values.put(COLUMN_NAME_TERRITORY_ID, territoryId);
        values.put(COLUMN_NAME_DISPLAY_NAME, displayName);

        return values;
    }

    @Override
    public boolean readFromCursor(Cursor cursor) {
        try {
            this.id = cursor.getLong(cursor.getColumnIndexOrThrow(_ID));
            this.serverID = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_NAME_SERVER_ID));
            this.territoryId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_NAME_TERRITORY_ID));
            this.displayName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_DISPLAY_NAME));
            return true;
        }catch (Exception ex){
            return false;
        }
    }

    // ---------------------------------------
    // Public methods
    // ---------------------------------------
    public long getTerritoryId() {
        return territoryId;
    }
    public void setTerritoryId(long territoryId) {
        this.territoryId = territoryId;
    }

    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
