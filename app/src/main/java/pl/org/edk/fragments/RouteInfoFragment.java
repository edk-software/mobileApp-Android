package pl.org.edk.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import pl.org.edk.R;
import pl.org.edk.Settings;
import pl.org.edk.kml.KMLTracker;
import pl.org.edk.kml.TrackerProvider;

/**
 * Created by darekpap on 2016-01-19.
 */
public class RouteInfoFragment extends Fragment implements KMLTracker.TrackListener {

    private KMLTracker mTracker;
    private Handler mTimeHandler = new Handler();
    private long mStartTime;
    private TextView mTimeView;
    private TextView mDistanceTraveledView;
    private TextView mDistanceToNextView;
    private TextView mDistanceLeftView;

    private final SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm:ss");

    private Runnable updateTime = new Runnable() {
        public void run() {

            long time = System.currentTimeMillis() - mStartTime;
            Log.d("EDK", "Measured time: " + time);
            long hours = TimeUnit.MILLISECONDS.toHours(time);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(time) - TimeUnit.HOURS.toMinutes(hours);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.MINUTES.toSeconds(minutes);
            mTimeView.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
            mTimeView.postDelayed(this, 1000);
        }
    };

    public RouteInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        View view = inflater.inflate(R.layout.fragment_route_info, container, false);

        mTimeView = (TextView) view.findViewById(R.id.timeValue);
        mDistanceTraveledView = (TextView) view.findViewById(R.id.distanceTraveledValue);
        mDistanceToNextView = (TextView) view.findViewById(R.id.distanceToStationValue);
        mDistanceLeftView = (TextView) view.findViewById(R.id.distanceLeftValue);

        initTracker();
        updateDistances();
        mStartTime = Settings.get(getActivity()).getLong(Settings.START_TIME, System.currentTimeMillis());

        return view;
    }

    private void initTracker() {
        if (mTracker == null) {
            mTracker = TrackerProvider.getTracker(getActivity());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mTracker.removeListener(this);
        mTimeHandler.removeCallbacks(updateTime);
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.addListener(this);
        updateTime.run();
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        Log.d("EDK", "setMenuVisibility called with " + visible);
        super.setMenuVisibility(visible);

        if (getActivity() == null) {
            Log.d("EDK", "Activity was null in setMenuVisibility");
            return;
        }

        changeLocationUpdates(visible);

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


    @Override
    public void onCheckpointReached(int checkpointId) {

    }

    @Override
    public void onOutOfTrack() {

    }

    @Override
    public void onBackOnTrack() {

    }

    @Override
    public void onLocationChanged(LatLng location) {

        updateDistances();

    }

    private void updateDistances() {
        double[] distanceInfo = mTracker.getDistanceInfo();
        mDistanceTraveledView.setText(String.format("%.1f", distanceInfo[KMLTracker.DISTANCE_TRAVELED_INDEX] / 1000));
        mDistanceLeftView.setText(String.format("%.1f", distanceInfo[KMLTracker.DISTANCE_LEFT_INDEX] / 1000));
        mDistanceToNextView.setText(String.format("%.1f", distanceInfo[KMLTracker.DISTANCE_TO_NEXT_INDEX] / 1000));


    }
}
