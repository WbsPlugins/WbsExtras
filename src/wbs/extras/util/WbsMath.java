package wbs.extras.util;

public class WbsMath {

	public static double roundTo(double number, int decimalPlaces) {
		return Math.round(number * (Math.pow(10, decimalPlaces)))/Math.pow(10, decimalPlaces);
	}
}
