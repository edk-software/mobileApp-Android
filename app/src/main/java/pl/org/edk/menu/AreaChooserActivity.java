package pl.org.edk.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pl.org.edk.database.DbManager;
import pl.org.edk.database.Entities.Area;
import pl.org.edk.database.Services.TerritoryService;
import pl.org.edk.R;
import pl.org.edk.Settings;
import pl.org.edk.util.DialogUtil;

import android.content.Intent;

public class AreaChooserActivity extends ChooserActivity {


	private List<Area> mCities;
	protected List<String> getItems() {
		TerritoryService territoryService = DbManager.getInstance().getTerritoryService();
        mCities = territoryService.GetAreasForTerritory(Settings.get(this).getLong(Settings.COUNTY_NAME, -1));
        if (mCities.isEmpty()){
            DialogUtil.showWarningDialog(getString(R.string.no_info_about_tracks_in_region), this);
            return Collections.emptyList();
        }
        ArrayList<String> items = new ArrayList<>();
        for (Area city : mCities) {
            items.add(city.getDisplayName());
        }
        return items;
	}

	
	protected void onItemClick(int pos) {
		Settings.get(this).set(Settings.CITY_NAME, mCities.get(pos).getId());
		Intent myIntent = new Intent(this, RouteChooserActivity.class);
		myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(myIntent);
	}


	@Override
	protected String getChooserTitle() {
		return getResources().getString(R.string.city_chooser_title);
	}

}
