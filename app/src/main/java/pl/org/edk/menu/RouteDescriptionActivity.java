package pl.org.edk.menu;

import android.Manifest;
import android.app.ActionBar;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import pl.org.edk.BootStrap;
import pl.org.edk.MainActivity;
import pl.org.edk.R;
import pl.org.edk.Settings;
import pl.org.edk.TempSettings;
import pl.org.edk.database.DbManager;
import pl.org.edk.database.entities.Route;
import pl.org.edk.fragments.MapFragment;
import pl.org.edk.fragments.ViewRouteFragment;
import pl.org.edk.managers.WebServiceManager;
import pl.org.edk.util.DialogUtil;

public class RouteDescriptionActivity extends FragmentActivity implements MapFragment.OnStationSelectListener {

    private static final int REQUEST_CODE = 4;
    private Route mRoute;
    private WebView mDescriptionTextView;
    private TextView mDescriptionHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_description);

        initView();

        mDescriptionTextView = findViewById(R.id.descriptionText);
        mDescriptionHeader = findViewById(R.id.descriptionHeader);

        long routeId = TempSettings.get(this).getLong(TempSettings.SELECTED_ROUTE_ID, -1);
        mRoute = DbManager.getInstance(this).getRouteService().getRoute(routeId, "pl");
        if (mRoute == null) {
            showRouteUnavailableWarning();
            return;
        }

        setTitle(mRoute.getName());

        ActionBar actionBar = getActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        downloadRouteDetailsAsync();
        TempSettings.get(this).set(TempSettings.CAMERA_ZOOM, -1);

    }

    private void initView(){
        Button startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.Start(RouteDescriptionActivity.this);
            }
        });

        Button viewRoute = (Button) findViewById(R.id.viewRouteButton);
        viewRoute.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment viewRouteFragment = ViewRouteFragment.newInstance(Settings.get(getApplicationContext()).getBoolean(Settings.FOLLOW_LOCATION_ON_MAP));
                FragmentManager fm = getSupportFragmentManager();
                fm.beginTransaction().add(R.id.container,viewRouteFragment,"view").addToBackStack("view").commit();
            }
        });
    }

    private void downloadRouteDetailsAsync() {
        if (!mRoute.isDownloaded()) {
            checkPermission();
        } else {
            setRouteDescription();
        }
    }

    private void checkPermission() {
        boolean canUseStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (canUseStorage){
            permissionGranted();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_CODE) {
            return;
        }
        if (grantResults.length == 0) {
            return;
        }
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
            BootStrap.initStorage(this);
            permissionGranted();
        }else{
            permissionDenied();
        }
    }

    private void permissionDenied() {
        DialogUtil.showWarningDialog(R.string.no_storage_permission_message_yesno, this, true);
    }

    private void permissionGranted() {
        DialogUtil.showBusyDialog(R.string.downloading_message, this);
        WebServiceManager.OnOperationFinishedEventListener listener = new WebServiceManager.OnOperationFinishedEventListener() {
            @Override
            public void onOperationFinished(Object result) {
                DialogUtil.closeBusyDialog();
                if (result != null && ((Route) result).isDownloaded()) {
                    mRoute = (Route) result;
                    setRouteDescription();
                } else {
                    showRouteUnavailableWarning();
                }
            }
        };
        WebServiceManager.getInstance(this).syncRouteAsync(mRoute.getServerID(), listener);
    }

    private void showRouteUnavailableWarning() {
        DialogUtil.showWarningDialog("Szczegóły tej trasy aktualnie nie są dostępne, proszę spróbować później.",
                RouteDescriptionActivity.this, false);
    }

    private void setRouteDescription() {
        String descriptionHtml = mRoute.getDescriptions().get(0).getDescription();
        if (descriptionHtml == null || descriptionHtml.isEmpty()) {
            mDescriptionHeader.setVisibility(View.GONE);
            mDescriptionTextView.setVisibility(View.GONE);
            return;
        }

        String text = decorateHtmlWithColors(descriptionHtml);

        mDescriptionTextView.loadDataWithBaseURL(null, text, "text/html", "utf-8", null);
    }

    @NonNull
    private String decorateHtmlWithColors(String descriptionHtml) {
        return "<html><head>"
                    + "<style type=\"text/css\">body{color: " + getColorString(R.color.white_on_black) + "; background-color: " + getColorString(R.color.darkGray) + ";} "
                    +"A:link {text-decoration: none;color: " + getColorString(R.color.red) +"}"
                    + "</style></head>"
                    + "<body>"
                    + descriptionHtml
                    + "</body></html>";
    }

    private String getColorString(int colorId) {
        return toHexColor(ContextCompat.getColor(this, colorId));
    }

    private static String toHexColor(int colorValue) {
        return String.format("#%06X", (0xFFFFFF & colorValue));
    }


    @Override
    public void onStationSelect(int stationIndex) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        TempSettings.get(this).set(TempSettings.TRACK_WARNING_SHOWN, false);
    }
}
