package pl.org.edk;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by darekpap on 2016-02-23.
 */
public abstract class SettingsBase {
	// ---------------------------------------
	// Class variables
	// ---------------------------------------
	private Context mContext = null;

	public SettingsBase(Context context) {
		mContext = context;
	}

	public void clear() {
		SharedPreferences preferences = getSharedPreferences();
		SharedPreferences.Editor editor = preferences.edit();
		editor.clear();
		editor.apply();
		mContext = null;
	}

	protected abstract SharedPreferences getSharedPreferences0(Context context);

	protected String getStringKey(int resId){
		return mContext.getString(resId);
	}

	protected String getString(String key) {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getString(key, null);
	}

	protected SharedPreferences getSharedPreferences() {
		return getSharedPreferences0(mContext);
	}

	protected void setString(String key, String value) {
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }
}
