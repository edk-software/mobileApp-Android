package pl.org.edk.menu;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import pl.org.edk.Settings;
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

    @Override
    protected void onStart() {
        super.onStart();
        Settings.CAN_USE_STORAGE = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        Settings.CAN_USE_GPS = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        //Toast.makeText(getApplicationContext(),"REF ACT can use storage: "+Settings.CAN_USE_STORAGE,Toast.LENGTH_SHORT).show();
    }
}
