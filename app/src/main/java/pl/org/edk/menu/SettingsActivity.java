package pl.org.edk.menu;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import pl.org.edk.fragments.SettingsFragment;

/**
 * Created by darekpap on 2016-02-10.
 */
public class SettingsActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(SettingsFragment.FRAGMENT_TAG);
            if (fragment == null) {
                fragment = new SettingsFragment();
            }


            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(android.R.id.content, fragment, SettingsFragment.FRAGMENT_TAG).commit();
        }
    }
}
