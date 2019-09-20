package wbs.extras.util;

import java.time.Duration;
import java.time.LocalDateTime;

public class WbsTime {

	private static double roundTo(double number, int decimalPlaces) {
		return Math.round(number * (Math.pow(10, decimalPlaces)))/Math.pow(10, decimalPlaces);
	}
	
	public static String prettyTime(Duration duration) {
		String prettyTime = null;
		double inMillis = duration.toMillis();

		prettyTime = roundTo((inMillis % 60000)/1000, 2) + " seconds";
		if (inMillis > 60000) {
			int minutes = ((int) inMillis/60000);
			if (minutes == 1 || minutes == 0) {
				prettyTime = minutes + " minute and " + prettyTime;
			} else {
				prettyTime = minutes + " minutes and " + prettyTime;
			}
		}
		
		return prettyTime;
	}
	
	public static String prettyTime(LocalDateTime timeStamp) {
		String prettyTime = null;
		
		int year = timeStamp.getYear();
		int month = timeStamp.getMonthValue();
		int dayOfMonth = timeStamp.getDayOfMonth();
		int hour = timeStamp.getHour();
		String twelveHourDenominator;
		if (hour % 12 != hour) { // PM
			hour = hour - 12;
			twelveHourDenominator = "PM";
		} else {
			twelveHourDenominator = "AM";
		}
		if (hour == 0) {
			hour = 12;
		}
		
		int minute = timeStamp.getMinute();
		String minuteString = minute + "";
		if (minuteString.length() == 1) {
			minuteString = "0" + minuteString;
		}
		
		prettyTime =  dayOfMonth + "/" + month + "/" + year + " " + hour + ":" + minuteString + " " + twelveHourDenominator;
		
		return prettyTime;
	}
}
