package pl.org.edk.EdkMobile.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import pl.org.edk.EdkMobile.Entities.CrossStation;
import pl.org.edk.EdkMobile.Fragments.Main.MapDisplayFragment;
import pl.org.edk.EdkMobile.Fragments.Main.StationFragment;
import pl.org.edk.EdkMobile.Fragments.Main.StatisticsFragment;
import pl.org.edk.EdkMobile.Managers.AppConfiguration;
import pl.org.edk.R;

/**
 * Created by Pawel on 2015-03-12.
 */
public class MainActivity extends FragmentActivity {
    private static final int NUM_PAGES = 2;
    private ViewPager mPager;
    private ScreenSlidePagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Intent intent = getIntent();
        CrossStation crossStation = intent.getParcelableExtra("DataContext");

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.main_pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(), crossStation);
        mPager.setAdapter(mPagerAdapter);

        if(AppConfiguration.getInstance().getGpsManager().checkGpsOn() == false){
            askForGps();
        }
    }

    private void askForGps(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.main_gpsQuestion))
                .setCancelable(false)
                .setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("Nie", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public void RefreshMap(){
        ((MapDisplayFragment)mPagerAdapter.mapFragment).RefreshMap(true);
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment =  mPagerAdapter.getItem(mPager.getCurrentItem());
        if(StationFragment.class.isInstance(currentFragment)){
            // Exit only, if the TextView is not visible
            if(((StationFragment)currentFragment).onBackPressed() == false){
                return;
            }
        }

        // If no other restrictions, open ListActivity
        startActivity(new Intent(this, ListActivity.class));
    }

    // Public methods ====================
    public void onPagerLeft(View v){
        int currentPage = mPager.getCurrentItem();
        mPager.setCurrentItem(currentPage-1);
    }

    public void onPagerRight(View v){
        int currentPage = mPager.getCurrentItem();
        mPager.setCurrentItem(currentPage+1);
    }

    public void changeView(CrossStation newStation){
        Intent newIntent = new Intent(this, MainActivity.class);
        newIntent.putExtra("DataContext", newStation);
        startActivity(newIntent);
    }
    // ===================================

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        private Fragment statsFragment, stationFragment, mapFragment;

        public ScreenSlidePagerAdapter(FragmentManager fm, CrossStation crossStation)
        {
            super(fm);
            initializeFragments(crossStation);
        }

        private void initializeFragments(CrossStation crossStation){
            // Statistics
            statsFragment = new StatisticsFragment();

            // Station
            stationFragment = new StationFragment();
            Bundle stationArgs = new Bundle();
            stationArgs.putParcelable("DataContext", crossStation);
            stationFragment.setArguments(stationArgs);

            // Map
            mapFragment = new MapDisplayFragment();
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return stationFragment;
                case 1:
                    return mapFragment;
                case 2:
                    return statsFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}
