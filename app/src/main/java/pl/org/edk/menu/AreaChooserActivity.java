package pl.org.edk.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pl.org.edk.database.DbManager;
import pl.org.edk.database.entities.Area;
import pl.org.edk.R;
import pl.org.edk.Settings;
import pl.org.edk.database.entities.Territory;
import pl.org.edk.managers.WebServiceManager;
import pl.org.edk.util.DialogUtil;

import android.content.Intent;

public class AreaChooserActivity extends ChooserActivity {
	private List<Area> mAreas;

	@Override
	protected List<String> getItems() {
		// Get areas from DB
		Territory territory = DbManager.getInstance(this).getTerritoryService().getTerritory(Settings.get(this).getLong(Settings.TERRITORY_NAME, -1));
        mAreas = territory.getAreas();

		// If nothing found in DB, download them from WS
		if(mAreas == null || mAreas.isEmpty()){
			mAreas = WebServiceManager.getInstance(this).getAreas(territory.getServerID());
		}

		// Display what's found
        if (mAreas != null && !mAreas.isEmpty()){
			ArrayList<String> items = new ArrayList<>();
			for (Area area : mAreas) {
				items.add(area.getDisplayName());
			}
			return items;
        }
		// Display pop-up, if nothing found
		else {
			DialogUtil.showWarningDialog(getString(R.string.no_info_about_tracks_in_region), this);
			return Collections.emptyList();
		}
	}

	@Override
	protected void onItemClick(int pos) {
		Settings.get(this).set(Settings.CITY_NAME, mAreas.get(pos).getId());
		Intent myIntent = new Intent(this, RouteChooserActivity.class);
		myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(myIntent);
	}

	@Override
	protected String getChooserTitle() {
		return getResources().getString(R.string.city_chooser_title);
	}

}
