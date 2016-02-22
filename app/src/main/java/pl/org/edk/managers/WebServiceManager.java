package pl.org.edk.managers;

import android.content.Context;
import android.os.AsyncTask;
import pl.org.edk.Settings;
import pl.org.edk.database.DbManager;
import pl.org.edk.database.entities.*;
import pl.org.edk.webServices.FileDownloader;
import pl.org.edk.webServices.WebServiceAccess;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by pwawrzynek on 2016-02-11.
 */
public class WebServiceManager {

    // ---------------------------------------
    // Subclasses
    // ---------------------------------------
    public interface OnOperationFinishedEventListener<Type>{
        void onOperationFinished(Type result);
    }

    // ---------------------------------------
    // Class members
    // ---------------------------------------
    private static WebServiceManager mInstance;

    private Context mContext;
    private WebServiceAccess mWsClient;

    private int mNotificationIcon;
    private boolean mDownloadInProgress = false;
    private ArrayList<Reflection> mReflectionsToDownload = new ArrayList<>();

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
    public void init(int notificationIcon){
        this.mNotificationIcon = notificationIcon;
    }

    // Territories

    public ArrayList<Territory> getTerritories(){
        ArrayList<Territory> rawTerritories = mWsClient.getTerritories();
        if(rawTerritories == null)
            return null;

        for(Territory territory : rawTerritories){
            DbManager.getInstance(mContext).getTerritoryService().updateTerritoryWithAreasByServerId(territory);
        }
        return rawTerritories;
    }

    public void getTerritoriesAsync(final OnOperationFinishedEventListener listener){
        AsyncTask<Integer, Integer, ArrayList<Territory>> downloadTask = new AsyncTask<Integer, Integer, ArrayList<Territory>>() {
            @Override
            protected ArrayList<Territory> doInBackground(Integer... params) {
                return getTerritories();
            }

            @Override
            protected void onPostExecute(ArrayList<Territory> territories) {
                super.onPostExecute(territories);

                if (listener != null){
                    listener.onOperationFinished(territories);
                }
            }
        };
        downloadTask.execute();
    }

    // Areas

    public ArrayList<Area> getAreas(){
        ArrayList<Area> rawAreas = mWsClient.getAreas();
        if(rawAreas == null)
            return null;

        for(Area area : rawAreas) {
            Area previous = DbManager.getInstance(mContext).getTerritoryService().getAreaByServerId(area.getServerID());
            // The area doesn't exist in DB - add it
            if(previous == null){
                Territory territory = DbManager.getInstance(mContext).getTerritoryService().getTerritoryByServerId(area.getTerritoryId());
                if(territory != null) {
                    area.setTerritoryId(territory.getId());
                    DbManager.getInstance(mContext).getTerritoryService().insertArea(area);
                }
            }
            // There's no territory to add it ofr
            else {
                // TODO: Add some error handling
            }
        }
        return rawAreas;
    }

    public void getAreasAsync(final OnOperationFinishedEventListener listener){
        AsyncTask<Long, Integer, ArrayList<Area>> downloadTask = new AsyncTask<Long, Integer, ArrayList<Area>>() {
            @Override
            protected ArrayList<Area> doInBackground(Long... params) {
                return getAreas();
            }

            @Override
            protected void onPostExecute(ArrayList<Area> areas) {
                super.onPostExecute(areas);

                if (listener != null){
                    listener.onOperationFinished(areas);
                }
            }
        };
        downloadTask.execute();
    }

    public ArrayList<Area> getAreas(long territoryServerId){
        // TODO: Request a WS method returning areas only for a single territory

        ArrayList<Area> rawAreas = mWsClient.getAreas();
        if(rawAreas == null)
            return null;

        ArrayList<Area> areas = new ArrayList<>();
        for(Area area : rawAreas) {
            // Ignore areas for different territory
            if(area.getTerritoryId() != territoryServerId) {
                continue;
            }

            Area previous = DbManager.getInstance(mContext).getTerritoryService().getAreaByServerId(area.getServerID());
            // The area doesn't exist in DB - add it
            if(previous == null) {
                Territory territory = DbManager.getInstance(mContext).getTerritoryService().getTerritoryByServerId(area.getTerritoryId());
                if (territory != null) {
                    area.setTerritoryId(territory.getId());
                    DbManager.getInstance(mContext).getTerritoryService().insertArea(area);
                }
                // There's no territory to add it ofr
                else {
                    // TODO: Add some error handling
                }
            }
            areas.add(area);
        }
        return areas;
    }

    public void getAreasAsync(long territoryServerId, final OnOperationFinishedEventListener listener){
        AsyncTask<Long, Integer, ArrayList<Area>> downloadTask = new AsyncTask<Long, Integer, ArrayList<Area>>() {
            @Override
            protected ArrayList<Area> doInBackground(Long... params) {
                return getAreas(params[0]);
            }

            @Override
            protected void onPostExecute(ArrayList<Area> areas) {
                super.onPostExecute(areas);

                if (listener != null){
                    listener.onOperationFinished(areas);
                }
            }
        };
        downloadTask.execute(territoryServerId);
    }

    // Routes

    public ArrayList<Route> getRoutesByArea(long areaServerId){
        ArrayList<Route> rawRoutes = mWsClient.getRoutesByArea(areaServerId);
        if(rawRoutes == null)
            return null;

        Area area = DbManager.getInstance(mContext).getTerritoryService().getAreaByServerId(areaServerId);
        if(area == null){
            // TODO: Fetch a single area (WS doesn't provide that data now)
        }

        for(Route route : rawRoutes){
            route.setAreaId(area.getId());
            DbManager.getInstance(mContext).getRouteService().updateRoute(route);
        }

        return rawRoutes;
    }

    public void getRoutesByAreaAsync(long areaServerId, final OnOperationFinishedEventListener listener){
        AsyncTask<Long, Integer, ArrayList<Route>> downloadTask = new AsyncTask<Long, Integer, ArrayList<Route>>() {
            @Override
            protected ArrayList<Route> doInBackground(Long... params) {
                return getRoutesByArea(params[0]);
            }

            @Override
            protected void onPostExecute(ArrayList<Route> routes) {
                super.onPostExecute(routes);

                if (listener != null){
                    listener.onOperationFinished(routes);
                }
            }
        };
        downloadTask.execute(areaServerId);
    }

    public Route getRoute(long serverID){
        // Get the route data
        Route rawRoute = mWsClient.getRoute(serverID);
        if(rawRoute == null)
            return null;

        // If KML file is available, download it
        String kmlServerPath = rawRoute.getKmlDataPath();
        if(kmlServerPath != null && kmlServerPath.length() > 0) {
            String kmlLocalPath = Settings.get(mContext).get(Settings.APP_DIRECTORY_KML) + "/route_" + String.valueOf(serverID) + ".kml";
            FileDownloader manager = new FileDownloader(mContext);
            FileDownloader.DownloadResult result = manager.downloadFile(kmlServerPath, kmlLocalPath);

            // Download succeeded
            if(result == FileDownloader.DownloadResult.NoErrorsOccurred){
                rawRoute.setKmlDataPath(kmlLocalPath);
            }
            else {
                rawRoute.setKmlDataPath("");
            }
        }

        // Save the data to DB
        Route previous = DbManager.getInstance(mContext).getRouteService().getRouteByServerID(serverID);
        if(previous != null){
            rawRoute.setId(previous.getId());
            rawRoute.setAreaId(previous.getAreaId());
            DbManager.getInstance(mContext).getRouteService().updateRoute(rawRoute);
        }
        else {
            DbManager.getInstance(mContext).getRouteService().insertRoute(rawRoute);
        }

        return rawRoute;
    }

    public void getRouteAsync(final long serverID, final OnOperationFinishedEventListener listener){
        // Get the route data
        final Route rawRoute = mWsClient.getRoute(serverID);
        if(rawRoute == null){
            if (listener != null){
                listener.onOperationFinished(null);
            }
            return;
        }

        // Get route language-dependent data
        RouteDesc routeDesc = mWsClient.getRouteDesc(serverID);
        if(routeDesc != null){
            rawRoute.getDescriptions().add(routeDesc);
        }

        // If KML file is unavailable, leave it be
        String kmlServerPath = rawRoute.getKmlDataPath();
        if(kmlServerPath == null || kmlServerPath.length() == 0) {
            rawRoute.setKmlDataPath("");
            if(listener != null){
                listener.onOperationFinished(rawRoute);
            }
            return;
        }

        // Download KML, if available
        final String kmlLocalPath = Settings.get(mContext).get(Settings.APP_DIRECTORY_KML) + "/route_" + String.valueOf(serverID) + ".kml";
        FileDownloader manager = new FileDownloader(mContext);
        manager.setListener(new FileDownloader.OnDownloadEventListener() {
            @Override
            public void onDownloadFinished(FileDownloader.DownloadResult result) {
                // Download succeeded
                if (result == FileDownloader.DownloadResult.NoErrorsOccurred) {
                    rawRoute.setKmlDataPath(kmlLocalPath);
                } else {
                    rawRoute.setKmlDataPath("");
                }

                // Save the data to DB
                Route previous = DbManager.getInstance(mContext).getRouteService().getRouteByServerID(serverID);
                if (previous != null) {
                    rawRoute.setId(previous.getId());
                    rawRoute.setAreaId(previous.getAreaId());
                    DbManager.getInstance(mContext).getRouteService().updateRoute(rawRoute);
                } else {
                    DbManager.getInstance(mContext).getRouteService().insertRoute(rawRoute);
                }

                // Inform interested parties
                if (listener != null) {
                    listener.onOperationFinished(rawRoute);
                }
            }
        });
        manager.downloadFileAsync(kmlServerPath, kmlLocalPath);
    }

    // Reflections

    public ReflectionList getReflectionList(String language){
        ReflectionList rawList = mWsClient.getReflectionList(language);
        if(rawList == null){
            return null;
        }

        // Insert missing data (not returned by WS)
        rawList.setReleaseDate(Calendar.getInstance().getTime());
        rawList.setEdition(Calendar.getInstance().get(Calendar.YEAR));

        // Insert it to the DB
        DbManager.getInstance(mContext).getReflectionService().insertReflectionList(rawList);

        return rawList;
    }

    public void getReflectionsAudioAsync(ReflectionList list, final OnOperationFinishedEventListener listener){
        if(mDownloadInProgress){
            return;
        }

        // Prepare a list of Reflections that need to be downloaded
        mReflectionsToDownload = new ArrayList<>();
        for(Reflection reflection : list.getReflections()) {
            if(reflection.getAudioServerPath() != null && reflection.getAudioServerPath().length() > 0) {
                mReflectionsToDownload.add(reflection);
            }
        }

        // Start the download if
        downloadNext(list, listener);
    }

    // ---------------------------------------
    // Public methods
    // ---------------------------------------
    private void downloadNext(final ReflectionList list, final OnOperationFinishedEventListener listener){
        // Downloading finished
        if(mReflectionsToDownload.size() == 0){
            // Save the results
            DbManager.getInstance(mContext).getReflectionService().insertReflectionList(list);

            listener.onOperationFinished(list);
            mDownloadInProgress = false;
            return;
        }

        mDownloadInProgress = true;
        String localPathBase = Settings.get(mContext).get(Settings.APP_DIRECTORY_AUDIO) + "/reflection_" +
                list.getEdition() + "_";

        final Reflection nextReflection = mReflectionsToDownload.get(0);
        String serverPath = nextReflection.getAudioServerPath();
        final String localPath = localPathBase + nextReflection.getStationIndex() + ".mp3";

        // Start the download and trigger the next one, when finished
        FileDownloader manager = new FileDownloader(mContext);
        int totalReflectionsCount = list.getReflections().size();
        manager.setNotificationDetails(mNotificationIcon, "Pobieranie rozważań",
                totalReflectionsCount + 1 - mReflectionsToDownload.size() + "/" + totalReflectionsCount, "Rozważania pobrane");
        manager.setListener(new FileDownloader.OnDownloadEventListener() {
            @Override
            public void onDownloadFinished(FileDownloader.DownloadResult result) {
                mReflectionsToDownload.remove(nextReflection);

                // Download succeeded
                if(result == FileDownloader.DownloadResult.NoErrorsOccurred){
                    nextReflection.setAudioLocalPath(localPath);
                }
                downloadNext(list, listener);
            }
        });
        manager.downloadFileAsync(serverPath, localPath);
    }
}
