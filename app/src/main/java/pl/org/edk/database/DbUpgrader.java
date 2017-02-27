package pl.org.edk.database;

import android.database.sqlite.SQLiteDatabase;

import pl.org.edk.database.entities.DbEntityBase;
import pl.org.edk.database.entities.Route;
import pl.org.edk.database.entities.RouteDesc;
import pl.org.edk.database.entities.Station;
import pl.org.edk.database.entities.StationDesc;

/**
 * Created by pwawrzynek on 2017-02-22.
 */

public class DbUpgrader {
    private final DbHelper helper;

    public DbUpgrader(DbHelper helper){
        this.helper = helper;
    }

    public boolean Upgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        for(int i = oldVersion+1; i <= newVersion; i++){
            if(!UpgradeToVersion(db, i))
                return false;
        }
        return true;
    }

    private boolean UpgradeToVersion(SQLiteDatabase db, int nextVersion) {
        switch (nextVersion)
        {
            case 2:
                return UpgradeTo2(db);
            default:
                return false;
        }
    }

    private boolean UpgradeTo2(SQLiteDatabase db) {
        for(String query : Route.getUpdateTwo())
            db.execSQL(query);

        return true;
    }
}
