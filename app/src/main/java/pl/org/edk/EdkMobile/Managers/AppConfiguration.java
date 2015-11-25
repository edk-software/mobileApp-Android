package pl.org.edk.EdkMobile.Managers;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import pl.org.edk.EdkMobile.Entities.CrossRoute;
import pl.org.edk.EdkMobile.Entities.CrossStation;

import java.util.List;

/**
 * Created by Pawel on 2015-03-03.
 */
public class AppConfiguration {

    // Singleton ====
    private static AppConfiguration instance;
    public static AppConfiguration getInstance(){
        if(instance == null)
            instance = new AppConfiguration();
        return instance;
    }

    private AppConfiguration(){}
    // =============

    // Cross route ====
    private CrossRoute crossRoute;

    public CrossRoute getCrossRoute() {
        return crossRoute;
    }
    public void setCrossRoute(CrossRoute crossRoute){
        instance.crossRoute = crossRoute;
    }

    public List<CrossStation> getCrossStations() {
        return crossRoute.getStations();
    }
    public CrossStation getCrossStation(int stationNumber){
        if(stationNumber < 1 || stationNumber > 14)
            return null;

        return crossRoute.getStations().get(stationNumber-1);
    }
    // ==================

    // Next frag_station =====
    private int currentStationNumber = 0;

    public CrossStation getLastAchievedStation(){
        return getCrossStation(currentStationNumber);
    }
    public int getLastAchievedStationNumber(){return currentStationNumber;}
    public CrossStation getStationToAchieve() {
        return getCrossStation(currentStationNumber+1);
    }

    public void setLastAchievedStation(int stationNumber){
        if(stationNumber < 0 || stationNumber > 14)
            return;

        instance.currentStationNumber = stationNumber;

        for (int i=1; i <= 14; i++){
            boolean isAchieved = (i <= stationNumber);
            instance.getCrossStation(i).setAchieved(isAchieved);
        }
    }
    // ==================

    // GPS info =========
    private GpsManager gpsManager;

    public GpsManager getGpsManager() {
        return gpsManager;
    }
    public void setGpsManager(GpsManager gpsManager) {
        this.gpsManager = gpsManager;
    }

    public LatLng getCurrentGpsPosition(){
        Location position = gpsManager.getLastLocation();
        if(position == null){
            return null;
        }
        else{
            return new LatLng(position.getLatitude(), position.getLongitude());
        }
    }
    // ==================
}
