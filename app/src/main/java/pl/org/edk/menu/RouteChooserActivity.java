package pl.org.edk.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pl.org.edk.*;
import pl.org.edk.database.DbManager;
import pl.org.edk.database.entities.Route;
import pl.org.edk.database.services.RouteService;
import pl.org.edk.services.GPSService;
import pl.org.edk.util.DialogUtil;

import android.content.Intent;


public class RouteChooserActivity extends ChooserActivity {

    private List<Route> mRoutes;

    @Override
    protected List<String> getItems() {

		RouteService routeService = DbManager.getInstance(this).getRouteService();
		mRoutes = routeService.getRoutesForArea(Settings.get(this).getLong(Settings.CITY_NAME, -1), false);
		if (mRoutes.isEmpty()){
        	DialogUtil.showWarningDialog(getString(R.string.no_info_about_tracks_in_region), this, true);
        	return Collections.emptyList();
        }
        if (mRoutes.size() == 1) {
            startMapActivityWith(mRoutes.get(0).getId());
        }
        ArrayList<String> items = new ArrayList<>();
        for (Route route : mRoutes) {
            items.add(route.getName());
        }


        return items;
    }

    @Override
    protected void onItemClick(int pos) {
        startMapActivityWith(mRoutes.get(pos).getId());
    }

    private void startMapActivityWith(long routeId) {
        Settings settings = Settings.get(this);
        settings.set(Settings.TRACK_NAME, routeId);
        Intent myIntent = new Intent(this, pl.org.edk.MainActivity.class);

        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        stopService(new Intent(this, GPSService.class));
        Settings.get(this).set(Settings.IS_BACKGROUND_TRACKING_ON, false);
        Settings.get(this).set(Settings.START_TIME, System.currentTimeMillis());
        startActivity(myIntent);
    }

    @Override
    protected String getChooserTitle() {
        return getResources().getString(R.string.track_chooser_title);
    }

}
