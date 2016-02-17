package pl.org.edk.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pl.org.edk.databaseTEMP.DbManager;
import pl.org.edk.databaseTEMP.entitiesTEMP.Territory;
import pl.org.edk.R;
import pl.org.edk.Settings;

import android.content.Intent;
import pl.org.edk.managers.WebServiceManager;
import pl.org.edk.util.DialogUtil;

public class TerritoryChooserActivity extends ChooserActivity {

	private List<Territory> mTerritories;

	protected List<String> getItems() {
		// Get territories from DB
        mTerritories = DbManager.getInstance(this).getTerritoryService().getTerritories();

		// If nothing found in DB, trigger downloading and wait for the results
		if(mTerritories == null || mTerritories.isEmpty()){
			DialogUtil.showBusyDialog(getString(R.string.downloading_message), this);
			WebServiceManager.getInstance(this).getTerritoriesAsync(new WebServiceManager.OnOperationFinishedEventListener() {
				@Override
				public void onOperationFinished(Object result) {
					DialogUtil.closeBusyDialog();
					mTerritories = (ArrayList<Territory>)result;
					refresh(getDisplayItems());
				}
			});
			return Collections.emptyList();
		}

		return getDisplayItems();
	}

	protected void onItemClick(int pos) {
		Settings.get(this).set(Settings.TERRITORY_NAME, mTerritories.get(pos).getId());
		Intent myIntent = new Intent(this, AreaChooserActivity.class);
		myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(myIntent);
	}

	@Override
	protected String getChooserTitle() {
		return getResources().getString(R.string.county_chooser_title);
	}

	private List<String> getDisplayItems(){
		if(mTerritories != null && !mTerritories.isEmpty()){
			List<String> items = new ArrayList<>();
			for (Territory territory : mTerritories) {
				items.add(territory.getDisplayName());
			}
			return items;
		}
		// Display pop-up, if nothing found
		else {
			DialogUtil.showWarningDialog(getString(R.string.no_territories_found), this, true);
			return Collections.emptyList();
		}
	}
}
