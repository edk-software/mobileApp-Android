package pl.org.edk.menu;

import java.text.MessageFormat;

public class TrackInfo {

	private int mTrackId;
	private String mCountyName;
	private String mCityName;
	private String mTrackName;
	
	private TrackInfo(String[] parts) {
		if (parts.length != 4){
			throw new IllegalArgumentException("Wrong number of parameters");
		}
		mTrackId = Integer.parseInt(parts[0]);
		mCountyName = parts[1];
		mCityName = parts[2];
		mTrackName = parts[3];
	}
	public int getTrackId() {
		return mTrackId;
	}
	public String getCountyName() {
		return mCountyName;
	}
	public String getCityName() {
		return mCityName;
	}
	public String getTrackName() {
		return mTrackName;
	}
	
	@Override
	public String toString() {
		return MessageFormat.format("{0},{1},{2},{3}", mTrackId, mCountyName,mCityName, mTrackName);
	}
	
	public static TrackInfo parse(String trackInfoString){
		String[] parts = trackInfoString.split(",");
		return new TrackInfo(parts);
	}
	
}
