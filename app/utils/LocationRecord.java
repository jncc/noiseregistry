package utils;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import models.ActivityApplication;
import models.OilAndGasBlock;
import play.i18n.Messages;
import play.Logger;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class LocationRecord {
	protected String sOGB = null;
	protected String sPoly = null;
	protected LatLongs ll = null;
	protected List<LatLongs> liLL = null; 
	protected String sDates = null;
	protected String sType = "";
	protected List<String> liErrors = new ArrayList<String>(); 
	protected int iLine;
	protected List<Date> liDates = new ArrayList<Date>();
	protected boolean bDatesOK = false;
	
	public LocationRecord(HashMap<String, LocationRecord> hmPoly, String colOGB, String colLat, String colLng, 
			String colPoly, String colDates, int iLine, boolean bNeedsDates, ActivityApplication aa)
	{
		this.iLine = iLine;
		
		processDates(colDates,bNeedsDates, iLine, aa);
		
		if (colOGB != null && !colOGB.equals("")) // OGB should be only entry
		{
			processOGB(colOGB,colLat,colLng,colPoly,iLine,bNeedsDates);
		}
		else if (colPoly != null && !colPoly.equals("")) // Poly needs lat/lng
		{
			processPoly(hmPoly, colPoly, colLat, colLng, iLine);
		}
		else
		{
			processLatLng(colLat, colLng, iLine);
		}
	}
	public List<String> getErrors()
	{
		return liErrors;
	}
	public List<Date> getDates()
	{
		return liDates;
	}
	public List<LatLongs> getPolyLLs()
	{
		return liLL;
	}
	public String getPolyRef()
	{
		return sPoly;
	}
	public LatLongs getLatLongs()
	{
		return ll;
	}
	public String getOGBCode()
	{
		return sOGB;
	}
	public String getType()
	{
		return sType;
	}
	public boolean isValid()
	{
		if (sType.equals("Poly"))
		{
			if (liLL.size()<3)
			{
				liErrors.add(Messages.get("csverror.polytoofewpoints",sPoly));
				return false;
			}
			else
			{
				WKTReader wktReader = new WKTReader();
				try 
				{
					Geometry geom = wktReader.read(polyAsGeomString());
					if (geom.getGeometryType().equals("Polygon")) 
					{
						geom.setSRID(4326);
						if (!geom.isValid())
						{
							liErrors.add(Messages.get("csverror.invalidpoly",sPoly));
							return false;
						}
					}
				}
				catch (Exception e)
				{
					liErrors.add(Messages.get("csverror.invalidpoly",sPoly));
					return false;
				}
			}
		}
		if (!bDatesOK || sType.equals(""))
			return false;
		if (sType.equals("OGB") || sType.equals("LatLng") )
			return true;
		return false;
	}
    /**
     * Convert polygon lat / lng values string into WKT format polygon 
     * @return
     */	
    private String polyAsGeomString() 
    {
    	String sReturn = "POLYGON((";
    	
    	Iterator<LatLongs> itll = liLL.iterator();
    	String sPoints = "";
    	String sFirst = "";
    	String sLast = "";
    	while (itll.hasNext())
    	{
    		LatLongs ll = itll.next();
    		if (sPoints.equals(""))
    			sFirst = ll.sLatitude+" "+ll.sLongitude;
    		else
    			sPoints += ", ";
    		sLast = ll.sLatitude+" "+ll.sLongitude;
    		sPoints += sLast;
    	}
    	if (!sFirst.equals(sLast))
    		sPoints += ", "+sFirst;

    	sReturn += sPoints + "))";
    	
		return sReturn;
    }	
	protected void processLatLng(String colLat, String colLng, int iLine)
	{
		if (colLat==null || colLng==null || colLat.equals("") || colLng.equals(""))
		{
			liErrors.add(Messages.get("csverror.latlngmissing",Integer.toString(iLine)));			
		}
		else if (!isValidLatLng(colLat,colLng))
		{
			liErrors.add(Messages.get("csverror.latlngoutofbounds",Integer.toString(iLine)));
		}
		else
		{
			ll = new LatLongs(colLat,colLng);
			sType = "LatLng";
		}
	}
	public void addToPoly(LatLongs ll)
	{
		liLL.add(ll);
	}
	protected void processPoly(HashMap<String, LocationRecord> hmPoly, String colPoly, String colLat, String colLng, int iLine)
	{
		if (colLat==null || colLng==null || colLat.equals("") || colLng.equals(""))
		{
			liErrors.add(Messages.get("csverror.polynolatlng",Integer.toString(iLine)));
		}
		else if (!isValidLatLng(colLat,colLng))
		{
			liErrors.add(Messages.get("csverror.polyoutofbounds",Integer.toString(iLine)));
		}
		else
		{
			LatLongs ll = new LatLongs(colLat,colLng);
			LocationRecord lr = this;
			sType = "Poly";			
			if (hmPoly.containsKey(colPoly))
			{
				sType = "Discard";
				lr = hmPoly.get(colPoly);
			}
			else
			{
				liLL = new ArrayList<LatLongs>();
			}
			lr.addToPoly(ll);
			lr.setPolyRef(colPoly);
			
			hmPoly.put(colPoly,lr);
		}	
	}
	public String toString()
	{
		String s = "";
		if (!isValid())
			s += "INVALID ";
		s += sType + " ";
		s += Integer.toString(iLine);
		
		return s;
	}
	public void setPolyRef(String sRef)
	{
		sPoly = sRef;
	}
	protected void processOGB(String colOGB, String colLat, String colLng, String colPoly, int iLine, boolean bNeedsDates)
	{
		if ((colPoly!=null && !colPoly.equals("")) || 
			(colLat!=null && !colLat.equals("")) || 
			(colLng!=null && !colLng.equals("")))
		{
			liErrors.add(Messages.get("csverror.blockandlatlng",Integer.toString(iLine)));
		}
		else
		{
			List<OilAndGasBlock> logb = OilAndGasBlock.findByCode(colOGB);
			if (logb == null || logb.size()==0)
			{
				liErrors.add(Messages.get("csverror.blockinvalidcode",Integer.toString(iLine)));
			}
			else
			{
				sOGB = colOGB;
				sType = "OGB";
			}
		}	
	}
	/***
	 * This attempts to parse the date using a variety of common formats
	 * @param sDate - string to try to parse to a date
	 * @return the parsed date
	 */
	protected Date tryDates(String sDate)
	{
		Date d = null;
	    SimpleDateFormat parser[] = new SimpleDateFormat[12]; 

	    parser[0] = new SimpleDateFormat("dd/MM/yyyy");
	    parser[1] = new SimpleDateFormat("d/MM/yyyy");
	    parser[2] = new SimpleDateFormat("dd/MMM/yyyy");
	    parser[3] = new SimpleDateFormat("d/MMM/yyyy");
	    parser[4] = new SimpleDateFormat("dd/M/yyyy");
	    parser[5] = new SimpleDateFormat("d/M/yyyy");
	    parser[6] = new SimpleDateFormat("yyyy-MM-dd");
	    parser[7] = new SimpleDateFormat("yyyy-M-dd");
	    parser[8] = new SimpleDateFormat("yyyy-MMM-dd");
	    parser[9] = new SimpleDateFormat("yyyy-MM-d");
	    parser[10] = new SimpleDateFormat("yyyy-M-d");
	    parser[11] = new SimpleDateFormat("yyyy-MMM-d");
	    
	    for (int i = 0; i <= parser.length; i++)
	    {
	    	try
	    	{
	    		parser[i].setLenient(false);
	    		d = parser[i].parse(sDate);
	    	}
	    	catch (Exception e) {
	    		// date will not parse for most formats but should hopefully parse one
	    	}
	    	if (d!=null)
	    		return d;
	    }
	    return d;
	}
	protected void processDates(String colDates, boolean bNeedsDates, int iLine, ActivityApplication aa)
	{
		Date date = null;
		if (!bNeedsDates)
		{
			bDatesOK = true;
			return;
		}
		if (colDates == null || colDates.equals(""))
		{
			return; // will leave things at false
		}
		String[] sDates = colDates.split(",");
		for (int i = 0; i<sDates.length; i++)
		{
			String s = sDates[i].trim();
			date = tryDates(s); 
			if (date == null)
			{
				liErrors.add(Messages.get("csverror.noparsedates",Integer.toString(iLine)));
				return; // i.e. not OK dates
			}
			if (date.before(aa.getDate_start()) || date.after(aa.getDate_end()))
				liErrors.add(Messages.get("csverror.datesoutsiderange",Integer.toString(iLine)));
			else	
				liDates.add(date);
		}
		bDatesOK = true;
	}
	private static boolean isValidLatLng(String colLat,String colLng)
	{
		Float fLat;
		Float fLng;
		
		try {
			fLat = Float.parseFloat(colLat);
			fLng = Float.parseFloat(colLng);
			
			if (fLat<48.00 || fLat>64.00)
				return false;
			if (fLng<-24.00 || fLng>4.00)
				return false;
		}
		catch (Exception e)
		{
			// only care if lat long is valid - if exception then definitely not valid
			return false;
		}
		return true;
	}
}
