package pl.org.edk.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pl.org.edk.databaseTEMP.DbManager;
import pl.org.edk.databaseTEMP.entitiesTEMP.Area;
import pl.org.edk.R;
import pl.org.edk.Settings;
import pl.org.edk.databaseTEMP.entitiesTEMP.Territory;
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

		// If nothing found in DB, trigger downloading and wait for the results
		if(mAreas == null || mAreas.isEmpty()){
			DialogUtil.showBusyDialog(getString(R.string.downloading_message), this);

			long territoryId = territory.getServerID();
			WebServiceManager.OnOperationFinishedEventListener listener = new WebServiceManager.OnOperationFinishedEventListener() {
				@Override
				public void onOperationFinished(Object result) {
					DialogUtil.closeBusyDialog();
					mAreas = (ArrayList<Area>) result;
					refresh(getDisplayItems());
				}
			};
			WebServiceManager.getInstance(this).getAreasAsync(territoryId, listener);
			return Collections.emptyList();
		}

		return getDisplayItems();
	}

	@Override
	protected void onItemClick(int pos) {
		Settings.get(this).set(Settings.AREA_ID, mAreas.get(pos).getId());
		Intent myIntent = new Intent(this, RouteChooserActivity.class);
		myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(myIntent);
	}

	@Override
	protected String getChooserTitle() {
		return getResources().getString(R.string.city_chooser_title);
	}

	private List<String> getDisplayItems(){
		if (mAreas != null && !mAreas.isEmpty()){
			ArrayList<String> items = new ArrayList<>();
			for (Area area : mAreas) {
				items.add(area.getDisplayName());
			}
			return items;
		}
		// Display pop-up, if nothing found
		else {
			DialogUtil.showWarningDialog(getString(R.string.no_info_about_tracks_in_region), this, true);
			return Collections.emptyList();
		}
	}
}
