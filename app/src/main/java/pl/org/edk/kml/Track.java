package pl.org.edk.kml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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

import com.google.android.gms.maps.model.LatLng;

public class Track {

	private static final String STACJA = "STACJA";
	private static final String TAG = "EDK";
	private LatLng[] checkpoints = new LatLng[16];
	private List<LatLng> track = new ArrayList<LatLng>();

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
				attachToTrack(checkpoint);
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

		if (!remainingPlacemarks.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (Placemark placemark : remainingPlacemarks) {
				sb.append(System.getProperty("line.separator"));
				sb.append(placemark);
			}
			Log.w(TAG, "Possible duplicates of placemarks " + sb.toString());
			// return;
		}

		// if (checkpoints[0] == null && !remainingPlacemarks.isEmpty()) {
		// checkpoints[0] = remainingPlacemarks.remove(0).getPoints().get(0);
		// }

		if (track.indexOf(checkpoints[14]) < track.indexOf(checkpoints[1])) {
			Collections.reverse(track);
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

		if (upperCaseName.contains(STACJA)) {
			String[] parts = splitIntoParts(upperCaseName);
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

			int index = upperCaseName.indexOf(STACJA) + STACJA.length();
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
		return -1;
	}

    @NonNull
    private String[] splitIntoParts(String upperCaseName) {
        String[] parts = upperCaseName.split(" |,|\\.|_|-");
        List<String> partsList = new ArrayList<>();
        for(int i=0;i<parts.length;i++){
            String trimmed = parts[i].trim();
            if (!trimmed.isEmpty()){
            partsList.add(trimmed);}
        }
        return partsList.toArray(new String[partsList.size()]);
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
			if (part.equals(STACJA)) {
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

		for (LatLng checkpoint : checkpoints) {
			attachToTrack(checkpoint);
		}

	}

	private void attachToTrack(LatLng checkpoint) {
		if (track.contains(checkpoint)) {
			return;
		}

		Log.d("EDK", "attaching checkpoint to track");
		int index = getClosestIndex(checkpoint);
		if (isAfter(checkpoint, index)) {
			index++;
		}
		track.add(index, checkpoint);
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

	private int getClosestIndex(LatLng checkpoint) {
		double dist = Double.MAX_VALUE;
		int index = 0;
		for (int i = 0; i < track.size(); i++) {
			LatLng point = track.get(i);
			double currDist = distanceBetween(point, checkpoint);
			if (currDist < dist) {
				dist = currDist;
				index = i;
			}
		}
		return index;
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

	public boolean isProperlyInitialized() {
		for (LatLng latLng : checkpoints) {
			if (latLng == null) {
				return false;
			}
		}
		return true;
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
