package pl.org.edk.services;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import pl.org.edk.database.entities.Reflection;
import pl.org.edk.Extra;
import pl.org.edk.MainActivity;
import pl.org.edk.R;

public class ReflectionsAudioService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private static final int ONGOING_NOTIFICATION_ID = 2121;
    private Reflection mReflection;

    private MediaPlayer mPlayer;
    private final IBinder musicBind = new MusicBinder();
    private boolean mPrepared;
    private int mStartPercent = 0;
    private List<OnPlayerStopListener> mListeners = new ArrayList<>();
    private boolean mIsPaused;
    private int mBindCount = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getBooleanExtra(Extra.STOP_REFLECTION_AUDIO, false)) {
            stopForeground(true);
            mPlayer.stop();
            notifyOnPlayerStopListeners();
            stopSelf();
            return START_NOT_STICKY;
        }
        return START_NOT_STICKY;

    }

    public void onCreate() {
        super.onCreate();
        mPlayer = new MediaPlayer();
        initMusicPlayer();
    }

    public void initMusicPlayer() {
        mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
    }

    public boolean isPlaying() {
        return mPrepared && mPlayer.isPlaying();
    }

    public void pause() {
        mPlayer.pause();
        mIsPaused = true;
        stopForeground(true);
    }

    public void stop() {
        mPrepared = false;
        mIsPaused = false;
        mPlayer.stop();
        mStartPercent = 0;
        stopForeground(true);
    }

    public int getDuration() {
        if (mPrepared) {
            return mPlayer.getDuration();
        }
        return 100;
    }

    public int getCurrentPosition() {
        if (mPrepared) {
            return mPlayer.getCurrentPosition();
        }
        return 0;
    }

    public boolean isPrepared() {
        return mPrepared;
    }

    public void seekTo(int msec) {
        if (isPlaying() || mIsPaused) {
            mPlayer.seekTo(msec);
        } else {
            mStartPercent = msec;
        }
    }

    public void continueInForeground(Class<? extends Activity> activityClass) {
        startForeground(ONGOING_NOTIFICATION_ID, getNotificationBuilder(activityClass).build());
    }

    private NotificationCompat.Builder getNotificationBuilder(Class<? extends Activity> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Extra.STATION_ID, mReflection.getStationIndex());
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        if (mReflection == null) {
            throw new IllegalStateException("Reflection was not set");
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(mReflection.getDisplayName())
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX);
        Intent intent2 = new Intent(this, ReflectionsAudioService.class);
        intent2.putExtra(Extra.STOP_REFLECTION_AUDIO, true);
        builder.addAction(R.drawable.stop, getString(R.string.stop_navigation_message), PendingIntent.getService(this, 0, intent2, PendingIntent.FLAG_CANCEL_CURRENT));
        return builder;
    }

    @Override
    public void onDestroy() {
        mPlayer.release();
        super.onDestroy();
    }

    public void add(OnPlayerStopListener listener) {
        mListeners.add(listener);
    }

    public void remove(OnPlayerStopListener listener) {
        mListeners.remove(listener);
    }

    public interface OnPlayerStopListener {
        void onPlayerStop();
    }

    //binder
    public class MusicBinder extends Binder {
        public ReflectionsAudioService getService() {
            return ReflectionsAudioService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        mBindCount++;
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mBindCount--;
        return false;
    }

    public void play() {
        mIsPaused = false;
        if (mPrepared) {
            mPlayer.start();
            return;
        }

        mPlayer.reset();
        Uri trackUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.stacja_1);
        //set the data source
        try {
            mPlayer.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        mPlayer.prepareAsync();
    }

    //set the song
    public void setReflection(Reflection reflection) {
        mReflection = reflection;
        mPrepared = false;
    }

    public Reflection getReflection() {
        return mReflection;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        notifyOnPlayerStopListeners();
        stopForeground(true);
        if (mBindCount == 0){
            stopSelf();
        }

    }

    private void notifyOnPlayerStopListeners() {
        for (OnPlayerStopListener listener : mListeners) {
            listener.onPlayerStop();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        int startTime = mp.getDuration() * mStartPercent / 100;
        mp.seekTo(startTime);
        mp.start();
        mStartPercent = 0;
        mPrepared = true;
    }

}
