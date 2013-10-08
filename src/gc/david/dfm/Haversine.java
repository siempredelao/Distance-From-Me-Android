package gc.david.dfm;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Haversine {
	
	// Radio de la Tierra
	private static double RADIUS = 6371000;
	
	public static native double getDistanceJNI(double lat_a, double lon_a, double lat_b, double lon_b);
	
	// Calcula la distancia entre dos puntos, en metros
	public static double getDistance(double lat_a, double lon_a, double lat_b, double lon_b){
		double lat1 = Math.toRadians(lat_a);
		double lat2 = Math.toRadians(lat_b);
		double lon1 = Math.toRadians(lon_a);
		double lon2 = Math.toRadians(lon_b);
		
		double dLat = lat2-lat1;
		double dLon = lon2-lon1;
		
		double sinlat = Math.sin(dLat / 2);
		double sinlon = Math.sin(dLon / 2);
		
		double a = (sinlat * sinlat) + Math.cos(lat1)*Math.cos(lat2)*(sinlon*sinlon);
		double c = 2 * Math.asin (Math.min(1.0, Math.sqrt(a)));
		
		return RADIUS * c ;
	}
	
	private static double daleFormato(double d){
		// Estudiar la posibilidad de diferenciar la cantidad atendiendo al Locale
		DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
		simbolos.setDecimalSeparator('.');
		DecimalFormat formatter = new DecimalFormat("#####0.00000", simbolos);
		String miNumero = formatter.format(d);
		
		return Double.valueOf(miNumero); 
	}
	
	// Normaliza la distancia a su correspondiente magnitud y con dos dígitos
	// decimales. Diferencia entre el sistema anglosajón y el no-anglosajón
	public static String normalize(double distance, Locale locale){
		
		String resultado;

		double measure;
		
		DecimalFormat formatter = new DecimalFormat("###,##0.00");
		
		String unidadMedida = "km";
		if (	locale.equals(Locale.CANADA) ||
				locale.equals(Locale.CHINA) ||
				locale.equals(Locale.JAPAN) ||
				locale.equals(Locale.KOREA) ||
				locale.equals(Locale.TAIWAN) ||
				locale.equals(Locale.UK) ||
				locale.equals(Locale.US)){
			unidadMedida = "mi";
			formatter = new DecimalFormat("###,##0.00");
			
			if (distance >= 1609.344) // Hay al menos una milla
				measure = distance / 1609.344;
			else { // No llega a una milla, medimos en yardas
				unidadMedida = "yd";
				measure = distance * 1.093613298337708;
			}
		} else {
			if (distance >= 1000.0) // Hay al menos un kilómetro
				measure = distance / 1000;
			else { // No llega a un kilómetro, medimos en metros
				unidadMedida = "m";
				measure = distance;
			}
		}
		resultado = formatter.format(measure);
		return resultado + " " + unidadMedida/* + (measure>1 ? "s" : "")*/;
	}
	
	static {
		System.loadLibrary("gc_david_dfm_Haversine");
	}
}
