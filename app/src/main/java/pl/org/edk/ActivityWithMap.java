package pl.org.edk;

import java.util.ArrayList;
import java.util.List;

import pl.org.edk.kml.KMLTracker;
import pl.org.edk.kml.KMLTracker.State;
import pl.org.edk.kml.KMLTracker.TrackListener;
import pl.org.edk.kml.TrackerProvider;
import pl.org.edk.menu.ConsiderationsViewActivity;
import pl.org.edk.services.GPSService;
import pl.org.edk.util.DialogUtil;
import pl.org.edk.util.NumConverter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class ActivityWithMap extends ActivityWithGPSMenu implements OnInfoWindowClickListener, OnCameraChangeListener,
		OnMapReadyCallback, TrackListener {

	private static final float NAVIGATION_DEFAULT_CAMERA_ZOOM = 16f;

	private static final float OVERVIEW_CAMERA_ZOOM = 10f;

	private Float lastCameraZoom = null;

	private GoogleMap mMap = null;

	private LatLng lastCameraPos = null;

	private List<Marker> markers = new ArrayList<Marker>();

	private static final String TAG = "EDK";

	private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 0;

	// private ImageButton mConsiderationsButton;
	private Button mNavigationButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map2);

		// mConsiderationsButton = (ImageButton)
		// findViewById(R.id.considerationsButton);
		// mConsiderationsButton.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// goToConsiderations(null);
		// }
		// });
		mNavigationButton = (Button) findViewById(R.id.navigationButton);
		mNavigationButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				verifyWhetherGPSIsEnabled();
				Intent serviceIntent = new Intent(ActivityWithMap.this, GPSService.class);
				Settings.get(ActivityWithMap.this).set(Settings.IS_BACKGROUND_TRACKING_ON, true);
				setNavigationButtonVisibility();

				startService(serviceIntent);
				processTrackerState();
			}
		});

		Button considerationsButton = (Button) findViewById(R.id.considerationsButton);
		considerationsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				goToConsiderations(null);
			}
		});
		// if (verifyTracker()) {
		// return;
		// }
		// Intent serviceIntent = new Intent(this, GPSService.class);
		// Settings settings = Settings.get();
		// if (settings.getBoolean(Settings.CHALLENGE_ACCEPTED)) {
		// startService(serviceIntent);
		// } else {
		// stopService(serviceIntent);
		// }
	}

	private boolean verifyTracker() {
		try {
			TrackerProvider.getTracker(this);
			return false;
		} catch (Exception e) {
			DialogUtil.showWarningDialog(e.getMessage(), this, true);
			return true;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem item = menu.getItem(MAP_MENU_INDEX);
		item.setVisible(false);
		MenuItem item2 = menu.getItem(CONSIDERATIONS_MENU_INDEX);
		item2.setVisible(false);
		return true;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Intent serviceIntent = new Intent(this, GPSService.class);
		if (intent.getBooleanExtra(Extra.TRACK_CHANGED, false)) {
			clearMap();
			stopService(serviceIntent);
			if (verifyTracker()) {
				return;
			}
			// startService(serviceIntent);
		}
		// Settings settings = Settings.get();
		// if (!settings.getBoolean(Settings.CHALLENGE_ACCEPTED)) {
		// stopService(serviceIntent);
		// }
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (verifyTracker()) {
			return;
		}
		loadCameraZoom();
		setNavigationButtonVisibility();
		verifyWhetherServicesAvailable();
		verifyWhetherGPSIsEnabled();
		setUpMapIfNeeded();
		addTrackListener();
		processTrackerState();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (!hasFocus) {
			return;
		}
		setNavigationButtonVisibility();
	}

	private void setNavigationButtonVisibility() {
		if (Settings.get(this).getBoolean(Settings.IS_BACKGROUND_TRACKING_ON)) {
			mNavigationButton.setVisibility(View.INVISIBLE);
		} else {
			mNavigationButton.setVisibility(View.VISIBLE);
			setTitle(R.string.track_overview);
		}
		setMapPadding();
	}

	private void setMapPadding() {
		if (mMap == null) {
			return;
		}
		if (mNavigationButton.getVisibility() == View.INVISIBLE) {
			mMap.setPadding(0, 0, 0, 0);
		} else {
			mMap.setPadding(0, 0, 0, 80);
		}
	}

	private void processTrackerState() {
		if (Settings.get(this).getBoolean(Settings.IS_BACKGROUND_TRACKING_ON)) {

			KMLTracker tracker = TrackerProvider.getTracker(this);
			State state = tracker.getState();
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
				setTitle(R.string.waiting_for_position_message);
				break;
			default:
				break;
			}
		} else {
			setTitle(R.string.track_overview);
		}
	}

	private void loadCameraZoom() {
		Settings settings = Settings.get(this);
		float defaultValue = -1;
		float loadedZoom = settings.getFloat(Settings.CAMERA_ZOOM, defaultValue);
		if (Math.abs(loadedZoom - defaultValue) > 0.1) {
			lastCameraZoom = loadedZoom;
		}
	}

	private void verifyWhetherGPSIsEnabled() {
		final LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
		if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			buildAlertMessageNoGps();
		}
	}

	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
		DialogUtil.addRedTitleDivider(this, dialog);
	}

	private void addTrackListener() {
		KMLTracker tracker = TrackerProvider.getTracker(this);
		tracker.addListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (verifyTracker()) {
			return;
		}
		removeTrackListener();
		saveCameraZoom();
	}

	private void saveCameraZoom() {
		if (lastCameraZoom != null) {
			Settings.get(this).set(Settings.CAMERA_ZOOM, lastCameraZoom);
		}
	}

	private void removeTrackListener() {
		KMLTracker tracker = TrackerProvider.getTracker(this);
		tracker.removeListener(this);
	}

	private void clearMap() {
		if (mMap != null) {
			mMap.clear();
		}
	}

	private void verifyWhetherServicesAvailable() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (ConnectionResult.SUCCESS == resultCode) {
			return;
		}

		Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this,
				CONNECTION_FAILURE_RESOLUTION_REQUEST);
		errorDialog.show();
	}

	private void focusCameraOnLastLocation() {
		KMLTracker tracker = TrackerProvider.getTracker(this);
		LatLng center = null;
		float defaultCameraZoom = OVERVIEW_CAMERA_ZOOM;
		if (Settings.get(this).getBoolean(Settings.IS_BACKGROUND_TRACKING_ON)) {
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

	private void setUpMapIfNeeded() {
		if (mMap == null) {
			((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
		}

	}

	private void decorateMap() {
		KMLTracker tracker = TrackerProvider.getTracker(this);
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
		for (int i = 1; i < checkpoints.size()-1; i++) {
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
		Intent intent = new Intent(this, ConsiderationsViewActivity.class);
		intent.putExtra(Extra.STATION_ID, stationId);
		startActivity(intent);
	}

	private int getStationId(Marker marker) {
		String title = marker.getTitle();
		if (title.equals(getString(R.string.considerations_start_title))){
			return 0;
		}
		if (title.equals(getString(R.string.considerations_end_title))){
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
		setTitle(title);
	}

	@Override
	public void onOutOfTrack() {
		setTitle(getString(R.string.out_of_track_message));
	}

	@Override
	public void onBackOnTrack() {
		setTitle(getString(R.string.back_on_track_message));
	}

	@Override
	public void onLocationChanged(LatLng location) {
		if (mMap != null) {
			mMap.animateCamera(CameraUpdateFactory.newLatLng(location), 2000, null);
		}

	}

	@Override
	public void onMapReady(GoogleMap map) {
		mMap = map;
		Log.i(TAG, "Map initialization");
		// if (Settings.get().getBoolean(Settings.CHALLENGE_ACCEPTED)) {
		mMap.setMyLocationEnabled(true);
		// }
		UiSettings settings = mMap.getUiSettings();
		settings.setCompassEnabled(true);
		settings.setZoomControlsEnabled(true);
		settings.setAllGesturesEnabled(true);
		setMapPadding();
		mMap.setOnInfoWindowClickListener(this);
		mMap.setOnCameraChangeListener(this);
		decorateMap();
		focusCameraOnLastLocation();

	}

}
