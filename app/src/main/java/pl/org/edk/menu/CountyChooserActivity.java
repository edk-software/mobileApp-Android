package pl.org.edk.menu;

import java.util.List;

import pl.org.edk.R;
import pl.org.edk.Settings;
import pl.org.edk.util.ResourcesUtil;
import android.content.Intent;

public class CountyChooserActivity extends ChooserActivity {

	private List<String> mCounties;


	protected List<String> getItems() {
		mCounties = ResourcesUtil.getCounties(this);
		return mCounties;
	}

	
	protected void onItemClick(int pos) {
		Settings.get(this).set(Settings.COUNTY_NAME, mCounties.get(pos));
		Intent myIntent = new Intent(this, CityChooserActivity.class);
		myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(myIntent);
	}


	@Override
	protected String getChooserTitle() {
		return getResources().getString(R.string.county_chooser_title);
	}

}
