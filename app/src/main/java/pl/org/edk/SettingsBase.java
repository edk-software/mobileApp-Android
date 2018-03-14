package pl.org.edk;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.Locale;

/**
 * Created by darekpap on 2016-02-23.
 */
public abstract class SettingsBase {
	// ---------------------------------------
	// Class variables
	// ---------------------------------------
	private Context mContext = null;

	protected SettingsBase(Context context) {
		mContext = context;
	}

	protected void clear() {
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

    protected String getCurrentSupportedLanguage(){
        Locale locale = getCurrentLocale();
        String lang = locale.getLanguage();
        if(!lang.equals("es") && !lang.equals("en")){
            //for now we only support english and spanish as extra languages, fallback to polish
            return "pl";
        }
        return lang;

    }

    private Locale getCurrentLocale(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return mContext.getResources().getConfiguration().getLocales().get(0);
        } else{
            //noinspection deprecation
            return mContext.getResources().getConfiguration().locale;
        }
    }
}
