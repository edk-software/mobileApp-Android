package pl.org.edk;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v7.preference.PreferenceManager;

public final class Settings {

	public static final String CAMERA_ZOOM = "cameraZoom";

	public static final String SELECTED_TERRITORY_ID = "territoryId";
	public static final String SELECTED_AREA_ID = "areaId";
	public static final String SELECTED_ROUTE_ID = "routeId";

	public static final int IS_BACKGROUND_TRACKING_ON = R.string.pref_backgroundTrackingOn;
    public static final int FOLLOW_LOCATION_ON_MAP = R.string.pref_followLocationOnMap;
	public static final int YEAR_ID = R.string.pref_reflectionsYear;
	public static final int AUDIO_DOWNLOAD_DIALOG_SHOWN = R.string.pref_audioDownloadDialogShown;

	public static final String APP_DIRECTORY_KML = "KmlDirectory";
	public static final String APP_DIRECTORY_AUDIO = "AudioDirectory";

	public static String START_TIME = "StartTime";

	private static Settings INSTANCE = null;
    private Context mContext = null;

	private Settings(Context context) {
		mContext = context;
	}

	public static synchronized Settings get(Context context) {
		return get0(context.getApplicationContext());
	}

	private static synchronized Settings get0(Context applicationContext) {
		if (INSTANCE == null) {
			INSTANCE = new Settings(applicationContext);
		}
		return INSTANCE;
	}

	public static synchronized void clear(Context context) {
		Settings instance = get(context);
		instance.clear();
	}

	public void clear() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		Editor editor = preferences.edit();
		editor.clear();
		editor.apply();
		INSTANCE.mContext = null;
		INSTANCE = null;
	}

    public String get(int resId){
        return get(mContext.getString(resId));
    }

	public String get(String key) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		return preferences.getString(key, null);
	}

    public int getInt(int resId, int defaultValue) {
        return getInt(mContext.getString(resId), defaultValue);
    }

	public int getInt(String key, int defaultValue) {
		String result = get(key);
		if (result == null) {
			return defaultValue;
		}
		return Integer.parseInt(result);
	}

	public long getLong(String key, long defaultValue) {
		String result = get(key);
		if (result == null) {
			return defaultValue;
		}
		return Long.parseLong(result);
	}

	public float getFloat(String key, float defaultValue) {
		String property = get(key);
		if (property == null) {
			return defaultValue;
		}
		return Float.parseFloat(property);
	}

	public float getFloat(String key) {
		return getFloat(key, 0f);
	}

	public boolean getBoolean(int resId, boolean defaultValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return preferences.getBoolean(mContext.getString(resId), defaultValue);
	}

	public boolean getBoolean(int resId) {
		return getBoolean(resId, false);
	}

    public <T> void set(int resId, T  value){
        set(mContext.getString(resId), String.valueOf(value));
    }


	public void set(String key, String value) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		Editor editor = preferences.edit();
		editor.putString(key, value);
		editor.apply();
	}

	public void set(String key, int value) {
		set(key, String.valueOf(value));
	}

	public void set(String key, long value) {
		set(key, String.valueOf(value));
	}

	public void set(String key, float value) {
		set(key, String.valueOf(value));
	}

	public void set(int resId, boolean value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        Editor editor = preferences.edit();
        editor.putBoolean(mContext.getString(resId), value);
        editor.apply();
	}


	public boolean isUserOnTrack() {
		return getLong(Settings.START_TIME, -1) != -1;
	}
}
