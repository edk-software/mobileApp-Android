package pl.org.edk.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import pl.org.edk.Extra;
import pl.org.edk.MainActivity;
import pl.org.edk.R;
import pl.org.edk.Settings;
import pl.org.edk.kml.KMLTracker;
import pl.org.edk.kml.KMLTracker.TrackListener;
import pl.org.edk.kml.TrackerProvider;
import pl.org.edk.util.NumConverter;

public class GPSService extends Service implements TrackListener{

	private static final String TAG = "EDK";

	private static final int ONGOING_NOTIFICATION_ID = 1234;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (intent.getBooleanExtra(Extra.CANCEL_BACKGROUND_TRACKING, false)){
			stopSelf();
			return START_NOT_STICKY;
		}

		Settings settings = Settings.get(this);
		settings.set(Settings.IS_BACKGROUND_TRACKING_ON, true);
		if (initializeTracker()){
			startForeground(ONGOING_NOTIFICATION_ID, getNotificationBuilder().build());
		}

		return START_NOT_STICKY;

	}

	private boolean initializeTracker() {
		try {
			KMLTracker tracker = TrackerProvider.getTracker(this);
			tracker.addListener(this);
			tracker.start();
			return true;
		} catch (Exception e){
			Log.e("EDK", "Invalid track", e);
			stopSelf();
			return false;
		}

	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "Service destroyed");
		stopForeground(true);
		Settings.get(this).set(Settings.IS_BACKGROUND_TRACKING_ON, false);
		try {
			KMLTracker tracker = TrackerProvider.getTracker(this);
			tracker.stop();
			tracker.removeListener(this);
		} catch (Exception e){
			Log.e("EDK", "Invalid track", e);
		}
		super.onDestroy();
	}

	@Override
	public void onCheckpointReached(int checkpointId) {
		if (checkpointId <= 0 || checkpointId >= 15){
			return;
		}
		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra(Extra.STATION_ID, checkpointId);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		Builder builder = getNotificationBuilder();
		String text = getString(R.string.near_checkpoint_message) + NumConverter.toRoman(checkpointId);
		builder.setContentText(text);
		builder.setTicker(text);
		builder.setVibrate(new long[] { 0, 2000 });
		builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
		builder.addAction(R.drawable.considerations, getString(R.string.go_to_consideration), pIntent);
		
		updateNotification(builder.build());
	}

	@Override
	public void onOutOfTrack() {
		Log.i(TAG, "Out of track");
		Builder builder = getNotificationBuilder();
		String text = getString(R.string.out_of_track_message);
		builder.setContentText(text);
		builder.setTicker(text);
		builder.setVibrate(new long[] { 0, 2000 });
		builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

		updateNotification(builder.build());
	}

	@Override
	public void onBackOnTrack() {
		Log.i(TAG, "Back to track");
		Builder builder = getNotificationBuilder();
		String text = getString(R.string.back_on_track_message);
		builder.setContentText(text);
		builder.setTicker(text);

		updateNotification(builder.build());
	}
	
	@Override
	public void onLocationChanged(LatLng location) {
		//ignore
	}
	
	private void updateNotification(Notification notification) {
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		if (notificationManager == null) {
			Log.w(TAG, "Notification manager was null");
			return;
		}
		notificationManager.notify(ONGOING_NOTIFICATION_ID, notification);
	}
	
	private Builder getNotificationBuilder() {
		Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(Extra.GO_TO_MAP, true);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		Builder builder = new NotificationCompat.Builder(this)
				.setContentTitle(getString(R.string.navigation_on_message))
				.setSmallIcon(R.mipmap.ic_launcher)
				.setContentIntent(pIntent)
		.setPriority(NotificationCompat.PRIORITY_MAX); 
		Intent intent2 = new Intent(this, GPSService.class);
		intent2.putExtra(Extra.CANCEL_BACKGROUND_TRACKING, true);
		builder.addAction(R.drawable.stop, getString(R.string.stop_navigation_message), PendingIntent.getService(this, 0, intent2, PendingIntent.FLAG_CANCEL_CURRENT));
		return builder;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
