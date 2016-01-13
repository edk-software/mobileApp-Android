package pl.org.edk.Database.Services;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import pl.org.edk.Database.DbHelper;

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

    protected Cursor executeQuery(String tableName, String[] columns, ArrayList<String> whereColumns, String[] whereArgs){
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

    protected Cursor executeQuery(String tableName, String[] columns, String interestColumn, String interestValue){
        // Put the single values into containers
        ArrayList<String> whereColumns = new ArrayList<>();
        whereColumns.add(interestColumn);
        String[] whereValues = {interestValue};

        return executeQuery(tableName, columns, whereColumns, whereValues);
    }

    // ---------------------------------------
    // Public methods
    // ---------------------------------------
    public static void Init(DbHelper dbClient){
        DbServiceBase.dbClient = dbClient;
    }
}
