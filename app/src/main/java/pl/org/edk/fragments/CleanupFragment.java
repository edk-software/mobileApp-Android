package pl.org.edk.fragments;

import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import java.io.File;
import java.util.ArrayList;

import pl.org.edk.R;
import pl.org.edk.database.DbManager;
import pl.org.edk.database.entities.Reflection;
import pl.org.edk.database.entities.ReflectionList;
import pl.org.edk.util.DialogUtil;

/**
 * Created by pwawrzynek on 2017-03-19.
 */

public class CleanupFragment extends PreferenceFragmentCompat {

    public static final String FRAGMENT_TAG = "cleanupMainFragment";

    private ArrayList<ReflectionList> mLists;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.cleanup);
        initView();
    }

    private void initView() {

        Preference cleanupButton = findPreference(getString(R.string.pref_cleanup_button));
        cleanupButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (mLists == null || mLists.size() == 0)
                    return true;

                ArrayList<ReflectionList> elementsToClean = new ArrayList<>();
                for (ReflectionList list : mLists) {
                    boolean value = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(getElementKey(list), false);
                    if (value)
                        elementsToClean.add(list);
                }
                cleanup(elementsToClean);
                DialogUtil.showDialog("Zakończono", "Dane zostały usunięte", getActivity(), true, new DialogUtil.OnCloseEventListener() {
                    @Override
                    public void onClose() {
                        getActivity().onBackPressed();
                    }
                });
                return true;
            }
        });

        boolean hasData = false;
        hasData |= initReflections();

        if(!hasData) {
            DialogUtil.showDialog("Zakończono", "Brak plików do usunięcia", getActivity(), true, new DialogUtil.OnCloseEventListener() {
                @Override
                public void onClose() {
                    getActivity().onBackPressed();
                }
            });
        }
    }

    private boolean initReflections() {
        ArrayList<ReflectionList> lists = DbManager.getInstance(getActivity()).getReflectionService().getReflectionLists(true);
        if (lists == null || lists.size() == 0)
            return false;

        final PreferenceCategory reflectionsCategory = (PreferenceCategory) findPreference(getString(R.string.pref_cleanup_reflections));
        mLists = new ArrayList<>();
        for (ReflectionList list : lists) {
            if (list.getFilesCount() == 0)
                continue;
            mLists.add(list);

            CheckBoxPreference item = new CheckBoxPreference(getContext());
            item.setKey(getElementKey(list));
            item.setDefaultValue(true);
            CharSequence title = list.getEdition() + " (" + list.getLanguage().toUpperCase() + ")";
            item.setTitle(title);
            CharSequence desc = list.getFilesCount() + " plików";
            item.setSummary(desc);

            try {
                reflectionsCategory.addPreference(item);
            } catch (Exception ex) {
            }
        }

        return mLists.size() > 0;
    }

    private static String getElementKey(ReflectionList list) {
        return "pref_cleanup_element_" + list.getEdition() + "_" + list.getLanguage();
    }

    private boolean cleanup(ArrayList<ReflectionList> lists) {
        boolean success = true;
        for (ReflectionList list : lists) {
            success &= cleanup(list);
        }
        return success;
    }

    private boolean cleanup(ReflectionList list) {
        if (!list.hasAnyAudio())
            return true;

        boolean success = true;
        for (Reflection reflection : list.getReflections()) {
            if (!reflection.hasAudio())
                continue;

            File file = new File(reflection.getAudioLocalPath());
            success &= file.delete();

            reflection.setAudioLocalPath(null);
            DbManager.getInstance(getActivity()).getReflectionService().updateReflection(reflection);
        }
        return success;
    }
}
