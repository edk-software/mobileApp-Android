package pl.org.edk.webServices.deserializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import pl.org.edk.database.entities.ReflectionList;
import pl.org.edk.util.JsonHelper;

/**
 * Created by pwawrzynek on 2017-03-13.
 */

public class ReflectionListDeserializer implements JsonDeserializer<ReflectionList> {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public ReflectionList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) throws JsonParseException {
        Map.Entry<String, JsonElement> entry = json.getAsJsonObject().entrySet().iterator().next();
        if (entry == null)
            return null;

        // Get date
        Date date;
        try {
            date = DATE_FORMAT.parse(entry.getValue().getAsString());
        } catch (ParseException e) {
            e.printStackTrace();
            date = null;
        }

        // Get edition
        Integer edition = Integer.parseInt(entry.getKey());

        // Create result instance and return it
        ReflectionList list = new ReflectionList();
        list.setEdition(edition);
        list.setReleaseDate(date);
        return  list;
    }

    public static ArrayList<ReflectionList> createListFromJson(String json){
        Map<String, String> items = JsonHelper.deserializeFromJson(json, new TypeToken<Map<String, String>>() {}.getType());

        ArrayList<ReflectionList> lists = new ArrayList<>();
        for (Map.Entry<String, String> item : items.entrySet()){
            lists.add(createFromJson(item));
        }
        return lists;
    }

    private static ReflectionList createFromJson(Map.Entry<String, String> entry){
        // Get date
        Date date;
        try {
            date = DATE_FORMAT.parse(entry.getValue());
        } catch (ParseException e) {
            e.printStackTrace();
            date = null;
        }

        // Get edition
        Integer edition = Integer.parseInt(entry.getKey());

        // Create result instance and return it
        ReflectionList list = new ReflectionList();
        list.setEdition(edition);
        list.setReleaseDate(date);
        return  list;
    }
}
