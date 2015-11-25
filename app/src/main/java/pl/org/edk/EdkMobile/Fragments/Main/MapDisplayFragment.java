package pl.org.edk.EdkMobile.Fragments.Main;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import pl.org.edk.EdkMobile.Entities.CrossStation;
import pl.org.edk.EdkMobile.Managers.AppConfiguration;
import pl.org.edk.R;

/**
 * Created by Pawel on 2015-03-13.
 */
public class MapDisplayFragment extends MainActivityFragmentBase implements OnMapReadyCallback{
    private SupportMapFragment mMapFragment;
    private GoogleMap googleMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_map, container, false);
        return view;
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible && mMapFragment == null){
            InitializeMap();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        RefreshMap(true);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        RefreshMap(true);
    }

    private void InitializeMap(){
        mMapFragment = SupportMapFragment.newInstance();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.map_container, mMapFragment).commit();

        mMapFragment.getMapAsync(this);
    }

    public void RefreshMap(boolean routeCenter){
        if(googleMap == null)
            return;

        // Remove previous markers
        googleMap.clear();

        // Move the camera to the next frag_station
        if(routeCenter)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(50.285064, 18.574042), 10.9f));
        else
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(AppConfiguration.getInstance().getStationToAchieve().getLatitude(), AppConfiguration.getInstance().getStationToAchieve().getLongitude()), 11.8f));

        for(int i=1; i <= 14; i++){
            CrossStation currentStation, previousStation;
            LatLng currentPoint, previousPoint;

            // Get points
            currentStation= AppConfiguration.getInstance().getCrossStation(i);
            currentPoint = new LatLng(currentStation.getLatitude(), currentStation.getLongitude());
            boolean isReached = currentStation.isAchieved();
            if(i > 1) {
                previousStation = AppConfiguration.getInstance().getCrossStation(i - 1);
                previousPoint = new LatLng(previousStation.getLatitude(), previousStation.getLongitude());

                // Add a line from the previous marker to this one
                googleMap.addPolyline(new PolylineOptions().add(previousPoint)
                        .add(currentPoint)
                        .color(isReached ? Color.GRAY : Color.BLACK)
                        .width(isReached ? 2 : 6));
            }

            // Add next marker
            String name = currentStation.getDisplayName();
            String title = currentStation.getTitle();
            String markerName;
            if(!isReached)
                markerName = "map_marker_" + i;
            else
                markerName = "map_marker_achieved";
            int markerId = getResources().getIdentifier(markerName, "drawable", getActivity().getPackageName());

            googleMap.addMarker(new MarkerOptions().position(currentPoint)
                                                   .title(name)
                                                   .snippet(title))
                                                   .setIcon(BitmapDescriptorFactory.fromResource(markerId));
        }
    }
}
