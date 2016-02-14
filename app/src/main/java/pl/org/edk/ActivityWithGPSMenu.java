package pl.org.edk;

import pl.org.edk.kml.KMLTracker;
import pl.org.edk.kml.TrackerProvider;
import pl.org.edk.menu.ConsiderationsViewActivity;
import pl.org.edk.services.GPSService;
import pl.org.edk.util.DialogUtil;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;

public class ActivityWithGPSMenu extends Activity {

    protected static final int CONSIDERATIONS_MENU_INDEX = 0;

    protected static final int MAP_MENU_INDEX = 1;

    protected static final int APPLICATION_FINISH_INDEX = 2;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gps_menu, menu);
        menu.getItem(CONSIDERATIONS_MENU_INDEX).setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                goToConsiderations(item);
                return true;
            }
        });

        menu.getItem(MAP_MENU_INDEX).setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                goToMap(item);
                return true;
            }
        });

        menu.getItem(APPLICATION_FINISH_INDEX).setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                finishEDK(item);
                return true;
            }
        });
        return true;
    }

    public void goToConsiderations(MenuItem item) {
        Intent intent = new Intent(this, ConsiderationsViewActivity.class);
        KMLTracker tracker = TrackerProvider.getTracker(this);
        int stationId = tracker.getCheckpointId();
        if (stationId == -1) {
            stationId = tracker.getNextCheckpointId();
            stationId = stationId == -1 ? 0 : stationId;
        }
        intent.putExtra(Extra.STATION_ID, stationId);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    public void goToMap(MenuItem item) {
        Intent intent = new Intent(this, ActivityWithMap.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    public void finishEDK(MenuItem item) {
        AlertDialog.Builder builder = new Builder(this);
        builder.setTitle(R.string.warning_dialog_title);
        builder.setMessage(R.string.end_dialog_message);
        builder.setNegativeButton(R.string.no, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton(R.string.yes, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                stopService(new Intent(ActivityWithGPSMenu.this, GPSService.class));
                Intent intent = new Intent(getApplicationContext(), EndActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        AlertDialog dialog = builder.show();
        DialogUtil.addRedTitleDivider(this, dialog);
    }

}