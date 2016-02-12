package pl.org.edk.managers;

import android.content.Context;
import pl.org.edk.Settings;
import pl.org.edk.database.DbManager;
import pl.org.edk.database.entities.Area;
import pl.org.edk.database.entities.Route;
import pl.org.edk.database.entities.Territory;
import pl.org.edk.webServices.FileDownloader;
import pl.org.edk.webServices.WebServiceAccess;

import java.util.ArrayList;

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
    public ArrayList<Territory> getTerritories(){
        ArrayList<Territory> rawTerritories = mWsClient.getTerritories();
        if(rawTerritories == null)
            return null;

        for(Territory territory : rawTerritories){
            Territory previous = DbManager.getInstance(mContext).getTerritoryService().getTerritoryByServerId(territory.getServerID());
            if(previous == null){
                DbManager.getInstance(mContext).getTerritoryService().insertTerritoryWithAreas(territory);
            }
        }
        return rawTerritories;
    }

    public void getTerritoriesAsync(){
        // TODO: this method
    }

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
                    DbManager.getInstance(mContext).getTerritoryService().insertAreaForTerritory(area, territory.getId());
                }
            }
            // There's no territory to add it ofr
            else {
                // TODO: Add some error handling
            }
        }
        return rawAreas;
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
                    DbManager.getInstance(mContext).getTerritoryService().insertAreaForTerritory(area, territory.getId());
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

    public void getAreasAsync(long territoryServerID){

    }

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
