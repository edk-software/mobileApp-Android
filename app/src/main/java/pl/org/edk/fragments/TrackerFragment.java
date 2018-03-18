package pl.org.edk.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

import pl.org.edk.R;
import pl.org.edk.Settings;
import pl.org.edk.TempSettings;
import pl.org.edk.kml.KMLTracker;
import pl.org.edk.kml.TrackerProvider;
import pl.org.edk.util.DialogUtil;

/**
 * Created by darekpap on 2016-02-14.
 *
 */
public abstract class TrackerFragment extends Fragment implements KMLTracker.TrackListener {
    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 0;
    private boolean mTrackerAvailable = true;

    @Override
    public void onResume() {
        super.onResume();
        if (!isFragmentVisible()) {
            return;
        }
        if (verifyTracker()) {
            return;
        }
        verifyWhetherServicesAvailable();
        verifyWhetherGPSIsEnabled();
        changeLocationUpdates(true);
        processTrackerState();
    }

    protected boolean isFragmentVisible() {
        return isVisible() && isMenuVisible();
    }

    @Override
    public void onPause() {
        super.onPause();
        //isFragmentVisible returns true when we hide the app using menu button or lock the screen
        if (!isFragmentVisible()) {
            return;
        }
        if (verifyTracker()) {
            return;
        }
        changeLocationUpdates(false);
    }

    protected KMLTracker getTracker() {
        return TrackerProvider.getTracker(getActivity());
    }

    private void processTrackerState() {
        KMLTracker tracker = getTracker();
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
    }

    public abstract void onCheckpointReached(int checkpointId);

    public abstract void onOutOfTrack();

    public abstract void onBackOnTrack();

    public abstract void onLocationChanged(LatLng location);

    protected boolean verifyTracker() {
        if (!mTrackerAvailable) {
            return true;
        }
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return true;
        }
        TempSettings tempSettings = TempSettings.get(activity);
        try {
            KMLTracker tracker = getTracker();
            if (!tracker.isComplete() && !tempSettings.getBoolean(TempSettings.TRACK_WARNING_SHOWN) && isFragmentVisible()) {
                DialogUtil.showWarningDialog(R.string.stations_missing_warning_message, activity, false);
                tempSettings.set(TempSettings.TRACK_WARNING_SHOWN, true);
            }
            return false;
        } catch (Exception e) {
            mTrackerAvailable = false;
            Log.e("EDK", "Invalid track", e);
            if (isFragmentVisible() && !tempSettings.getBoolean(TempSettings.TRACK_WARNING_SHOWN)) {
                DialogUtil.showWarningDialog(R.string.unrecognized_error_while_reading_track, activity, false);
                tempSettings.set(TempSettings.TRACK_WARNING_SHOWN, true);
            }
            return true;
        }
    }

    private void verifyWhetherGPSIsEnabled() {
        FragmentActivity activity = getActivity();
        if (activity == null){
            return;
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED){
            return;
        }
        final LocationManager manager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (manager == null){
            return;
        }
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }

    private void buildAlertMessageNoGps() {
        if (Settings.get(getActivity()).getBoolean(Settings.DO_NOT_SHOW_GPS_DIALOG)) {
            return;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater adbInflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View eulaLayout = adbInflater.inflate(R.layout.checkbox, null);
        final CheckBox dontShowAgain = (CheckBox) eulaLayout.findViewById(R.id.skip);
        builder.setView(eulaLayout);

        builder.setMessage(R.string.GPS_off_warning_message);
        builder.setTitle(R.string.warning_dialog_title);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.go_to_location_settings, new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                if (dontShowAgain.isChecked()) {
                    Settings.get(getActivity()).set(Settings.DO_NOT_SHOW_GPS_DIALOG, true);
                }
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        builder.setNegativeButton(R.string.ignore, new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                if (dontShowAgain.isChecked()) {
                    Settings.get(getActivity()).set(Settings.DO_NOT_SHOW_GPS_DIALOG, true);
                }
                dialog.cancel();
            }
        });
        final AlertDialog dialog = builder.show();
        DialogUtil.addRedTitleDivider(getActivity(), dialog);
    }

    private void verifyWhetherServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int resultCode = api.isGooglePlayServicesAvailable(getActivity());
        if (ConnectionResult.SUCCESS == resultCode) {
            return;
        }
        if (api.isUserResolvableError(resultCode)) {
            api.getErrorDialog(getActivity(), resultCode, CONNECTION_FAILURE_RESOLUTION_REQUEST).show();
        } else {
            Toast.makeText(getActivity(), R.string.google_api_unavailable_message, Toast.LENGTH_LONG).show();
            getActivity().finish();
        }
    }

    protected void changeLocationUpdates(boolean visible) {
        KMLTracker tracker = getTracker();
        if (visible) {
            tracker.addListener(this);
        } else {
            tracker.removeListener(this);
        }

        if (Settings.get(getActivity()).getBoolean(Settings.IS_BACKGROUND_TRACKING_ON)) {
            return;
        }

        if (visible) {
            LocationRequest request = getLocationRequest();
            tracker.requestLocationUpdates(request);
        } else {
            tracker.removeLocationUpdates();
        }
    }

    @NonNull
    protected abstract LocationRequest getLocationRequest();

    @Override
    public void setMenuVisibility(final boolean visible) {
        Log.d("EDK", "setMenuVisibility called with " + visible);
        super.setMenuVisibility(visible);
        if (getActivity() == null) {
            Log.d("EDK", "Activity was null in setMenuVisibility");
            return;
        }
        if (verifyTracker()) {
            doSetMenuVisibility(visible);
            return;
        }

        changeLocationUpdates(visible);

        if (visible) {
            verifyWhetherServicesAvailable();
            verifyWhetherGPSIsEnabled();
        }
        doSetMenuVisibility(visible);
    }

    protected void doSetMenuVisibility(boolean visible) {
    }
}
