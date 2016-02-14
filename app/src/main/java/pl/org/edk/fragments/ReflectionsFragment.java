package pl.org.edk.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.SeekBar;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import pl.org.edk.R;
import pl.org.edk.database.DbManager;
import pl.org.edk.database.entities.Reflection;
import pl.org.edk.database.entities.ReflectionList;
import pl.org.edk.database.services.ReflectionService;
import pl.org.edk.services.ReflectionsAudioService;
import pl.org.edk.services.ReflectionsAudioService.OnPlayerStopListener;
import pl.org.edk.util.ExpandableListAdapter;

/**
 * Created by darekpap on 2015-11-30.
 */
public class ReflectionsFragment extends Fragment implements OnPlayerStopListener {
    private ExpandableListView expListView;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listDataChild;
    private ExpandableListAdapter listAdapter;
    private int mCurrentStation = -1;

    private View mPlayerView;
    private ImageButton mPlayButton;
    private ImageButton mNextButton;
    private ImageButton mPrevButton;

    private int timeElapsed = 0;
    private int finalTime = 100;
    private Handler mDurationHandler = new Handler();
    private SeekBar mSeekBar;

    private ReflectionsAudioService mAudioService;
    private boolean mServiceBound = false;
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ReflectionsAudioService.MusicBinder binder = (ReflectionsAudioService.MusicBinder) service;
            mAudioService = binder.getService();
            mServiceBound = true;
            mAudioService.add(ReflectionsFragment.this);
            if (mAudioService.isPlaying()) {
                openReflections(mAudioService.getReflection().getStationIndex());
                loadPlayer();
            } else if (mCurrentStation != -1) {
                loadPlayer();
            } else {
                if (mCurrentStation == -1) {
                    hidePlayer();
                } else {
                    showPlayer();
                    resetSeekBar();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }
    };

    public void selectStation(int stationIndex) {
        if (expListView == null) {
            Log.d("EDK", "List not ready");
            return;
        }
        boolean playerResetNeeded = stationIndex != mCurrentStation;
        openReflections(stationIndex);
        if (playerResetNeeded) {
            preparePlayer(stationIndex);
            return;
        }
        if (mServiceBound){
            loadPlayer();
        } else{
            showPlayer();
        }
    }

    public ReflectionsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reflections, container, false);

        expListView = (ExpandableListView) view.findViewById(R.id.expandableList);

        prepareListData();

        listAdapter = new ExpandableListAdapter(getActivity(), listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);
        expListView.setGroupIndicator(null);

        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(final ExpandableListView parent, final View v, final int groupPosition, long id) {

                if (parent.isGroupExpanded(groupPosition)) {
                    parent.collapseGroup(groupPosition);
                    hidePlayer();
                    mCurrentStation = -1;
                    if (mAudioService.isPlaying()) {
                        mAudioService.continueInForeground(getActivity().getClass());
                    }
                } else {
                    openReflections(groupPosition);
                    if (mAudioService.isPlaying() && mAudioService.getReflection().getStationIndex() == groupPosition) {
                        loadPlayer();
                    } else {
                        preparePlayer(groupPosition);
                    }
                }
                return true;
            }
        });

        initializePlayerView(view);
        if (mCurrentStation == -1) {
            hidePlayer();
        }
        return view;
    }

    private void hidePlayer() {
//        mPlayerView.setTranslationY(mPlayerView.getHeight());
//        mPlayerView.animate().translationY(mPlayerView.getHeight()).start();
        mPlayerView.setVisibility(View.GONE);
    }

    private void showPlayer() {
        mPlayerView.setVisibility(View.VISIBLE);
    }


    //start and bind the service when the activity starts
    @Override
    public void onStart() {
        super.onStart();
        if (!mServiceBound) {
            Intent playIntent = new Intent(getActivity(), ReflectionsAudioService.class);
            getActivity().bindService(playIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(playIntent);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mDurationHandler.removeCallbacks(updateSeekBarTime);
        if (mServiceBound) {
            boolean playing = mAudioService.isPlaying();
            if (playing) {
                mAudioService.continueInForeground(getActivity().getClass());
            }
            mAudioService.remove(this);
            getActivity().unbindService(mServiceConnection);
            mServiceBound = false;
            if (!playing) {
                getActivity().stopService(new Intent(getActivity(), ReflectionsAudioService.class));
            }
        }
    }

    private void loadPlayer() {
        showPlayer();

        finalTime = mAudioService.getDuration();
        mSeekBar.setMax(finalTime);
        timeElapsed = mAudioService.getCurrentPosition();
        mSeekBar.setProgress(timeElapsed);

        if (mAudioService.isPlaying()) {
            mPlayButton.setImageResource(R.drawable.pause);
            updateSeekBarTime.run();
        } else {
            mPlayButton.setImageResource(R.drawable.play);
        }
    }

    private void preparePlayer(int stationIndex) {
        showPlayer();

        mPlayButton.setImageResource(R.drawable.play);
        mPrevButton.setEnabled(stationIndex != 0);
        mNextButton.setEnabled(stationIndex != 15);

        resetSeekBar();

        if (mServiceBound) {
            if (mAudioService.isPlaying()) {
                mAudioService.stop();
            }
            Reflection reflection = getReflection(stationIndex);
            mAudioService.setReflection(reflection);
        }
    }

    @NonNull
    private Reflection getReflection(int stationIndex) {
        Reflection reflection = new Reflection();
        reflection.setStationIndex(stationIndex);
        reflection.setDisplayName(listDataHeader.get(stationIndex));
        return reflection;
    }


    @Override
    public void onPlayerStop() {
        preparePlayer(mCurrentStation);
    }

    private void resetSeekBar() {
        mSeekBar.setProgress(0);
        mSeekBar.setMax(100);
        finalTime = 100;
        timeElapsed = 0;
    }

    public void initializePlayerView(View view) {
        mSeekBar = (SeekBar) view.findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mAudioService.seekTo(progress);
                }
            }
        });

        mPlayerView = view.findViewById(R.id.player);
        mPlayButton = (ImageButton) view.findViewById(R.id.playButton);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAudioService.isPlaying()) {
                    mAudioService.pause();
                    mPlayButton.setImageResource(R.drawable.play);
                } else {
                    if (mAudioService.getReflection() == null) {
                        Log.i("EDK", "Audio service reflection was null when clicked play");
                        mAudioService.setReflection(getReflection(mCurrentStation));
                    }
                    mAudioService.play();
                    mPlayButton.setImageResource(R.drawable.pause);
                    updateSeekBarTime.run();
                }
            }
        });

        mNextButton = (ImageButton) view.findViewById(R.id.nextButton);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectStation(mCurrentStation + 1);
            }
        });

        mPrevButton = (ImageButton) view.findViewById(R.id.prevButton);
        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectStation(mCurrentStation - 1);
            }
        });

    }

    private Runnable updateSeekBarTime = new Runnable() {
        public void run() {
            if (!mAudioService.isPrepared()) {
                mDurationHandler.postDelayed(this, 1000);
                return;
            }
//            if (!mAudioService.isPlaying()) {
//                preparePlayer(mCurrentStation);
//                return;
//            }

            if (finalTime == 100) {
                finalTime = mAudioService.getDuration();
                mSeekBar.setMax(finalTime);
            }

            timeElapsed = mAudioService.getCurrentPosition();
            mSeekBar.setProgress(timeElapsed);
            if (mAudioService.isPlaying()) {
                mDurationHandler.postDelayed(this, 1000);
            }
        }
    };

    private void openReflections(final int stationId) {
        if (mCurrentStation != -1 && stationId != mCurrentStation) {
            expListView.collapseGroup(mCurrentStation);
        }
        expListView.expandGroup(stationId);
        expListView.post(new Runnable() {
            @Override
            public void run() {
                smoothScrollToPositionFromTop(expListView, stationId);
            }
        });
        mCurrentStation = stationId;
    }


    //workaround from https://stackoverflow.com/questions/14479078/smoothscrolltopositionfromtop-is-not-always-working-like-it-should/20997828#20997828
    public static void smoothScrollToPositionFromTop(final AbsListView view, final int position) {
        View child = getChildAtPosition(view, position);
        // There's no need to scroll if child is already at top or view is already scrolled to its end
        if ((child != null) && ((child.getTop() == 0) || ((child.getTop() > 0) && !view.canScrollVertically(1)))) {
            return;
        }

        view.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(final AbsListView view, final int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    view.setOnScrollListener(null);

                    // Fix for scrolling bug
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            view.setSelection(position);
                        }
                    });
                }
            }

            @Override
            public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount,
                                 final int totalItemCount) {
            }
        });

        // Perform scrolling to position
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                view.smoothScrollToPositionFromTop(position, 0, 200);
            }
        });
    }

    public static View getChildAtPosition(final AdapterView view, final int position) {
        final int index = position - view.getFirstVisiblePosition();
        if ((index >= 0) && (index < view.getChildCount())) {
            return view.getChildAt(index);
        } else {
            return null;
        }
    }

    private void prepareListData() {

        ReflectionService reflectionService = DbManager.getInstance(getActivity()).getReflectionService();
        ReflectionList reflectionList = reflectionService.GetReflectionList("pl", true);
        if (reflectionList == null || reflectionList.getReflections().isEmpty()) {


            listDataHeader = Arrays.asList(getResources().getStringArray(R.array.stations));
            listDataChild = new HashMap<>();

            for (int i = 0; i < listDataHeader.size(); i++) {
                String title = listDataHeader.get(i);
                List<String> children = new ArrayList<>();
                children.add(getResources().getString(getReflectionStringId(i)));
                listDataChild.put(title, children);
            }
        } else {
            throw new UnsupportedOperationException("Implementation missing");
        }
    }

    //TODO change to service call
    private int getReflectionStringId(int station) {
        try {
            Field field = R.string.class.getField(String.format("EDK2015S%02d", station));
            return field.getInt(null);
        } catch (Exception e) {
            Log.e("EDK", "Cannot find audio for given station " + station, e);
        }
        return R.string.EDK2015S00;
    }

}
