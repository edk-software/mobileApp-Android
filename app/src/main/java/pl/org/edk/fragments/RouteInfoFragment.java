package pl.org.edk.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

import java.util.concurrent.TimeUnit;

import pl.org.edk.R;
import pl.org.edk.Settings;
import pl.org.edk.kml.KMLTracker;

/**
 * Created by darekpap on 2016-01-19.
 */
public class RouteInfoFragment extends TrackerFragment {

    private long mStartTime;
    private TextView mTimeView;
    private TextView mDistanceTraveledView;
    private TextView mDistanceToNextView;
    private TextView mDistanceLeftView;
    private final String mDistanceFormat = "%.1f km";

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

        updateDistances();
        mStartTime = Settings.get(getActivity()).getLong(Settings.START_TIME, System.currentTimeMillis());

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        mTimeView.removeCallbacks(updateTime);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTime.run();
    }

    @NonNull
    @Override
    protected LocationRequest getLocationRequest() {
        return LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(60000);
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
        double[] distanceInfo = getTracker().getDistanceInfo();
        mDistanceTraveledView.setText(String.format(mDistanceFormat, distanceInfo[KMLTracker.DISTANCE_TRAVELED_INDEX] / 1000));
        mDistanceLeftView.setText(String.format(mDistanceFormat, distanceInfo[KMLTracker.DISTANCE_LEFT_INDEX] / 1000));
        mDistanceToNextView.setText(String.format(mDistanceFormat, distanceInfo[KMLTracker.DISTANCE_TO_NEXT_INDEX] / 1000));


    }
}
