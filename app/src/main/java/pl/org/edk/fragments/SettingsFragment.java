package pl.org.edk.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

import java.util.ArrayList;
import java.util.Calendar;

import pl.org.edk.R;
import pl.org.edk.Settings;
import pl.org.edk.TempSettings;
import pl.org.edk.database.DbManager;
import pl.org.edk.database.entities.ReflectionList;
import pl.org.edk.managers.WebServiceManager;
import pl.org.edk.services.GPSService;
import pl.org.edk.util.DialogUtil;

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

        Settings.CAN_USE_GPS = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        PreferenceCategory category = (PreferenceCategory) findPreference(getString(R.string.pref_cat_location));
        category.setEnabled(Settings.CAN_USE_GPS);

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
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }
        if (key.equals(getString(Settings.IS_BACKGROUND_TRACKING_ON)) && TempSettings.get(activity).isUserOnTrack()) {
            boolean trackingOn = sharedPreferences.getBoolean(key, false);
            Intent serviceIntent = new Intent(activity, GPSService.class);

            if (trackingOn) {
                activity.startService(serviceIntent);

            } else {
                activity.stopService(serviceIntent);
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
            if (preference.getKey().equals(getResources().getString(R.string.pref_reflectionsLanguage))) {
                preference.setSummary(getLangDisplayName(value));
            } else {
                preference.setSummary(value);
            }
        }
    }

    private void initView() {
        initReflectionSection();
        initUpdateSection();
    }

    private void initReflectionSection() {
        ListPreference reflectionsYear = (ListPreference) findPreference(getString(R.string.pref_reflectionsEdition));
        reflectionsYear.setDefaultValue(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));

        String language = Settings.get(getContext()).get(Settings.REFLECTIONS_LANGUAGE);

        ListPreference reflectionsLanguage = (ListPreference) findPreference(getString(R.string.pref_reflectionsLanguage));

        String langDisplayName = getLangDisplayName(language);
        reflectionsLanguage.setSummary(langDisplayName);
        String[] langs = getResources().getStringArray(R.array.reflection_languages);
        CharSequence[] langNames = new CharSequence[langs.length];
        for (int i = 0; i < langs.length; i++) {
            langNames[i] = getLangDisplayName(langs[i]);
        }
        reflectionsLanguage.setEntries(langNames);

        // Get years available on the server
        ArrayList<Integer> editions = WebServiceManager.getInstance(getContext()).getReflectionEditions();

        // If failed, check the local ones
        if (editions == null) {
            ArrayList<ReflectionList> dbLists = DbManager.getInstance(getActivity()).getReflectionService().getReflectionLists(language, false);
            if (dbLists == null)
                return;

            editions = new ArrayList<>();
            for (ReflectionList dbList : dbLists) {
                editions.add(dbList.getEdition());
            }
        }

        // Add the fetched items to the list
        CharSequence[] editionItems = new CharSequence[editions.size()];
        for (int i = 0; i < editions.size(); i++) {
            editionItems[i] = String.valueOf(editions.get(i));
        }
        reflectionsYear.setEntries(editionItems);
        reflectionsYear.setEntryValues(editionItems);
    }

    @NonNull
    private String getLangDisplayName(String language) {
        switch (language) {
            case "pl":
                return getResources().getString(R.string.pl_language);
            case "en":
                return getResources().getString(R.string.en_language);
            case "es":
                return getResources().getString(R.string.es_language);
                default:
                    throw new IllegalArgumentException("Unknown language used");
        }
    }

    private void initUpdateSection() {
        // Update reflections checkboxes
        final CheckBoxPreference reflectionsCheck = (CheckBoxPreference) findPreference(getString(R.string.pref_update_reflections));
        final CheckBoxPreference reflectionsAudioCheck = (CheckBoxPreference) findPreference(getString(R.string.pref_update_reflections_audio));

        reflectionsCheck.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                boolean newValue = (boolean) o;
                if (!newValue) {
                    reflectionsAudioCheck.setChecked(false);
                }
                return true;
            }
        });
        reflectionsAudioCheck.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                boolean newValue = (boolean) o;
                if (newValue) {
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

        // Clean-up button
        Preference cleanupButton = findPreference(getString(R.string.pref_cleanup_button));
        cleanupButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Fragment fragment = getActivity().getSupportFragmentManager().findFragmentByTag(CleanupFragment.FRAGMENT_TAG);
                if (fragment == null) {
                    fragment = new CleanupFragment();
                }
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(android.R.id.content, fragment, CleanupFragment.FRAGMENT_TAG).addToBackStack(CleanupFragment.FRAGMENT_TAG).commit();
                return true;
            }
        });
    }

    private void sync(boolean regions, boolean routes, boolean reflections, boolean audio) {
        DialogUtil.showBusyDialog(R.string.update_in_progress_message, getActivity());
        WebServiceManager.OnOperationFinishedEventListener listener = new WebServiceManager.OnOperationFinishedEventListener() {
            @Override
            public void onOperationFinished(Object result) {
                DialogUtil.closeBusyDialog();
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    boolean finishActivity = !TempSettings.get(activity).isUserOnTrack();
                    DialogUtil.showWarningDialog(R.string.update_download_finished_message, activity, finishActivity);
                }
            }
        };
        WebServiceManager.getInstance(getActivity()).updateDataAsync(regions, routes, reflections, audio, listener);
    }
}
