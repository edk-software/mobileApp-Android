package pl.org.edk.menu;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;

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

    private Route mRoute;
    private WebView mDescriptionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_description);

        initView();

        mDescriptionTextView = (WebView) findViewById(R.id.descriptionText);

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
    }

    private void initView(){
        Button startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                TempSettings.get(RouteDescriptionActivity.this).set(TempSettings.START_TIME, System.currentTimeMillis());
                startActivity(new Intent(RouteDescriptionActivity.this, MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        });

        Button changeButton = (Button) findViewById(R.id.changeButton);
        changeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RouteDescriptionActivity.this, TerritoryChooserActivity.class));
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
        } else {
            setRouteDescription();
        }
    }

    private void showRouteUnavailableWarning() {
        DialogUtil.showWarningDialog("Szczegóły tej trasy aktualnie nie są dostępne, proszę spróbować później.",
                RouteDescriptionActivity.this, false);
    }

    private void setRouteDescription() {
        String descriptionHtml = mRoute.getDescriptions().get(0).getDescription();
        if (descriptionHtml == null || descriptionHtml.isEmpty()) {
            descriptionHtml = getString(R.string.description_unavailable);
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
}
