package pl.org.edk.EdkMobile;

import android.content.Context;

import pl.org.edk.EdkMobile.Entities.CrossRoute;
import pl.org.edk.EdkMobile.Managers.AppConfiguration;
import pl.org.edk.EdkMobile.Managers.GpsManager;
import pl.org.edk.EdkMobile.Managers.JsonManager;

import java.io.IOException;

/**
 * Created by Pawel on 2015-03-03.
 */
public final class Bootstrap {
    private Bootstrap(){}

    private static boolean isInitialized = false;

    public static void Initialize(Context context){
        if(isInitialized)
            return;

        // Get CrossRoute
        try {
            JsonManager jsonManager = new JsonManager(context);
            CrossRoute crossRoute = jsonManager.readAssetJson("routes", CrossRoute.class);
            AppConfiguration.getInstance().setCrossRoute(crossRoute);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Initialize GPS manager
        AppConfiguration.getInstance().setGpsManager(new GpsManager(context));

        isInitialized = true;
    }
}
