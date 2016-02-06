package pl.org.edk.managers;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Admin on 2016-01-31.
 */
public class HttpManager {
    // ---------------------------------------
    // Class members
    // ---------------------------------------
    private String serverPath;
    private HttpURLConnection connection;

    // ---------------------------------------
    // Constructors
    // ---------------------------------------
    public HttpManager(String serverPath){
        this.serverPath = serverPath;
    }

    // ---------------------------------------
    // Public methods
    // ---------------------------------------
    public String sendRequest(String methodName, HashMap<String, String> parameters){
        String targetPath = methodName;
        if(parameters != null && parameters.size() > 0){
            targetPath += "?";
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                targetPath += key + "=" + value + "&";
            }
            // Remove the last "&"
            targetPath.substring(0, targetPath.length()-1);
        }
        if(!openConnection(targetPath))
            return null;

        try {
            InputStream responseStream = new BufferedInputStream(connection.getInputStream());
            return readStream(responseStream);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ex){
            String as = ex.getMessage();
            ex.printStackTrace();
        } finally {
            connection.disconnect();
        }

        return null;
    }

    // ---------------------------------------
    // Public methods
    // ---------------------------------------
    private boolean openConnection(String methodName){
        try {
            URL url = new URL(this.serverPath + "/" + methodName);
            connection = (HttpURLConnection) url.openConnection();
            //connection.setRequestMethod("GET");
            return true;
        } catch (MalformedURLException e) {
            // The specified value is not an URL
            e.printStackTrace();
            if (connection != null)
                connection.disconnect();
            return false;
        } catch (IOException e) {
            // Cannot connect to the specified URL
            e.printStackTrace();
            if (connection != null)
                connection.disconnect();
            return false;
        }
    }

    private String readStream(InputStream stream){
        BufferedReader rd = new BufferedReader(new InputStreamReader(stream));
        String line;
        StringBuffer response = new StringBuffer();
        try {
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
