package pl.org.edk.menu;

import java.util.Arrays;
import java.util.List;

import pl.org.edk.R;
import pl.org.edk.Settings;
import android.content.Intent;

public class ConsiderationsYearChooserActivity extends ChooserActivity {

	@Override
	protected List<String> getItems() {
		return Arrays.asList(getResources().getStringArray(R.array.considerations_years));
	}

	@Override
	protected void onItemClick(int pos) {
		Settings settings = Settings.get(this);
		settings.set(Settings.YEAR_ID, pos);

		Intent myIntent = new Intent(this, StationsChooserActivity.class);
		myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(myIntent);
		
	}

	@Override
	protected String getChooserTitle() {
		return getResources().getString(R.string.considerations_year_chooser);
	}
}
