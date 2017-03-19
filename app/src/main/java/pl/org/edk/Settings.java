package pl.org.edk;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v7.preference.PreferenceManager;

import java.util.Calendar;

public class Settings extends SettingsBase {
    // ---------------------------------------
    // Constants
    // ---------------------------------------
    public static final int APP_LANGUAGE = R.string.pref_language;
    public static final int CURRENT_EDITION = R.string.pref_currentEdition;
    public static final int REFLECTIONS_EDITION = R.string.pref_reflectionsEdition;
    public static final int APP_DIRECTORY_KML = R.string.pref_kml_directory;
    public static final int APP_DIRECTORY_AUDIO = R.string.pref_audio_directory;

    public static final int IS_BACKGROUND_TRACKING_ON = R.string.pref_backgroundTrackingOn;
    public static final int FOLLOW_LOCATION_ON_MAP = R.string.pref_followLocationOnMap;
    public static final int SHOW_ARCHIVE_ROUTES = R.string.pref_show_archive_routes;

    public static final int DO_NOT_SHOW_GPS_DIALOG = R.string.pref_do_not_show_gps_dialog;
    public static final int AUDIO_DOWNLOAD_DIALOG_SHOWN = R.string.pref_audioDownloadDialogShown;
    public static final int ARCHIVE_ROUTES_DIALOG_SHOWN = R.string.pref_archiveRoutesDialogShown;

    public static final int ROTATE_MAP_TO_WALK_DIRECTION = R.string.pref_rotate_map_to_walk_dir; // should map be oriented to travel direction or to north

    // Variables

    public static boolean CAN_USE_STORAGE = false; // if app has permission WRITE_EXTERNAL_STORAGE
    public static boolean CAN_USE_GPS = false; // if app has permission ACCESS_FINE_LOCATION
    public static boolean DO_NOT_SHOW_AGAIN_GPS_PERMISSIONS_DIALOG = false; // should the 'no GPS permissions' dialog be still shown

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

    public void init() {
        // TEMP: Constant values
        set(Settings.APP_LANGUAGE, "pl");
        set(Settings.CURRENT_EDITION, Calendar.getInstance().get(Calendar.YEAR));

        // Add default values
        if (getInt(Settings.REFLECTIONS_EDITION) == 0)
            set(Settings.REFLECTIONS_EDITION, Calendar.getInstance().get(Calendar.YEAR));
        if(getBoolean(Settings.FOLLOW_LOCATION_ON_MAP, true)){
            set(Settings.FOLLOW_LOCATION_ON_MAP, true);
        }
    }

    // Getters

    public String get(int resId) {
        return getString(getStringKey(resId));
    }

    public boolean getBoolean(int resId, boolean defaultValue) {
        SharedPreferences preferences = getSharedPreferences();
        return preferences.getBoolean(getStringKey(resId), defaultValue);
    }

    public boolean getBoolean(int resId) {
        return getBoolean(resId, false);
    }

    public int getInt(int resId, int defaultValue) {
        try{
            String stringValue = get(resId);
            return Integer.parseInt(stringValue);
        }catch (Exception ex){
            return defaultValue;
        }
    }

    public int getInt(int resId) {
        return getInt(resId, 0);
    }

    // Setters

    public <T> void set(int resId, T value) {
        setString(getStringKey(resId), String.valueOf(value));
    }

    public void set(int resId, boolean value) {
        SharedPreferences preferences = getSharedPreferences();
        Editor editor = preferences.edit();
        editor.putBoolean(getStringKey(resId), value);
        editor.apply();
    }

    public void set(int resId, int value) {
        set(resId, String.valueOf(value));
    }

    @Override
    protected SharedPreferences getSharedPreferences0(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
