package pl.org.edk.kml;

import java.io.InputStream;

import pl.org.edk.Database.DbManager;
import pl.org.edk.Database.Entities.Route;
import pl.org.edk.Database.Services.RouteService;
import pl.org.edk.R;
import pl.org.edk.Settings;
import pl.org.edk.menu.TrackInfo;
import pl.org.edk.util.ResourcesUtil;
import android.content.Context;
import android.util.Log;

public class TrackerProvider {
	private static final String TAG = "EDK";
	private KMLTracker mKMLTracker;
	private String mTrackId;
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
		String trackId = getTrackId();
		boolean wasStarted = false;
		if (mKMLTracker != null && !trackId.equals(mTrackId)) {
			wasStarted = mKMLTracker.isStarted();
			mKMLTracker.stop();
			mKMLTracker = null;
		}
		if (mKMLTracker == null) {
			initializeTracker(trackId);
			mTrackId = trackId;
		}
		if (wasStarted) {
			mKMLTracker.start();
		}
		return mKMLTracker;
	}

	private String getTrackId() {

        RouteService routeService = DbManager.getInstance().getRouteService();
        Route route = routeService.GetRoute(Settings.get(mContext).getLong(Settings.TRACK_NAME, -1));
        //TODO get path to kml from route

        return "edk-gps-trasa-12";

//        int trackId = ResourcesUtil.getTrackId(mContext);
//		if (trackId == -1) {
//			throw new UnsupportedOperationException(mContext.getString(R.string.no_info_about_track_message));
//		}
//		return "edk-gps-trasa-" + trackId;
	}

	// private String trackFromMalopolskie() {
	// int trackId = Settings.get(mContext).getInt(Settings.TRACK_ID, 0);
	// switch (trackId) {
	// case 0:
	// return "pomaranczowa_sw_benedykta";
	// case 1:
	// return "zielona_bl_jana_pawla_II";
	// case 2:
	// return "niebieska_sw_karola_de_foucauld";
	// case 3:
	// return "zolta_bl_piotra_jerzego_frassatiego";
	// case 4:
	// return "fioletowa_sw_krzysztofa";
	// case 5:
	// return "brazowa_sw_sylwestra";
	// case 6:
	// return "srebrna_sw_franciszka_z_asyzu";
	// case 7:
	// return "czerwona_sw_rafala_kalinowskiego";
	// case 8:
	// return "biala_sw_ignacego_loyoli";
	// case 9:
	// return "zawoja_kz_niebieska";
	// case 10:
	// return "zawoja_kz_zielona";
	// case 11:
	// return "nowy_sacz_kozieniec";
	// default:
	// throw new
	// UnsupportedOperationException(mContext.getString(R.string.no_info_about_track_message));
	// }
	// }
	//
	// private String trackFromMazowieckie() {
	// int trackId = Settings.get(mContext).getInt(Settings.TRACK_ID, 0);
	// switch (trackId) {
	// case 0:
	// return "wm_gk";
	// case 1:
	// return "wu_niepokalanow";
	// case 2:
	// return "radom_skrzynsko";
	// default:
	// throw new
	// UnsupportedOperationException(mContext.getString(R.string.no_info_about_track_message));
	// }
	// }
	//
	// private String trackFromLubelskie() {
	// return "lubelskie_tl_zamosc";
	// }

	private void initializeTracker(String trackId) {
		try {
			InputStream stream = mContext.getAssets().open("tracks/" + trackId + ".kml");
			Track track = Track.fromStream(stream);
			if (track == null){
				throw new IllegalStateException(mContext.getString(R.string.unrecognized_error_while_reading_track));
			}
			mKMLTracker = new KMLTracker(track, mContext);
		} catch (Exception e) {
			Log.e(TAG, "Couldn't create kml tracker for " + getTrackInfo(), e);
			throw new IllegalStateException(mContext.getString(R.string.unrecognized_error_while_reading_track), e);
		}
		Log.i(TAG, "mKMLTracker created");
	}

	private String getTrackInfo() {

		TrackInfo trackInfo = ResourcesUtil.getTrackInfo(mContext);
		return trackInfo.toString();
	}

}
