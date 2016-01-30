package pl.org.edk;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by darekpap on 2015-11-30.
 */
public class MainActivity extends AppCompatActivity implements MapFragment.OnStationSelectListener {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    private MapFragment.OnStationSelectListener mListener;

    public static final int MAP_INDEX = 0;
    public static final int CONSIDERATIONS_INDEX = 1;
    public static final int INFO_INDEX = 2;
    public static final int SETTINGS_INDEX = 3;

    private int[] tabIcons = {
            R.drawable.tracks_big,
            R.drawable.considerations_big,
            R.drawable.info_big,
            R.drawable.settings_big
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//
//        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//        getSupportActionBar().setTitle(null);

        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();


    }

    private void setupTabIcons() {
        tabLayout.getTabAt(MAP_INDEX).setIcon(tabIcons[MAP_INDEX]);
        tabLayout.getTabAt(CONSIDERATIONS_INDEX).setIcon(tabIcons[CONSIDERATIONS_INDEX]);
        tabLayout.getTabAt(INFO_INDEX).setIcon(tabIcons[INFO_INDEX]);
        tabLayout.getTabAt(SETTINGS_INDEX).setIcon(tabIcons[SETTINGS_INDEX]);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new MapFragment(), "Mapa");
        ReflectionsFragment reflectionsFragment = new ReflectionsFragment();
        mListener = reflectionsFragment;
        adapter.addFragment(reflectionsFragment, "Rozważania");
        adapter.addFragment(new RouteInfoFragment(), "Info");
        adapter.addFragment(new SettingsFragment(), "Ustawienia");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onStationSelect(int stationIndex) {
        viewPager.setCurrentItem(CONSIDERATIONS_INDEX);
        mListener.onStationSelect(stationIndex);
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
//         return null;
            return mFragmentTitleList.get(position);
        }




    }

//        extends FragmentActivity {
//    private static final int NUM_PAGES = 2;
//    private ViewPager mPager;
//    private ScreenSlidePagerAdapter mPagerAdapter;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//
//        // Instantiate a ViewPager and a PagerAdapter.
//        mPager = (ViewPager) findViewById(R.id.main_pager);
//        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
//        mPager.setAdapter(mPagerAdapter);
//    }
//
//    private void askForGps(){
//        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage(getString(R.string.main_gpsQuestion))
//                .setCancelable(false)
//                .setPositiveButton("Tak", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
//                    }
//                })
//                .setNegativeButton("Nie", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        dialog.cancel();
//                    }
//                });
//        final AlertDialog alert = builder.create();
//        alert.show();
//    }
//
//    public void RefreshMap(){
//        ((MapFragment)mPagerAdapter.mapFragment).RefreshMap(true);
//    }
//
//    @Override
//    public void onBackPressed() {
////        Fragment currentFragment =  mPagerAdapter.getItem(mPager.getCurrentItem());
////        if(StationFragment.class.isInstance(currentFragment)){
////            // Exit only, if the TextView is not visible
////            if(((StationFragment)currentFragment).onBackPressed() == false){
////                return;
////            }
////        }
////
////        // If no other restrictions, open ListActivity
////        startActivity(new Intent(this, ListActivity.class));
//    }
//
//    // Public methods ====================
//    public void onPagerLeft(View v){
//        int currentPage = mPager.getCurrentItem();
//        mPager.setCurrentItem(currentPage - 1);
//    }
//
//    public void onPagerRight(View v){
//        int currentPage = mPager.getCurrentItem();
//        mPager.setCurrentItem(currentPage+1);
//    }
//
//    public void changeView(){
//        Intent newIntent = new Intent(this, MainActivity.class);
////        newIntent.putExtra("DataContext", newStation);
//        startActivity(newIntent);
//    }
//    // ===================================
//
//    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
//        private Fragment reflectionsFragment, mapFragment;
//
//        public ScreenSlidePagerAdapter(FragmentManager fm)
//        {
//            super(fm);
//            initializeFragments();
//        }
//
//        private void initializeFragments(){
//            // Station
//            reflectionsFragment = new ReflectionsFragment();
//            // Map
//            mapFragment = new MapFragment();
//        }
//
//        @Override
//        public Fragment getItem(int position) {
//            switch (position){
//                case 0:
//                    return mapFragment;
//                case 1:
//                    return reflectionsFragment;
//                default:
//                    return null;
//            }
//        }
//
//        @Override
//        public int getCount() {
//            return NUM_PAGES;
//        }
//    }
}
