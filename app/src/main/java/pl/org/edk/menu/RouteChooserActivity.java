package pl.org.edk.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pl.org.edk.*;
import pl.org.edk.database.DbManager;
import pl.org.edk.database.entities.Area;
import pl.org.edk.database.entities.Route;
import pl.org.edk.database.services.RouteService;
import pl.org.edk.managers.WebServiceManager;
import pl.org.edk.util.DialogUtil;

import android.content.Intent;
import android.os.Bundle;

public class RouteChooserActivity extends ChooserActivity {

    private DbManager mDbManager;
    private Settings mSettings;
    private List<Route> mRoutes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mDbManager = DbManager.getInstance(this);
        mSettings = Settings.get(this);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected List<String> getItems() {
        // Check which routes to display
        long areaId = TempSettings.get(this).getLong(TempSettings.SELECTED_AREA_ID, -1);
        final Area area = mDbManager.getTerritoryService().getArea(areaId);

        // Trigger downloading and wait for the results
        DialogUtil.showBusyDialog(R.string.downloading_message, this);
        WebServiceManager.OnOperationFinishedEventListener listener = new WebServiceManager.OnOperationFinishedEventListener() {
            @Override
            public void onOperationFinished(Object result) {
                DialogUtil.closeBusyDialog();

                boolean showArchiveRoutes = mSettings.getBoolean(Settings.SHOW_ARCHIVE_ROUTES);
                mRoutes = mDbManager.getRouteService().getRoutesForArea(area.getId(), !showArchiveRoutes, false);
                refresh(getDisplayItems());
            }
        };
        WebServiceManager.getInstance(this).getRoutesByAreaAsync(area.getServerID(), listener);
        return Collections.emptyList();
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
        // Display pop-up, if no routes were found
        if (mRoutes.isEmpty()) {
            DialogUtil.showWarningDialog(getString(R.string.no_info_about_routes_in_area), this, true);
            return Collections.emptyList();
        }

        // Navigate forward, if there's only one item to display
        if (mRoutes.size() == 1) {
            startRouteDescriptionActivity(0);
            return Collections.emptyList();
        }

        ArrayList<String> items = new ArrayList<>();
        for (Route route : mRoutes) {
            items.add(route.getName());
        }
        return items;
    }
}
