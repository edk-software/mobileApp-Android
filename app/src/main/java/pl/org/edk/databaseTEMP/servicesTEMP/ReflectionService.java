package pl.org.edk.databaseTEMP.servicesTEMP;

import android.database.Cursor;
import pl.org.edk.databaseTEMP.entitiesTEMP.Reflection;
import pl.org.edk.databaseTEMP.entitiesTEMP.ReflectionList;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Admin on 2015-12-16.
 */
public class ReflectionService extends DbServiceBase {
    // ---------------------------------------
    // Insert
    // ---------------------------------------
    public boolean insertReflectionList(ReflectionList reflectionList){
        // Insert the list
        long listId = executeQueryInsert(ReflectionList.TABLE_NAME, reflectionList);
        if(listId <= 0)
            return false;
        reflectionList.setId(listId);

        // Insert the list's items
        boolean success = true;
        for(Reflection reflection : reflectionList.getReflections()){
            reflection.setReflectionList(reflectionList);
            success &= insertReflection(reflection);
        }

        return success;
    }

    public boolean insertReflection(Reflection reflection){
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
    public ArrayList<ReflectionList> getReflectionLists(){
        Cursor cursor = executeQueryGetAll(ReflectionList.TABLE_NAME, ReflectionList.getFullProjection());

        ArrayList<ReflectionList> lists = new ArrayList<>();
        for(int i = 0; i < cursor.getCount(); i++){
            cursor.moveToPosition(i);
            ReflectionList nextList = new ReflectionList();
            if(nextList.readFromCursor(cursor)){
                lists.add(nextList);
            }
        }
        return lists;
    }

    public ReflectionList getReflectionList(String language, int edition, boolean includeItems){
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
            list.setReflections(getReflections(list.getId()));

        return list;
    }

    public ReflectionList getReflectionList(String language, boolean includeItems){
        return getReflectionList(language, Calendar.getInstance().get(Calendar.YEAR), includeItems);
    }

    public ArrayList<Reflection> getReflections(long reflectionListId){
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
