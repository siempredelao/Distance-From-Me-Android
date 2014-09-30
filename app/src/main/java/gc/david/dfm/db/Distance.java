package gc.david.dfm.db;

/**
 * Distance class is our model and contains the data we will save in the
 * database and show in the user interface.
 * 
 * @author David
 * 
 */
public class Distance {
	private long id;
	private String name;
	private double lat_a;
	private double lon_a;
	private double lat_b;
	private double lon_b;
	private String distance;
	private String date;
	

	public Distance(long id, String nombre, double lat_a, double lon_a,
			double lat_b, double lon_b, String distancia,String fecha) {
		this.id = id;
		this.name = nombre;
		this.lat_a = lat_a;
		this.lon_a = lon_a;
		this.lat_b = lat_b;
		this.lon_b = lon_b;
		this.distance = distancia;
		this.date = fecha;
	}


	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public double getLat_a() {
		return lat_a;
	}
	
	public double getLon_a() {
		return lon_a;
	}

	public double getLat_b() {
		return lat_b;
	}

	public double getLon_b() {
		return lon_b;
	}

	public String getDistance() {
		return distance;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
}
