package pl.org.edk;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import pl.org.edk.fragments.MapFragment;
import pl.org.edk.fragments.ReflectionsFragment;
import pl.org.edk.fragments.RouteInfoFragment;
import pl.org.edk.fragments.SettingsFragment;
import pl.org.edk.services.GPSService;

/**
 * Created by darekpap on 2015-11-30.
 */
public class MainActivity extends AppCompatActivity implements MapFragment.OnStationSelectListener {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    public static final int MAP_INDEX = 0;
    public static final int REFLECTIONS_INDEX = 1;
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

        Intent intent = getIntent();
        processIntent(intent);

    }

    private void processIntent(Intent intent) {
        int stationId = intent.getIntExtra(Extra.STATION_ID, -1);
        if (stationId != -1) {
            onStationSelect(stationId);
        } else if (intent.getBooleanExtra(Extra.GO_TO_MAP, false)) {
            viewPager.setCurrentItem(MAP_INDEX);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }

    @Override
    public void onBackPressed() {
        int currentItem = viewPager.getCurrentItem();
        if (currentItem > 0) {
            viewPager.setCurrentItem(currentItem-1);
            return;
        }
        super.onBackPressed();
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(MAP_INDEX).setIcon(tabIcons[MAP_INDEX]);
        tabLayout.getTabAt(REFLECTIONS_INDEX).setIcon(tabIcons[REFLECTIONS_INDEX]);
        tabLayout.getTabAt(INFO_INDEX).setIcon(tabIcons[INFO_INDEX]);
        tabLayout.getTabAt(SETTINGS_INDEX).setIcon(tabIcons[SETTINGS_INDEX]);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new MapFragment(), "Mapa");
        adapter.addFragment(new ReflectionsFragment(), "Rozważania");
        adapter.addFragment(new RouteInfoFragment(), "Info");
        adapter.addFragment(new SettingsFragment(), "Ustawienia");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onStationSelect(int stationIndex) {
        viewPager.setCurrentItem(REFLECTIONS_INDEX);
        ViewPagerAdapter adapter = (ViewPagerAdapter) viewPager.getAdapter();
        ReflectionsFragment fragment = (ReflectionsFragment) adapter.getRegisteredFragment(REFLECTIONS_INDEX);
        if (fragment == null) {
            Log.d("EDK", "Reflections fragment not registered yet");
            return;
        }
        fragment.selectStation(stationIndex);
    }

    public static void Start(Context context){
        TempSettings.get(context).set(TempSettings.START_TIME, System.currentTimeMillis());
        if (Settings.get(context).getBoolean(Settings.IS_BACKGROUND_TRACKING_ON)){
            context.startService(new Intent(context, GPSService.class));
        }
        context.startActivity(new Intent(context, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

    }


    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();
        private final SparseArray<Fragment> mRegisteredFragments = new SparseArray<>();

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
            return null;
//            return mFragmentTitleList.get(position);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            mRegisteredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            mRegisteredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public Fragment getRegisteredFragment(int position) {
            return mRegisteredFragments.get(position);
        }


    }

}
