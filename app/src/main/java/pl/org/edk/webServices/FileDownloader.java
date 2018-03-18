package pl.org.edk.webServices;

import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import pl.org.edk.Settings;

/**
 * Created by pwawrzynek on 2016-02-10.
 */
public class FileDownloader {
    // ---------------------------------------
    // Class members
    // ---------------------------------------
    private Context mContext;
    private OnDownloadEventListener mListener;

    private boolean mDisplayNotification = false;
    private String mProgressText;
    private String mFinishedText;

    private NotificationManager mNotifyManager;
    private Builder mBuilder;

    // ---------------------------------------
    // Constructors
    // ---------------------------------------
    public FileDownloader(Context context){
        this.mContext = context;
    }

    // ---------------------------------------
    // Public methods
    // ---------------------------------------
    public void setNotificationDetails(int icon, String title, String progressText, String finishedText){
        mDisplayNotification = true;

        mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(mContext, Settings.NOTIFICATION_CHANNEL_ID);
        mBuilder.setContentTitle(title)
                .setContentText("")
                .setSmallIcon(icon);

        mProgressText = progressText;
        mFinishedText = finishedText;
    }

    public void setListener(OnDownloadEventListener listener){
        this.mListener = listener;
    }

    public AsyncTask<String, Integer, DownloadResult> downloadFileAsync(String serverPath, String localFilePath){
        final DownloadTask downloadTask = new DownloadTask(mContext.getApplicationContext());
        return downloadTask.execute(serverPath, localFilePath);
    }

    public DownloadResult downloadFile(String serverPath, String localFilePath) {
        final DownloadTask downloadTask = new DownloadTask(mContext.getApplicationContext());
        try {
            return downloadTask.execute(serverPath, localFilePath).get();
        } catch (Exception e) {
            return DownloadResult.DownloadInterrupted;
        }
    }

    // ---------------------------------------
    // Subclasses
    // ---------------------------------------
    public enum DownloadResult {
        IncorrectURL,
        ResponseError,
        IncorrectLocalPath,
        DownloadInterrupted,
        NoErrorsOccurred
    }

    private class DownloadTask extends AsyncTask<String, Integer, DownloadResult> {

        private Context mContext;

        public DownloadTask(Context context) {
            this.mContext = context;
        }

        @Override
        protected DownloadResult doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                // Connect to the server
                URL serverUrl = new URL(sUrl[0]);
                connection = (HttpURLConnection) serverUrl.openConnection();
                connection.connect();

                // Expect HTTP 200 OK
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return DownloadResult.ResponseError;
                }

                // Download the file
                input = connection.getInputStream();
                output =  new FileOutputStream(sUrl[1]);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                int fileLength = connection.getContentLength();
                int lastProgressUpdate = 0;
                while ((count = input.read(data)) != -1) {
                    // Cancel, if requested
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }

                    // Download next piece of data
                    total += count;
                    output.write(data, 0, count);

                    // Update the progress
                    if (fileLength > 0) // only if total length is known
                    {
                        int currentProgress = (int) (total * 100 / fileLength);
                        Log.d("Download", "Current progress is " + currentProgress);
                        if (currentProgress - lastProgressUpdate >= 10){
                            Log.d("Download", "Progress update");
                            lastProgressUpdate = currentProgress;
                            publishProgress(currentProgress);}
                    }
                }
            } catch (MalformedURLException e) {
                return DownloadResult.IncorrectURL;
            } catch (FileNotFoundException e) {
                return DownloadResult.IncorrectLocalPath;
            } catch (IOException e) {
                return DownloadResult.IncorrectURL;
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
            return DownloadResult.NoErrorsOccurred;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Display the progress bar
            if(mDisplayNotification) {
                mBuilder.setProgress(100, 0, false);
                notifySafe();
            }
        }

        private void notifySafe() {
            try {
                mNotifyManager.notify(1, mBuilder.build());
            } catch (IllegalArgumentException iae){
                Log.w("EDK", "Notification exception ignored", iae);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            // Update the progress value
            if(mDisplayNotification) {
                mBuilder.setProgress(100, values[0], false)
                        .setContentText(mProgressText + " (" + String.valueOf(values[0]) + "%)");
                notifySafe();
            }
        }

        @Override
        protected void onPostExecute(DownloadResult result) {
            super.onPostExecute(result);

            // Remove the progress bar
            if(mDisplayNotification) {
                mBuilder.setContentText(mFinishedText);
                mBuilder.setProgress(0, 0, false);
                notifySafe();
            }

            if(mListener != null)
                mListener.onDownloadFinished(result);
        }
    }

    public interface OnDownloadEventListener{
        void onDownloadFinished(DownloadResult result);
    }
}
