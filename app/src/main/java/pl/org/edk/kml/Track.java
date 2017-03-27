package pl.org.edk.kml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import pl.org.edk.kml.KMLHandler.Placemark;
import pl.org.edk.kml.KMLHandler.Placemarks;
import pl.org.edk.util.NumConverter;

import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;

public class Track {

    private static final String STATION = "STATION";
    private static final String STATION_SHORT = "ST";
    private static final String STATION_PL = "STACJA";
    private static final String TAG = "EDK";
    private LatLng[] checkpoints = new LatLng[16];
    private Integer[] checkpointIndexes = new Integer[16];
    private List<LatLng> track = new ArrayList<LatLng>();
    private static HashSet<String> introStationNames = new HashSet<>();
    private static HashSet<String> summaryStationNames = new HashSet<>();
    private List<Placemark> mIgnoredPlacemarks = new ArrayList<>();
    private Status mStatus = Status.Ok;

    public Track(Placemarks placemarks) {
        createTrack(placemarks.getTrackPlacemarks());
        if (track.size() == 0) {
            throw new IllegalArgumentException("Track was empty");
        }
        fillCheckpointsArray(placemarks.getSinglePlacemarks());
        // if (!track.containsAll(Arrays.asList(checkpoints))) {
        // attachCheckpointsToTrack();
        // }
        // orderCheckpoints();
    }

    static {
        introStationNames.add("WSTĘP");
        introStationNames.add("WPROWADZENIE");
        introStationNames.add("POCZĄTEK");
        introStationNames.add("START");
        introStationNames.add("ROZPOCZĘCIE");

        summaryStationNames.add("ZAKOŃCZENIE");
        summaryStationNames.add("KONIEC");
        summaryStationNames.add("PODSUMOWANIE");
    }

    // private void orderCheckpoints() {
    // List<Integer> checkpointIndexes = new ArrayList<Integer>();
    // for (LatLng checkpoint : checkpoints) {
    // checkpointIndexes.add(track.indexOf(checkpoint));
    // }
    // ListIndexComparator indexComparator = new
    // ListIndexComparator(checkpointIndexes);
    // Integer[] indexArray = indexComparator.createIndexArray();
    // Arrays.sort(indexArray, indexComparator);
    // List<LatLng> orderedCheckpoints = new ArrayList<LatLng>();
    // for (Integer index : indexArray) {
    // orderedCheckpoints.add(checkpoints.get(index));
    // }
    // checkpoints = orderedCheckpoints;
    // }

    public static Track fromStream(InputStream stream) {
        try {
            Placemarks placemarks = readKml(stream);

            return new Track(placemarks);

        } catch (ParserConfigurationException e) {
            Log.e(TAG, "Couldn't parse " + stream, e);
            return null;
        } catch (SAXException e) {
            Log.e(TAG, "Couldn't parse " + stream, e);
            return null;
        } catch (IOException e) {
            Log.e(TAG, "Couldn't parse " + stream, e);
            return null;
        }
    }

    private static Placemarks readKml(InputStream stream) throws ParserConfigurationException, SAXException,
            IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        KMLHandler dh = new KMLHandler();
        parser.parse(stream, dh);
        Placemarks placemarks = dh.getPlacemarks();
        return placemarks;

    }

    private void createTrack(List<Placemark> placemarks) {
        for (Placemark placemark : placemarks) {
            List<LatLng> points = placemark.getPoints();
            if (points.size() > 15) {
                track.addAll(points);
            }
        }
    }

    private void fillCheckpointsArray(List<Placemark> placemarks) {
        List<Placemark> remainingPlacemarks = new ArrayList<Placemark>();
        for (Placemark placemark : placemarks) {
            int index = getStationIndex(placemark);
            if (index != -1 && checkpoints[index] == null) {
                LatLng checkpoint = placemark.getPoints().get(0);
                checkpoints[index] = checkpoint;
            } else {
                remainingPlacemarks.add(placemark);
            }
        }
        // for (int i = 0; i < checkpoints.length; i++) {
        // LatLng station = checkpoints[i];
        // if (station == null && !remainingPlacemarks.isEmpty()){
        // checkpoints[i] = remainingPlacemarks.remove(0).getPoints().get(0);
        // }
        // }
        if(!hasAllCheckpoints()){
            mStatus = Status.StationsMissing;
        }
        attachCheckpointsToTrack();

        if (!remainingPlacemarks.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Placemark placemark : remainingPlacemarks) {
                sb.append(System.getProperty("line.separator"));
                sb.append(placemark);
                mIgnoredPlacemarks.add(placemark);

            }
            Log.w(TAG, "Possible duplicates of placemarks " + sb.toString());
            // return;
        }

        if (checkpoints[0] == null) {
            checkpoints[0] = track.get(0);
        }
        // if (checkpoints[15] == null && !remainingPlacemarks.isEmpty()) {
        // checkpoints[15] = remainingPlacemarks.remove(0).getPoints().get(0);
        // }
        if (checkpoints[15] == null) {
            checkpoints[15] = track.get(track.size() - 1);
        }
    }

    private int getStationIndex(Placemark placemark) {
        String name = placemark.getName();
        if (name == null) {
            return -1;
        }
        String upperCaseName = name.toUpperCase();
        int stationIndex = tryGetStationIndex(upperCaseName);
        if (stationIndex != -1) {
            return stationIndex;
        }

        stationIndex = tryGetStationIndex(upperCaseName.replace(".", ""));
        if (stationIndex != -1) {
            return stationIndex;
        }

        String[] parts = splitIntoParts(upperCaseName);
        if (upperCaseName.contains(STATION_PL)) {
            stationIndex = tryGetStationIndex(parts);
            if (stationIndex != -1) {
                return stationIndex;
            }

            int index = upperCaseName.indexOf(STATION_PL) + STATION_PL.length();
            String afterStation = upperCaseName.substring(index).trim();
            int nextSpace = afterStation.indexOf(' ');
            String stationIndexString = afterStation;
            if (nextSpace != -1) {
                stationIndexString = afterStation.substring(0, nextSpace);
            }
            stationIndex = tryGetStationIndex(stationIndexString);
            if (stationIndex != -1) {
                return stationIndex;
            }
        }

        if (introStationNames.contains(upperCaseName.trim())) {
            return 0;
        }
        if (summaryStationNames.contains(upperCaseName.trim())) {
            return 15;
        }
        if (upperCaseName.contains(STATION) || upperCaseName.contains(STATION_SHORT)) {
            stationIndex = tryGetStationIndex(parts);
            if (stationIndex != -1) {
                return stationIndex;
            }
        }
        if (parts.length > 0) {
            return tryGetStationIndex(parts[0]);
        }
        return -1;
    }

    private int tryGetStationIndex(String[] parts) {
        int stationIndex;
        int stationPartIndex = getStationPartIndex(parts);
        if (stationPartIndex != -1) {
            String partAfter = stationPartIndex < (parts.length - 1) ? parts[stationPartIndex + 1] : null;
            stationIndex = tryGetStationIndex(partAfter);
            if (stationIndex != -1) {
                return stationIndex;
            }
            String partBefore = stationPartIndex > 0 ? parts[stationPartIndex - 1] : null;
            stationIndex = tryGetStationIndex(partBefore);
            if (stationIndex != -1) {
                return stationIndex;
            }

            String lastPart = stationPartIndex != (parts.length - 1) ? parts[parts.length - 1] : null;
            stationIndex = tryGetStationIndex(lastPart);
            if (stationIndex != -1) {
                return stationIndex;
            }
        }
        return -1;
    }

    @NonNull
    private String[] splitIntoParts(String upperCaseName) {
        String withFixedSpaces = replaceNonBreakingSpaceWithNormal(upperCaseName);
        String[] parts = withFixedSpaces.split(" |,|\\.|_|-|–|:");
        List<String> partsList = new ArrayList<>();
        for (int i = 0; i < parts.length; i++) {
            String trimmed = parts[i].trim();
            if (!trimmed.isEmpty()) {
                partsList.add(trimmed);
            }
        }
        return partsList.toArray(new String[partsList.size()]);
    }

    private String replaceNonBreakingSpaceWithNormal(String upperCaseName) {
        return upperCaseName.replace(String.valueOf((char) 160), " ");
    }

    private int tryGetStationIndex(String stationIndexString) {
        if (stationIndexString == null || stationIndexString.isEmpty()) {
            return -1;
        }
        int arabic2 = NumConverter.toArabic(stationIndexString);
        if (arabic2 != 0) {
            return arabic2;
        }
        int parseInt = parseSafe(stationIndexString);
        if (parseInt > 0 && parseInt < 16) {
            return parseInt;
        }
        return -1;
    }

    private int getStationPartIndex(String[] parts) {
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.startsWith(STATION_PL) || part.startsWith(STATION) || part.startsWith(STATION_SHORT)) {
                return i;
            }
        }
        return -1;
    }

    private int parseSafe(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void attachCheckpointsToTrack() {
        attachCheckpointsToTrack(4, 11);
        List<Integer> list = Arrays.asList(checkpointIndexes);
        if(!areOrdered(list)){
            //reverts also the indexes in the original checkpointIndexes array
            Collections.reverse(list);
            if (areOrdered(list)){
                Collections.reverse(track);
            } else{
                mStatus = Status.OutOfOrder;
            }
        }
        attachCheckpointsToTrack(0, 3);
        attachCheckpointsToTrack(12, 15);
        list = Arrays.asList(checkpointIndexes);
        if(!areOrdered(list)){
            mStatus = Status.OutOfOrder;
        }
    }

    private void attachCheckpointsToTrack(int from, int to) {
        int previousCheckpointTrackIndex = 0;
        if(from > 0 && checkpointIndexes[from - 1] != null){
            previousCheckpointTrackIndex = checkpointIndexes[from - 1];
        }
        for (int i = from; i <= to; i++) {
            LatLng checkpoint = checkpoints[i];
            if (checkpoint == null) {
                continue;
            }
            previousCheckpointTrackIndex = attackCheckpointToTrack(checkpoint, previousCheckpointTrackIndex);
            checkpointIndexes[i] = previousCheckpointTrackIndex;
        }
    }

    private boolean areOrdered(List<Integer> checkpointIndexes) {
        int previous = -1;
        for (Integer checkpointIndex : checkpointIndexes) {
            if (checkpointIndex == null){
                continue;
            }
            if(previous > checkpointIndex){
                return false;
            }
            previous = checkpointIndex;
        }
        return true;
    }

    private int attackCheckpointToTrack(LatLng checkpoint, int startIndex) {
        Log.d("EDK", "attaching checkpoint to track");
        Pair<Integer, Boolean> pair = getClosestIndex(checkpoint, startIndex);
        if (!pair.second) {
            Pair<Integer, Boolean> closestIndex = getClosestIndex(checkpoint, 0);
            if (closestIndex.second) {
                pair = closestIndex;
            }
        }
        int index = pair.first;
        if (isAfter(checkpoint, index)) {
            index++;
        }
        track.add(index, checkpoint);
        return index;
    }

    private boolean isAfter(LatLng checkpoint, int index) {
        if (track.size() <= 2) {
            return false;
        }
        if (index < track.size() - 1) {
            LatLng closest = track.get(index);
            LatLng next = track.get(index + 1);
            if (distanceBetween(closest, next) > distanceBetween(checkpoint, next)) {
                return true;
            }
        } else {
            LatLng last = track.get(track.size() - 1);
            LatLng beforeLast = track.get(track.size() - 2);
            if (distanceBetween(last, beforeLast) < distanceBetween(checkpoint, beforeLast)) {
                return true;
            }
        }
        return false;
    }

    private Pair<Integer, Boolean> getClosestIndex(LatLng checkpoint, int startIndex) {
        double dist = Double.MAX_VALUE;
        int closeEnoughDist = 20;
        int locationsToCheckAfterCloseFound = 10;

        boolean closeEnoughFound = false;

        int index = 0;
        for (int i = startIndex; i < track.size(); i++) {
            if ((closeEnoughFound && i - index > locationsToCheckAfterCloseFound)) {
                return Pair.create(index, true);
            }
            LatLng point = track.get(i);
            double currDist = distanceBetween(point, checkpoint);
            if (currDist < dist) {
                dist = currDist;
                if (dist < closeEnoughDist) {
                    closeEnoughFound = true;
                }
                index = i;
            }
        }
        return Pair.create(index, closeEnoughFound);
    }

    private double distanceBetween(LatLng point, LatLng checkpoint) {
        float[] results = new float[3];
        Location.distanceBetween(point.latitude, point.longitude, checkpoint.latitude, checkpoint.longitude, results);
        return results[0];
    }

    private double distanceBetween(int index, LatLng location) {
        return distanceBetween(track.get(index), location);
    }

    private double distanceBetween(int firstIndex, int secondIndex) {
        return distanceBetween(track.get(firstIndex), track.get(secondIndex));
    }

    public List<LatLng> getTrackPoints() {
        return track;
    }

    public List<LatLng> getCheckpoints() {
        return Arrays.asList(checkpoints);
    }

    private boolean hasAllCheckpoints() {
        for (LatLng latLng : checkpoints) {
            if (latLng == null) {
                return false;
            }
        }
        return true;
    }

    public Status GetStatus() {
        return mStatus;
    }

    public enum Status {
        Ok, StationsMissing, OutOfOrder, Reversed
    }

    public List<Placemark> getIgnoredPlacemarks() {
        return mIgnoredPlacemarks;
    }

    public double calculateLength() {
        double length = 0.0;
        for (int i = 1; i < track.size(); i++) {
            length += distanceBetween(i - 1, i);
        }
        return length;
    }

    public class ListIndexComparator implements Comparator<Integer> {
        private final List<Integer> list;

        public ListIndexComparator(List<Integer> list) {
            this.list = list;
        }

        public Integer[] createIndexArray() {
            Integer[] indexes = new Integer[list.size()];
            for (int i = 0; i < list.size(); i++) {
                indexes[i] = i; // Autoboxing
            }
            return indexes;
        }

        @Override
        public int compare(Integer index1, Integer index2) {
            // Autounbox from Integer to int to use as array indexes
            return list.get(index1).compareTo(list.get(index2));
        }
    }

}
