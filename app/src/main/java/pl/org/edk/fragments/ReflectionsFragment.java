package pl.org.edk.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.SeekBar;

import java.util.*;

import pl.org.edk.R;
import pl.org.edk.Settings;
import pl.org.edk.database.DbManager;
import pl.org.edk.database.entities.Reflection;
import pl.org.edk.database.entities.ReflectionList;
import pl.org.edk.managers.WebServiceManager;
import pl.org.edk.services.ReflectionsAudioService;
import pl.org.edk.services.ReflectionsAudioService.OnPlayerStopListener;
import pl.org.edk.util.DialogUtil;
import pl.org.edk.util.ExpandableListAdapter;

/**
 * Created by darekpap on 2015-11-30.
 */
public class ReflectionsFragment extends Fragment implements OnPlayerStopListener {
    public static final String FRAGMENT_TAG = "reflectionsFragment";

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
                    showPlayerIfAvailable();
                    resetSeekBar();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }
    };

    private Runnable updateSeekBarTime = new Runnable() {

        @Override
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
    private ReflectionList mReflectionList;
    private Button mDownloadButton;

    // ---------------------------------------
    // Constructors
    // ---------------------------------------
    public ReflectionsFragment() {
        // Required empty public constructor
    }

    // ---------------------------------------
    // Public methods
    // ---------------------------------------
    public void selectStation(int stationIndex) {
        if (expListView == null) {
            Log.d("EDK", "List not ready");
            return;
        }
        if (listDataHeader.isEmpty()) {
            return;
        }
        if (listDataHeader.size() == 14) {
            Log.d("EDK", "There are only 14 reflections available");
            stationIndex = Math.min(13, Math.max(0, stationIndex - 1));
        }
        boolean playerResetNeeded = stationIndex != mCurrentStation;
        openReflections(stationIndex);
        hideDownloadButton();
        if (playerResetNeeded) {
            preparePlayer(stationIndex);
            return;
        }
        if (mServiceBound) {
            loadPlayer();
        } else {
            showPlayerIfAvailable();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reflections, container, false);
        expListView = (ExpandableListView) view.findViewById(R.id.expandableList);

        // Load reflections from DB or download them
        if (!prepareListData()) {
            DialogUtil.showWarningDialog(R.string.failed_to_download_audio_message, getActivity(), true);
            return view;
        }

        // Ask about audio reflections
        Settings settings = Settings.get(getActivity());
        boolean dialogShown = settings.getBoolean(Settings.AUDIO_DOWNLOAD_DIALOG_SHOWN, false);
        if (!isAudioAvailable() && !dialogShown) {
            showDownloadDialog();
            settings.set(Settings.AUDIO_DOWNLOAD_DIALOG_SHOWN, true);
        }
        refreshViewItems();

        initializePlayerView(view);
        initializeDownloadButton(view);
        if (mCurrentStation == -1 || !isAudioAvailable()) {
            hidePlayer();
        }
        if (mReflectionList.hasAudio()) {
            hideDownloadButton();
        } else {
            showDownloadButtonIfNeeded();
        }
        return view;
    }

    private void initializeDownloadButton(View view) {
        mDownloadButton = (Button) view.findViewById(R.id.downloadButton);
        mDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDownloadDialog();
                refreshViewItems();
            }
        });
    }

    private void showDownloadButtonIfNeeded() {
        if (isAudioAvailable()) {
            return;
        }
        mDownloadButton.setVisibility(View.VISIBLE);
    }

    private void hideDownloadButton() {
        mDownloadButton.setVisibility(View.GONE);
    }

    //start and bind the service when the activity starts
    @Override
    public void onStart() {
        super.onStart();
        if (isAudioAvailable()) {
            bindAudioService();
        }
    }

    private void bindAudioService() {
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

    @Override
    public void onPlayerStop() {
        preparePlayer(mCurrentStation);
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
                        mAudioService.setReflection(mReflectionList.getReflections().get(mCurrentStation));
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

    // ---------------------------------------
    // Private methods
    // ---------------------------------------
    private void hidePlayer() {
//        mPlayerView.setTranslationY(mPlayerView.getHeight());
//        mPlayerView.animate().translationY(mPlayerView.getHeight()).start();
        mPlayerView.setVisibility(View.GONE);
    }

    private void showPlayerIfAvailable() {
        if (!isAudioAvailable()) {
            return;
        }
        mPlayerView.setVisibility(View.VISIBLE);
    }

    private void loadPlayer() {
        showPlayerIfAvailable();

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
        showPlayerIfAvailable();

        mPlayButton.setImageResource(R.drawable.play);
        mPrevButton.setEnabled(stationIndex != 0);
        mNextButton.setEnabled(stationIndex != 15);

        resetSeekBar();

        if (mServiceBound) {
            if (mAudioService.isPlaying()) {
                mAudioService.stop();
            }
            mAudioService.setReflection(mReflectionList.getReflections().get(stationIndex));
        }
    }

    private void resetSeekBar() {
        mSeekBar.setProgress(0);
        mSeekBar.setMax(100);
        finalTime = 100;
        timeElapsed = 0;
    }

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

    private boolean isAudioAvailable() {
        return mReflectionList != null && mReflectionList.hasAudio();
    }

    private boolean prepareListData() {
        mReflectionList = DbManager.getInstance(getActivity()).getReflectionService().getReflectionList("pl", true);
        // No reflections found
        if (mReflectionList == null || mReflectionList.getReflections().isEmpty()) {
            mReflectionList = WebServiceManager.getInstance(getActivity()).getReflectionList("pl");
            // Download failed
            if (mReflectionList == null) {
                return false;
            }
        }
        return true;
    }

    private void showDownloadDialog() {
        DialogUtil.showYesNoDialog(R.string.downloading_reflections_title, R.string.downloading_audio_question,
                getActivity(), new DialogUtil.OnSelectedEventListener() {
                    @Override
                    public void onAccepted() {
                        WebServiceManager.getInstance(getActivity()).getReflectionsAudioAsync(mReflectionList, new WebServiceManager.OnOperationFinishedEventListener() {
                            @Override
                            public void onOperationFinished(Object result) {
                                FragmentActivity activity = getActivity();

                                if (activity != null) {

                                // Inform about the result
                                String message;
                                if (((ReflectionList) result).hasAudio()) {
                                    message = activity.getString(R.string.audio_download_finished_message);
                                } else {
                                    message = activity.getString(R.string.failed_to_download_audio_message);
                                }
                                    DialogUtil.showDialog(activity.getString(R.string.audio_download_finished_title), message, activity, true, null);
                                }

                                bindAudioService();
                                hideDownloadButton();
                            }
                        });
                        hideDownloadButton();
                    }

                    @Override
                    public void onRejected() { /* Just close */ }
                });
    }

    private void refreshViewItems() {
        // Make sure that the list is sorted by stationIndex
        Collections.sort(mReflectionList.getReflections(), new Comparator<Reflection>() {
            @Override
            public int compare(Reflection lhs, Reflection rhs) {
                return lhs.getStationIndex() - rhs.getStationIndex();
            }
        });

        // Create the lists
        listDataHeader = new ArrayList<>(mReflectionList.getReflections().size());
        listDataChild = new HashMap<>(mReflectionList.getReflections().size());

        for (final Reflection reflection : mReflectionList.getReflections()) {
            String title = reflection.getDisplayName();
            listDataHeader.add(reflection.getStationIndex(), title);
            listDataChild.put(title, new ArrayList<String>() {{
                add(reflection.getContent());
            }});
        }

        // Prepare the view
        listAdapter = new ExpandableListAdapter(getActivity(), listDataHeader, listDataChild);
        expListView.setAdapter(listAdapter);
        expListView.setGroupIndicator(null);
        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(final ExpandableListView parent, final View v, final int groupPosition, long id) {

                if (parent.isGroupExpanded(groupPosition)) {
                    parent.collapseGroup(groupPosition);
                    hidePlayer();
                    showDownloadButtonIfNeeded();
                    mCurrentStation = -1;
                    if (mServiceBound && mAudioService.isPlaying()) {
                        mAudioService.continueInForeground(getActivity().getClass());
                    }
                } else {
                    openReflections(groupPosition);
                    hideDownloadButton();
                    if (mServiceBound && mAudioService.isPlaying() && mAudioService.getReflection().getStationIndex() == groupPosition) {
                        loadPlayer();
                    } else {
                        preparePlayer(groupPosition);
                    }
                }
                return true;
            }
        });
    }
}
