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
                TempSettings.get(this).getLong(TempSettings.SELECTED_AREA_ID, -1));
        mRoutes = DbManager.getInstance(this).getRouteService().getRoutesForArea(area.getId(), false);

        // If nothing found in DB, trigger downloading and wait for the results
        if (mRoutes == null || mRoutes.isEmpty()) {
            DialogUtil.showBusyDialog(R.string.downloading_message, this);
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
        startRouteDescriptionActivity(pos);
    }

    @Override
    protected String getChooserTitle() {
        return getResources().getString(R.string.track_chooser_title);
    }

    private void startRouteDescriptionActivity(int pos) {
        TempSettings.get(RouteChooserActivity.this).set(TempSettings.SELECTED_ROUTE_ID, mRoutes.get(pos).getId());
        startActivity(new Intent(RouteChooserActivity.this, RouteDescriptionActivity.class));
    }

    private List<String> getDisplayItems() {
        if (mRoutes.isEmpty()) {
            DialogUtil.showWarningDialog(getString(R.string.no_info_about_tracks_in_region), this, true);
            return Collections.emptyList();
        }
        if (mRoutes.size() == 1) {
            startRouteDescriptionActivity(0);
        }

        ArrayList<String> items = new ArrayList<>();
        for (Route route : mRoutes) {
            items.add(route.getName());
        }
        return items;
    }
}
