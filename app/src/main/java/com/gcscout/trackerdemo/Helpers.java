package com.gcscout.trackerdemo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.location.Location;

public class Helpers {
	public static String addWordEnding(String word, long value) {
		word = word.substring(0, word.length() - 2);
		if (value % 100 > 10 && value % 100 < 20)
			return word + "ий";
		if (value % 10 == 1)
			return word + "ие";
		if (value % 10 > 1 && value % 10 < 5)
			return word + "ия";
		return word + "ий";
	}

	public static String addVerbWordEnding(String word, long value) {
		word = word.substring(0, word.length() - 2);
		if (value % 100 > 10 && value % 100 < 20)
			return word + "ют";
		if (value % 10 == 1)
			return word + "ет";
		return word + "ют";
	}

	public static String msToMinAndSec(long time) {
		if (time == 0)
			return "0с";
		long sec = time / 1000 % 60;// + (float) (time % 1000) / 1000;
		long min = time / 1000 / 60;

		return (min > 0 ? min + " м " : "") + (sec > 0 ? sec + " с" : "");
	}

	public static String dateToString(long dateMillis) {
		return new SimpleDateFormat("dd.MM.yyyy     HH:mm:ss", Locale.US).format(new Date(dateMillis));
	}

	public static boolean isStringValid(String value) {
		return !(value == null || value.length() == 0 || value.length() > 255);
	}

	public static String locationToString(Location location) {
		return "(" + location.getLatitude() + ";" + location.getLongitude() + ")" + " S=" + location.getSpeed() + " C=" + location
				.getBearing() + " A=" + location.getAccuracy();
	}
}
