package pl.org.edk.managers;

import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by pwawrzynek on 2016-02-10.
 */
public class FileDownloadManager {
    // ---------------------------------------
    // Class members
    // ---------------------------------------
    private Context mContext;

    private boolean mDisplayNotification = false;
    private String mFinishedText;

    private NotificationManager mNotifyManager;
    private Builder mBuilder;

    // ---------------------------------------
    // Constructors
    // ---------------------------------------
    public FileDownloadManager(Context context){
        this.mContext = context;
    }

    // ---------------------------------------
    // Public methods
    // ---------------------------------------
    public void setNotificationDetails(int icon, String title, String finishedText){
        mDisplayNotification = true;

        mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setContentTitle(title)
                .setSmallIcon(icon);

        mFinishedText = finishedText;
    }

    public void downloadFile(String serverPath, String localFilePath){
        final DownloadTask downloadTask = new DownloadTask(mContext.getApplicationContext());
        downloadTask.execute(serverPath, localFilePath);
    }

    // ---------------------------------------
    // Subclasses
    // ---------------------------------------
    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context mContext;

        public DownloadTask(Context context) {
            this.mContext = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                // Connect to the server
                URL serverUrl = new URL(sUrl[0]);
                connection = (HttpURLConnection) serverUrl.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();
                }

                // Download the file
                input = connection.getInputStream();
                output = new FileOutputStream(sUrl[1]);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // Cancel, if requested
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }

                    // TEMP
                    publishProgress((int) (total * 100 / 80000));
                    Thread.sleep(500);

                    // Download next piece of data
                    total += count;
                    output.write(data, 0, count);

                    // Update the progress
                    int fileLength = connection.getContentLength();
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Display the progress bar
            if(mDisplayNotification) {
                mBuilder.setProgress(100, 0, false);
                mNotifyManager.notify(1, mBuilder.build());
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            // Update the progress value
            if(mDisplayNotification) {
                mBuilder.setProgress(100, values[0], false)
                        .setContentText(String.valueOf(values[0]) + "%");
                mNotifyManager.notify(1, mBuilder.build());
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // Remove the progress bar
            if(mDisplayNotification) {
                mBuilder.setContentText(mFinishedText);
                mBuilder.setProgress(0, 0, false);
                mNotifyManager.notify(1, mBuilder.build());
            }
        }
    }
}
