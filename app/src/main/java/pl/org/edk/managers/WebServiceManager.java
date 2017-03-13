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
import java.util.Date;

/**
 * Created by pwawrzynek on 2016-02-11.
 */
public class WebServiceManager {

    // ---------------------------------------
    // Subclasses
    // ---------------------------------------
    public interface OnOperationFinishedEventListener<Type> {
        // TODO: Add operation type here
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
    private ArrayList<OnOperationFinishedEventListener> mDownloadListeners = new ArrayList<>();

    // ---------------------------------------
    // Constructors
    // ---------------------------------------
    private WebServiceManager(Context context) {
        this.mContext = context;
        mWsClient = new WebServiceAccess(context);
    }

    private static synchronized WebServiceManager get(Context applicationContext) {
        if (mInstance == null)
            mInstance = new WebServiceManager(applicationContext);
        return mInstance;
    }

    public static synchronized WebServiceManager getInstance(Context context) {
        return get(context.getApplicationContext());
    }

    // ---------------------------------------
    // Public methods
    // ---------------------------------------
    public void init(int notificationIcon) {
        this.mNotificationIcon = notificationIcon;
    }

    // Territories

    public ArrayList<Territory> getTerritories() {
        ArrayList<Territory> rawTerritories = mWsClient.getTerritories();
        if (rawTerritories == null)
            return null;

        for (Territory territory : rawTerritories) {
            DbManager.getInstance(mContext).getTerritoryService().updateTerritoryWithAreasByServerId(territory);
        }
        return rawTerritories;
    }

    public void getTerritoriesAsync(final OnOperationFinishedEventListener listener) {
        AsyncTask<Integer, Integer, ArrayList<Territory>> downloadTask = new AsyncTask<Integer, Integer, ArrayList<Territory>>() {
            @Override
            protected ArrayList<Territory> doInBackground(Integer... params) {
                return getTerritories();
            }

            @Override
            protected void onPostExecute(ArrayList<Territory> territories) {
                super.onPostExecute(territories);

                if (listener != null) {
                    listener.onOperationFinished(territories);
                }
            }
        };
        downloadTask.execute();
    }

    // Areas

    public ArrayList<Area> getAreas() {
        ArrayList<Area> rawAreas = mWsClient.getAreas();
        if (rawAreas == null)
            return null;

        for (Area area : rawAreas) {
            Territory territory = DbManager.getInstance(mContext).getTerritoryService().getTerritoryByServerId(area.getTerritoryId());
            if (territory != null) {
                area.setTerritoryId(territory.getId());
                DbManager.getInstance(mContext).getTerritoryService().updateAreaByServerId(area);
            }
        }

        return rawAreas;
    }

    public void getAreasAsync(final OnOperationFinishedEventListener listener) {
        AsyncTask<Long, Integer, ArrayList<Area>> downloadTask = new AsyncTask<Long, Integer, ArrayList<Area>>() {
            @Override
            protected ArrayList<Area> doInBackground(Long... params) {
                return getAreas();
            }

            @Override
            protected void onPostExecute(ArrayList<Area> areas) {
                super.onPostExecute(areas);

                if (listener != null) {
                    listener.onOperationFinished(areas);
                }
            }
        };
        downloadTask.execute();
    }

    public ArrayList<Area> getAreas(long territoryServerId) {
        // TODO: Request a WS method returning areas only for a single territory

        ArrayList<Area> rawAreas = mWsClient.getAreas();
        if (rawAreas == null)
            return null;

        ArrayList<Area> areas = new ArrayList<>();
        for (Area area : rawAreas) {
            // Ignore areas for different territory
            if (area.getTerritoryId() != territoryServerId) {
                continue;
            }

            Area previous = DbManager.getInstance(mContext).getTerritoryService().getAreaByServerId(area.getServerID());
            // The area doesn't exist in DB - add it
            if (previous == null) {
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

    public void getAreasAsync(long territoryServerId, final OnOperationFinishedEventListener listener) {
        AsyncTask<Long, Integer, ArrayList<Area>> downloadTask = new AsyncTask<Long, Integer, ArrayList<Area>>() {
            @Override
            protected ArrayList<Area> doInBackground(Long... params) {
                return getAreas(params[0]);
            }

            @Override
            protected void onPostExecute(ArrayList<Area> areas) {
                super.onPostExecute(areas);

                if (listener != null) {
                    listener.onOperationFinished(areas);
                }
            }
        };
        downloadTask.execute(territoryServerId);
    }

    // Routes

    public ArrayList<Route> getRoutesByArea(long areaServerId) {
        ArrayList<Route> rawRoutes = mWsClient.getRoutesByArea(areaServerId);
        if (rawRoutes == null)
            return null;

        Area area = DbManager.getInstance(mContext).getTerritoryService().getAreaByServerId(areaServerId);
        if (area == null) {
            // TODO: Fetch a single area (WS doesn't provide that data now)
        }

        for (Route route : rawRoutes) {
            route.setAreaId(area.getId());
            DbManager.getInstance(mContext).getRouteService().updateRoute(route);
        }

        return rawRoutes;
    }

    public void getRoutesByAreaAsync(long areaServerId, final OnOperationFinishedEventListener listener) {
        AsyncTask<Long, Integer, ArrayList<Route>> downloadTask = new AsyncTask<Long, Integer, ArrayList<Route>>() {
            @Override
            protected ArrayList<Route> doInBackground(Long... params) {
                return getRoutesByArea(params[0]);
            }

            @Override
            protected void onPostExecute(ArrayList<Route> routes) {
                super.onPostExecute(routes);

                if (listener != null) {
                    listener.onOperationFinished(routes);
                }
            }
        };
        downloadTask.execute(areaServerId);
    }

    /**
     * Download the latest version of the Route, its KML data and RouteDesc (pl) from WS and update it in the local DB
     *
     * @param serverID ServerId of the Route to be updated
     * @param listener Operations to be performed, when the download's finished
     */
    public void syncRouteAsync(final long serverID, final OnOperationFinishedEventListener listener) {
        // Get the route data
        final Route rawRoute = mWsClient.getRoute(serverID);
        if (rawRoute == null) {
            if (listener != null) {
                listener.onOperationFinished(null);
            }
            return;
        }

        // Get route language-dependent data
        RouteDesc routeDesc = mWsClient.getRouteDesc(serverID);
        if (routeDesc != null) {
            rawRoute.getDescriptions().add(routeDesc);
        }

        // Download KML data
        downloadKmlForRouteAsync(rawRoute, new OnOperationFinishedEventListener() {
            @Override
            public void onOperationFinished(Object result) {
                if (listener != null) {
                    listener.onOperationFinished(rawRoute);
                }
            }
        });
    }

    // Reflections

    public boolean syncReflections(boolean includeAudio) {
        // TODO: Upload more than the default language
        String defaultLanguage = Settings.get(mContext).get(Settings.APP_LANGUAGE);

        // Get the WS list
        ArrayList<ReflectionList> wsLists = mWsClient.getReflectionLists(defaultLanguage);
        if (wsLists == null)
            return false;

        for (ReflectionList wsList : wsLists) {
            try {
                ReflectionList dbList = DbManager.getInstance(mContext)
                        .getReflectionService()
                        .getReflectionList(defaultLanguage, wsList.getEdition(), false);

                // This list doesn't exist locally
                if (dbList == null) {
                    dbList = mWsClient.getReflectionList(wsList.getLanguage(), wsList.getEdition());
                    if (dbList == null) {
                        continue;
                    }

                    dbList.setReleaseDate(wsList.getReleaseDate());
                    DbManager.getInstance(mContext).getReflectionService().insertReflectionList(dbList);
                    if (includeAudio) {
                        getReflectionsAudioAsync(dbList, null);
                    }
                    continue;
                }

                // This list is outdated
                if (dbList.getReleaseDate().before(wsList.getReleaseDate())) {
                    updateReflectionList(dbList.getLanguage(), dbList.getEdition(), wsList.getReleaseDate(), includeAudio);
                    continue;
                }
            }
            catch (Exception ex)
            {
                // TODO: Do something about it
                continue;
            }
        }

        return true;
    }

    public void getReflectionsAudioAsync(ReflectionList list, final OnOperationFinishedEventListener listener) {
        if (mDownloadInProgress) {
            return;
        }

        // Prepare a list of Reflections that need to be downloaded
        mReflectionsToDownload = new ArrayList<>();
        for (Reflection reflection : list.getReflections()) {
            if (reflection.getAudioServerPath() != null && reflection.getAudioServerPath().length() > 0) {
                mReflectionsToDownload.add(reflection);
            }
        }

        // Start the download if
        downloadNext(list, listener);
    }

    // Sync

    public boolean isDownloadInProgress() {
        return mDownloadInProgress;
    }

    public void addDownloadListener(OnOperationFinishedEventListener newListener) {
        if (!mDownloadListeners.contains(newListener)) {
            mDownloadListeners.add(newListener);
        }
    }

    public void removeDownloadListener(OnOperationFinishedEventListener listener) {
        if (!mDownloadListeners.contains(listener)) {
            mDownloadListeners.remove(listener);
        }
    }

    public void updateData(boolean includeAreas, boolean includeLocalRoutes, boolean includeReflections, boolean includeAudio) {
        if (includeAreas) {
            syncAreas();
        }

        if (includeLocalRoutes) {
            syncRoutes();
        }

        if (includeReflections) {
            syncReflections(includeAudio);
        }
    }

    public void updateDataAsync(boolean includeAreas, boolean includeLocalRoutes, boolean includeReflections, boolean includeAudio,
                                final OnOperationFinishedEventListener listener) {
        AsyncTask<Boolean, Integer, Boolean> downloadTask = new AsyncTask<Boolean, Integer, Boolean>() {
            @Override
            protected Boolean doInBackground(Boolean... params) {
                updateData(params[0], params[1], params[2], params[3]);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);

                if (listener != null) {
                    listener.onOperationFinished(result);
                }
            }
        };
        downloadTask.execute(includeAreas, includeLocalRoutes, includeReflections, includeAudio);
    }

    // ---------------------------------------
    // Private methods
    // ---------------------------------------
    private void downloadKmlForRouteAsync(final Route route, final OnOperationFinishedEventListener listener) {
        final String kmlLocalPath = Settings.get(mContext).get(Settings.APP_DIRECTORY_KML) +
                "/route_" + String.valueOf(route.getServerID()) + ".kml";
        String kmlServerPath = route.getKmlDataPath();
        if ((kmlServerPath == null || kmlServerPath.length() == 0) && listener != null) {
            listener.onOperationFinished(true);
        }

        FileDownloader manager = new FileDownloader(mContext);
        manager.setListener(new FileDownloader.OnDownloadEventListener() {
            @Override
            public void onDownloadFinished(FileDownloader.DownloadResult result) {
                // Download succeeded
                route.setKmlDataPath((result == FileDownloader.DownloadResult.NoErrorsOccurred) ? kmlLocalPath : "");

                // Save the data to DB
                DbManager.getInstance(mContext).getRouteService().updateRouteByServerId(route);

                // Inform interested parties
                if (listener != null) {
                    listener.onOperationFinished(true);
                }
            }
        });
        manager.downloadFileAsync(kmlServerPath, kmlLocalPath);
    }

    private void downloadNext(final ReflectionList list, final OnOperationFinishedEventListener listener) {
        // Downloading finished
        if (mReflectionsToDownload.size() == 0) {
            // Save the results
            DbManager.getInstance(mContext).getReflectionService().updateReflectionList(list);

            mDownloadInProgress = false;
            if (listener != null) {
                listener.onOperationFinished(list);
            }
            for (OnOperationFinishedEventListener downloadListener : mDownloadListeners) {
                downloadListener.onOperationFinished(list);
            }
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
                if (result == FileDownloader.DownloadResult.NoErrorsOccurred) {
                    nextReflection.setAudioLocalPath(localPath);
                }
                downloadNext(list, listener);
            }
        });
        manager.downloadFileAsync(serverPath, localPath);
    }

    private void syncAreas() {
        getTerritories();
        getAreas();
    }

    private void syncRoutes() {
        ArrayList<Route> routes = DbManager.getInstance(mContext).getRouteService().getAllRoutes();
        for (Route route : routes) {
            syncRouteAsync(route.getServerID(), null);
        }
    }

    private void updateReflectionList(String language, int edition, Date releaseDate, boolean includeAudio){
        ReflectionList fullList = mWsClient.getReflectionList(language, edition);
        if(fullList == null)
            return;

        fullList.setReleaseDate(releaseDate);
        DbManager.getInstance(mContext).getReflectionService().updateReflectionListByVersion(fullList);
        if (includeAudio){
            getReflectionsAudioAsync(fullList, null);
        }
    }
}
