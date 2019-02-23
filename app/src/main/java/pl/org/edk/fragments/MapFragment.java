package pl.org.edk.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

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

import pl.org.edk.BootStrap;
import pl.org.edk.R;
import pl.org.edk.Settings;
import pl.org.edk.TempSettings;
import pl.org.edk.kml.KMLTracker;
import pl.org.edk.util.DialogUtil;
import pl.org.edk.util.NumConverter;

/**
 * Created by darekpap on 2015-11-30.
 */
public class MapFragment extends TrackerFragment implements GoogleMap.OnInfoWindowClickListener, GoogleMap.OnCameraIdleListener,
        OnMapReadyCallback {

    private static final int REQUEST_CODE_MAP = 3;
    private static final int REQUEST_CODE_STORAGE = 2;

    public interface OnStationSelectListener {
        void onStationSelect(int stationIndex);
    }

    private OnStationSelectListener mListener;

    private static final float NAVIGATION_DEFAULT_CAMERA_ZOOM = 16f;

    protected static final float OVERVIEW_CAMERA_ZOOM = 9.5f;

    private Float lastCameraZoom = null;

    private GoogleMap mMap = null;

    private LatLng lastCameraPos = null;

    private List<Marker> markers = new ArrayList<>();

    private static final String TAG = "EDK";

    private LatLng lastUserPosition = null;

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
        TempSettings settings = TempSettings.get(getActivity());
        float defaultValue = -1;
        float loadedZoom = settings.getFloat(TempSettings.CAMERA_ZOOM, defaultValue);
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
            TempSettings.get(getActivity()).set(TempSettings.CAMERA_ZOOM, lastCameraZoom);
        }
    }

    private void focusCameraOnLastLocation() {
        LatLng center = getCameraPos();
        float zoomToApply = getCameraZoomToApply();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, zoomToApply));
    }

    private float getCameraZoomToApply() {
        float zoomToApply = getInitialCameraZoom();
        if (lastCameraZoom != null) {
            zoomToApply = lastCameraZoom;
        }
        return zoomToApply;
    }

    protected float getInitialCameraZoom() {
        return NAVIGATION_DEFAULT_CAMERA_ZOOM;
    }

    protected LatLng getCameraPos() {
        KMLTracker tracker = getTracker();
        LatLng center = null;
        LatLng lastLoc = tracker.getLastLoc();
        if (shouldFollowLocation() && lastLoc != null) {
            center = lastLoc;
        } else if (lastCameraPos != null) {
            center = lastCameraPos;
        }
        if (center == null) {
            List<LatLng> track = tracker.getTrack();
            center = track.get(0);
        }
        return center;
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
            LatLng position = checkpoints.get(i);
            if (position == null) {
                continue;
            }
            marker.position(position);
            marker.title(getString(R.string.station) + " " + NumConverter.toRoman(i));
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
    public void onCameraIdle() {
        if (mMap == null) {
            return;
        }
        lastCameraZoom = mMap.getCameraPosition().zoom;
        lastCameraPos = mMap.getCameraPosition().target;
    }

    @Override
    public void onCheckpointReached(int checkpointId) {
        if (getActivity() == null) {
            return;
        }
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
        if (getActivity() == null) {
            return;
        }
        if (mMap != null && shouldFollowLocation()) {
            if (!Settings.get(getActivity()).getBoolean(Settings.ROTATE_MAP_TO_WALK_DIRECTION)) { // map oriented to north

                mMap.animateCamera(CameraUpdateFactory.newLatLng(location), 2000, null);

            } else { // map oriented to walk direction

                if (lastUserPosition == null) {
                    lastUserPosition = getTracker().getLastLoc();
                }
                float currentBearing = getBearing(lastUserPosition, location);

                CameraPosition cameraPosition = new CameraPosition.Builder(mMap.getCameraPosition())
                        .target(location)      // Sets the center of the map
                        .zoom(getCameraZoomToApply())                   // Sets the zoom
                        .bearing(currentBearing)                // Sets the orientation of the camera
                        //.tilt(30)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 500, null);
                //Toast.makeText(getContext(),"bearing "+currentBearing,Toast.LENGTH_SHORT).show();
                lastUserPosition = location;

            }
        }

    }

    /**
     * Method for calculating bearing (walk direction) between two locations. North = 0, East = 90 deg
     *
     * @param prevLocation    - previous location
     * @param currentLocation - current location
     * @return walk bearing in degrees. If both locations are equal, returns 0.
     */
    public float getBearing(LatLng prevLocation, LatLng currentLocation) {
        double output;
        double dX = currentLocation.latitude - prevLocation.latitude;
        double dY = currentLocation.longitude - prevLocation.longitude;
        try {
            if (dY == 0) {
                return (dX >= 0) ? 0 : 180;
            }
            if (dX == 0) {
                return (dY >= 0) ? 90 : 270;
            }
            output = Math.atan(dY / dX) * 180 / Math.PI;
            if (dX < 0) {
                return (float) (output + 180);
            }
            if (dY < 0) {
                return (float) (output + 360);
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private boolean shouldFollowLocation() {
        return Settings.get(getActivity()).getBoolean(Settings.FOLLOW_LOCATION_ON_MAP);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        Log.i(TAG, "Map initialization");
        Log.i(TAG, "isVisible() " + isVisible() + " isMenuVisible()" + isMenuVisible());

        boolean permissionGranted = useLocationIfPermissionGranted();

        if (permissionGranted) {
            initMap();
            return;
        }
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_MAP);
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
    public void doSetMenuVisibility(final boolean visible) {
        if (mMap == null) {
            return;
        }
        useLocationIfPermissionGranted();
    }

    private boolean useLocationIfPermissionGranted() {
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return false;
        }
        boolean canUseGPS = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (canUseGPS) {
            mMap.setMyLocationEnabled(isFragmentVisible());
        }
        return canUseGPS;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != REQUEST_CODE_MAP && requestCode != REQUEST_CODE_STORAGE) {
            return;
        }
        if (grantResults.length == 0) {
            return;
        }
        if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initMap();
            } else {
                buildAlertMessageNoGpsPermissions();
            }
        } else if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                BootStrap.initStorage(getContext());
                initTrackerRelatedMapStuff();
            } else {
                DialogUtil.showWarningDialog(R.string.no_storage_permission_message, getActivity(), false);
            }
        }
    }

    private void initMap() {
        UiSettings settings = mMap.getUiSettings();
        settings.setCompassEnabled(true);
        settings.setZoomControlsEnabled(true);
        settings.setAllGesturesEnabled(true);
        settings.setMapToolbarEnabled(false);
        setMapPadding();
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnCameraIdleListener(this);

        FragmentActivity activity = getActivity();
        if (activity != null && ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE);
        }

        initTrackerRelatedMapStuff();
    }

    private void initTrackerRelatedMapStuff() {
        if (verifyTracker()) {
            return;
        }

        decorateMap();
        focusCameraOnLastLocation();

        changeLocationUpdates(isFragmentVisible());
    }

    @NonNull
    @Override
    protected LocationRequest getLocationRequest() {
        return LocationRequest.create().setPriority(LocationRequest.PRIORITY_NO_POWER).setInterval(1000);
    }

    private void startAppSettingsActivity() {
        Intent intent = new Intent();
        intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
        intent.setData(uri);
        getActivity().startActivity(intent);
    }

    private void buildAlertMessageNoGpsPermissions() {
        if (Settings.DO_NOT_SHOW_AGAIN_GPS_PERMISSIONS_DIALOG) {
            return;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater adbInflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View eulaLayout = adbInflater.inflate(R.layout.checkbox, null);
        final CheckBox dontShowAgain = (CheckBox) eulaLayout.findViewById(R.id.skip);
        builder.setView(eulaLayout);

        builder.setMessage(R.string.no_gps_permission_message_yesno);
        builder.setTitle(R.string.no_permission_title);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                if (dontShowAgain.isChecked()) {
                    Settings.DO_NOT_SHOW_AGAIN_GPS_PERMISSIONS_DIALOG = true;
                }
                startAppSettingsActivity();
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                if (dontShowAgain.isChecked()) {
                    Settings.DO_NOT_SHOW_AGAIN_GPS_PERMISSIONS_DIALOG = true;
                }
                dialog.cancel();
            }
        });
        final AlertDialog dialog = builder.show();
        DialogUtil.addRedTitleDivider(getActivity(), dialog);
    }
}
