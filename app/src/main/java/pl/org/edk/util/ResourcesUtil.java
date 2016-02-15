package pl.org.edk.util;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import pl.org.edk.R;
import pl.org.edk.Settings;
import pl.org.edk.menu.TrackInfo;
import android.content.Context;
import android.util.Log;

public class ResourcesUtil {

//	public static String[] getRegions(Context context) {
//		return context.getResources().getStringArray(R.array.regions);
//	}

	public static List<String> getCounties(Context context) {
		HashSet<String> countiesSet = new HashSet<String>();
		for (TrackInfo trackInfo : getTrackInfos(context)) {
			countiesSet.add(trackInfo.getCountyName());
		}
		ArrayList<String> distinctCounties = new ArrayList<String>(countiesSet);
		Collections.sort(distinctCounties, Collator.getInstance(new Locale("pl", "PL")));
		return distinctCounties;
	}

	public static List<String> getCities(Context context) {
		String countyName = Settings.get(context).get(Settings.TERRITORY_NAME);
		HashSet<String> citiesSet = new HashSet<String>();
		for (TrackInfo trackInfo : getTrackInfos(context)) {
			if (!countyName.equals(trackInfo.getCountyName())) {
				continue;
			}
			citiesSet.add(trackInfo.getCityName());
		}
		ArrayList<String> distinctCities = new ArrayList<String>(citiesSet);
		Collections.sort(distinctCities, Collator.getInstance(new Locale("pl", "PL")));
		return distinctCities;

	}

	public static List<String> getTracks(Context context) {
		String countyName = Settings.get(context).get(Settings.TERRITORY_NAME);
		String cityName = Settings.get(context).get(Settings.AREA_ID);
		HashSet<String> tracksSet = new HashSet<String>();
		for (TrackInfo trackInfo : getTrackInfos(context)) {
			if (!countyName.equals(trackInfo.getCountyName())) {
				continue;
			}
			if (!cityName.equals(trackInfo.getCityName())) {
				continue;
			}
			tracksSet.add(trackInfo.getTrackName());
		}
		ArrayList<String> distinctCities = new ArrayList<String>(tracksSet);
		Collections.sort(distinctCities, Collator.getInstance(new Locale("pl", "PL")));
		return distinctCities;

	}

	public static int getTrackId(Context context) {
		Settings settings = Settings.get(context);
		String countyName = settings.get(Settings.TERRITORY_NAME);
		String cityName = settings.get(Settings.AREA_ID);
		String trackName = settings.get(Settings.TRACK_NAME);
		for (TrackInfo trackInfo : getTrackInfos(context)) {
			if (!countyName.equals(trackInfo.getCountyName())) {
				continue;
			}
			if (!cityName.equals(trackInfo.getCityName())) {
				continue;
			}
			if (trackName.equals(trackInfo.getTrackName())) {
				return trackInfo.getTrackId();
			}
		}

		return -1;
	}

	private static List<TrackInfo> getTrackInfos(Context context) {
		// long start = System.currentTimeMillis();
		String[] infoStrings = context.getResources().getStringArray(R.array.tracks);
		List<TrackInfo> infos = new ArrayList<TrackInfo>();
		for (String infoString : infoStrings) {
			infos.add(TrackInfo.parse(infoString));
		}

		try {
			String[] files = context.getAssets().list("tracks");
			Set<Integer> tracksWithKMLs = new HashSet<Integer>();
			for (String file : files) {
				if (!file.endsWith(".kml")) {
					continue;
				}
				int start = file.lastIndexOf('-') + 1;
				int end = file.indexOf('.');
				String trackId = file.substring(start, end);
				tracksWithKMLs.add(Integer.parseInt(trackId));
			}
			List<TrackInfo> validInfos = new ArrayList<TrackInfo>();
			for (TrackInfo trackInfo : infos) {
				if (tracksWithKMLs.contains(trackInfo.getTrackId())) {
					validInfos.add(trackInfo);
				}
			}

			return validInfos;

		} catch (IOException e) {
			Log.e("EDK", "Error while checking assets folder, no track infos filtered", e);
			return infos;
		}

		// long end = System.currentTimeMillis();
		// Log.d("EDK", "Time to parse track infos " + (end-start));
		// return infos;
	}

	public static TrackInfo getTrackInfo(Context context) {
		Settings settings = Settings.get(context);
		String countyName = settings.get(Settings.TERRITORY_NAME);
		String cityName = settings.get(Settings.AREA_ID);
		String trackName = settings.get(Settings.TRACK_NAME);
		for (TrackInfo trackInfo : getTrackInfos(context)) {
			if (!countyName.equals(trackInfo.getCountyName())) {
				continue;
			}
			if (!cityName.equals(trackInfo.getCityName())) {
				continue;
			}
			if (trackName.equals(trackInfo.getTrackName())) {
				return trackInfo;
			}
		}
		return null;
	}
}
