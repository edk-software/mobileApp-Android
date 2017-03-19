package pl.org.edk.database.entities;

import android.content.ContentValues;
import android.database.Cursor;

import pl.org.edk.util.NumConverter;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by pwawrzynek on 2015-12-16.
 */
public class ReflectionList extends DbEntityBase {
    // ---------------------------------------
    // Constant variables
    // ---------------------------------------
    public static final String TABLE_NAME = "ReflectionList";
    public static final String COLUMN_NAME_LANGUAGE = "Language";
    public static final String COLUMN_NAME_EDITION = "Edition";
    public static final String COLUMN_NAME_RELEASE_DATE = "ReleaseDate";

    // ---------------------------------------
    // Class variables
    // ---------------------------------------
    private String language;
    private int edition;
    private String releaseDate;
    // External tables
    private ArrayList<Reflection> reflections;

    // ---------------------------------------
    // Constructors
    // ---------------------------------------
    public ReflectionList() {
        reflections = new ArrayList<>();
    }

    // ---------------------------------------
    // Static methods
    // ---------------------------------------
    public static String getCreateEntries() {
        return "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + INTEGER_TYPE + PRIMARY_KEY + COMMA +
                COLUMN_NAME_SERVER_ID + INTEGER_TYPE + COMMA +
                COLUMN_NAME_LANGUAGE + TEXT_TYPE + COMMA +
                COLUMN_NAME_EDITION + INTEGER_TYPE + COMMA +
                COLUMN_NAME_RELEASE_DATE + TEXT_TYPE + ");";
    }

    public static String getDeleteEntries() {
        return "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static String[] getFullProjection() {
        String[] projection = {
                _ID,
                COLUMN_NAME_SERVER_ID,
                COLUMN_NAME_LANGUAGE,
                COLUMN_NAME_EDITION,
                COLUMN_NAME_RELEASE_DATE
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
        values.put(COLUMN_NAME_EDITION, edition);
        values.put(COLUMN_NAME_RELEASE_DATE, releaseDate);

        return values;
    }

    @Override
    public boolean readFromCursor(Cursor cursor) {
        try {
            this.id = cursor.getLong(cursor.getColumnIndexOrThrow(_ID));
            this.serverID = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_NAME_SERVER_ID));
            this.language = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_LANGUAGE));
            this.edition = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_EDITION));
            this.releaseDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_RELEASE_DATE));
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
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

    public int getEdition() {
        return edition;
    }

    public void setEdition(int edition) {
        this.edition = edition;
    }

    public Date getReleaseDate() {
        return NumConverter.stringToDate(this.releaseDate);
    }

    public void setReleaseDate(Date date) {
        this.releaseDate = NumConverter.dateToString(date);
    }

    public ArrayList<Reflection> getReflections() {
        return reflections;
    }

    public void setReflections(ArrayList<Reflection> reflections) {
        this.reflections = reflections;
    }

    public boolean hasAnyAudio() {
        return getFilesCount() > 0;
    }

    public boolean hasAllAudio() {
        return getFilesCount() == reflections.size();
    }

    public Reflection getReflection(int stationIndex) {
        if (reflections == null) {
            return null;
        }

        for (Reflection reflection : reflections) {
            if (reflection.getStationIndex() == stationIndex) {
                return reflection;
            }
        }

        return null;
    }

    public int getFilesCount() {
        if (reflections == null || reflections.size() == 0) {
            return 0;
        }

        int count = 0;
        for (Reflection reflection : reflections) {
            if (reflection.hasAudio()) {
                count++;
            }
        }
        return count;
    }
}
