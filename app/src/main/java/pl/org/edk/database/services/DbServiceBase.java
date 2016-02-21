package pl.org.edk.database.services;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import pl.org.edk.database.DbHelper;
import pl.org.edk.database.entities.DbEntityBase;

import java.util.ArrayList;

/**
 * Created by pwawrzynek on 2015-12-15.
 */
public class DbServiceBase {
    // ---------------------------------------
    // Class variables
    // ---------------------------------------
    private static DbHelper dbClient;

    // ---------------------------------------
    // Protected methods
    // ---------------------------------------
    protected SQLiteDatabase dbRead(){
        return dbClient.getReadableDatabase();
    }

    protected SQLiteDatabase dbWrite(){
        return dbClient.getWritableDatabase();
    }

    protected long executeQueryInsert(DbEntityBase entity){
        ContentValues values = entity.getContentValues();
        return dbWrite().insert(entity.getTableName(), null, values);
    }

    protected int executeQueryUpdate(DbEntityBase entity){
        if(entity.getId() <= 0)
            return 0;

        ContentValues values = entity.getContentValues();
        String whereClause = DbEntityBase._ID + " = ? ";
        return dbWrite().update(entity.getTableName(), values, whereClause, new String[]{String.valueOf(entity.getId())});
    }

    protected int executeQueryUpdate(DbEntityBase entity, String comparisonColumn, String value){
        ContentValues values = entity.getContentValues();
        String whereClause = comparisonColumn + " = ? ";
        return dbWrite().update(entity.getTableName(), values, whereClause, new String[]{value});
    }

    protected int executeQueryUpdate(DbEntityBase entity, ArrayList<String> whereColumns, String[] whereArgs){
        if(entity.getId() <= 0)
            return 0;

        ContentValues values = entity.getContentValues();

        // Format the statement
        String whereStatement = "";
        for(int i = 0; i < whereColumns.size(); i++){
            String column = whereColumns.get(i);
            whereStatement += column + " = ? ";
            if(i + 1 < whereColumns.size())
                whereStatement += "AND ";
        }

        return dbWrite().update(entity.getTableName(), values, whereStatement, whereArgs);
    }

    protected Cursor executeQueryGetAll(String tableName, String[] columns){
        return dbRead().query(tableName, columns, null, null, null, null, null);
    }

    protected Cursor executeQueryWhere(String tableName, String[] columns, ArrayList<String> whereColumns, String[] whereArgs){
        // Format the statement
        String whereStatement = "";
        for(int i = 0; i < whereColumns.size(); i++){
            String column = whereColumns.get(i);
            whereStatement += column + " = ? ";
            if(i + 1 < whereColumns.size())
                whereStatement += "AND ";
        }
        return dbRead().query(tableName, columns, whereStatement, whereArgs, null, null, null);
    }

    protected Cursor executeQueryWhere(String tableName, String[] columns, String interestColumn, String interestValue){
        // Put the single values into containers
        ArrayList<String> whereColumns = new ArrayList<>();
        whereColumns.add(interestColumn);
        String[] whereValues = {interestValue};

        return executeQueryWhere(tableName, columns, whereColumns, whereValues);
    }

    // ---------------------------------------
    // Public methods
    // ---------------------------------------
    public static void init(DbHelper dbClient){
        DbServiceBase.dbClient = dbClient;
    }
}
