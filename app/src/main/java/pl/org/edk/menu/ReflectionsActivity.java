package pl.org.edk.menu;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import pl.org.edk.fragments.ReflectionsFragment;

/**
 * Created by darekpap on 2016-02-10.
 */
public class ReflectionsActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            ReflectionsFragment fragment = new ReflectionsFragment();
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, fragment).commit();
        }
    }

}
