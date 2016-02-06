package pl.org.edk.database.Entities;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by Admin on 2015-12-16.
 */
public class Reflection extends DbEntityBase {
    // ---------------------------------------
    // Constant variables
    // ---------------------------------------
    public static final String TABLE_NAME = "Reflection";
    public static final String COLUMN_NAME_LANGUAGE = "Language";
    public static final String COLUMN_NAME_LIST_ID = "ListId";
    public static final String COLUMN_NAME_STATION_INDEX = "StationIndex";
    public static final String COLUMN_NAME_DISPLAY_NAME = "DisplayName";
    public static final String COLUMN_NAME_CONTENT = "Content";

    // ---------------------------------------
    // Class variables
    // ---------------------------------------
    private String language;
    private long listId;
    private int stationIndex;
    private String displayName;
    private String content;
    // External tables
    private ReflectionList reflectionList;

    // ---------------------------------------
    // Static methods
    // ---------------------------------------
    public static String getCreateEntries() {
        return "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + INTEGER_TYPE + PRIMARY_KEY + COMMA +
                COLUMN_NAME_SERVER_ID + INTEGER_TYPE + COMMA +
                COLUMN_NAME_LANGUAGE + TEXT_TYPE + COMMA +
                COLUMN_NAME_LIST_ID + INTEGER_TYPE + COMMA +
                COLUMN_NAME_STATION_INDEX + INTEGER_TYPE + COMMA +
                COLUMN_NAME_DISPLAY_NAME + TEXT_TYPE + COMMA +
                COLUMN_NAME_CONTENT + TEXT_TYPE + ");";
    }

    public static String getDeleteEntries() {
        return "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static String[] getFullProjection(){
        String[] projection = {
                _ID,
                COLUMN_NAME_SERVER_ID,
                COLUMN_NAME_LANGUAGE,
                COLUMN_NAME_LIST_ID,
                COLUMN_NAME_STATION_INDEX,
                COLUMN_NAME_DISPLAY_NAME,
                COLUMN_NAME_CONTENT
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
        values.put(COLUMN_NAME_LANGUAGE, language);
        values.put(COLUMN_NAME_LIST_ID, listId);
        values.put(COLUMN_NAME_STATION_INDEX, stationIndex);
        values.put(COLUMN_NAME_DISPLAY_NAME, displayName);
        values.put(COLUMN_NAME_CONTENT, content);

        return values;
    }

    @Override
    public boolean readFromCursor(Cursor cursor) {
        try {
            this.id = cursor.getLong(cursor.getColumnIndexOrThrow(_ID));
            this.serverID = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_NAME_SERVER_ID));
            this.language = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_LANGUAGE));
            this.listId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_NAME_LIST_ID));
            this.stationIndex = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_LANGUAGE));
            this.displayName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_DISPLAY_NAME));
            this.content= cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_CONTENT));
            return true;
        }catch (Exception ex){
            return false;
        }
    }

    // ---------------------------------------
    // Public methods
    // ---------------------------------------
    public String getLanguage() {
        return language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }

    public long getListId() {
        return listId;
    }
    public void setListId(long listId) {
        this.listId = listId;
    }

    public int getStationIndex() {
        return stationIndex;
    }
    public void setStationIndex(int stationIndex) {
        this.stationIndex = stationIndex;
    }

    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public ReflectionList getReflectionList() {
        return reflectionList;
    }
    public void setReflectionList(ReflectionList reflectionList) {
        this.reflectionList = reflectionList;
        this.listId = reflectionList.getId();
    }
}
