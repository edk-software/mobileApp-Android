package pl.org.edk;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
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

import pl.org.edk.kml.KMLTracker;
import pl.org.edk.kml.TrackerProvider;
import pl.org.edk.util.DialogUtil;
import pl.org.edk.util.NumConverter;

/**
 * Created by darekpap on 2015-11-30.
 */
public class MapFragment extends Fragment implements GoogleMap.OnInfoWindowClickListener, GoogleMap.OnCameraChangeListener,
        OnMapReadyCallback, KMLTracker.TrackListener {

    public interface OnStationSelectListener {
        void onStationSelect(int stationIndex);
    }

    private OnStationSelectListener mListener;


    private static final float NAVIGATION_DEFAULT_CAMERA_ZOOM = 16f;

    private static final float OVERVIEW_CAMERA_ZOOM = 10f;

    private Float lastCameraZoom = null;

    private GoogleMap mMap = null;

    private LatLng lastCameraPos = null;

    private List<Marker> markers = new ArrayList<Marker>();

    private static final String TAG = "EDK";

    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (OnStationSelectListener) context;
        } catch (ClassCastException cce) {
            throw new ClassCastException(context.toString() + " must implement OnStationSelectListener");
        }
    }

    private boolean verifyTracker() {
        try {
            TrackerProvider.getTracker(getActivity());
            return false;
        } catch (Exception e) {
            DialogUtil.showWarningDialog(e.getMessage(), getActivity());
            return true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (verifyTracker()) {
            return;
        }
        loadCameraZoom();
        if (isVisible() && isMenuVisible()) {
            verifyWhetherServicesAvailable();
            verifyWhetherGPSIsEnabled();
        }
//        setNavigationButtonVisibility();

//        setUpMapIfNeeded();
        addTrackListener();
        processTrackerState();
    }

    private void setMapPadding() {
        if (mMap == null) {
            return;
        }
        mMap.setPadding(0, 0, 0, 0);
    }

    private void processTrackerState() {
        if (Settings.get(getActivity()).getBoolean(Settings.IS_BACKGROUND_TRACKING_ON)) {

            KMLTracker tracker = TrackerProvider.getTracker(getActivity());
            KMLTracker.State state = tracker.getState();
            switch (state) {
                case OnTrack:
                    onBackOnTrack();
                    break;
                case OffTrack:
                    onOutOfTrack();
                    break;
                case NearCheckpoint:
                    onCheckpointReached(tracker.getCheckpointId());
                    break;
                case WaitingForPosition:
                    Log.d(getClass().getSimpleName(), getString(R.string.waiting_for_position_message));
                    break;
                default:
                    break;
            }
        } else {
            Log.d(getClass().getSimpleName(), getString(R.string.track_overview));
        }
    }

    private void loadCameraZoom() {
        Settings settings = Settings.get(getActivity());
        float defaultValue = -1;
        float loadedZoom = settings.getFloat(Settings.CAMERA_ZOOM, defaultValue);
        if (Math.abs(loadedZoom - defaultValue) > 0.1) {
            lastCameraZoom = loadedZoom;
        }
    }

    private void verifyWhetherGPSIsEnabled() {
        final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.GPS_off_warning_message);
        builder.setTitle(R.string.warning_dialog_title);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.go_to_location_settings, new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        builder.setNegativeButton(R.string.ignore, new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.cancel();
            }
        });
        final AlertDialog dialog = builder.show();
        DialogUtil.addRedTitleDivider(getActivity(), dialog);
    }

    private void addTrackListener() {
        KMLTracker tracker = TrackerProvider.getTracker(getActivity());
        tracker.addListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (verifyTracker()) {
            return;
        }
        removeTrackListener();
        saveCameraZoom();
    }

    private void saveCameraZoom() {
        if (lastCameraZoom != null) {
            Settings.get(getActivity()).set(Settings.CAMERA_ZOOM, lastCameraZoom);
        }
    }

    private void removeTrackListener() {
        KMLTracker tracker = TrackerProvider.getTracker(getActivity());
        tracker.removeListener(this);
    }

    private void clearMap() {
        if (mMap != null) {
            mMap.clear();
        }
    }

    private void verifyWhetherServicesAvailable() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (ConnectionResult.SUCCESS == resultCode) {
            return;
        }

        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(),
                CONNECTION_FAILURE_RESOLUTION_REQUEST);
        errorDialog.show();
    }

    private void focusCameraOnLastLocation() {
        KMLTracker tracker = TrackerProvider.getTracker(getActivity());
        LatLng center = null;
        float defaultCameraZoom = OVERVIEW_CAMERA_ZOOM;
        if (Settings.get(getActivity()).getBoolean(Settings.IS_BACKGROUND_TRACKING_ON)) {
            LatLng lastLoc = tracker.getLastLoc();
            if (lastLoc != null) {
                center = lastLoc;
            } else if (lastCameraPos != null) {
                center = lastCameraPos;
            }
            defaultCameraZoom = NAVIGATION_DEFAULT_CAMERA_ZOOM;
        }
        if (center == null) {
            List<LatLng> track = tracker.getTrack();
            center = track.get(track.size() / 2);
        }
        float zoomToApply = defaultCameraZoom;
        if (lastCameraZoom != null) {
            zoomToApply = lastCameraZoom;
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, zoomToApply));
    }

//    private void setUpMapIfNeeded() {
//        if (mMap == null) {
//            ((com.google.android.gms.maps.MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
//        }
//
//    }

    private void decorateMap() {
        KMLTracker tracker = TrackerProvider.getTracker(getActivity());
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
        List<MarkerOptions> markerOptions = new ArrayList<MarkerOptions>();
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
        int stationId = NumConverter.toArabic(checkpoint);
        return stationId;
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
        if (mMap != null && Settings.get(getActivity()).getBoolean(Settings.FOLLOW_LOCATION_ON_MAP)){
            mMap.animateCamera(CameraUpdateFactory.newLatLng(location), 2000, null);
        }

    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        Log.i(TAG, "Map initialization");
        // if (Settings.get().getBoolean(Settings.CHALLENGE_ACCEPTED)) {
        Log.i(TAG, "isVisible() " + isVisible() + " isMenuVisible()" + isMenuVisible());

        mMap.setMyLocationEnabled(isVisible() && isMenuVisible());
        // }
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

        changeLocationUpdates(isVisible() && isMenuVisible());

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

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return view;

    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        Log.d("EDK", "setMenuVisibility called with " + visible);
        super.setMenuVisibility(visible);
        if (getActivity() == null){
            Log.d("EDK", "Activity was null in setMenuVisibility");
            return;
        }

        changeLocationUpdates(visible);
        if (mMap == null) {
            return;
        }
        if (visible) {
            verifyWhetherServicesAvailable();
            verifyWhetherGPSIsEnabled();
        }

        mMap.setMyLocationEnabled(visible);



    }

    private void changeLocationUpdates(boolean visible) {
        if (Settings.get(getActivity()).getBoolean(Settings.IS_BACKGROUND_TRACKING_ON)) {
            return;
        }


        KMLTracker tracker = TrackerProvider.getTracker(getActivity());
        if (visible) {
            LocationRequest request = LocationRequest.create().setPriority(LocationRequest.PRIORITY_NO_POWER).setInterval(1000);
            tracker.requestLocationUpdates(request);
        } else {
            tracker.removeLocationUpdates();
        }
    }
}
