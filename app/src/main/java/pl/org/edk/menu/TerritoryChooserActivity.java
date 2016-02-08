package pl.org.edk.menu;

import java.util.ArrayList;
import java.util.List;

import pl.org.edk.database.DbManager;
import pl.org.edk.database.entities.Territory;
import pl.org.edk.database.services.TerritoryService;
import pl.org.edk.R;
import pl.org.edk.Settings;

import android.content.Intent;

public class TerritoryChooserActivity extends ChooserActivity {

	private List<Territory> mTerritories;

	protected List<String> getItems() {
        TerritoryService territoryService = DbManager.getInstance(this).getTerritoryService();
        mTerritories = territoryService.GetTerritories();
        List<String> items = new ArrayList<>();
        for (Territory territory:mTerritories) {
            items.add(territory.getDisplayName());

        }
        return items;
	}

	protected void onItemClick(int pos) {
		Settings.get(this).set(Settings.COUNTY_NAME, mTerritories.get(pos).getId());
		Intent myIntent = new Intent(this, AreaChooserActivity.class);
		myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(myIntent);
	}

	@Override
	protected String getChooserTitle() {
		return getResources().getString(R.string.county_chooser_title);
	}
}
