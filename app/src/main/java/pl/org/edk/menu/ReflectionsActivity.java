package pl.org.edk.menu;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import pl.org.edk.fragments.ReflectionsFragment;

/**
 * Created by darekpap on 2016-02-10.
 */
public class ReflectionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(ReflectionsFragment.FRAGMENT_TAG);
            if (fragment == null) {
                fragment = new ReflectionsFragment();
            }


            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(android.R.id.content, fragment, ReflectionsFragment.FRAGMENT_TAG).commit();
        }
    }

}
