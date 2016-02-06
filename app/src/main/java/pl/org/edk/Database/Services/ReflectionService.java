package pl.org.edk.database.Services;

import pl.org.edk.database.Entities.Reflection;
import pl.org.edk.database.Entities.ReflectionList;

import java.util.ArrayList;

/**
 * Created by Admin on 2015-12-16.
 */
public class ReflectionService extends DbServiceBase {
    // ---------------------------------------
    // Insert
    // ---------------------------------------
    public void InsertReflectionListData(ReflectionList reflectionList)
    {

    }

    public void InsertReflections(ArrayList<Reflection> reflections)
    {

    }

    // ---------------------------------------
    // Get
    // ---------------------------------------
    public ReflectionList GetReflectionListData(String language, int edition)
    {
        return null;
    }

    public ArrayList<Reflection> GetReflections(String language, int edition)
    {
        return null;
    }
}
