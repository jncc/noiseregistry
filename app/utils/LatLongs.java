package utils;

public class LatLongs {
	public Double dLongitude;
	public Double dLatitude;
	public String sLongitude;
	public String sLatitude;
	
	public LatLongs(String colLat, String colLong)
	{
		sLatitude = colLat;
		sLongitude = colLong;
		
		dLatitude = Double.parseDouble(colLat);
		dLongitude = Double.parseDouble(colLong);
	}
}
