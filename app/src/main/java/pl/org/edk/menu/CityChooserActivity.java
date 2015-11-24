package pl.org.edk.menu;

import java.util.List;

import pl.org.edk.R;
import pl.org.edk.Settings;
import pl.org.edk.util.ResourcesUtil;
import android.content.Intent;

public class CityChooserActivity extends ChooserActivity {


	private List<String> mCities;
	protected List<String> getItems() {
		mCities = ResourcesUtil.getCities(this);
		return mCities;
	}

	
	protected void onItemClick(int pos) {
		Settings.get(this).set(Settings.CITY_NAME, mCities.get(pos));
		Intent myIntent = new Intent(this, TrackChooserActivity.class);
		myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(myIntent);
	}


	@Override
	protected String getChooserTitle() {
		return getResources().getString(R.string.city_chooser_title);
	}

}
