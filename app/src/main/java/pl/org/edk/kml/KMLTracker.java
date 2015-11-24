package pl.org.edk.kml;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;


public class KMLTracker implements LocationListener, OnConnectionFailedListener, ConnectionCallbacks  {
	private static final double CHECKPOINT_MIN_DIST = 200;
	private static final String TAG = "EDK";
	private static final float ACCEPTED_ACCURACY = 50;
	private static final double MIN_REF_DISTANCE = 200;
	private List<LatLng> track;
	private List<LatLng> checkpoints;
	private LatLng currentLoc = null;
	private List<TrackListener> listeners = new ArrayList<TrackListener>();
	private int checkpointId = -1;
//	private Set<Integer> visited = new HashSet<Integer>();
	private State prevState = null;
	private State currState = State.WaitingForPosition;
	private Context mContext;
	private GoogleApiClient mLocationClient = null;
//	private static final long TWO_MINUTES = 120000;
//	 private static final long TEN_MINUTES = 600000;
	 private static final long FIVE_MINUTES = 300000;
	private static final LocationRequest REQUEST = LocationRequest.create().setInterval(FIVE_MINUTES)
			.setFastestInterval(16) // 16ms = 60fps
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	
	public KMLTracker(Track track, Context context) {
		mContext = context;
		this.track = track.getTrackPoints();
		this.checkpoints = track.getCheckpoints();
	}

	private boolean isAfter(LatLng checkpoint, int index) {
		if (track.size() <= 2) {
			return false;
		}
		if (index < track.size() - 1) {
			LatLng closest = track.get(index);
			LatLng next = track.get(index + 1);
			if (distanceBetween(closest, next) > distanceBetween(checkpoint, next)) {
				return true;
			}
		} else {
			LatLng last = track.get(track.size() - 1);
			LatLng beforeLast = track.get(track.size() - 2);
			if (distanceBetween(last, beforeLast) < distanceBetween(checkpoint, beforeLast)) {
				return true;
			}
		}
		return false;
	}

	private int getClosestIndex(LatLng checkpoint) {
		double dist = Double.MAX_VALUE;
		int index = 0;
		for (int i = 0; i < track.size(); i++) {
			LatLng point = track.get(i);
			double currDist = distanceBetween(point, checkpoint);
			if (currDist < dist) {
				dist = currDist;
				index = i;
			}
		}
		return index;
	}

	public void addListener(TrackListener listener) {
		listeners.add(listener);
		Log.i(TAG, "Listener added, current list size is " + listeners.size());
	}

	public void removeListener(TrackListener listener) {
		listeners.remove(listener);
		Log.i(TAG, "Listener removed, current list size is " + listeners.size());
	}
	
	public List<LatLng> getTrack() {
		return track;
	}
	
	public List<LatLng> getCheckpoints() {
		return checkpoints;
	}
	
	public LatLng getLastLoc() {
		return currentLoc;
	}
	
	public int getCheckpointId() {
		return checkpointId;
	}
	
	public int getNextCheckpointId() {
		if (checkpointId != -1 && checkpointId < 15) {
			return checkpointId + 1;
		}
		if (currentLoc == null) {
			return 0;
		}
		int closest = getClosestIndex(currentLoc);
		for (int id = 0; id < checkpoints.size(); id++) {
			LatLng checkpoint = checkpoints.get(id);
			if (closest < track.indexOf(checkpoint)) {
				return id;
			}
		}
		return 0;
	}
	
	public State getState() {
		return currState;
	}
	
	public void start() {
		if (mLocationClient == null) {
			Builder builder = new GoogleApiClient.Builder(mContext);
			builder.addApi(LocationServices.API);
			builder.addConnectionCallbacks(this);
			builder.addOnConnectionFailedListener(this);
			mLocationClient = builder.build();
			mLocationClient.connect();
		}
	}

	public void stop() {
		if (mLocationClient != null && mLocationClient.isConnected()) {
			LocationServices.FusedLocationApi.removeLocationUpdates(mLocationClient, this);
			mLocationClient.disconnect();
			mLocationClient = null;
			Log.i(TAG, "Location client updates removed");
		}
	}
	
	public boolean isStarted() {
		return mLocationClient != null;
	}
	
	/**
	 * Callback called when connected to GCore. Implementation of
	 * {@link ConnectionCallbacks}.
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		Log.i(TAG, "Connected to location client");
		Location location = LocationServices.FusedLocationApi.getLastLocation(mLocationClient);
		if (location != null) {
			currentLoc = new LatLng(location.getLatitude(), location.getLongitude());
		}
		LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, REQUEST, this);
		Log.i(TAG, "Location client updates requested");
	}

	@Override
	public void onLocationChanged(Location location) {
		if (location == null) {
			Log.w(TAG, "Location was null");
			return;
		}
		Log.i("EDK", "location received: " + location.getLatitude() + " " + location.getLongitude());
		if (location.getAccuracy() > ACCEPTED_ACCURACY) {
			return;
		}
		currentLoc = new LatLng(location.getLatitude(), location.getLongitude());
		checkIfOutOfTrack();
		checkIfCheckpointReached();
		notifyListeners();
		
	}

	private void checkIfCheckpointReached() {
		checkpointId = determineCheckpointId();
		if (checkpointId != -1) {
			currState = State.NearCheckpoint;
		}
	}

	private int determineCheckpointId() {
		for (LatLng point : checkpoints) {
			if (distanceBetween(point, currentLoc) < CHECKPOINT_MIN_DIST) {
				return checkpoints.indexOf(point);
			}
		}
		return -1;
	}
	
	private void checkIfOutOfTrack() {
		int closest = getClosestIndex(currentLoc);
		int next = isAfter(currentLoc, closest) ? closest + 1 : closest - 1;
		double refDist;
		if (next < 0 || next >= track.size()) {
			refDist = MIN_REF_DISTANCE;
		} else {
			refDist = Math.max(2 * distanceBetween(closest, next), MIN_REF_DISTANCE);
		}
		prevState = currState;
		if (distanceBetween(closest, currentLoc) < refDist) {
			currState = State.OnTrack;
		} else {
			currState = State.OffTrack;
		}
	}
	
	private double distanceBetween(LatLng point, LatLng checkpoint) {
		float[] results = new float[3];
		Location.distanceBetween(point.latitude, point.longitude, checkpoint.latitude, checkpoint.longitude, results);
		return results[0];
	}

	private double distanceBetween(int index, LatLng location) {
		return distanceBetween(track.get(index), location);
	}

	private double distanceBetween(int firstIndex, int secondIndex) {
		return distanceBetween(track.get(firstIndex), track.get(secondIndex));
	}

	private void notifyListeners() {
		for (TrackListener listener : listeners) {
			listener.onLocationChanged(currentLoc);
		}
		if (currState == prevState) {
			return;
		}
		switch (currState) {
		case NearCheckpoint:
			for (TrackListener listener : listeners) {
				listener.onCheckpointReached(checkpointId);
			}
			break;
		case OffTrack:
			for (TrackListener listener : listeners) {
				listener.onOutOfTrack();
			}
			break;
		case OnTrack:
			for (TrackListener listener : listeners) {
				listener.onBackOnTrack();
			}
			break;
		default:
			break;
		}
	}
	
	public enum State {
		OnTrack, OffTrack, NearCheckpoint, WaitingForPosition;
	}

	public static interface TrackListener {
		void onCheckpointReached(int checkpointId);

		void onOutOfTrack();
		
		void onBackOnTrack();
		
		void onLocationChanged(LatLng location);
	}

	@Override
	public void onConnectionSuspended(int flag) {
		Log.d(TAG, "Connection suspended with " + flag);
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.w(TAG, "Connection failed");
		Log.w(TAG, result.toString());
		
	}

}
