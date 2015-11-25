package pl.org.edk.EdkMobile.Managers;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;

/**
 * Created by Pawel on 2015-03-07.
 */
public class JsonManager {
    private Context context;

    public JsonManager(Context context){
        this.context = context;
    }

    private void writeToFile(String data, String fileName) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(fileName, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private String readFromFile(String fileName) {
        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(fileName);
            ret = readFromFile(inputStream);
        }
        catch (FileNotFoundException e) {
            Log.e("Json - read from file", "File not found: " + e.toString());
        }

        return ret;
    }

    private String readFromFile(InputStream inputStream){
        String ret = null;
        try{
            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (IOException e) {
            Log.e("Json - read from file", "Can not read file: " + e.toString());
        }
        return ret;
    }

    public void saveJson(Object objectToSave, Type objectType){
        Gson gson = new Gson();
        String jsonString = gson.toJson(objectToSave, objectType);
    }

    public <T> T readAssetJson(String fileName, Type objectType) throws IOException {
        InputStream stream = context.getAssets().open(fileName);
        String jsonString = readFromFile(stream);
        Gson gson = new Gson();
        T object = gson.fromJson(jsonString, objectType);
        return object;
    }
}
