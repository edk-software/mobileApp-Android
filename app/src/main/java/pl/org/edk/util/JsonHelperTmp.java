package pl.org.edk.util;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * Created by pwawrzynek on 2016-01-31.
 */
public class JsonHelper {
    public static <T> T deserializeFromJson(String jsonString, Type type) {
        try{
            Gson gson = new Gson();
            T object = gson.fromJson(jsonString, type);
            return object;
        }
        catch (Exception ex){
            return null;
        }
    }

    public static <T> T deserializeFromJson(String jsonString, Type conversionType, Object typeConverter) {
        try {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(conversionType, typeConverter)
                    .create();
            return gson.fromJson(jsonString, new TypeToken<T>() {}.getType());
        } catch (Exception ex) {
            return null;
        }
    }

    public static <T> T deserializeFromJson(String jsonString, Map<Type, Object> typeConverters) {
        try {
            GsonBuilder builder = new GsonBuilder();
            for (Map.Entry<Type, Object> converter : typeConverters.entrySet()) {
                builder.registerTypeAdapter(converter.getKey(), converter.getValue());
            }

            Gson gson = builder.create();
            return gson.fromJson(jsonString, new TypeToken<T>() {
            }.getType());
        } catch (Exception ex) {
            return null;
        }
    }

    public String serializeToJson(Object objectToSave, Type objectType) {
        Gson gson = new Gson();
        return gson.toJson(objectToSave, objectType);
    }
}
