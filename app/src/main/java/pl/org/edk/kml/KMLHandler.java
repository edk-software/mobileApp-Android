package pl.org.edk.kml;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.android.gms.maps.model.LatLng;

public class KMLHandler extends DefaultHandler {

	private static final String NAME = "name";
	private static final String PLACEMARK = "Placemark";
	private static final String COORDINATES = "coordinates";
	private static final String GX_COORD = "gx:coord";
	private Placemark currentPlacemark = null;
	private List<Placemark> placemarks = new ArrayList<Placemark>();
	private List<Placemark> singlePlacemarks = new ArrayList<Placemark>();

	private StringBuilder nameSb = null;
	private StringBuilder coordinatesSb = null;
	private StringBuilder coordSb = null;

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equals(PLACEMARK)) {
			currentPlacemark = new Placemark();
		} else if (qName.equals(NAME)) {
			nameSb = new StringBuilder();
		} else if (qName.equals(COORDINATES)) {
			coordinatesSb = new StringBuilder();
		} else if (qName.equals(GX_COORD)){
			coordSb = new StringBuilder();
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (currentPlacemark != null && nameSb != null) {
			for (int i = start; i < start + length; i++) {
				nameSb.append(ch[i]);
			}
		}
		if (currentPlacemark != null && coordinatesSb != null) {
			for (int i = start; i < start + length; i++) {
				coordinatesSb.append(ch[i]);
			}
		}
		if (currentPlacemark != null && coordSb != null) {
			for (int i = start; i < start + length; i++) {
				coordSb.append(ch[i]);
			}
		}

	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equals(PLACEMARK)) {
			if (currentPlacemark.isSinglePoint()) {
				singlePlacemarks.add(currentPlacemark);
			} else {
				placemarks.add(currentPlacemark);
			}
			currentPlacemark = null;
		} else if (qName.equals(NAME) && currentPlacemark != null && nameSb != null) {
			currentPlacemark.setName(nameSb.toString());
			nameSb = null;
		} else if (qName.equals(COORDINATES) && currentPlacemark != null && coordinatesSb != null) {
			currentPlacemark.addAll(readAllPoints(coordinatesSb.toString()));
			coordinatesSb = null;
		} else if (qName.equals(GX_COORD)){
			currentPlacemark.add(readPoint(coordSb.toString(), " "));
			coordSb = null;
		}
	}

	private List<LatLng> readAllPoints(String allCoordinates) {
		List<LatLng> result = new ArrayList<LatLng>();
		String[] split = allCoordinates.split("\\s");
		for (String coordinates : split) {
			if (coordinates.trim().length() == 0) {
				continue;
			}
			LatLng gpsPoint = readPoint(coordinates);
			if (gpsPoint != null) {
				result.add(gpsPoint);
			}
		}
		return result;
	}

	private LatLng readPoint(String coordinates) {
		return readPoint(coordinates, ",");
	}

	private LatLng readPoint(String coordinates, String separator) {
		String[] split = coordinates.split(separator);
		if (split.length < 2) {
			return null;
		}
		double longitude = Double.parseDouble(split[0]);
		double latitude = Double.parseDouble(split[1]);
		return new LatLng(latitude, longitude);
	}

	public Placemarks getPlacemarks() {
		return new Placemarks();
	}
	
	public class Placemarks {
		public List<Placemark> getSinglePlacemarks(){
			return singlePlacemarks;
		}
		public List<Placemark> getTrackPlacemarks(){
			return placemarks;
		}
	}

	public class Placemark {
		List<LatLng> points = new ArrayList<LatLng>();
		String name = null;

		public void addAll(List<LatLng> points) {
			Track.addAllToTrack(this.points, points);
		}

		public void add(LatLng point) {
			points.add(point);
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public boolean isSinglePoint() {
			return points.size() == 1;
		}

		public List<LatLng> getPoints() {
			return points;
		}

		@Override
		public String toString() {
			return name + "\n" + points.toString();
		}
	}

}
