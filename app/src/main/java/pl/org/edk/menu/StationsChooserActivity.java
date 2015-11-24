package pl.org.edk.menu;

import java.util.Arrays;
import java.util.List;

import pl.org.edk.Extra;
import pl.org.edk.R;
import android.content.Intent;


public class StationsChooserActivity extends ChooserActivity {

	@Override
	protected List<String> getItems() {
		return Arrays.asList(getResources().getStringArray(R.array.stations));
	}

	@Override
	protected void onItemClick(int pos) {
		Intent myIntent = new Intent(this, ConsiderationsViewActivity.class);
		myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		myIntent.putExtra(Extra.STATION_ID, pos);
        startActivity(myIntent);		
	}

	@Override
	protected String getChooserTitle() {
		return getResources().getString(R.string.stations_chooser_title);
	}
    
}