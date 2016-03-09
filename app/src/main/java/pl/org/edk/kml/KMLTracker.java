package pl.org.edk.kml;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import java.util.ArrayList;
import java.util.List;

public class KMLTracker implements LocationListener, OnConnectionFailedListener, ConnectionCallbacks {
    public static final int DISTANCE_TRAVELED_INDEX = 0;
    public static final int DISTANCE_LEFT_INDEX = 1;
    public static final int DISTANCE_TO_NEXT_INDEX = 2;


    private static final double CHECKPOINT_MIN_DIST = 200;
    private static final String TAG = "EDK";
    private static final float ACCEPTED_ACCURACY = 50;
    private static final double MIN_REF_DISTANCE = 200;
    private final double trackLength;
    private final List<Double> mStationsDistancesTraveled;
    private final List<Integer> mStationIndexes;
    private List<LatLng> track;
    private List<LatLng> checkpoints;
    private LatLng currentLoc = null;
    private List<TrackListener> listeners = new ArrayList<>();
    private int checkpointId = -1;
    private State prevState = null;
    private State currState = State.WaitingForPosition;
    private Context mContext;
    private GoogleApiClient mLocationClient = null;
    private static final long FIVE_MINUTES = 300000;
    private LocationRequest mQueuedRequest;
    private int mClosestIndex;
    private boolean mStarted;

    @NonNull
    private static LocationRequest getBackgroundRequest() {
        return LocationRequest.create().setInterval(FIVE_MINUTES)
                .setFastestInterval(16) // 16ms = 60fps
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public KMLTracker(Track track, Context context) {
        mContext = context;
        this.track = track.getTrackPoints();
        this.checkpoints = track.getCheckpoints();
        mStationIndexes = findStationIndexes();
        mStationsDistancesTraveled = calculateDistancesForStations();
        this.trackLength = trackDistanceBetween(0, this.track.size() - 1);
    }

    private List<Integer> findStationIndexes() {
        ArrayList<Integer> indexes = new ArrayList<>();
        for (LatLng checkpoint : checkpoints) {
            indexes.add(track.indexOf(checkpoint));
        }
        return indexes;

    }

    private List<Double> calculateDistancesForStations() {
        if (!isComplete()) {
            ArrayList<Double> distances = new ArrayList<>();
            for (int i = 0; i < mStationIndexes.size(); i++) {
                distances.add(0.0);
            }
            return distances;


        }
        ArrayList<Double> distances = new ArrayList<>();
        int previous = 0;
        for (int stationIndex = 0; stationIndex < mStationIndexes.size(); stationIndex++) {
            Integer trackPointIndex = mStationIndexes.get(stationIndex);

            Double previousDist = stationIndex == 0 ? 0.0 : distances.get(stationIndex - 1);
            if (trackPointIndex == -1) {
                distances.add(0.0 + previousDist);
                continue;
            }
            distances.add(calcTrackDistanceBetween(previous, trackPointIndex) + previousDist);
            previous = trackPointIndex;

        }

        return distances;

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
        int closeEnoughDist = 20;
        int locationsToCheckAfterCloseFound = 10;

        boolean closeEnoughFound = false;

        int index = 0;
        for (int i = 0; i < track.size(); i++) {
            if ((closeEnoughFound && i - index > locationsToCheckAfterCloseFound)) {
                return index;
            }
            LatLng point = track.get(i);
            double currDist = distanceBetween(point, checkpoint);
            if (currDist < dist) {
                dist = currDist;
                if (dist < closeEnoughDist) {
                    closeEnoughFound = true;
                }
                index = i;
            }
        }
        return index;
    }

    public void addListener(TrackListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            Log.i(TAG, "Listener added " + listener.getClass().getSimpleName() + ", current list size is " + listeners.size());
        } else {
            Log.i(TAG, "Listener already registered " + listener.getClass().getSimpleName());
        }
    }

    public void removeListener(TrackListener listener) {
        listeners.remove(listener);
        if (listeners.isEmpty() && !mStarted) {
            removeLocationUpdates();
        }
        Log.i(TAG, "Listener removed " + listener.getClass().getSimpleName() + ", current list size is " + listeners.size());
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

    public double[] getDistanceInfo() {
        if (currentLoc == null) {
            double distanceToNext = trackDistanceBetween(0, mStationIndexes.get(0));

            return new double[]{0, trackLength, distanceToNext};
        }

        double traveled = trackDistanceBetween(0, mClosestIndex);
        int nextCheckpointId = getNextCheckpointId();
        double distanceToNext = nextCheckpointId == -1 ? 0 : trackDistanceBetween(mClosestIndex, mStationIndexes.get(nextCheckpointId));
        return new double[]{traveled, trackLength - traveled, distanceToNext};
    }

    public int getCheckpointId() {
        return checkpointId;
    }

    public int getNextCheckpointId() {
        if (checkpointId == 15) {
            return -1;
        }
        if (checkpointId != -1 && checkpointId < 15) {
            return getNextValidCheckpointId(checkpointId + 1);
        }
        if (currentLoc == null) {
            return 0;
        }
        for (int id = 0; id < mStationIndexes.size(); id++) {
            if (mClosestIndex < mStationIndexes.get(id)) {
                return id;
            }
        }
        return -1;
    }

    private int getNextValidCheckpointId(int firstCandidateId) {
        for (int i = firstCandidateId; i < checkpoints.size(); i++) {
            if (checkpoints.get(i) != null) {
                return i;
            }
        }
        return -1;
    }

    public boolean isComplete() {
        for (LatLng pos : checkpoints) {
            if (pos == null) {
                return false;
            }
        }
        return true;

    }

    public State getState() {
        return currState;
    }


    public void requestLocationUpdates(LocationRequest locationRequest) {
        if (mLocationClient == null) {
            initLocationClient();
            mQueuedRequest = locationRequest;
            return;
        }
        if (mLocationClient.isConnected()) {
            try {
                LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, locationRequest, this);
            } catch (SecurityException se) {
                Log.e(TAG, "Cannot use location api", se);
            }
            return;
        }
        if (!mLocationClient.isConnecting()) {
            initLocationClient();
        }
        mQueuedRequest = locationRequest;

    }

    public void removeLocationUpdates() {
        mQueuedRequest = null;
        mStarted = false;
        if (mLocationClient != null && mLocationClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mLocationClient, this);
            Log.i(TAG, "Location client updates removed");
        }
    }

    public void start() {
        requestLocationUpdates(getBackgroundRequest());
        mStarted = true;

    }

    private void initLocationClient() {
        if (mLocationClient == null) {
            Builder builder = new Builder(mContext);
            builder.addApi(LocationServices.API);
            builder.addConnectionCallbacks(this);
            builder.addOnConnectionFailedListener(this);
            mLocationClient = builder.build();
            mLocationClient.connect();
        }
    }

    public void stop() {
        removeLocationUpdates();
        if (mLocationClient != null && mLocationClient.isConnected()) {
            mLocationClient.disconnect();
            mLocationClient = null;
            Log.i(TAG, "Location client updates removed");
        }
    }

    public boolean isStarted() {
        return mLocationClient != null && mStarted;
    }

    /**
     * Callback called when connected to GCore. Implementation of
     * {@link ConnectionCallbacks}.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        try {
            Log.i(TAG, "Connected to location client");
            Location location = LocationServices.FusedLocationApi.getLastLocation(mLocationClient);
            if (location != null) {
                currentLoc = new LatLng(location.getLatitude(), location.getLongitude());
                mClosestIndex = getClosestIndex(currentLoc);
            }
            if (mQueuedRequest != null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mQueuedRequest, this);
            }
        } catch (SecurityException se) {
            Log.e(TAG, "Cannot use location api", se);
        }
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
        mClosestIndex = getClosestIndex(currentLoc);
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
            if (point == null) {
                continue;
            }
            if (distanceBetween(point, currentLoc) < CHECKPOINT_MIN_DIST) {
                return checkpoints.indexOf(point);
            }
        }
        return -1;
    }

    private void checkIfOutOfTrack() {
        int next = isAfter(currentLoc, mClosestIndex) ? mClosestIndex + 1 : mClosestIndex - 1;
        double refDist;
        if (next < 0 || next >= track.size()) {
            refDist = MIN_REF_DISTANCE;
        } else {
            refDist = Math.max(2 * distanceBetween(mClosestIndex, next), MIN_REF_DISTANCE);
        }
        prevState = currState;
        if (distanceBetween(mClosestIndex, currentLoc) < refDist) {
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

    private double trackDistanceBetween(int startIndex, int endIndex) {
        int lastBeforeStart = 0;
        int lastBeforeEnd = 0;

        for (int i = 0; i < mStationIndexes.size(); i++) {
            Integer index = mStationIndexes.get(i);
            if (index == -1) {
                continue;
            }
            if (startIndex > index) {
                lastBeforeStart = i;
            }
            if (endIndex > index) {
                lastBeforeEnd = i;
            }
        }
        if (lastBeforeEnd <= lastBeforeStart + 1) {
            return calcTrackDistanceBetween(startIndex, endIndex);
        }

        if (lastBeforeEnd < 0 || lastBeforeEnd >= mStationsDistancesTraveled.size() || lastBeforeStart < 0 || lastBeforeStart >= mStationsDistancesTraveled.size()) {
            //backup to make sure we don't throw exception, shouldn't happen:)
            Log.w(TAG, "Unexpected values when finding distance between track points. Fallback to naive calculation");
            return calcTrackDistanceBetween(startIndex, endIndex);
        }

        double distBetweenStations = mStationsDistancesTraveled.get(lastBeforeEnd) - mStationsDistancesTraveled.get(lastBeforeStart + 1);
        return calcTrackDistanceBetween(startIndex, mStationIndexes.get(lastBeforeStart + 1)) + distBetweenStations + calcTrackDistanceBetween(mStationIndexes.get(lastBeforeEnd), endIndex);
    }

    private double calcTrackDistanceBetween(int startIndex, int endIndex) {
        double length = 0.0;
        for (int i = startIndex + 1; i <= endIndex; i++) {
            length += distanceBetween(i - 1, i);
        }
        return length;
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
        OnTrack, OffTrack, NearCheckpoint, WaitingForPosition
    }

    public interface TrackListener {
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
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.w(TAG, "Connection failed");
        Log.w(TAG, result.toString());

    }

}
