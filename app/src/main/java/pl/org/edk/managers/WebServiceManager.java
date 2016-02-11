package pl.org.edk.managers;

import android.content.Context;
import pl.org.edk.Settings;
import pl.org.edk.database.DbManager;
import pl.org.edk.database.entities.Route;
import pl.org.edk.webServices.FileDownloader;
import pl.org.edk.webServices.WebServiceAccess;

/**
 * Created by pwawrzynek on 2016-02-11.
 */
public class WebServiceManager {
    // ---------------------------------------
    // Class members
    // ---------------------------------------
    private static WebServiceManager mInstance;

    private Context mContext;
    private WebServiceAccess mWsClient;

    // ---------------------------------------
    // Constructors
    // ---------------------------------------
    private WebServiceManager(Context context){
        this.mContext = context;
        mWsClient = new WebServiceAccess(context);
    }

    private static synchronized WebServiceManager get(Context applicationContext){
        if(mInstance == null)
            mInstance = new WebServiceManager(applicationContext);
        return mInstance;
    }

    public static synchronized WebServiceManager getInstance(Context context){
        return get(context.getApplicationContext());
    }

    // ---------------------------------------
    // Public methods
    // ---------------------------------------
    public Route getRoute(long serverID){
        // Get the route data
        Route rawRoute = mWsClient.getRoute(serverID);
        if(rawRoute == null)
            return null;

        // If KML file is available, download it
        String kmlServerPath = rawRoute.getKmlData();
        if(kmlServerPath != null && kmlServerPath.length() > 0) {
            String kmlLocalPath = Settings.get(mContext).get(Settings.APP_DIRECTORY_KML) + "/route_" + String.valueOf(serverID) + ".kml";
            FileDownloader manager = new FileDownloader(mContext);
            FileDownloader.DownloadResult result = manager.downloadFile(kmlServerPath, kmlLocalPath);

            // Download succeeded
            if(result == FileDownloader.DownloadResult.NoErrorsOccurred){
                rawRoute.setKmlData(kmlLocalPath);
            }
            else {
                rawRoute.setKmlData("");
            }
        }

        // Save the data to DB
        Route previous = DbManager.getInstance(mContext).getRouteService().getRouteByServerID(serverID);
        if(previous != null){
            rawRoute.setId(previous.getId());
            DbManager.getInstance(mContext).getRouteService().updateRoute(rawRoute);
        }
        else {
            DbManager.getInstance(mContext).getRouteService().insertRoute(rawRoute);
        }

        return rawRoute;
    }

    public void getRouteAsync(final long serverID){
        // Get the route data
        final Route rawRoute = mWsClient.getRoute(serverID);
        if(rawRoute == null)
            return;

        // If KML file is available, download it
        String kmlServerPath = rawRoute.getKmlData();
        if(kmlServerPath != null && kmlServerPath.length() > 0) {
            final String kmlLocalPath = Settings.get(mContext).get(Settings.APP_DIRECTORY_KML) + "/route_" + String.valueOf(serverID) + ".kml";
            FileDownloader manager = new FileDownloader(mContext);
            manager.setListener(new FileDownloader.OnDownloadEventListener() {
                @Override
                public void onDownloadFinished(FileDownloader.DownloadResult result) {
                    // Download succeeded
                    if(result == FileDownloader.DownloadResult.NoErrorsOccurred){
                        rawRoute.setKmlData(kmlLocalPath);
                    }
                    else {
                        rawRoute.setKmlData("");
                    }

                    // Save the data to DB
                    Route previous = DbManager.getInstance(mContext).getRouteService().getRouteByServerID(serverID);
                    if(previous != null){
                        rawRoute.setId(previous.getId());
                        DbManager.getInstance(mContext).getRouteService().updateRoute(rawRoute);
                    }
                    else {
                        DbManager.getInstance(mContext).getRouteService().insertRoute(rawRoute);
                    }
                }
            });
            manager.downloadFileAsync(kmlServerPath, kmlLocalPath);
        }
    }
}
