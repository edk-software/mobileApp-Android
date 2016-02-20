package pl.org.edk.menu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;

import pl.org.edk.MainActivity;
import pl.org.edk.R;
import pl.org.edk.Settings;
import pl.org.edk.database.DbManager;
import pl.org.edk.database.entities.Route;
import pl.org.edk.managers.WebServiceManager;
import pl.org.edk.util.DialogUtil;

public class RouteDescriptionActivity extends Activity {


    private Route mRoute;
    private WebView mDescriptionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_description);

        Button startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Settings.get(RouteDescriptionActivity.this).set(Settings.START_TIME, System.currentTimeMillis());
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

        mDescriptionTextView = (WebView) findViewById(R.id.descriptionText);

        mRoute = DbManager.getInstance(this).getRouteService()
                .getRoute(Settings.get(this).getLong(Settings.SELECTED_ROUTE_ID, -1), "pl");

        if (mRoute == null) {
            showRouteUnavailableWarning();
            return;
        }

        setTitle(mRoute.getName());

        downloadRouteDetailsAsync();

    }


    private void downloadRouteDetailsAsync() {
        if (!mRoute.isDownloaded()) {
            DialogUtil.showBusyDialog(getString(R.string.downloading_message), this);
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
            WebServiceManager.getInstance(this).getRouteAsync(mRoute.getServerID(), listener);
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


}
