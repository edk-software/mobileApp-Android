package pl.org.edk.services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import pl.org.edk.database.entities.Reflection;
import pl.org.edk.R;

public class ReflectionsAudioService extends Service implements
		MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
		MediaPlayer.OnCompletionListener {

	private MediaPlayer mPlayer;
	private int mStationIndex;
    private final IBinder musicBind = new MusicBinder();
    private boolean mPrepared;

    public void onCreate(){
		//create the service
		super.onCreate();
		//initialize position
		mStationIndex = 0;
		//create player
		mPlayer = new MediaPlayer();
		//initialize
		initMusicPlayer();
	}

	public void initMusicPlayer(){
		//set player properties
		mPlayer.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		//set listeners
		mPlayer.setOnPreparedListener(this);
		mPlayer.setOnCompletionListener(this);
		mPlayer.setOnErrorListener(this);
	}

    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }

    public void pause() {
        mPlayer.pause();
    }

    public void stop() {
        mPrepared = false;
        mPlayer.stop();
    }

    public int getDuration(){
        if (mPrepared){
            return mPlayer.getDuration();
        }
        return -1;
    }

    public int getCurrentPosition(){
        if (mPrepared){
            return mPlayer.getCurrentPosition();
        }
        return -1;
    }

    public boolean isPrepared() {
        return mPrepared;
    }

    public void seekTo(int msec) {
        mPlayer.seekTo(msec);
    }

    //binder
	public class MusicBinder extends Binder {
		public ReflectionsAudioService getService() {
			return ReflectionsAudioService.this;
		}
	}

	//activity will bind to service
	@Override
	public IBinder onBind(Intent intent) {
		return musicBind;
	}

	//release resources when unbind
	@Override
	public boolean onUnbind(Intent intent){
		mPlayer.stop();
		mPlayer.release();
		return false;
	}

	//play a song
	public void play(){
        if (mPrepared){
            mPlayer.start();
            return;
        }

		mPlayer.reset();
		Uri trackUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.stacja_1);
		//set the data source
		try{
			mPlayer.setDataSource(getApplicationContext(), trackUri);
		}
		catch(Exception e){
			Log.e("MUSIC SERVICE", "Error setting data source", e);
		}

		mPlayer.prepareAsync();
	}

	//set the song
	public void setReflection(Reflection reflection){
        mStationIndex = reflection.getStationIndex();
        mPrepared = false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		//start playback
		mp.start();
        mPrepared = true;
    }

}
