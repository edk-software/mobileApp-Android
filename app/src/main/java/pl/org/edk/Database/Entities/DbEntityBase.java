package pl.org.edk.Database.Entities;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

/**
 * Created by pwawrzynek on 2015-12-15.
 */
public abstract class DbEntityBase implements BaseColumns {
    // ---------------------------------------
    // Constants
    // ---------------------------------------
    protected static String TEXT_TYPE = " TEXT";
    protected static String INTEGER_TYPE = " INTEGER";
    protected static String FLOAT_TYPE = " REAL";
    protected static String PRIMARY_KEY = " PRIMARY KEY";
    protected static String COMMA = ", ";

    protected static String DATE_FORMAT = "YYYY-MM-DD HH:MM:SS.SSS";

    // ---------------------------------------
    // Variables
    // ---------------------------------------
    protected long id;

    // ---------------------------------------
    // Methods
    // ---------------------------------------
    public abstract ContentValues getContentValues();
    public abstract boolean readFromCursor(Cursor cursor);

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
}
