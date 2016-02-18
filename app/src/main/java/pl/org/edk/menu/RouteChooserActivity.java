package pl.org.edk.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pl.org.edk.*;
import pl.org.edk.database.DbManager;
import pl.org.edk.database.entities.Area;
import pl.org.edk.database.entities.Route;
import pl.org.edk.managers.WebServiceManager;
import pl.org.edk.util.DialogUtil;

import android.content.Intent;


public class RouteChooserActivity extends ChooserActivity {

    private List<Route> mRoutes;

    @Override
    protected List<String> getItems() {
        // Get routes from DB
        Area area = DbManager.getInstance(this).getTerritoryService().getArea(
                Settings.get(this).getLong(Settings.SELECTED_AREA_ID, -1));
		mRoutes = DbManager.getInstance(this).getRouteService().getRoutesForArea(area.getId(), false);

        // If nothing found in DB, trigger downloading and wait for the results
        if(mRoutes == null || mRoutes.isEmpty()){
            DialogUtil.showBusyDialog(getString(R.string.downloading_message), this);
            WebServiceManager.OnOperationFinishedEventListener listener = new WebServiceManager.OnOperationFinishedEventListener() {
                @Override
                public void onOperationFinished(Object result) {
                    DialogUtil.closeBusyDialog();
                    mRoutes = (ArrayList<Route>) result;
                    refresh(getDisplayItems());
                }
            };
            WebServiceManager.getInstance(this).getRoutesByAreaAsync(area.getServerID(), listener);
            return Collections.emptyList();
        }

        return getDisplayItems();
    }

    @Override
    protected void onItemClick(int pos) {
        final Route selectedRoute = mRoutes.get(pos);
        if(!selectedRoute.isDownloaded()) {
            DialogUtil.showBusyDialog(getString(R.string.downloading_message), this);
            WebServiceManager.OnOperationFinishedEventListener listener = new WebServiceManager.OnOperationFinishedEventListener() {
                @Override
                public void onOperationFinished(Object result) {
                    DialogUtil.closeBusyDialog();
                    if(result != null && ((Route)result).isDownloaded()) {
                        startMapActivityWith(selectedRoute.getId());
                    }
                    else {
                        DialogUtil.showWarningDialog("Szczegóły tej trasy aktualnie nie są dostępne, proszę spróbować później.",
                                RouteChooserActivity.this, false);
                    }
                }
            };
            WebServiceManager.getInstance(this).getRouteAsync(selectedRoute.getServerID(), listener);
        }
        else {
            startMapActivityWith(selectedRoute.getId());
        }
    }

    @Override
    protected String getChooserTitle() {
        return getResources().getString(R.string.track_chooser_title);
    }

    private List<String> getDisplayItems() {
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

    private void startMapActivityWith(long routeId) {
        Settings.get(this).set(Settings.SELECTED_ROUTE_ID, routeId);
        Settings.get(this).set(Settings.START_TIME, System.currentTimeMillis());

        startActivity(new Intent(this, pl.org.edk.MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }
}
