package pl.org.edk.database.services;

import android.database.Cursor;

import pl.org.edk.database.entities.Reflection;
import pl.org.edk.database.entities.ReflectionList;

import java.io.File;
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
    public boolean insertReflectionList(ReflectionList reflectionList) {
        // Insert the list
        long listId = executeQueryInsert(reflectionList);
        if (listId <= 0)
            return false;
        reflectionList.setId(listId);

        // Insert the list's items
        boolean success = true;
        for (Reflection reflection : reflectionList.getReflections()) {
            reflection.setReflectionList(reflectionList);
            success &= insertReflection(reflection);
        }

        return success;
    }

    public boolean insertReflection(Reflection reflection) {
        long reflectionId = executeQueryInsert(reflection);
        if (reflectionId <= 0)
            return false;
        else {
            reflection.setId(reflectionId);
            return true;
        }
    }

    // ---------------------------------------
    // Update
    // ---------------------------------------
    public boolean updateReflectionList(ReflectionList reflectionList) {
        if (reflectionList.getId() <= 0) {
            throw new InvalidParameterException("Specified ReflectionList has Id=0!");
        }

        int count = executeQueryUpdate(reflectionList);
        if (count == 0) {
            if (!insertReflectionList(reflectionList)) {
                return false;
            }
        }

        for (Reflection reflection : reflectionList.getReflections()) {
            updateReflection(reflection);
        }
        return true;
    }

    public boolean updateReflectionListByVersion(ReflectionList list) {
        if (list.getEdition() == 0) {
            throw new InvalidParameterException("Specified ReflectionList has no edition set!");
        }
        if (list.getLanguage() == null || list.getLanguage().length() < 2) {
            throw new InvalidParameterException("Specified ReflectionList has no language set!");
        }

        ReflectionList previous = getReflectionList(list.getLanguage(), list.getEdition(), true);
        if (previous != null) {
            list.setId(previous.getId());

            // Rewrite Ids of all Reflections in the list
            for (Reflection reflection : list.getReflections()) {
                reflection.setReflectionList(list);
                Reflection previousReflection = previous.getReflection(reflection.getStationIndex());
                if (previousReflection != null) {
                    reflection.setId(previousReflection.getId());
                    reflection.setAudioLocalPath(previousReflection.getAudioLocalPath());
                }
            }

            return updateReflectionList(list);
        } else {
            return insertReflectionList(list);
        }
    }

    public boolean updateReflection(Reflection reflection) {
        // Try to update the route
        int count = executeQueryUpdate(reflection);
        if (count > 0) {
            return true;
        } else {
            return insertReflection(reflection);
        }
    }

    // ---------------------------------------
    // Get
    // ---------------------------------------
    public ArrayList<ReflectionList> getReflectionLists() {
        return getReflectionLists(false);
    }

    public ArrayList<ReflectionList> getReflectionLists(final boolean includeItems) {
        return Execute(new CursorCommand<ArrayList<ReflectionList>>() {
            @Override
            public Cursor GetCursor() {
                return executeQueryGetAll(ReflectionList.TABLE_NAME, ReflectionList.getFullProjection());
            }

            @Override
            public ArrayList<ReflectionList> Run(Cursor cursor) {
                ArrayList<ReflectionList> lists = new ArrayList<>();
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    ReflectionList nextList = new ReflectionList();
                    if (nextList.readFromCursor(cursor)) {
                        // Fetch the items
                        if (includeItems)
                            nextList.setReflections(getReflections(nextList.getId()));
                        lists.add(nextList);
                    }
                }
                return lists;
            }
        });
    }

    public ArrayList<ReflectionList> getReflectionLists(final String language, final boolean includeItems) {
        return Execute(new CursorCommand<ArrayList<ReflectionList>>() {
            @Override
            public Cursor GetCursor() {
                return executeQueryWhere(ReflectionList.TABLE_NAME, ReflectionList.getFullProjection(),
                        ReflectionList.COLUMN_NAME_LANGUAGE, language);
            }

            @Override
            public ArrayList<ReflectionList> Run(Cursor cursor) {
                // No Routes with this id found
                if (cursor.getCount() == 0)
                    return null;

                ArrayList<ReflectionList> lists = new ArrayList<>();
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    ReflectionList nextList = new ReflectionList();
                    if (nextList.readFromCursor(cursor)) {
                        // Fetch the items
                        if (includeItems)
                            nextList.setReflections(getReflections(nextList.getId()));
                        lists.add(nextList);
                    }
                }
                return lists;
            }
        });

    }

    public ReflectionList getReflectionList(String language, int edition, final boolean includeItems) {
        final ArrayList<String> whereColumns = new ArrayList<>(2);
        whereColumns.add(ReflectionList.COLUMN_NAME_LANGUAGE);
        whereColumns.add(ReflectionList.COLUMN_NAME_EDITION);
        final String[] whereValues = {language, String.valueOf(edition)};

        return Execute(new CursorCommand<ReflectionList>() {
            @Override
            public Cursor GetCursor() {
                return executeQueryWhere(ReflectionList.TABLE_NAME, ReflectionList.getFullProjection(), whereColumns, whereValues);
            }

            @Override
            public ReflectionList Run(Cursor cursor) {
                // No Routes with this id found
                if (cursor.getCount() == 0)
                    return null;

                // Get this Route info
                cursor.moveToFirst();
                ReflectionList list = new ReflectionList();
                list.readFromCursor(cursor);

                // Fetch the items
                if (includeItems)
                    list.setReflections(getReflections(list.getId()));

                return list;
            }
        });
    }

    public ReflectionList getReflectionList(String language, boolean includeItems) {
        return getReflectionList(language, Calendar.getInstance().get(Calendar.YEAR), includeItems);
    }

    public ArrayList<Reflection> getReflections(final long reflectionListId) {
        return Execute(new CursorCommand<ArrayList<Reflection>>() {
            @Override
            public Cursor GetCursor() {
                return executeQueryWhere(Reflection.TABLE_NAME, Reflection.getFullProjection(), Reflection.COLUMN_NAME_LIST_ID, String.valueOf(reflectionListId));
            }

            @Override
            public ArrayList<Reflection> Run(Cursor cursor) {
                ArrayList<Reflection> reflections = new ArrayList<>();
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    Reflection reflection = new Reflection();
                    if (reflection.readFromCursor(cursor))
                        reflections.add(reflection);
                }

                return reflections;
            }
        });

    }

    // ---------------------------------------
    // Remove
    // ---------------------------------------
    public int removeReflectionFiles() {
        int removedCount = 0;
        ArrayList<ReflectionList> lists = getReflectionLists();
        for (ReflectionList list : lists)
            removedCount = removeReflectionFiles(list);

        return removedCount;
    }

    public int removeReflectionFiles(String language, int edition) {
        ReflectionList list = getReflectionList(language, edition, true);
        return removeReflectionFiles(list);
    }

    public int removeReflectionFiles(ReflectionList list) {
        if (list == null)
            return 0;

        int removedCount = 0;
        for (Reflection reflection : list.getReflections()) {
            String path = reflection.getAudioLocalPath();
            if (path == null)
                continue;
            if (removeFile(path))
                removedCount++;
        }

        return removedCount;
    }

    private boolean removeFile(String path) {
        File file = new File(path);
        return file.delete();
    }
}
