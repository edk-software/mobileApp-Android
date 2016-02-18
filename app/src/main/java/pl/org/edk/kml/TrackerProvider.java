package pl.org.edk.kml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import pl.org.edk.database.DbManager;
import pl.org.edk.database.entities.Route;
import pl.org.edk.R;
import pl.org.edk.Settings;

import android.content.Context;
import android.util.Log;

public class TrackerProvider {
    private static final String TAG = "EDK";
    private KMLTracker mKMLTracker;
    private String mKmlPath;
    private Context mContext;

    private static TrackerProvider INSTANCE = null;

    private TrackerProvider(Context context) {
        mContext = context;
    }

    public synchronized static void dismiss() {
        INSTANCE = null;
    }

    public synchronized static KMLTracker getTracker(Context context) {
        TrackerProvider provider = getTrackerProvider(context.getApplicationContext());
        return provider.getTracker();
    }

    private synchronized static TrackerProvider getTrackerProvider(Context applicationContext) {
        if (INSTANCE == null) {
            INSTANCE = new TrackerProvider(applicationContext);
        }
        return INSTANCE;
    }

    private KMLTracker getTracker() {
        Route route = DbManager.getInstance(mContext).getRouteService()
                .getRoute(Settings.get(mContext).getLong(Settings.SELECTED_ROUTE_ID, -1), "pl");
        String kmlPath = route.getKmlDataPath();

        boolean wasStarted = false;
        if (mKMLTracker != null && !kmlPath.equals(mKmlPath)) {
            wasStarted = mKMLTracker.isStarted();
            mKMLTracker.stop();
            mKMLTracker = null;
        }
        if (mKMLTracker == null) {
            initializeTracker(kmlPath);
            mKmlPath = kmlPath;
        }
        if (wasStarted) {
            mKMLTracker.start();
        }
        return mKMLTracker;
    }

    private void initializeTracker(String trackId) {
        InputStream stream = null;
        try {
//			InputStream stream = mContext.getAssets().open("tracks/" + trackId + ".kml");
            stream = new FileInputStream(trackId);
            Track track = Track.fromStream(stream);
            if (track == null) {
                throw new IllegalStateException(mContext.getString(R.string.unrecognized_error_while_reading_track));
            }
            mKMLTracker = new KMLTracker(track, mContext);
        } catch (Exception e) {
            Log.e(TAG, "Couldn't create kml tracker for " + trackId, e);
            throw new IllegalStateException(mContext.getString(R.string.unrecognized_error_while_reading_track), e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to close stream", e);
                }
            }
        }
        Log.i(TAG, "mKMLTracker created");
    }

}
