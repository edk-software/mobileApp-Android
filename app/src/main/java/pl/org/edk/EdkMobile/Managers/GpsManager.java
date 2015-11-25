package pl.org.edk.EdkMobile.Managers;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * Created by Pawel on 2015-03-09.
 */
public class GpsManager implements LocationListener {
    private LocationManager locationManager;
    private Location lastLocation;
    private Context context;

    private boolean askedForGps = false;

    // Constants
    private final int TIME_ACCURACY = 300000,   // 5 min
                      DISTANCE_ACCURACY = 200;  // 200 m

    public GpsManager(Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        Initialize();
    }

    private void Initialize() {
        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, TIME_ACCURACY, DISTANCE_ACCURACY, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME_ACCURACY, DISTANCE_ACCURACY, this);
    }

    // Getters =====================

    public Location getLastLocation() {
        return lastLocation;
    }

    public boolean checkGpsOn() {
        // Do not ask for the second time
        if(askedForGps)
            return true;
        else if(locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) == false){
            askedForGps = true;
            return false;
        }
        else
            return true;
    }

    // =============================

    // LocationListener interface methods ====================
    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
    // =======================================================
}
