package pl.org.edk.util;

import com.google.gson.Gson;
import java.lang.reflect.Type;

/**
 * Created by pwawrzynek on 2016-01-31.
 */
public class JsonHelper {
    public static <T> T deserializeFromJson(String jsonString, Type objectType){
        Gson gson = new Gson();
        T object = gson.fromJson(jsonString, objectType);
        return object;
    }

    public String serializeToJson(Object objectToSave, Type objectType){
        Gson gson = new Gson();
        return gson.toJson(objectToSave, objectType);
    }
}
