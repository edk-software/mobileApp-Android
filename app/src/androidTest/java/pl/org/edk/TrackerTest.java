package pl.org.edk;

import android.test.InstrumentationTestCase;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pl.org.edk.database.entities.Route;
import pl.org.edk.kml.KMLHandler;
import pl.org.edk.kml.Track;
import pl.org.edk.managers.WebServiceManager;

/**
 * Created by darekpap on 2016-02-22.
 */
public class TrackerTest extends InstrumentationTestCase {

    @Test
    public void test() throws IOException {

        final List<Route> unloadedRoutes = new ArrayList<>();
        final List<Track> partialTracks = new ArrayList<>();
        final List<Track> reversedTracks = new ArrayList<>();
        final List<Route> partialRoutes = new ArrayList<>();
        final List<Route> reversedRoutes = new ArrayList<>();

        final int[] successfulCount = {0};
        for (int routeId =0; routeId < 2000; routeId++) {
            final int finalRouteId = routeId;
            WebServiceManager.OnOperationFinishedEventListener listener = new WebServiceManager.OnOperationFinishedEventListener() {
                @Override
                public void onOperationFinished(Object result) {
                    Log.d("TEST", "received route " + finalRouteId);
                    Route route = (Route) result;
                    if (result != null && route.isDownloaded()) {
                        Log.d("TEST", "route downloaded successfully");
                        successfulCount[0]++;

                        Track track = loadTrack(route);
                        if (track == null) {
                            unloadedRoutes.add(route);
                        } else if (!track.isProperlyInitialized()) {
                            partialTracks.add(track);
                            partialRoutes.add(route);
                        } else if (track.GetStatus() == Track.Status.Reversed){
                            reversedTracks.add(track);
                            reversedRoutes.add(route);
                        }else{
                            Log.i("TEST", "route " + finalRouteId + " loaded successfully");

                        }
                    } else {
                        Log.i("TEST", "route "  + finalRouteId + " invalid");

                    }
                }
            };
            WebServiceManager.getInstance(getInstrumentation().getTargetContext()).syncRouteAsync(routeId, listener);
        }


        StringBuilder sb = new StringBuilder();
        if (!unloadedRoutes.isEmpty()){
            sb.append(getLineSeparator());
            sb.append("Failed to read "+ unloadedRoutes.size()+ " routes out of " + successfulCount[0]);
            sb.append(getLineSeparator());
        }
        for (Route route : unloadedRoutes) {
            sb.append("FAILED TO LOAD ROUTE ");
            sb.append(getRouteInfo(route));
            sb.append(getLineSeparator());
            sb.append(getLineSeparator());

        }

        if (!partialTracks.isEmpty()){
            sb.append(getLineSeparator());
            sb.append(String.format("Problems occurred when reading %d routes out of " + successfulCount[0], partialTracks.size()));
            sb.append(getLineSeparator());
        }
        for (int i = 0; i < partialTracks.size(); i++) {
            Track track1 = partialTracks.get(i);
            Route route1 = partialRoutes.get(i);
            sb.append("Problems loading route ");
            sb.append(getRouteInfo(route1));
            sb.append(getLineSeparator());
            sb.append(createErrorMessage(track1));
            sb.append(getLineSeparator());

        }
        if (!reversedTracks.isEmpty()){
            sb.append(getLineSeparator());
            sb.append(String.format("%d routes reversed out of " + successfulCount[0], reversedTracks.size()));
            sb.append(getLineSeparator());
        }
        for (int i = 0; i < reversedTracks.size(); i++) {
            Track track = reversedTracks.get(i);
            Route route = reversedRoutes.get(i);
            sb.append("Reversed route ");
            sb.append(getRouteInfo(route));
            sb.append(getLineSeparator());

        }

        if (sb.length() > 0) {
            String msg = sb.toString();
            fail(msg);
        }
    }

    private String getRouteInfo(Route route) {
        return "Name: " + route.getName() + " ServerId: " + route.getServerID();
    }

    private String getLineSeparator() {
        return "\n";
    }

    private Track loadTrack(Route route) {

        try {

            String kmlDataPath = route.getKmlDataPath();
            if (kmlDataPath == null || kmlDataPath.isEmpty()){
                Log.i("TEST", "Path to KML was null or empty");
                return null;
            }
            Log.d("TEST", "Path to KML " + kmlDataPath);
            Track track = Track.fromStream(new FileInputStream(kmlDataPath));
            return track;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException ae){
            ae.printStackTrace();
        }
        return null;
    }

    private String createErrorMessage(Track track) {
        StringBuilder sb = new StringBuilder();
        List<LatLng> checkpoints = track.getCheckpoints();
        sb.append("Missing stations: ");
        for (int i = 0; i < checkpoints.size(); i++) {
            LatLng latLng = checkpoints.get(i);
            if (latLng == null){
            sb.append(i + ", ");
            }
        }
        sb.append(getLineSeparator());
        sb.append("Ignored placemarks: ");
        sb.append(getLineSeparator());
        for (KMLHandler.Placemark placemark : track.getIgnoredPlacemarks()) {
            sb.append(placemark);
            sb.append(getLineSeparator());
        }

        return sb.toString();
    }

}
