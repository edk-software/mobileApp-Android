package pl.org.edk.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import pl.org.edk.R;
import pl.org.edk.Settings;
import pl.org.edk.kml.KMLTracker;
import pl.org.edk.util.NumConverter;

/**
 * Created by darekpap on 2015-11-30.
 */
public class MapFragment extends TrackerFragment implements GoogleMap.OnInfoWindowClickListener, GoogleMap.OnCameraChangeListener,
        OnMapReadyCallback {

    public interface OnStationSelectListener {
        void onStationSelect(int stationIndex);
    }

    private OnStationSelectListener mListener;

    private static final float NAVIGATION_DEFAULT_CAMERA_ZOOM = 16f;

    private static final float OVERVIEW_CAMERA_ZOOM = 10f;

    private Float lastCameraZoom = null;

    private GoogleMap mMap = null;

    private LatLng lastCameraPos = null;

    private List<Marker> markers = new ArrayList<>();

    private static final String TAG = "EDK";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (OnStationSelectListener) context;
        } catch (ClassCastException cce) {
            throw new ClassCastException(context.toString() + " must implement OnStationSelectListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCameraZoom();
    }

    private void loadCameraZoom() {
        Settings settings = Settings.get(getActivity());
        float defaultValue = -1;
        float loadedZoom = settings.getFloat(Settings.CAMERA_ZOOM, defaultValue);
        if (Math.abs(loadedZoom - defaultValue) > 0.1) {
            lastCameraZoom = loadedZoom;
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        saveCameraZoom();
    }

    private void saveCameraZoom() {
        if (lastCameraZoom != null) {
            Settings.get(getActivity()).set(Settings.CAMERA_ZOOM, lastCameraZoom);
        }
    }

    private void focusCameraOnLastLocation() {
        KMLTracker tracker = getTracker();
        LatLng center = null;
            LatLng lastLoc = tracker.getLastLoc();
            if (lastLoc != null) {
                center = lastLoc;
            } else if (lastCameraPos != null) {
                center = lastCameraPos;
            }
        if (center == null) {
            List<LatLng> track = tracker.getTrack();
            center = track.get(track.size() / 2);
        }
        float zoomToApply = OVERVIEW_CAMERA_ZOOM;
        if (lastCameraZoom != null) {
            zoomToApply = lastCameraZoom;
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, zoomToApply));
    }

    private void decorateMap() {
        KMLTracker tracker = getTracker();
        addPolyline(tracker.getTrack());
        addMarkers(tracker.getCheckpoints());
    }

    private void addPolyline(List<LatLng> track) {
        PolylineOptions line = new PolylineOptions();
        line.addAll(track);
        line.color(Color.rgb(180, 0, 0));
        mMap.addPolyline(line);
        Log.i(TAG, "Added track to map");
    }

    private void addMarkers(List<LatLng> checkpoints) {
        List<MarkerOptions> markerOptions = new ArrayList<>();
        markerOptions.add(new MarkerOptions().position(checkpoints.get(0)).title(getString(R.string.considerations_start_title)));

        //stations 1-14
        for (int i = 1; i < checkpoints.size() - 1; i++) {
            MarkerOptions marker = new MarkerOptions();
            marker.position(checkpoints.get(i));
            marker.title(getString(R.string.station) + NumConverter.toRoman(i));
            markerOptions.add(marker);
        }
        markerOptions.add(new MarkerOptions().position(checkpoints.get(15)).title(getString(R.string.considerations_end_title)));

        for (MarkerOptions marker : markerOptions) {
            marker.snippet(getString(R.string.go_to_consideration));
            markers.add(mMap.addMarker(marker));
        }
        Log.i(TAG, "Added checkpoints to map");
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        int stationId = getStationId(marker);
        mListener.onStationSelect(stationId);
    }

    private int getStationId(Marker marker) {
        String title = marker.getTitle();
        if (title.equals(getString(R.string.considerations_start_title))) {
            return 0;
        }
        if (title.equals(getString(R.string.considerations_end_title))) {
            return 15;
        }

        int index = title.indexOf(" ");
        String checkpoint = title.substring(index + 1);
        return NumConverter.toArabic(checkpoint);
    }

    @Override
    public void onCameraChange(CameraPosition camera) {
        lastCameraZoom = camera.zoom;
        lastCameraPos = camera.target;
    }

    @Override
    public void onCheckpointReached(int checkpointId) {
        setNearCheckpointTitle(checkpointId);
        if (markers.isEmpty()) {
            Log.w(TAG, "Markers not initialized");
            return;
        }
        Marker marker = markers.get(checkpointId);
        marker.showInfoWindow();
    }

    private void setNearCheckpointTitle(int checkpointId) {
        String title = getString(R.string.near_checkpoint_message) + NumConverter.toRoman(checkpointId + 1);
        Log.d(getClass().getSimpleName(), title);
    }

    @Override
    public void onOutOfTrack() {
        Log.d(getClass().getSimpleName(), "Out of track");
    }

    @Override
    public void onBackOnTrack() {
        Log.d(getClass().getSimpleName(), "Back on track");
    }

    @Override
    public void onLocationChanged(LatLng location) {
        if (getActivity() == null){
            return;
        }
        if (mMap != null && Settings.get(getActivity()).getBoolean(Settings.FOLLOW_LOCATION_ON_MAP)) {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(location), 2000, null);
        }

    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        Log.i(TAG, "Map initialization");
        Log.i(TAG, "isVisible() " + isVisible() + " isMenuVisible()" + isMenuVisible());

        mMap.setMyLocationEnabled(isFragmentVisible());

        UiSettings settings = mMap.getUiSettings();
        settings.setCompassEnabled(true);
        settings.setZoomControlsEnabled(true);
        settings.setAllGesturesEnabled(true);
        settings.setMapToolbarEnabled(false);
        setMapPadding();
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnCameraChangeListener(this);
        decorateMap();
        focusCameraOnLastLocation();

        changeLocationUpdates(isFragmentVisible());

    }

    private void setMapPadding() {
        if (mMap == null) {
            return;
        }
        mMap.setPadding(0, 0, 0, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_map, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_container);
        if (mapFragment == null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            mapFragment = SupportMapFragment.newInstance();
            fragmentTransaction.replace(R.id.map_container, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);

        return view;
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (mMap == null) {
            return;
        }
        mMap.setMyLocationEnabled(visible);
    }

    @NonNull
    @Override
    protected LocationRequest getLocationRequest() {
        return LocationRequest.create().setPriority(LocationRequest.PRIORITY_NO_POWER).setInterval(1000);
    }
}
