package pl.org.edk.database.services;

import android.database.Cursor;
import pl.org.edk.database.entities.Reflection;
import pl.org.edk.database.entities.ReflectionList;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by pwawrzynek on 2015-12-16.
 */
public class ReflectionService extends DbServiceBase {
    // ---------------------------------------
    // Insert
    // ---------------------------------------
    public boolean insertReflectionList(ReflectionList reflectionList){
        // Insert the list
        long listId = executeQueryInsert(reflectionList);
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
        long reflectionId = executeQueryInsert(reflection);
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
    public boolean updateReflectionList(ReflectionList reflectionList){
        if(reflectionList.getId() <= 0){
            throw new InvalidParameterException("Specified ReflectionList has Id=0!");
        }

        int count = executeQueryUpdate(reflectionList);
        if(count == 0){
            if(!insertReflectionList(reflectionList)){
                return false;
            }
        }

        for (Reflection reflection : reflectionList.getReflections()){
            updateReflection(reflection);
        }
        return true;
    }

    public boolean updateReflectionListByVersion(ReflectionList list) {
        if(list.getEdition() == 0){
            throw new InvalidParameterException("Specified ReflectionList has no edition set!");
        }
        if(list.getLanguage() == null || list.getLanguage().length() < 2){
            throw new InvalidParameterException("Specified ReflectionList has no language set!");
        }

        ReflectionList previous = getReflectionList(list.getLanguage(), list.getEdition(), true);
        if(previous != null){
            list.setId(previous.getId());

            // Rewrite Ids of all Reflections in the list
            for(Reflection reflection : list.getReflections()){
                reflection.setReflectionList(list);
                Reflection previousReflection = previous.getReflection(reflection.getStationIndex());
                if(previousReflection != null){
                    reflection.setId(previousReflection.getId());
                    reflection.setAudioLocalPath(previousReflection.getAudioLocalPath());
                }
            }

            return updateReflectionList(list);
        }
        else {
            return insertReflectionList(list);
        }
    }

    public boolean updateReflection(Reflection reflection){
        // Try to update the route
        int count = executeQueryUpdate(reflection);
        if(count > 0) {
            return true;
        }
        else {
            return insertReflection(reflection);
        }
    }

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
