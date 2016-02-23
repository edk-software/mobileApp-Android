package pl.org.edk;

import android.content.Context;
import android.content.SharedPreferences;

public final class TempSettings extends SettingsBase{
	// ---------------------------------------
	// Constants
	// ---------------------------------------
	public static final String CAMERA_ZOOM = "cameraZoom";

	public static final String SELECTED_TERRITORY_ID = "territoryId";
	public static final String SELECTED_AREA_ID = "areaId";
	public static final String SELECTED_ROUTE_ID = "routeId";

	public static String START_TIME = "StartTime";

	private static final String PREF_KEY = "TempPreferences";
    // ---------------------------------------
	// Class variables
	// ---------------------------------------
    private static TempSettings mInstance;

	// ---------------------------------------
	// Singleton
	// ---------------------------------------
	private TempSettings(Context context) {
		super(context);
	}

	public static synchronized TempSettings get(Context context) {
		return get0(context.getApplicationContext());
	}

	private static synchronized TempSettings get0(Context applicationContext) {
		if (mInstance == null) {
			mInstance = new TempSettings(applicationContext);
		}
		return mInstance;
	}

    public static synchronized void clear(Context context) {
        get(context).clear();
        mInstance = null;

    }

    public int getInt(String key, int defaultValue) {
        String result = getString(key);
        if (result == null) {
            return defaultValue;
        }
        return Integer.parseInt(result);
    }

    public long getLong(String key, long defaultValue) {
        String result = getString(key);
        if (result == null) {
            return defaultValue;
        }
        return Long.parseLong(result);
    }

    public float getFloat(String key, float defaultValue) {
        String property = getString(key);
        if (property == null) {
            return defaultValue;
        }
        return Float.parseFloat(property);
    }

    public float getFloat(String key) {
        return getFloat(key, 0f);
    }


    public void set(String key, int value) {
        setString(key, String.valueOf(value));
    }

    public void set(String key, long value) {
        setString(key, String.valueOf(value));
    }

    public void set(String key, float value) {
        setString(key, String.valueOf(value));
    }

	// Other

	public boolean isUserOnTrack() {
		return getLong(TempSettings.START_TIME, -1) != -1;
	}

    @Override
    protected SharedPreferences getSharedPreferences0(Context context) {

        return context.getSharedPreferences(PREF_KEY, 0);
    }
}
