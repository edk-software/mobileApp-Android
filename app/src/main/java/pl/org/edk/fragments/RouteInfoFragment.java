package pl.org.edk.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

import pl.org.edk.EndActivity;
import pl.org.edk.R;
import pl.org.edk.Settings;
import pl.org.edk.kml.KMLTracker;
import pl.org.edk.services.GPSService;
import pl.org.edk.util.DialogUtil;

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

        mStartTime = Settings.get(getActivity()).getLong(Settings.START_TIME, System.currentTimeMillis());

        configureFinishButton(view);

        return view;
    }

    private void configureFinishButton(View view) {
        view.findViewById(R.id.finishButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.warning_dialog_title);
                builder.setMessage(R.string.end_dialog_message);
                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        getActivity().stopService(new Intent(getActivity(), GPSService.class));
                        Intent intent = new Intent(getActivity(), EndActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
                AlertDialog dialog = builder.show();
                DialogUtil.addRedTitleDivider(getActivity(), dialog);
            }
        });
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

    @Override
    public void setMenuVisibility(boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
            updateDistances();
        }
    }

    private void updateDistances() {
        if (verifyTracker()) {
            return;
        }
        double[] distanceInfo = getTracker().getDistanceInfo();
        mDistanceTraveledView.setText(String.format(mDistanceFormat, distanceInfo[KMLTracker.DISTANCE_TRAVELED_INDEX] / 1000));
        mDistanceLeftView.setText(String.format(mDistanceFormat, distanceInfo[KMLTracker.DISTANCE_LEFT_INDEX] / 1000));
        mDistanceToNextView.setText(String.format(mDistanceFormat, distanceInfo[KMLTracker.DISTANCE_TO_NEXT_INDEX] / 1000));


    }
}
