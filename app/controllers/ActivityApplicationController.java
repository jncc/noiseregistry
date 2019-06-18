package controllers;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import play.Logger;
import play.data.Form;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.twirl.api.Html;
import play.libs.Json;
import utils.*;
import views.html.*;
import models.*;
import play.data.validation.*;

import org.apache.commons.csv.*;

import com.typesafe.config.*;

@Security.Authenticated(SecuredController.class)
@Api(value = "/aas", description = "Operations on Activity Applications (aas)")
public class ActivityApplicationController extends Controller {
	
	static FormLimitless<ActivityApplication> appForm = new FormLimitless<ActivityApplication>(ActivityApplication.class);
	static FormLimitless<ActivityApplicationCloseOut> appcloseoutForm = new FormLimitless<ActivityApplicationCloseOut>(ActivityApplicationCloseOut.class);
	
	/**
	 * List of applications
	 * @return page with list of applications
	 */
	@Transactional(readOnly=true)
	public static Result index()
	{
		session("lastview","Proposed");

		AppUser au = AppUser.getSystemUser(request().username());
	    
		String status="Proposed,Interim Close-out";
		List<String> statusVals = Arrays.asList(status.split(","));
		
		Comparator<ActivityApplication> comparator;
		if (au.isRegulatorMember()) {
			comparator = new ActivityApplication.DateDueComparator();
		} else {
			comparator = new ActivityApplication.NoiseProducerComparator();
		}
		
		if (request().accepts("text/html")) {
			//The default list is for noise producer users...  to handle regulators we need a different sort order...
			return ok(activities.render(au, au.findApplicationsByStatus(status, comparator), statusVals));
		} else if (request().accepts("application/json")) {
			return ok(Json.toJson(au.findApplicationsByStatus(status, comparator)));
		} else {
			return badRequest("Unsupported content type");
		}
	}
	
	/**
	 * List of drafts
	 * @return page with list of drafts
	 */
	@Transactional(readOnly=true)
	public static Result findDraft()
	{
		session("lastview","Draft");
		AppUser au = AppUser.getSystemUser(request().username());
		if (au.isRegulatorMember()) {
			String activeTab="HOME";
	        return status(403,index.render(au, activeTab));
		}
		String status="Draft";
		List<String> statusVals = Arrays.asList(status.split(","));
		
		Comparator<ActivityApplication> comparator;
		comparator = new ActivityApplication.NoiseProducerComparator();
		if (request().accepts("text/html")) {
			return ok(activities.render(au, au.findApplicationsByStatus(status, comparator), statusVals));
		} else if (request().accepts("application/json")) {
			return ok(Json.toJson(au.findApplicationsByStatus(status, comparator)));
		} else {
			return badRequest("Unsupported content type");
		}
	}
	
	/**
	 * List of completed applications
	 * @return page with list of completed applications
	 */
	@Transactional(readOnly=true)
	public static Result findCompleted()
	{
		session("lastview","Completed");

		AppUser au = AppUser.getSystemUser(request().username());
		
		String status="Closed,Cancelled";
		List<String> statusVals = Arrays.asList(status.split(","));
		Comparator<ActivityApplication> comparator;
		if (au.isRegulatorMember()) {
			comparator = new ActivityApplication.DateClosedComparator();
		} else {
			comparator = new ActivityApplication.NoiseProducerComparator();
		}
		if (request().accepts("text/html")) {
			return ok(activities.render(au, au.findApplicationsByStatus(status, comparator), statusVals));
		} else if (request().accepts("application/json")) {
			return ok(Json.toJson(au.findApplicationsByStatus(status, comparator)));
		} else {
			return badRequest("Unsupported content type");
		}
	}

	/**
	 * List of incomplete applications
	 * @return page with list of incomplete applications (including draft)
	 */
	@Transactional(readOnly=true)
	public static Result findIncomplete()
	{
		session("lastview","Incomplete");

		AppUser au = AppUser.getSystemUser(request().username());
		
		String status="Draft,Proposed,Interim Close-out";
		List<String> statusVals = Arrays.asList(status.split(","));
		Comparator<ActivityApplication> comparator;
		if (au.isRegulatorMember()) {
			comparator = new ActivityApplication.DateClosedComparator();
		} else {
			comparator = new ActivityApplication.NoiseProducerComparator();
		}
		if (request().accepts("text/html")) {
			return ok(activities.render(au, au.findApplicationsByStatus(status, comparator), statusVals));
		} else if (request().accepts("application/json")) {
			return ok(Json.toJson(au.findApplicationsByStatus(status, comparator)));
		} else {
			return badRequest("Unsupported content type");
		}
	}

	/**
	 * Finds all applications with the given status that are allowed to be seen by the user
	 * @param status
	 * @return List of applications with the given status
	 */
	@Transactional(readOnly=true)
	@ApiOperation(value = "Finds Activity Applications by Status",
            notes = "Returns all Activity Applications with a Status value matching the parameter.  Multiple comma-separated status can be used.",
            nickname = "findByStatus",
            response = ActivityApplication.class,
            responseContainer = "List", httpMethod = "GET")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid status value")})
	public static Result findByStatus(
			@ApiParam(value = "Status value for filter", 
				required = true, 
				defaultValue = "Proposed", 
				allowableValues = "Proposed,Cancelled,Interim Close-out,Closed,Deleted", allowMultiple = true) 
			@QueryParam("status") String status)
	{
		AppUser au = AppUser.getSystemUser(request().username());

		List<String> statusVals = Arrays.asList(status.split(","));
		
		if (request().accepts("text/html")) {
			return ok(activities.render(au, au.findApplicationsByStatus(status, null), statusVals));
		} else if (request().accepts("application/json")) {
			return ok(Json.toJson(au.findApplicationsByStatus(status, null)));
		} else {
			return badRequest("Unsupported content type");
		}
	}
	
	/**
	 * Shows the UI for entering a new application
	 * @return Page allowing entry of new application
	 */
	@Transactional(readOnly=true)
	public static Result add()
	{
		AppUser au = AppUser.getSystemUser(request().username());
		
		if (au.getOrgRole().isEmpty()) {
			return unauthorized(views.html.errors.unauthorised.render(au, "HOME"));
		} else {
			if (request().accepts("text/html")) {
				//Prevent caching the form (doesn't work for the current version of Chrome v40.0)
				response().setHeader(CACHE_CONTROL, "no-cache, no-store, must-revalidate");
				response().setHeader(PRAGMA, "no-cache, no-store");
				response().setHeader(EXPIRES, "0");
				
				return ok(activityform.render(au, appForm, Messages.get("activityform.title_new"), null, null));
			} else {
				return badRequest("Unsupported content type");
			}
		}
	}

	/**
	 * Starts the CSV upload process for activities
	 * @param lid - id of the activity
	 * @return UI for the upload
	 */
	@Transactional(readOnly=true)
	public static Result upload(Long lid)
	{
		AppUser au = AppUser.getSystemUser(request().username());
		ActivityApplication aa = JPA.em().find(ActivityApplication.class, lid);
		
		// If the activity id does not exist in the database OR the user has no right to
		// modify activity OR the activity is in an end state (Closed | Cancelled | 
		// Deleted | IsEmpty) then return an unauthorised message 
		if (aa == null || aa.getStatus().equals("Closed") || 
				aa.getStatus().equals("Cancelled") || aa.getStatus().equals("Deleted") || 
				aa.getStatus().isEmpty() || !userHasRightsForActivity(au, aa)) {
			return generateUnauthorized(au);	
		}

		return ok(upload.render(au, "", null, null, null, null, null, lid, null, aa));
	}

	/**
	 * Downloads a list of activities requiring close out
	 */
	@Transactional(readOnly=false)
	public static Result downloadAppsRequiringCloseOut()
	{
		AppUser au = AppUser.getSystemUser(request().username());
		
		if (au.getOrgRole() == "OVERALL_ADMIN") {
			StringBuffer sb = new StringBuffer();
			try {
		        CSVFormat csvFileFormat = CSVFormat.DEFAULT;
		
		        CSVPrinter csvFilePrinter = new CSVPrinter(sb, csvFileFormat);
		
		        csvFilePrinter.printRecord(ActivityApplication.getCSVHeader());        
		        List<ActivityApplication> liaa = getAppsRequiringCloseOut(); 
		        Iterator<ActivityApplication> it = liaa.iterator();
		        while (it.hasNext())
		            csvFilePrinter.printRecord(it.next().getCSVRecord());        
		        
		        csvFilePrinter.close();
		
				response().setContentType("application/csv; charset=iso-8859-1");
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
				String sYYYYMMDD = simpleDateFormat.format(new Date());
				
				response().setHeader("content-disposition", "attachment; filename=\"MNR_closeout_"+sYYYYMMDD+".csv\"");
				return ok(new ByteArrayInputStream(sb.toString().getBytes("UTF-8")));
			} catch (Exception e) {
				Logger.error("Error downloading apps requiring closeout: "+e.toString());
			}
		
			return badRequest("Something bad happened");
		} else {
			return unauthorized(views.html.errors.unauthorised.render(au, "HOME"));
		}
	}

	/**
	 * Play isn't very good at deleting temporary files so we move the file to another directory
	 * and then run the tidy files to try to delete it
	 * @param in - the reader used to read the file
	 * @param file - the File
	 * @return List of applications with the given status
	 */
	private static void tidyFiles(Reader in, File file)
	{
		try {
			if (in!=null)
				in.close();
			if (file!=null)
				file.deleteOnExit();
		} catch (Exception e)
		{
			// don't worry just trying to tidy up
			Logger.info("Error tidying files. These should be tidied by scheduled task anyway: "+e.toString());
		}
	}

	/**
	 * For the CSV upload this checks whether the required columns are there and different from each other
	 * @return false if there is a problem
	 */
	private static boolean checkCols(int iOGB, int iPoly, int iLat, int iLng, int iDates)
	{
		HashMap<Integer, Boolean> hmCols = new HashMap<Integer, Boolean>();

		if (iOGB>=0)
			hmCols.put(new Integer(iOGB), new Boolean(true));
		if (iPoly>=0)
		{
			if (hmCols.containsKey(new Integer(iPoly)))
				return false;
			hmCols.put(new Integer(iPoly), new Boolean(true));
		}
		if (iLat>=0)
		{
			if (hmCols.containsKey(new Integer(iLat)))
				return false;
			hmCols.put(new Integer(iLat), new Boolean(true));
		}
		if (iLng>=0)
		{
			if (hmCols.containsKey(new Integer(iLng)))
				return false;
			hmCols.put(new Integer(iLng), new Boolean(true));
		}
		if (iDates>=0)
		{
			if (hmCols.containsKey(new Integer(iDates)))
				return false;
		}
		
		return true;
	}

	/**
	 * Copies the locations from a parent activity to child activity
	 * @param aa - the ActivityApplication to copy locations from its parent
	 */
	public static void copyLocations(ActivityApplication aa) 
	{
		ActivityApplication aaPar = null;
		
		aaPar = aa.getParent(); 
		if (aaPar==null)
		{
			return;
		}
		aaPar = JPA.em().find(ActivityApplication.class, aaPar.getId()); // needed to get the locations 
		if (aaPar.getActivitylocations() == null)
		{
			return;
		}
		
		Iterator<ActivityLocation> it = aaPar.getActivitylocations().iterator();
		while (it.hasNext())
		{
			ActivityLocation al = it.next();
			ActivityLocation alNew = new ActivityLocation(al);
			alNew.setAa(aa);
			aa.getActivitylocations().add(alNew);
		}		
	}

	/**
	 * Processing for the CSV file upload (maximum file upload size specified in annotation)
	 * @param lid - the id of the application
	 */
	@Transactional(readOnly=false)
	@BodyParser.Of(value=BodyParser.MultipartFormData.class, maxLength=2048000)
	public static Result uploadfile(Long lid) 
	{
		AppUser au = AppUser.getSystemUser(request().username());
		ActivityApplication aa = JPA.em().find(ActivityApplication.class, lid);
		
		// If the activity id does not exist in the database OR the user has no right to
		// modify activity OR the activity is in an end state (Closed | Cancelled | 
		// Deleted | IsEmpty) then return an unauthorised message 
		if (aa == null || aa.getStatus().equals("Closed") || 
				aa.getStatus().equals("Cancelled") || aa.getStatus().equals("Deleted") || 
				aa.getStatus().isEmpty() || !userHasRightsForActivity(au, aa)) {
			return generateUnauthorized(au);	
		}

		HashMap<String,String> hmHeadersChosen = new HashMap<String,String>();
		HashMap<Integer,String> hmHeaders = new HashMap<Integer,String>();
        Map<String,String[]> multipartFormData = new HashMap<String,String[]>();
		HashMap<String, LocationRecord> hmPoly = new HashMap<String, LocationRecord>();
		List<LocationRecord> liLR = new ArrayList<LocationRecord>();
		String fileName = null;
		File file = null;
		List<String> liErrors = new ArrayList<String>();
		int iOGB = -1;
		int iPoly = -1;
		int iLat = -1;
		int iLng = -1;
		int iDates = -1;
		
		String sReplace = "";

		if (play.mvc.Controller.request().body().asMultipartFormData() == null)
		{
			liErrors.add(Messages.get("csverror.filetoolarge"));
			return ok(upload.render(au, "", liErrors, null, null, null, null, lid, null, aa));
		}

        multipartFormData = play.mvc.Controller.request().body().asMultipartFormData().asFormUrlEncoded();
        for(String key: multipartFormData.keySet()) 
        {
            String[] value = multipartFormData.get(key);
            if(value.length > 0) 
            {
             	hmHeadersChosen.put(key, value[0]);
                if (key.equals("latcol"))
                  	iLat = Integer.parseInt(value[0]);
                if (key.equals("longcol"))
                 	iLng = Integer.parseInt(value[0]);
                if (key.equals("ogbcol"))
                 	iOGB = Integer.parseInt(value[0]);
                if (key.equals("polycol"))
                	iPoly = Integer.parseInt(value[0]);
                if (key.equals("datescol"))
                	iDates = Integer.parseInt(value[0]);
                 if (key.equals("replace"))
                 	sReplace = value[0];
            }
        }
        
		if (aa.getStatus().equals("Draft") && sReplace.equals(""))
		{
			liErrors.add(Messages.get("csverror.choosereplace"));
			return ok(upload.render(au, "", liErrors, null, null, null, null, lid, null, aa));
		}

        boolean bHasHeader;
        if (aa.getStatus().equals("Draft"))
        	bHasHeader = (iLat>=0 && iLng>=0) || iOGB>=0;
        else // need dates
        	bHasHeader = ((iLat>=0 && iLng>=0) || iOGB>=0) & iDates >=0;
        	        
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart csv = body.getFile("csvfile");
		if (csv != null)
		{		
			fileName = csv.getFilename();
		    String contentType = csv.getContentType();
		    file = csv.getFile();

	    	String sStoreFiles = ConfigFactory.load().getString("upload.path");
	    	String sPathSep = ConfigFactory.load().getString("upload.pathseparator");
	    	if (sStoreFiles!=null && !sStoreFiles.equals(""))
	    	{
	    		String sNewPath = sStoreFiles+sPathSep+file.getName();
	    		if (file.renameTo(new File(sNewPath)))
	    		{
	    			file = new File(sNewPath);
	    		}
	    	}
	    	if (!fileName.toLowerCase().endsWith(".csv"))
	    	{
				tidyFiles(null, file);
				liErrors.add(Messages.get("csverror.mustendcsv"));
				return ok(upload.render(au, "", liErrors, null, null, null, null, lid, null, aa));
	    	}
		}
		else
		{
			if (hmHeadersChosen.containsKey("filename"))
			{
				file = new File(hmHeadersChosen.get("filename"));
			}
			else
			{
				liErrors.add("Can't find file");
				return ok(upload.render(au, "", liErrors, null, null, null, null, lid, null, aa));
			}
		}
			
	    fileName = file.getAbsolutePath();

	    Reader in=null;
		try {
			in = new FileReader(file);
		} catch(Exception e1)
		{
			// dunno what we do at moment
			Logger.error("Can't get filereader: "+e1.toString());
			tidyFiles(in, file);
			liErrors.add(e1.toString());
			return ok(upload.render(au, "", liErrors, null, null, null, null, lid, null, aa));
		}
		CSVParser csvparser = null;
		Iterable<CSVRecord> records = null;
		boolean bParsed = false;
			
		try {
			csvparser = CSVFormat.EXCEL.parse(in);
			bParsed = true;
		}
		catch (Exception e2) {
			Logger.debug("CSV doesn't parse as Excel - not an issue as may be ina number of formats");
		}
			
		try {
			if (!bParsed) {
				csvparser = CSVFormat.RFC4180.parse(in);
				bParsed = true;
			}
		}
		catch (Exception e3) {
			Logger.debug("CSV doesn't parse as RFC4180 - not an issue as may be ina number of formats");			
		}
			
		try {
			if (!bParsed) {
				csvparser = CSVFormat.MYSQL.parse(in);
				bParsed = true;
			}
		}
		catch (Exception e4) {
			Logger.debug("CSV doesn't parse as MySQL - not an issue as may be ina number of formats");
		}
			
		List<CSVRecord> licsvr = null;
		try {
			licsvr = csvparser.getRecords();
		} catch (Exception e5)
		{
			Logger.info("Couldn't parse CSV file:"+e5.toString());
			bParsed = false;
		}
		if (!bParsed) 
		{
			tidyFiles(in, file);
			liErrors.add(Messages.get("csverror.parse"));
			return ok(upload.render(au, "", liErrors, null, null, null, null, lid, null, aa));
		}
									
		boolean bHeader = false;
		int iRec = 0; // line number for errors etc
		for (CSVRecord record : licsvr) 
		{
			iRec++;
				
			boolean bBlank = true;
			for (int iCol=0; iCol<record.size() && bBlank; iCol++)
			{
				if (!record.get(iCol).equals(""))
				{
					if (iOGB>=0 || iPoly>=0 || iLat>=0 || iLng>=0 || iDates>=0) // we know which cols to look for something
					{
						if (iCol==iOGB || iCol==iPoly || iCol==iLat || iCol==iLng || iCol==iDates)
							bBlank = false;
					}
					else
						bBlank = false;
				}
			}
			if (bBlank)
				continue;
				
			if (!bHeader)
			{
				bHeader = true;
				for (int j = 0; j<record.size(); j++)
				{
					String sTmp = record.get(j);
					if (!sTmp.trim().equals(""))
						hmHeaders.put(new Integer(j), sTmp);
				}
				continue;
			}
	        if (bHasHeader && !checkCols(iOGB, iPoly, iLat, iLng, iDates))
	        {
				liErrors.add(Messages.get("csverror.colsdifferent"));
				return ok(upload.render(au, "", liErrors, hmHeaders, fileName, null, sReplace, lid, null, aa));
	        }

			if (!bHasHeader)
			{
				if (hmHeadersChosen.containsKey("latcol")) // been shown them
				{
					liErrors.add(Messages.get("csverror.musthavecols"));
					return ok(upload.render(au, "", liErrors, hmHeaders, fileName, null, sReplace, lid, null, aa));
				}
				if (hmHeaders.size() > 0)
					return ok(upload.render(au, "", liErrors, hmHeaders, fileName, null, sReplace, lid, null, aa));

				liErrors.add(Messages.get("csverror.parse"));
				return ok(upload.render(au, "", liErrors, null, null, null, null, lid, null, aa));
			}
			int iMinCols;
			if (aa.getStatus().equals("Draft"))
				iMinCols = Math.max(iOGB,Math.max(iPoly,Math.max(iLat,iLng)));
			else 
				iMinCols = Math.max(Math.max(iOGB,Math.max(iPoly,Math.max(iLat,iLng))),iDates);
			if (record.size()<iMinCols)
			{			
				tidyFiles(in, file);
				liErrors.add(Messages.get("csverror.insufficientcols"));
				return ok(upload.render(au, "", liErrors, hmHeaders, null, null, sReplace, lid, null, aa));
			}

			String colOGB = null;
			String colPoly = null;
			String colLat = null;
			String colLng= null;
			String colDates= null;
					
			if (iOGB>=0)
				colOGB = record.get(iOGB);
			if (iPoly>=0)
				colPoly = record.get(iPoly);
			if (iLat>=0)
				colLat = record.get(iLat);
			if (iLng>=0)
				colLng = record.get(iLng);
			if (iDates>=0)
				colDates = record.get(iDates);
					
			LocationRecord lr = new LocationRecord(hmPoly, colOGB, colLat, colLng, 
					colPoly, colDates, iRec, true, aa);
			
			if (!lr.getType().equals("") && !lr.getType().equals("Discard"))
			{
				liLR.add(lr);
				if (lr.getType().equals("Poly"))
					hmPoly.put(colPoly,lr);
			}
			liErrors.addAll(lr.getErrors());
		}
		
		tidyFiles(in, file);
		
		Iterator<String> itPoly=hmPoly.keySet().iterator();
		while (itPoly.hasNext())
		{
			String sKey = itPoly.next();
			LocationRecord lrP = hmPoly.get(sKey);
			lrP.isValid();
			liErrors.addAll(lrP.getErrors());
		}
		if (liLR.size()<1)
			liErrors.add(Messages.get("csverror.nodata"));
			
		if (liErrors.size() > 0)
			return ok(upload.render(au, "", liErrors, hmHeaders, null, null, sReplace, lid, null, aa));
		
    	if (hmHeadersChosen.containsKey("confirm") && hmHeadersChosen.get("confirm").equals("yes"))
    	{
			ActivityLocation al = null;
    		
    		if (aa!=null)
    		{
    			String sStatus = aa.getStatus(); 
    			if (sStatus.equals("Draft"))
    				sStatus = "Proposed";
    			else
    				sStatus = "Additional";

    			if (sReplace.equalsIgnoreCase("true"))
    			{
    				Iterator<ActivityLocation> itrem = aa.getActivitylocations().iterator();
    				while (itrem.hasNext())
    				{
    					JPA.em().remove(itrem.next());
    				}
    				aa.getActivitylocations().clear();    				
    			}
    			else if (iDates>=0)
    			{
    				Iterator<ActivityLocation> itremd = aa.getActivitylocations().iterator();
    				while (itremd.hasNext())
    				{
    					al = itremd.next();
    					Iterator<ActivityLocationDate> itald = al.getActivitydates().iterator();
    					while (itald.hasNext())
    						JPA.em().remove(itald.next());
    					al.setActivitydates(new ArrayList<ActivityLocationDate>());
    				}
    			}

        		LocationRecord lr = null;
    			Iterator <LocationRecord> itlr = liLR.iterator();
    			List<ActivityLocation> lial = aa.getActivitylocations();
    			while (itlr.hasNext())
    			{
    				lr = itlr.next();
    				if (lr.getType().equals("OGB"))
    				{
        				al = new ActivityLocation();
        				al.setEntrytype("ogb");
        				al.setDescription("Oil and Gas Block Code: "+lr.getOGBCode());
        				al.setEntered_ogb_code(lr.getOGBCode());
        				al.setCreation_type(sStatus);
        				al.setAa(aa);
        				al = getALToUse(al,lial);
        				setALDates(lr,al);
        				aa.getActivitylocations().add(al);
    				} 
    				else if (lr.getType().equals("LatLng"))
    				{
	    				al = new ActivityLocation();
	    				al.setEntrytype("latlng");
	    				LatLongs ll = lr.getLatLongs();
	    				al.setDescription("Latitude/Longitude: "+ll.sLatitude+" / "+ll.sLongitude);
	    				al.setLat(ll.dLatitude);
	    				al.setLng(ll.dLongitude);
	    				al.setCreation_type(sStatus);
	    				al.setAa(aa);
        				al = getALToUse(al,lial);
        				setALDates(lr,al);
	    				aa.getActivitylocations().add(al);
    				}
    				else if (lr.getType().equals("Poly") && iDates < 0) // no polygons for close out
    				{
        				String sPolyLLs = "";
        				Iterator<LatLongs> itll = lr.getPolyLLs().iterator();
        				while (itll.hasNext())
        				{
        					LatLongs ll = itll.next();
           					if (!sPolyLLs.equals(""))
           						sPolyLLs += ", ";
           					sPolyLLs += ll.sLatitude+" / "+ll.sLongitude;
        				}
        				al = new ActivityLocation();
        				al.setEntrytype("polygon");
        				al.setDescription("Polygon: "+sPolyLLs);
        				al.setPolygon_latlngs(sPolyLLs);
        				al.setCreation_type(sStatus);
        				al.setAa(aa);
        				al = getALToUse(al,lial);
        				aa.getActivitylocations().add(al);
    				}
    			}
    	    	JPA.em().merge(aa);
    	    	JPA.em().flush();
    	    	
    	    	AuditTrail.WriteAudit(au, aa.getId(), "Updated via CSV", "CSV Upload", "ActivityApplication");

    			if (iDates>=0)
    				return redirect(routes.ActivityApplicationController.closeOut(lid));
    	    	
				return redirect(routes.ActivityApplicationController.read(lid));
    		}
    	}
		return ok(upload.render(au, "", liErrors, hmHeaders, fileName, hmHeadersChosen, sReplace, lid, liLR, aa));
	}

	/**
	 * Finds all ActivityLocation which matches the location in the CSV file upload
	 * @param al - The created ActivityLocation
	 * @param lial - the list of ActivityLocations contained in the activity
	 * @return al - the matching ActivityLocation
	 */
	protected static ActivityLocation getALToUse(ActivityLocation al,List<ActivityLocation> lial)
	{
		Iterator<ActivityLocation> it = lial.iterator();
		while (it.hasNext())
		{
			ActivityLocation alCheck = it.next();
			if (alCheck.getEntered_ogb_code()!=null && !alCheck.getEntered_ogb_code().toString().equals(""))
			{
				if (alCheck.getEntered_ogb_code().equals(al.getEntered_ogb_code()))
					return alCheck;
			}
			if (alCheck.getEntered_point()!=null && !alCheck.getEntered_point().toString().equals("")
					&& al.getEntered_point()!=null)		
			{
				if (alCheck.getEntered_point().toString().equals(al.getEntered_point().toString()))
					return alCheck;
			}
			if (alCheck.getEntered_polygon()!=null && !alCheck.getEntered_polygon().toString().equals("")
					&& al.getEntered_polygon()!=null)
			{
				if (alCheck.getEntered_polygon().toString().equals(al.getEntered_polygon().toString()))
					return alCheck;
			}
		}
		return al;
	}
	
	/**
	 * Sets the dates in the ActivityLocation from the LocationRecord
	 * @param lr - LocationRecord
	 * @param al - the ActivityLocation
	 */
	protected static void setALDates(LocationRecord lr,ActivityLocation al)
	{
		List<ActivityLocationDate> liald = new ArrayList<ActivityLocationDate>();
		Iterator<Date> it = lr.getDates().iterator();
		while (it.hasNext())
		{
			Date d = it.next();	
			ActivityLocationDate ald = new ActivityLocationDate();
			ald.setActivity_date(d);
			ald.setActivitylocation(al);
			liald.add(ald);
		}
		if (liald.size() > 0)
		{
			al.setActivitydates(liald);
		}
	}
	
	/**
	 * Show the UI for editing an existing application
	 * @param id the id of the application to be edited 
	 * @return page with the application
	 */
	@Transactional(readOnly=true)
	public static Result edit(Long id)
	{
		AppUser au = AppUser.getSystemUser(request().username());
		
		ActivityApplication aa = ActivityApplication.findById(id);
		if (aa==null || au.isRegulatorMember()) {
			String activeTab="HOME";
	        return status(403,index.render(au, activeTab));
		}
		aa.setLocation_entry_type("usemanentry");
		
		Form<ActivityApplication> filledForm = appForm.fill(aa);
		if (request().accepts("text/html")) {
			//Prevent caching the form (doesn't work for the current version of Chrome v40.0)
			response().setHeader(CACHE_CONTROL, "no-cache, no-store, must-revalidate");
			response().setHeader(PRAGMA, "no-cache, no-store");
			response().setHeader(EXPIRES, "0");
			
			return ok(activityform.render(au, filledForm, Messages.get("activityform.title"), id, aa.getParent()));
		} else {
			return badRequest("Unsupported content type");
		}
	}
	
	/**
	 * Shows the UI for creating a linked application
	 * @param id the id of the application to be linked to
	 * @return the page allowing creation of the linked application
	 */
	@Transactional(readOnly=true)
	public static Result createLinked(Long id)
	{
		AppUser au = AppUser.getSystemUser(request().username());
		
		ActivityApplication aa = ActivityApplication.findById(id);
		
		if (aa==null || au.isRegulatorMember()) {
			String activeTab="HOME";
	        return status(403,index.render(au, activeTab));
		}
		ActivityApplication linked = aa.createLinked();
		
		Form<ActivityApplication> filledForm = appForm.fill(linked);
		
		if (request().accepts("text/html")) {
			return ok(activityform.render(au, filledForm, Messages.get("activityform.title"), null, aa));
		} else {
			return badRequest("Unsupported content type");
		}
	}

	/**
	 * Shows the UI for reverting back to a draft
	 * @param id the id of the application
	 * @return the page allowing reverting
	 */
	@Transactional(readOnly=true)
	public static Result revertActivityStatus(Long id, Long lTime)
	{
		AppUser au = AppUser.getSystemUser(request().username());
		
		ActivityApplication aa = ActivityApplication.findById(id);
		
		if (aa==null) {
			String activeTab="HOME";
	        return status(403,index.render(au, activeTab));
		}
		
		if (request().accepts("text/html")) {
			return ok(activityformrevert.render(au, aa, appForm));
		} else {
			return badRequest("Unsupported content type");
		}
	}

	/** 
	 * UI part of the revert application 
	 * @return page following cancellation
	 */
	@Transactional
	@ApiOperation(value = "Revert Activity by ID",
	nickname = "revert",
	response = ActivityApplication.class, httpMethod = "POST")
	@ApiResponses(value = {@ApiResponse(code = 404, message = "Activity not found")})
	public static Result revertActivity()
	{
		Form<ActivityApplication>  filledForm = appForm.bindFromRequest();
		Map<String,String> map = filledForm.data();
		String sId = (String)map.get("id"); 
		Long id = Long.parseLong(sId);
		
		return revertActivityById(id);
	}
	
	/**
	 * reverts an application to draft(used for both UI and REST)
	 * @param id the id of the application to revert
	 * @return appropriate data
	 */
	@Transactional
	public static Result revertActivityById(
			@ApiParam(value = "Id value that identifies the Activity Application", 
			required = true, 
			allowMultiple = false) 
	@PathParam("id") Long id) 
	{
		String sNewStatus = "";
		AppUser au = AppUser.getSystemUser(request().username());
		ActivityApplication aa = ActivityApplication.findById(id);
		if (aa==null) {
			String activeTab="HOME";
	        return status(403,index.render(au, activeTab));
		} else {
			Form<ActivityApplication>  filledForm = appForm.bindFromRequest();
			Map<String,String> map = filledForm.data();
			String sReason = (String)map.get("reason"); 
				
			if (sReason==null || sReason.equals(""))
			{
				filledForm.reject("reason","validation.revert.reason_required");
				return badRequest(activityformrevert.render(au, aa, filledForm));
			}
			sNewStatus = aa.revert(au,sReason);
		}
		JPA.em().flush();  
		if (request().accepts("text/html")) {
			if (sNewStatus.equals("Draft"))
				return ActivityApplicationController.findDraft();
			else
				return ActivityApplicationController.index();
		} else if (request().accepts("application/json")) {
    		return redirect(routes.ActivityApplicationController.read(id));
    	} else {
    		return badRequest("Unsupported content type");
    	}
	}
	
	/**
	 * Shows the UI asking users to confirm the deletion of the application
	 * @param id the application about to be deleted
	 * @return the page asking for confirmation
	 */
	@Transactional(readOnly=true)
	public static Result preDelete(Long id)
	{
		AppUser au = AppUser.getSystemUser(request().username());
		ActivityApplication aa = ActivityApplication.findById(id);
		if (aa==null) {
			String activeTab="HOME";
	        return status(403,index.render(au, activeTab));
		}
		if (request().accepts("text/html")) {
			return ok(activityformdelete.render(au, aa, appForm));
		} else {
			return badRequest("Unsupported content type"); 
		}
	}
	
	/**
	 * Delete an application
	 * @param id the id of the application to be deleted
	 * @return appropriate result
	 */
	@Transactional
	public static Result delete(Long id)
	{
		AppUser au = AppUser.getSystemUser(request().username());
		ActivityApplication aa = ActivityApplication.findById(id);
		if (aa==null) {
			String activeTab="HOME";
	        return status(403,index.render(au, activeTab));
		}
		try {
			String sReason = "Draft deletion";
			Form<ActivityApplication>  filledForm = appForm.bindFromRequest();
				
			Map<String,String> map = filledForm.data();
			sReason = (String)map.get("reason");
				
			if (sReason==null || sReason.equals(""))
			{
				filledForm.reject("reason","validation.delete.reason_required");
				return badRequest(activityformdelete.render(au, aa, filledForm));
			}

			aa.setStatus("Deleted");
			JPA.em().flush();
			
			AuditTrail.WriteAudit(au,aa.getId(),"Delete",sReason,"ActivityApplication");
			
			if (request().accepts("text/html")) {
				if (session("lastview")==null || session("lastview").equals("Draft"))
					return redirect(routes.ActivityApplicationController.findDraft());
				if (session("lastview").equals("Proposed"))
					return index();
				if (session("lastview").equals("Completed"))
					return findCompleted();
				return redirect(routes.ActivityApplicationController.findDraft());
			} else if (request().accepts("application/json")) {
				return status(204);
			} else {
				return badRequest("Unsupported content type");
			}
		} catch (Exception e) {
			Logger.debug("Error deleting activity:"+e.toString());
			return badRequest(e.getMessage());
		}
	}

	/**
	 * Shows the confirmation page that an application has been added
	 * @param id the application id added
	 * @param lConfirm whether this is just confirmation
	 * @return the page with the confirmation details
	 */
	@Transactional(readOnly=true)
	public static Result confirmadd(String id, Long lConfirm)
	{
		AppUser au = AppUser.getSystemUser(request().username());
		
		ActivityApplication aa = ActivityApplication.findById(Long.parseLong(id));
		Logger.error("3: "+Integer.toString(aa.getActivitylocations().size()));

		if (aa==null) {
			String activeTab="HOME";
	        return status(403,index.render(au, activeTab));
		}
		if (lConfirm==1)
		{
			Form<ActivityApplication> filledForm = appForm.fill(aa);
			aa.setMustconfirm("true");

			return ok(activityformcloseout.render(au, aa, filledForm, Long.parseLong(id)));
		}
		else
			return ok(activityformread.render(au, aa, "confirmadd"));
	}
	/**
	 * Gets an activity application
	 * @param id the id of the application to be returned
	 * @return appropriate data
	 */
	@Transactional(readOnly=true)
	@ApiOperation(value = "Get Activity Application by ID",
	            notes = "Returns an Activity Application",
	            nickname = "getById",
	            response = ActivityApplication.class, httpMethod = "GET")
	@ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid ID supplied"),
	            @ApiResponse(code = 404, message = "Activity Application not found")})
	public static Result read(
			@ApiParam(value = "Id value that identifies the Activity Application", 
			required = true, 
			allowMultiple = false) 
		@PathParam("id") Long id)
	{
		AppUser au = AppUser.getSystemUser(request().username());
	    
		ActivityApplication aa = ActivityApplication.findById(id);
		
		if (aa==null) {
			String activeTab="HOME";
	        return status(403,index.render(au, activeTab));
		}
		if (request().accepts("text/html")) {
			return ok(activityformread.render(au, aa , "read"));
		} else if (request().accepts("application/json")) {
			return ok(Json.toJson(aa));
		} else {
			return badRequest("Unsupported content type");
		}
	}
	
	/**
	 * Shows the UI allowing a user to cancel an application
	 * @param id the application to be cancelled
	 * @return the page allowing the user to cancel
	 */
	@Transactional
	public static Result cancel(String id)
	{
		AppUser au = AppUser.getSystemUser(request().username());
		ActivityApplication aa = ActivityApplication.findById(Long.parseLong(id));
		if (aa==null) {
			String activeTab="HOME";
	        return status(403,index.render(au, activeTab));
		}
		return ok(activityformcancel.render(au, aa));
	}
	
	/**
	 * Gets an application to close
	 * @param id the application to be closed
	 * @return appropriate data
	 */
	@Transactional(readOnly = true)
	@ApiOperation(value = "Get Activity Application for close out by ID",
    	notes = "Returns an Activity Application for close out",
    	nickname = "getById",
    	response = ActivityApplicationCloseOut.class, httpMethod = "GET")
	@ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid ID supplied"),
    @ApiResponse(code = 404, message = "Activity Application not found")})
	public static Result closeOut(
			@ApiParam(value = "Id value that identifies the Activity Application", 
				required = true, 
				allowMultiple = false) 
		@PathParam("id") Long id) 
	{
		AppUser au = AppUser.getSystemUser(request().username());
		ActivityApplication aa = ActivityApplication.findById(id);
		if (aa==null) {
			String activeTab="HOME";
	        return status(403,index.render(au, activeTab));
		}
		
		if (aa.getStatus().equals("Proposed") || aa.getStatus().equals("Interim Close-out")) {
			//populate the close out partial model
			ActivityApplicationCloseOut aaco = ActivityApplicationCloseOut.findById(aa.getId());
			aaco.setActivitylocations(aa.getActivitylocations());
			aaco.populateActivityDefaults();

			Form<ActivityApplicationCloseOut> filledForm = appcloseoutForm.fill(aaco);
			
			if (request().accepts("text/html")) {
				return ok(activityformcloseout.render(au, aa, filledForm, id));
			} else if (request().accepts("application/json")) {
				return ok(Json.toJson(aaco));
			} else {
				return badRequest("Unsupported content type");
			}
		} else {
			return notFound();
		}
	}
	
/*	protected static void actualsValidate(Form<ActivityApplicationCloseOut> filledForm, Form<ActivityApplication> frm)
	{
		Map<String, List<ValidationError>> mve = frm.errors();
		for (Entry<String, List<ValidationError>> entry : mve.entrySet()) 
		{
			if (entry.getKey().contains("_actual"))
			{
				List<ValidationError> lve = entry.getValue();
				Iterator<ValidationError> it = lve.iterator();
				while (it.hasNext())
				{
					ValidationError ve = it.next();
					filledForm.reject(ve);
				}
			}
		}
	}*/

	/**
	 * Closes the application
	 * @param id the application to be closed
	 * @return appropriate data
	 */
	@Transactional
	@ApiOperation(value="Close out Activity Application",
	notes = "Closes out an Activity Application using the data supplied (if valid). The parameter 'interimcloseout' must be non-null if the update is an interim close out, otherwise the ActivityApplication status will be set to closed.",
	nickname = "closeoutAA",
	response = ActivityApplicationCloseOut.class, httpMethod = "POST")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "body", value = "Activity Application details", required = true, dataType="models.ActivityApplicationCloseOut", paramType = "body"),
	})
	@ApiResponses(value = {@ApiResponse(code = 405, message = "Invalid input")})
	public static Result closeOutApplication(@ApiParam(value = "Id value that identifies the Activity Application", 
			required = true, 
			allowMultiple = false) 
	@PathParam("id") Long id)
	{
		AppUser au = AppUser.getSystemUser(request().username());
		ActivityApplication aa = ActivityApplication.findById(id);
		if (aa==null) {
			String activeTab="HOME";
	        return status(403,index.render(au, activeTab));
		}
		
		if (aa.getStatus().equals("Proposed") || aa.getStatus().equals("Interim Close-out")) 
		{
			//set the closing indicator to allow validation checks based on status during the bindFromRequest call...
			aa.setClosing(true);
			Form<ActivityApplicationCloseOut> filledForm = appcloseoutForm.bindFromRequest();

			if (filledForm.data().containsKey("csvupload") && !filledForm.data().get("csvupload").equals(""))
			{
				aa.setCsvclosing(true);
				filledForm = appcloseoutForm.bindFromRequest();
				aa.setSupp_info(filledForm.data().get("supp_info"));
		    	JPA.em().flush();
				return ok(upload.render(au, "", null, null, null, null, null, id, null, aa));
			}
			
			try 
			{
				if (filledForm.get().getActivitylocations() == null || filledForm.get().getActivitylocations().isEmpty()) {
					filledForm.reject(new ValidationError("", Messages.get("validation.closeout.locations.empty")));
				}
			} catch(Exception ex) {
				filledForm.reject(new ValidationError("", Messages.get("validation.closeout.locations.error")));
			}
			
			if (filledForm.hasErrors()) 
			{
				if (request().accepts("text/html")) {
					return badRequest(activityformcloseout.render(au, aa, filledForm, id));
				} else if (request().accepts("application/json")) {
	        		return badRequest(filledForm.errorsAsJson());
	        	} else {
	        		return badRequest("Unsupported content type");
	        	}
			} else {
				try {
					ActivityApplicationCloseOut aaFrm = filledForm.get(); 
					if ((aaFrm.getInterimcloseout()==null || aaFrm.getInterimcloseout().equals("")) && aaFrm.getMustconfirm().equals(""))
					{
						aaFrm.closeOut(id, filledForm.data(),false);
						return redirect(routes.ActivityApplicationController.confirmadd(Long.toString(id),1));
					}
					
					// Clear out dates if no activity is recorded in a location
					Iterator<ActivityLocation> it = aaFrm.getActivitylocations().iterator();
					while (it.hasNext()) {
						ActivityLocation loc = it.next();
						if (loc.getNo_activity() != null && loc.getNo_activity()) {
							loc.setActivitydates(null);
						}
					}
					
					String sNewStatus = aaFrm.closeOut(id, filledForm.data(), true);

					JPA.em().flush();  //make sure any persistence errors are raised before emailing

					sendRegulatorNotification(aa, aa.getStatus().toLowerCase().replace(' ', '_'));
					
			    	JPA.em().flush();
		    		AuditTrail.WriteAudit(au,aaFrm.getId(),"Update to "+sNewStatus,"Updated","ActivityApplication"); // doesn't like blank reason
					
					if (request().accepts("text/html")) {
						return redirect(routes.ActivityApplicationController.confirmadd(Long.toString(id),0));
					} else if (request().accepts("application/json")) {
		        		return redirect(routes.ActivityApplicationController.read(id));
		        	} else {
		        		return badRequest("Unsupported content type");
		        	}
				} catch (Exception e) {
					Logger.error("CloseOut error: "+e.toString());
					return badRequest(activityformcloseout.render(au, aa, filledForm, id));
				}
			}
		} else {
			String activeTab="HOME";
			return status(404,index.render(au, activeTab));
		}
		
	}
	
	/** 
	 * UI part of the application cancellation
	 * @return page following cancellation
	 */
	@Transactional
	public static Result cancelApplication()
	{
		Form<ActivityApplication>  filledForm = appForm.bindFromRequest();
		Map<String,String> map = filledForm.data();
		String sId = (String)map.get("id"); 
		Long id = Long.parseLong(sId);
		
		return cancelApplicationById(id);
	}
		
	/**
	 * cancels an application (used for both UI and REST)
	 * @param id the id of the application to cancel
	 * @return appropriate data
	 */
	@Transactional
	@ApiOperation(value = "Cancel Activity Application by ID",
	nickname = "cancel",
	response = ActivityApplicationCloseOut.class, httpMethod = "POST")
	@ApiResponses(value = {@ApiResponse(code = 404, message = "Activity Application not found")})
	public static Result cancelApplicationById(
			@ApiParam(value = "Id value that identifies the Activity Application", 
			required = true, 
			allowMultiple = false) 
	@PathParam("id") Long id) 
	{
		AppUser au = AppUser.getSystemUser(request().username());
		ActivityApplication aa = ActivityApplication.findById(id);
		if (aa==null) {
			String activeTab="HOME";
	        return status(403,index.render(au, activeTab));
		} else {
			aa.cancel(au);
		}
		JPA.em().flush();  //make sure any persistence errors are raised before emailing
		sendRegulatorNotification(aa, aa.getStatus().toLowerCase());
		if (request().accepts("text/html")) {
			return redirect(routes.ActivityApplicationController.index());
		} else if (request().accepts("application/json")) {
    		return redirect(routes.ActivityApplicationController.read(id));
    	} else {
    		return badRequest("Unsupported content type");
    	}
	}
	
	/**
	 * Saves an application
	 * @return appropriate data
	 */
	@Transactional
	@ApiOperation(value="Create Activity Application",
	notes = "Creates a new Activity Application using the data supplied (if valid).  The following fields are read-only and should be excluded from any POST: all id values, all aa values, status, date_due, date_closed.  To create a linked application, specify the id of the parent in an additional parameter 'parent.id' (not shown in the included schema).",
	nickname = "createAA",
	response = ActivityApplication.class, httpMethod = "POST")
		@ApiImplicitParams({
			@ApiImplicitParam(name = "body", value = "Activity Application details", required = true, dataType="models.ActivityApplication", paramType = "body"),
		})
		@ApiResponses(value = {@ApiResponse(code = 405, message = "Invalid input")})
	public static Result save()
	{
		AppUser au = AppUser.getSystemUser(request().username());		
		Form<ActivityApplication> filledForm = appForm.bindFromRequest();

		if (
			(filledForm.data().get("use_parent_location")==null || filledForm.data().get("use_parent_location")=="") &&
			(filledForm.data().get("location_entry_type")==null || filledForm.data().get("location_entry_type")=="")
			)
			filledForm.reject(new ValidationError("location_entry_type", Messages.get("activityform.error.nolocationtype")));
		
		if(filledForm.hasErrors()) 
		{
			if (request().accepts("text/html")) 
			{
				filledForm.reject(new ValidationError("", Messages.get("activityform.error.global_heading")));
				ActivityApplication aaParent = null;
				
				if (filledForm.data().get("parent.id") != null && !filledForm.data().get("parent.id").isEmpty())
				{
					Long lPar = Long.parseLong((String)filledForm.data().get("parent.id"));
					aaParent = ActivityApplication.findById(lPar);
				}

				return badRequest(
						activityform.render(au, filledForm, Messages.get("activityform.title_new"), null, aaParent)
			    );
			} else if (request().accepts("application/json")) {
        		return badRequest(filledForm.errorsAsJson());
        	} else {
        		return badRequest("Unsupported content type");
        	}
			
		} 
		else 
		{
			ActivityApplication aa = filledForm.get();
			
			boolean bCSV = false;
			
			if (aa!=null)
			{
				if (aa.getLocation_entry_type()!=null && aa.getLocation_entry_type().equals("usecsventry"))
				{
					aa.setSaveasdraft("draft");
					bCSV = true;
				}
					
				if (aa.getParent() != null && aa.getParent().getId() == null) 
				{
					aa.setParent(null);
				}

				if (bCSV) // unset locations if csv entry
					aa.setActivitylocations(new ArrayList<ActivityLocation>());

				if (aa.getCopyparentlocations()!=null)
				{
					aa.setSaveasdraft("draft");
					if (aa.getCopyparentlocations()!=null)
					{
						aa.setActivitylocations(new ArrayList<ActivityLocation>());
						copyLocations(aa);
					}
				}
				
				aa.save(au);
			}
			JPA.em().flush();  //make sure any persistence errors are raised before emailing
			//if this is a newly proposed item, then send a mail...
			if (aa.getStatus().equals("Proposed")) {
				sendRegulatorNotification(aa, aa.getStatus().toLowerCase());
			}
			if (request().accepts("text/html")) {
				if (bCSV)
					return ActivityApplicationController.upload(aa.getId());
				return redirect(routes.ActivityApplicationController.confirmadd(Long.toString(aa.getId()),0));
			} else if (request().accepts("application/json")) {
				return redirect(routes.ActivityApplicationController.read(aa.getId()));
        	} else {
        		return badRequest("Unsupported content type");
        	}	
		}
	}
	
	/**
	 * Emails the regulator
	 * @param aa the ActivityApplication
	 * @param statusKey status of the application
	 */
	public static void sendRegulatorNotification(ActivityApplication aa, String statusKey)
	{
		try {
			Regulator reg = JPA.em().find(Regulator.class,  aa.getRegulator().getId());
			NoiseProducer np = JPA.em().find(NoiseProducer.class,  aa.getNoiseproducer().getId());
			
			if (reg.getAccepts_email().booleanValue()) {
				if (Messages.get("regulator.activity." + statusKey + ".mail.subject").equals("regulator.activity." + statusKey + ".mail.subject")) {
					//no message translation - don't try to send mail out in this case...
					Logger.error("No mail settings found for application status value: " + statusKey);
					return;
				}
			
				Session session = MailSettings.getSession();
				String regulatorEmail = reg.getOrganisation().getContact_email();
						
				String overrideAddress = AppConfigSettings.getConfigString("regulatorOverrideAddress", "email.regulator_override_address");

				InternetAddress[] addresses = new InternetAddress[1];
				
				addresses[0] = new InternetAddress(regulatorEmail);
				Html mailBody = views.html.email.regulatoractivity.render(aa, AppConfigSettings.getConfigString("hostname", "application.hostname"), statusKey, overrideAddress, reg, np, true);
				Html mailAlt = views.html.email.regulatoractivity.render(aa, AppConfigSettings.getConfigString("hostname", "application.hostname"), statusKey, overrideAddress, reg, np, false);

				String sSubject = Messages.get("regulator.activity." + statusKey + ".mail.subject");
				MailSettings.send(mailBody, mailAlt, sSubject, addresses, true, false, false);						
			} else {
				Logger.error("Regulator is not set to receive email - not sending activity notification for status " + statusKey);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("Send regulator notification error: "+e.getMessage());
		}
	}
	
		
	/**
	 * Returns the activity types
	 * @return activity types
	 */
	@Transactional(readOnly=true)
	@ApiOperation(value = "Find list options for Activity Types",
		notes = "Returns a list of Activity Type options which are valid in activity application submissions.",
		nickname = "options",
		response = LinkedHashMap.class,
		httpMethod = "GET")
	@ApiResponses(value = {})
	public static Result activityTypeOptions()
	{
		if (request().accepts("text/html")) {
			return badRequest("Unsupported content type");
		} else if (request().accepts("application/json")) {
			return ok(Json.toJson(ActivityTypes.getOptions()));
		} else {
			return badRequest("Unsupported content type");
		}
	}
	
	/**
	 * Retrieves the list of Noise Produces of which the user is a verified member
	 * @return List of Noise Producers
	 */
	@Transactional(readOnly=true)
	@ApiOperation(value = "Find list options for Noise Producers of which User is a verified member",
		notes = "Returns a list of Noise Producer options which are valid in activity application submissions.",
		nickname = "options",
		response = LinkedHashMap.class,
		httpMethod = "GET")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Invalid user")
		})
	public static Result noiseProducerOptions()
	{
		AppUser au = AppUser.getSystemUser(request().username());
		
		if (request().accepts("text/html")) {
			return badRequest("Unsupported content type");
		} else if (request().accepts("application/json")) {
			return ok(Json.toJson(NoiseProducer.getOptions(au)));
		} else {
			return badRequest("Unsupported content type");
		}
	}
	
	private static Date getDateFromFragments(String day, String month, String year) throws IllegalArgumentException {
		Integer day_int = Integer.parseInt(day);
		Integer month_int = Integer.parseInt(month);
		Integer year_int = Integer.parseInt(year);
					
		Calendar cal = Calendar.getInstance();
		cal.setLenient(false);
		cal.set(Integer.parseInt(year), Integer.parseInt(month)-1, Integer.parseInt(day), 0, 0, 0);
		
		return cal.getTime();
	}
	
	/**
	 * Restores a "deleted" draft
	 * @param id - the id of the "deleted" activity
	 */
	@Transactional
	public static Result restore(Long id) 
	{
		try 
		{
			AppUser au = AppUser.getSystemUser(request().username());
			ActivityApplication aa = JPA.em().find(ActivityApplication.class, id);
			aa.setSaveasdraft("Draft");
			aa.update(id, au);
		}
		catch (Exception e)
		{
			Logger.error("Restore error: "+e.toString());
		}
		return findDraft();
	}
	
	/**
	 * Shows a list of "deleted" activities
	 */
	@Transactional
	public static Result deletedactivities() 
	{
		AppUser au = AppUser.getSystemUser(request().username());
		
		List<String> listat = new ArrayList<String>();
		listat.add("Deleted");
		List<ActivityApplication> lidel = ActivityApplication.findAllByStatus("Deleted");
		
		return ok(activities.render(au,lidel,listat));
	}
	
	/**
	 * Submits an activity
	 * @param id - the id of the activity
	 */
	@Transactional
	public static Result submit(Long id) 
	{
		try{
			AppUser au = AppUser.getSystemUser(request().username());

			ActivityApplication aa = JPA.em().find(ActivityApplication.class, id);

			String sOrgStatus = aa.getStatus(); 
			aa.update(id,au);
			JPA.em().flush();  //make sure any persistence errors are raised before emailing
		
			//if this is a newly proposed item, then send a mail...
			if (aa.getStatus().equals("Proposed") && !aa.getStatus().equals(sOrgStatus)) 
			{
				sendRegulatorNotification(aa, aa.getStatus().toLowerCase());
			}			
		}
		catch (Exception e) {
			Logger.error("Submit error: "+e.toString());
		}
		return redirect(routes.ActivityApplicationController.confirmadd(Long.toString(id),0));
	}
	
	/**
	 * Updates an activity from a submitted form
	 * @param id - id of the activity
	 */
	@Transactional
	public static Result update(Long id) 
	{
		AppUser au = AppUser.getSystemUser(request().username());
				
		FormLimitless<ActivityApplication> filledForm = appForm.bindFromRequest();
		
		return updateWithForm(id,au,filledForm);
	}

	/**
	 * Processes the activity update
	 * @param id - the id of the activity
	 * @param - au - the the user
	 * @param - filledForm - the form containing the data
	 */	
	protected static Result updateWithForm(Long id,AppUser au, Form<ActivityApplication> filledForm) 
	{
		/** 
		 * TODO: Fix this properly, date is reset to initial input date on update no mater what is put in the form 
		 */
		// TODO: Replace date input system with an actual real system, why would you take the date as separate
		// form inputs for day, month, year should be the same as the rest of the app with a datepicker and one 
		// entry restriction

		// Grab the actual entry from the form for updates as the code below can't handle date updates, some issue
		// in the binding that would take too long to unpick
		if (
			(filledForm.data().get("use_parent_location")==null || filledForm.data().get("use_parent_location")=="") &&
			(filledForm.data().get("location_entry_type")==null || filledForm.data().get("location_entry_type")=="")
			)
			filledForm.reject(new ValidationError("location_entry_type", Messages.get("activityform.error.nolocationtype")));
				
		Date startDate = null;
		Date endDate = null;

		try {
			startDate = getDateFromFragments(filledForm.data().get("date_start_day"), 
					filledForm.data().get("date_start_month"), 
					filledForm.data().get("date_start_year"));
		} catch (Exception ex) {
			filledForm.reject(new ValidationError("date_start", Messages.get("activityform.error.start_date")));
		}
		
		try {	
			endDate = getDateFromFragments(filledForm.data().get("date_end_day"), 
					filledForm.data().get("date_end_month"), 
					filledForm.data().get("date_end_year"));
		} catch (Exception ex) {
			filledForm.reject(new ValidationError("date_end", Messages.get("activityform.error.end_date")));
		}
		/**
		 * END TODO
		 */

		if(filledForm.hasErrors()) 
		{
			ActivityApplication aaParent = null;
			if (filledForm.data().get("parent.id") != null && !filledForm.data().get("parent.id").isEmpty())
			{
				Long lPar = Long.parseLong((String)filledForm.data().get("parent.id"));
				aaParent = ActivityApplication.findById(lPar);
			}
			filledForm.reject(new ValidationError("", Messages.get("activityform.error.global_heading")));
			if (request().accepts("text/html")) {
				return badRequest(activityform.render(au, filledForm, Messages.get("activityform.title"), id, aaParent));
			} else if (request().accepts("application/json")) {
        		return badRequest(filledForm.errorsAsJson());
        	} else {
        		return badRequest("Unsupported content type");
        	}
			
		} else {
			ActivityApplication aa = filledForm.get();
			/** 
			 * TODO: Fix this properly, date is reset to initial input date on update no mater what is put in the form 
			 */			
			if (aa.getDate_start() != startDate) {
				aa.setDate_start(startDate);
			}
			if (aa.getDate_end() != endDate) {
				aa.setDate_end(endDate);
			}
			/**
			 * END TODO
			 */
				
			try {
				if (aa.getCsvupload()!=null || aa.getSaveasdraft()!=null || aa.getCopyparentlocations()!=null)
				{
					aa.setSaveasdraft("draft");
					if (aa.getCopyparentlocations()!=null)
					{
						aa.setActivitylocations(new ArrayList<ActivityLocation>());
						copyLocations(aa);
					}
				}
				aa.update(id,au);
				JPA.em().flush();  //make sure any persistence errors are raised before emailing
							
				//if this is a newly proposed item, then send a mail...
				if (aa.getStatus().equals("Proposed")) {
					sendRegulatorNotification(aa, aa.getStatus().toLowerCase());
				}
			} catch (Exception e) {
				filledForm.reject(e.getMessage());
				return badRequest(activityform.render(au, filledForm, Messages.get("activityform.title"), id, null));
			}
			if (request().accepts("text/html")) {			
				if (aa.getCsvupload()!=null && aa.getCsvupload()!="")
					return ActivityApplicationController.upload(aa.getId());
				return redirect(routes.ActivityApplicationController.confirmadd(Long.toString(aa.getId()),0));
			} else if (request().accepts("application/json")) {
				return redirect(routes.ActivityApplicationController.read(aa.getId()));
        	} else {
        		return badRequest("Unsupported content type");
        	}	
		}
	}
	
	/**
	 * Returns a list of Regulator options which are valid in activity application submissions
	 * @return List of regulators
	 */
	@Transactional(readOnly=true)
	@ApiOperation(value = "Find list options for Regulators",
		notes = "Returns a list of Regulator options which are valid in activity application submissions.",
		nickname = "options",
		response = LinkedHashMap.class,
		httpMethod = "GET")
	@ApiResponses(value = {})
	public static Result regulatorOptions()
	{
		if (request().accepts("text/html")) {
			return badRequest("Unsupported content type");
		} else if (request().accepts("application/json")) {
			return ok(Json.toJson(Regulator.getOptions()));
		} else {
			return badRequest("Unsupported content type");
		}
	}

	/**
	 * Gets a list of activities requiring close out
	 * @return List of applications requiring close out
	 */
	protected static List<ActivityApplication> getAppsRequiringCloseOut()
	{
		List<String> status = new ArrayList<String>();
		status.add("Draft");
		status.add("Proposed");
		return ActivityApplication.findLate();
	}
	
	/**
	 * The UI for activities requiring close out
	 */	
	@Transactional(readOnly=true)
	public static Result requirecloseout()
	{
		AppUser au = AppUser.getSystemUser(request().username());
		if (au.getOrgRole().equals(AppUser.OVERALL_ADMIN)) {
			return ok(activitieslate.render(au, getAppsRequiringCloseOut()));
		} else {
			return generateUnauthorized(au);
		}
	}
	
	/**
	 * Checks to see if a user has rights for a given activity application
	 * @param au The AppUser to check
	 * @param aa The ActivityApplication to check against
	 * @return True if the user has some rights for the organisation in question
	 */
	private static boolean userHasRightsForActivity(AppUser au, ActivityApplication aa) 
	{	
		if (au.getOrgRoleForOrg(aa.getNoiseproducer().getOrganisation()).isEmpty()) {
			return false;				
		}
		return true;
	}
	
	private static Result generateUnauthorized(AppUser au) {
		return unauthorized(views.html.errors.unauthorised.render(au, "HOME")); 
	}
}
