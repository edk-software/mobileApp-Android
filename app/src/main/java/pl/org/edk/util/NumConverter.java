package pl.org.edk.util;

import pl.org.edk.managers.LogManager;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NumConverter {
	private NumConverter(){}

	// Arabic -> roman =====================================================
	private static Map<Integer, String> arabic2roman = new HashMap<Integer, String>();
	private static Map<String, Integer> roman2arabic = new HashMap<String, Integer>();
	
	static{
		arabic2roman.put(1, "I");
		arabic2roman.put(2, "II");
		arabic2roman.put(3, "III");
		arabic2roman.put(4, "IV");
		arabic2roman.put(5, "V");
		arabic2roman.put(6, "VI");
		arabic2roman.put(7, "VII");
		arabic2roman.put(8, "VIII");
		arabic2roman.put(9, "IX");
		arabic2roman.put(10, "X");
		arabic2roman.put(11, "XI");
		arabic2roman.put(12, "XII");
		arabic2roman.put(13, "XIII");
		arabic2roman.put(14, "XIV");
		arabic2roman.put(15, "XV");

		roman2arabic.put("I", 1);
		roman2arabic.put("II", 2);
		roman2arabic.put("III", 3);
		roman2arabic.put("IV", 4);
		roman2arabic.put("V", 5);
		roman2arabic.put("VI", 6);
		roman2arabic.put("VII", 7);
		roman2arabic.put("VIII", 8);
		roman2arabic.put("IX", 9);
		roman2arabic.put("X", 10);
		roman2arabic.put("XI", 11);
		roman2arabic.put("XII", 12);
		roman2arabic.put("XIII", 13);
		roman2arabic.put("XIV", 14);
		roman2arabic.put("XV", 15);
	}
	
	public static int toArabic(String roman) {
		Integer result = roman2arabic.get(roman);
		return result == null ? 0 : result;
	}
	
	public static String toRoman(int arabic) {
		String result = arabic2roman.get(arabic);
		return result == null ? "" : result;
	}

	// Date -> String ======================================================
	private static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static SimpleDateFormat DATE_CONVERTER = new SimpleDateFormat(DATE_FORMAT);

	public static Date stringToDate(String string){
		try {
			return DATE_CONVERTER.parse(string, new ParsePosition(0));
		}catch (Exception e){
			LogManager.LogError("Date conversion error: " + e.getMessage());
			return null;
		}
	}

	public static String dateToString(Date date){
		try {
			return DATE_CONVERTER.format(date);
		}catch (Exception e){
			LogManager.LogError("Date conversion error: " + e.getMessage());
			return null;
		}
	}
}
