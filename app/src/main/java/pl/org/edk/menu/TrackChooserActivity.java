package pl.org.edk.menu;

import java.util.Collections;
import java.util.List;

import pl.org.edk.ActivityWithMap;
import pl.org.edk.GPSService;
import pl.org.edk.R;
import pl.org.edk.Settings;
import pl.org.edk.util.DialogUtil;
import pl.org.edk.util.ResourcesUtil;
import android.content.Intent;


public class TrackChooserActivity extends ChooserActivity {

	private List<String> mTracks;

	@Override
	protected List<String> getItems() {
		mTracks = ResourcesUtil.getTracks(this);
		if (mTracks.isEmpty()){
        	DialogUtil.showWarningDialog(getString(R.string.no_info_about_tracks_in_region), this);
        	return Collections.emptyList();
        }
		if (mTracks.size()==1){
			startMapActivityWith(mTracks.get(0));
		}
		return mTracks;
	}

	@Override
	protected void onItemClick(int pos) {
		
		String trackName = mTracks.get(pos);
		startMapActivityWith(trackName);
		
	}

	private void startMapActivityWith(String trackName) {
		Settings settings = Settings.get(this);
		settings.set(Settings.TRACK_NAME, trackName);
		Intent myIntent = new Intent(this, ActivityWithMap.class);
		
		myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		
		stopService(new Intent(this, GPSService.class));
		Settings.get(this).set(Settings.IS_BACKGROUND_TRACKING_ON, false);
		startActivity(myIntent);
	}

	@Override
	protected String getChooserTitle() {
		return getResources().getString(R.string.track_chooser_title);
	}
    
}
