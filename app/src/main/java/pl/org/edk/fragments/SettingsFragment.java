package pl.org.edk.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

import pl.org.edk.R;
import pl.org.edk.Settings;
import pl.org.edk.services.GPSService;

/**
 * Created by darekpap on 2016-01-19.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    public SettingsFragment() {
        // Required empty public constructor
    }

    public static final String FRAGMENT_TAG = "settingsFragment";

    private boolean mInitialized = false;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);
        mInitialized = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        if (isVisible() && isMenuVisible()) {
            refreshPreferenceView();
        }
    }

    @Override
    public void setMenuVisibility(boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
            refreshPreferenceView();
        }
    }

    private void refreshPreferenceView() {
        if (!mInitialized) {
            return;
        }
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        for (int i = 0; i < preferenceScreen.getPreferenceCount(); i++) {
            Preference preference = preferenceScreen.getPreference(i);
            updatePreference(preference, preference.getSharedPreferences());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(Settings.IS_BACKGROUND_TRACKING_ON))&& Settings.get(getActivity()).isUserOnTrack()) {
            boolean trackingOn = sharedPreferences.getBoolean(key, false);
            Intent serviceIntent = new Intent(getActivity(), GPSService.class);

            if (trackingOn) {
                getActivity().startService(serviceIntent);

            } else {
                getActivity().stopService(serviceIntent);
            }
        }
        if (isVisible() && isMenuVisible()) {
            Preference preference = findPreference(key);
            updatePreference(preference, sharedPreferences);
        }
    }

    private void updatePreference(Preference preference, SharedPreferences sharedPreferences) {
        if (preference instanceof CheckBoxPreference) {
            ((CheckBoxPreference) preference).setChecked(sharedPreferences.getBoolean(preference.getKey(), false));
        } else if (preference instanceof ListPreference) {
            String value = sharedPreferences.getString(preference.getKey(), "");
            ((ListPreference) preference).setValue(value);
            preference.setSummary(value);
        }
    }
}
