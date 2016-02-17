package pl.org.edk.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import pl.org.edk.R;
import pl.org.edk.Settings;
import pl.org.edk.kml.KMLTracker;
import pl.org.edk.kml.TrackerProvider;
import pl.org.edk.util.DialogUtil;
import pl.org.edk.util.NumConverter;

/**
 * Created by darekpap on 2016-02-14.
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

        try {
            getTracker();
            return false;
        } catch (Exception e) {
            mTrackerAvailable = false;
            Log.e("EDK", "Invalid track", e);
            DialogUtil.showWarningDialog(e.getMessage(), getActivity(), false);
            return true;
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

    private void verifyWhetherServicesAvailable() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (ConnectionResult.SUCCESS == resultCode) {
            return;
        }

        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(),
                CONNECTION_FAILURE_RESOLUTION_REQUEST);
        errorDialog.show();
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
            return;
        }

        changeLocationUpdates(visible);

        if (visible) {
            verifyWhetherServicesAvailable();
            verifyWhetherGPSIsEnabled();
        }
    }
}
