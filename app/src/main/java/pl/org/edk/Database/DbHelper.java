package pl.org.edk.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import pl.org.edk.Database.Entities.*;

/**
 * Created by pwawrzynek on 2015-12-15.
 */
public class DbHelper extends SQLiteOpenHelper {
    public static final int DB_VERSION = 1;
    public static final String DB_FILE_NAME = "Edk.db";

    public DbHelper(Context context){
        super(context, DB_FILE_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        /* NOTE: Create all tables here */
        db.execSQL(District.getCreateEntries());
        db.execSQL(Reflection.getCreateEntries());
        db.execSQL(ReflectionList.getCreateEntries());
        db.execSQL(Region.getCreateEntries());
        db.execSQL(Route.getCreateEntries());
        db.execSQL(RouteDesc.getCreateEntries());
        db.execSQL(Station.getCreateEntries());
        db.execSQL(StationDesc.getCreateEntries());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        /* NOTE: Delete all tables here */
        db.execSQL(District.getDeleteEntries());
        db.execSQL(Reflection.getDeleteEntries());
        db.execSQL(ReflectionList.getDeleteEntries());
        db.execSQL(Region.getDeleteEntries());
        db.execSQL(Route.getDeleteEntries());
        db.execSQL(RouteDesc.getDeleteEntries());
        db.execSQL(Station.getDeleteEntries());
        db.execSQL(StationDesc.getDeleteEntries());

        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
        onUpgrade(db, oldVersion, newVersion);
    }
}
