package pl.org.edk;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public final class Settings {

	public static final String CAMERA_ZOOM = "cameraZoom";
	public static final String YEAR_ID = "year";
	private static final String PREF_KEY = "EDK_preferences";
	public static final String IS_BACKGROUND_TRACKING_ON = "trackingTurnedOn";
	public static final String COUNTY_NAME = "countyName";
	public static final String CITY_NAME = "cityName";
	public static final String TRACK_NAME = "trackName";

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
		SharedPreferences preferences = mContext.getSharedPreferences(PREF_KEY, 0);
		Editor editor = preferences.edit();
		editor.clear();
		editor.apply();
		INSTANCE.mContext = null;
		INSTANCE = null;
	}

	public String get(String key) {
		SharedPreferences preferences = mContext.getSharedPreferences(PREF_KEY, 0);
		return preferences.getString(key, null);
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

	public boolean getBoolean(String key, boolean defaultValue) {
		String property = get(key);
		if (property == null) {
			return defaultValue;
		}
		return Boolean.parseBoolean(property);
	}

	public boolean getBoolean(String key) {
		return getBoolean(key, false);
	}

	public void set(String key, String value) {
		SharedPreferences preferences = mContext.getSharedPreferences(PREF_KEY, 0);
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

	public void set(String key, boolean value) {
		set(key, String.valueOf(value));
	}

}
