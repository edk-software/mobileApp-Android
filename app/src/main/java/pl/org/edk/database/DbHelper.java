package pl.org.edk.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import pl.org.edk.database.entities.*;

/**
 * Created by pwawrzynek on 2015-12-15.
 */
public class DbHelper extends SQLiteOpenHelper {
    private DbUpgrader dbUpgrader;

    public static final int DB_VERSION = 2;
    public static final String DB_FILE_NAME = "Edk.db";

    public DbHelper(Context context){
        super(context, DB_FILE_NAME, null, DB_VERSION);
        dbUpgrader = new DbUpgrader(this);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(Area.getCreateEntries());
        db.execSQL(Reflection.getCreateEntries());
        db.execSQL(ReflectionList.getCreateEntries());
        db.execSQL(Route.getCreateEntries());
        db.execSQL(RouteDesc.getCreateEntries());
        db.execSQL(Station.getCreateEntries());
        db.execSQL(StationDesc.getCreateEntries());
        db.execSQL(Territory.getCreateEntries());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        dbUpgrader.Upgrade(db, oldVersion, newVersion);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
        onUpgrade(db, oldVersion, newVersion);
    }

    public void delete(Context context) {
        context.deleteDatabase(DB_FILE_NAME);
    }
}
