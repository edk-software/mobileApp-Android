package pl.org.edk;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v7.preference.PreferenceManager;

public class Settings extends SettingsBase {
	// ---------------------------------------
	// Constants
	// ---------------------------------------
	public static final int IS_BACKGROUND_TRACKING_ON = R.string.pref_backgroundTrackingOn;
    public static final int FOLLOW_LOCATION_ON_MAP = R.string.pref_followLocationOnMap;
	public static final int YEAR_ID = R.string.pref_reflectionsYear;
	public static final int AUDIO_DOWNLOAD_DIALOG_SHOWN = R.string.pref_audioDownloadDialogShown;

    public static final int APP_LANGUAGE = R.string.pref_language;
	public static final int APP_DIRECTORY_KML = R.string.pref_kml_directory;
	public static final int APP_DIRECTORY_AUDIO = R.string.pref_audio_directory;
    public static final int DO_NOT_SHOW_GPS_DIALOG = R.string.do_not_show_gps_dialog;

    private static Settings mInstance = null;
    // ---------------------------------------
	// Singleton
	// ---------------------------------------
	protected Settings(Context context) {
        super(context);
    }

	public static synchronized Settings get(Context context) {
		return get0(context.getApplicationContext());
	}

	private static synchronized Settings get0(Context applicationContext) {
		if (mInstance == null) {
			mInstance = new Settings(applicationContext);
		}
		return mInstance;
	}

	// ---------------------------------------
	// Public methods
	// ---------------------------------------

    // Getters

    public String get(int resId){
        return getString(getStringKey(resId));
    }

    public boolean getBoolean(int resId, boolean defaultValue) {
        SharedPreferences preferences = getSharedPreferences();
        return preferences.getBoolean(getStringKey(resId), defaultValue);
	}

	public boolean getBoolean(int resId) {
		return getBoolean(resId, false);
	}

	// Setters

    public <T> void set(int resId, T  value){
        setString(getStringKey(resId), String.valueOf(value));
    }

    public void set(int resId, boolean value) {
        SharedPreferences preferences = getSharedPreferences();
        Editor editor = preferences.edit();
        editor.putBoolean(getStringKey(resId), value);
        editor.apply();
    }


    @Override
    protected SharedPreferences getSharedPreferences0(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
