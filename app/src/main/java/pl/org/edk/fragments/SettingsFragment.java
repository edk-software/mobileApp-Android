package pl.org.edk.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

import pl.org.edk.R;
import pl.org.edk.Settings;
import pl.org.edk.managers.WebServiceManager;
import pl.org.edk.services.GPSService;
import pl.org.edk.util.DialogUtil;
import pl.org.edk.webServices.FileDownloader;

/**
 * Created by darekpap on 2016-01-19.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    public SettingsFragment() {
        // Required empty public constructor
    }

    public static final String FRAGMENT_TAG = "settingsMainFragment";

    private boolean mInitialized = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

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

    private void updatePreference(Preference preference, SharedPreferences sharedPreferences) {
        if (preference instanceof CheckBoxPreference) {
            ((CheckBoxPreference) preference).setChecked(sharedPreferences.getBoolean(preference.getKey(), false));
        } else if (preference instanceof ListPreference) {
            String value = sharedPreferences.getString(preference.getKey(), "");
            ((ListPreference) preference).setValue(value);
            preference.setSummary(value);
        }
    }

    private void initView(){
        final CheckBoxPreference reflectionsCheck = (CheckBoxPreference)findPreference(getString(R.string.pref_update_reflections));
        final CheckBoxPreference reflectionsAudioCheck = (CheckBoxPreference)findPreference(getString(R.string.pref_update_reflections_audio));

        reflectionsCheck.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                boolean newValue = (boolean)o;
                if(!newValue){
                    reflectionsAudioCheck.setChecked(false);
                }
                return true;
            }
        });
        reflectionsAudioCheck.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                boolean newValue = (boolean)o;
                if(newValue){
                    reflectionsCheck.setChecked(true);
                }
                return true;
            }
        });

        // Update button
        Preference updateButton = findPreference(getString(R.string.pref_update_button));
        updateButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                boolean regions = preferences.getBoolean(getString(R.string.pref_update_areas), false);
                boolean routes = preferences.getBoolean(getString(R.string.pref_update_routes), false);
                boolean ref = preferences.getBoolean(getString(R.string.pref_update_reflections), false);
                boolean refAudio = preferences.getBoolean(getString(R.string.pref_update_reflections_audio), false);
                sync(regions, routes, ref, refAudio);
                return true;
            }
        });
    }

    private void sync(boolean regions, boolean routes, boolean reflections, boolean audio){
        DialogUtil.showBusyDialog("Trwa synchronizacja danych...", getActivity());
        WebServiceManager.OnOperationFinishedEventListener listener = new WebServiceManager.OnOperationFinishedEventListener() {
            @Override
            public void onOperationFinished(Object result) {
                DialogUtil.closeBusyDialog();
                DialogUtil.showWarningDialog("Zakończono aktualizację danych.", getActivity(), true);
            }
        };
        WebServiceManager.getInstance(getActivity()).updateDataAsync(regions, routes, reflections, audio, listener);
    }
}
