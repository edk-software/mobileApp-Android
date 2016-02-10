package pl.org.edk.managers;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pwawrzynek on 2016-01-31.
 */
public class HttpManager {
    // ---------------------------------------
    // Class members
    // ---------------------------------------
    private String mServerPath;
    private HttpURLConnection mConnection;

    // ---------------------------------------
    // Constructors
    // ---------------------------------------
    public HttpManager(String serverPath) {
        this.mServerPath = serverPath;
    }

    // ---------------------------------------
    // Public methods
    // ---------------------------------------
    public String callMethod(String methodName, HashMap<String, String> parameters) {
        String targetPath = methodName;
        if (parameters != null && parameters.size() > 0) {
            targetPath += "?";
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                targetPath += key + "=" + value + "&";
            }
            // Remove the last "&"
            targetPath.substring(0, targetPath.length() - 1);
        }
        if (!openConnection(targetPath))
            return null;

        try {
            InputStream responseStream = new BufferedInputStream(mConnection.getInputStream());
            return readStreamToString(responseStream);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            mConnection.disconnect();
        }

        return null;
    }

    // ---------------------------------------
    // Private methods
    // ---------------------------------------
    private boolean openConnection(String methodName) {
        try {
            URL url = new URL(this.mServerPath + "/" + methodName);
            mConnection = (HttpURLConnection) url.openConnection();
            return true;
        } catch (MalformedURLException e) {
            // The specified value is not an URL
            e.printStackTrace();
            if (mConnection != null)
                mConnection.disconnect();
            return false;
        } catch (IOException e) {
            // Cannot connect to the specified URL
            e.printStackTrace();
            if (mConnection != null)
                mConnection.disconnect();
            return false;
        }
    }

    private static String readStreamToString(InputStream stream) {
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

    private static byte[] readStreamToBytes(InputStream stream) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        try {
            while ((nRead = stream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return buffer.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}