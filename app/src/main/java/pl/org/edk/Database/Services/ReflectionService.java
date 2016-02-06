package pl.org.edk.database.Services;

import android.database.Cursor;
import pl.org.edk.database.Entities.Reflection;
import pl.org.edk.database.Entities.ReflectionList;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Admin on 2015-12-16.
 */
public class ReflectionService extends DbServiceBase {
    // ---------------------------------------
    // Insert
    // ---------------------------------------
    public boolean InsertReflectionList(ReflectionList reflectionList){
        // Insert the list
        long listId = executeQueryInsert(ReflectionList.TABLE_NAME, reflectionList);
        if(listId <= 0)
            return false;
        reflectionList.setId(listId);

        // Insert the list's items
        boolean success = true;
        for(Reflection reflection : reflectionList.getReflections()){
            reflection.setReflectionList(reflectionList);
            success &= InsertReflection(reflection);
        }

        return success;
    }

    public boolean InsertReflection(Reflection reflection){
        long reflectionId = executeQueryInsert(Reflection.TABLE_NAME, reflection);
        if(reflectionId <= 0)
            return false;
        else {
            reflection.setId(reflectionId);
            return true;
        }
    }

    // ---------------------------------------
    // Update
    // ---------------------------------------

    // ---------------------------------------
    // Get
    // ---------------------------------------
    public ReflectionList GetReflectionList(String language, int edition, boolean includeItems){
        ArrayList<String> whereColumns = new ArrayList<>(2);
        whereColumns.add(ReflectionList.COLUMN_NAME_LANGUAGE);
        whereColumns.add(ReflectionList.COLUMN_NAME_EDITION);
        String[] whereValues = {language, String.valueOf(edition) };

        Cursor cursor = executeQueryWhere(ReflectionList.TABLE_NAME, ReflectionList.getFullProjection(), whereColumns, whereValues);

        // No Routes with this id found
        if(cursor.getCount() == 0)
            return null;

        // Get this Route info
        cursor.moveToFirst();
        ReflectionList list = new ReflectionList();
        list.readFromCursor(cursor);

        // Fetch the items
        if(includeItems)
            list.setReflections(GetReflections(list.getId()));

        return list;
    }

    public ReflectionList GetReflectionList(String language, boolean includeItems){
        return GetReflectionList(language, Calendar.getInstance().get(Calendar.YEAR), includeItems);
    }

    public ArrayList<Reflection> GetReflections(long reflectionListId){
        Cursor cursor = executeQueryWhere(Reflection.TABLE_NAME, Reflection.getFullProjection(), Reflection.COLUMN_NAME_LIST_ID, String.valueOf(reflectionListId));

        ArrayList<Reflection> reflections = new ArrayList<>();
        for(int i=0; i < cursor.getCount(); i++){
            cursor.moveToPosition(i);
            Reflection reflection = new Reflection();
            if(reflection.readFromCursor(cursor))
                reflections.add(reflection);
        }

        return reflections;
    }
}
