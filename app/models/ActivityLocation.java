package models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import javax.validation.Valid;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Range;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import play.Logger;
import play.data.validation.ValidationError;
import play.db.jpa.JPA;
import play.i18n.Messages;

@NamedNativeQueries({
	@NamedNativeQuery(name="ActivityLocation.findProposedPolygonEntriesByPolygon", query="select * from activitylocation al where activityapplication_id = :aa_id AND (ST_intersects(al.entered_polygon, :polygon))", resultClass = ActivityLocation.class),
	@NamedNativeQuery(name = "ActivityLocation.findProposedPolygonEntriesByPoint", query = "select * from activitylocation al where activityapplication_id = :aa_id AND (ST_intersects(al.entered_polygon, :point))", resultClass = ActivityLocation.class)	
})

@Entity
@Table(name="activitylocation")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
public class ActivityLocation
{
	
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "activitylocation_seq_gen")
    @SequenceGenerator(name = "activitylocation_seq_gen", sequenceName = "activitylocation_id_seq")
    @Column(columnDefinition = "serial")
    protected Long id;   
    
    @JsonIgnore
    @ManyToOne(optional=false)
    @JoinColumn(name="activityapplication_id",referencedColumnName="id", updatable=false)  //don't allow the application to be changed
    protected ActivityApplication aa;
    
    @Column(updatable=false)
    protected String entered_ogb_code;
   
    //The total polygon shape that covers the allowed area for location entry.
    private static String VALID_AREA_POLYGON = "POLYGON((-24 48, -24 64, 4 64, 4 48, -24 48))";
    
    @Range(max=64, min=48, message="validation.invalidrange")
    @Transient
	protected Double lat;
	
    @Range(max=4, min=-24, message="validation.invalidrange")
    @Transient
    protected Double lng;
	
    @JsonIgnore
    @Type(type = "org.hibernate.spatial.GeometryType")
    @Column(updatable=false)
  	protected Point entered_point;     
    
    @JsonIgnore
    @Type(type = "org.hibernate.spatial.GeometryType")  
    @Column(updatable=false)
	protected Polygon entered_polygon;
    
    @Column(length=10, updatable=false)
    @JsonIgnore
    protected String creation_type;		//computed based on status of activity application
    
    @Column(columnDefinition = "BIT", length = 1)
    protected Boolean no_activity;
    
    @JsonIgnore
    @Column(columnDefinition = "BIT", length = 1)
    protected Boolean decomposed;
    
    @JsonIgnore
	@Transient
	protected String entrytype;
	
	@Transient
	@JsonIgnore
	protected String activitydate;
	
	@Transient
	protected String polygon_latlngs;
	
	@JsonIgnore
	@Transient
	protected String description;
	
	@JsonManagedReference("activitylocation-dates")
	@OneToMany(mappedBy="activitylocation",targetEntity=ActivityLocationDate.class, cascade = {CascadeType.ALL})
	@Valid
	protected List<ActivityLocationDate> activitydates;
	
	/*
	 * Getters/ Setters...
	 */
	public String getActivitydate() {
		if (this.activitydate==null) {
			if (this.getActivitydates()!=null) {
				SimpleDateFormat sdf = new SimpleDateFormat("d/M/y");
				Iterator<ActivityLocationDate> it = this.getActivitydates().iterator();
				StringBuffer result = new StringBuffer();
				while (it.hasNext()) {
					String dtVal = sdf.format(it.next().getActivity_date());
					if(result.length()>0) {
						result.append(",");
					}
					result.append(dtVal);
				}
				if (result.length()>0) {
					this.activitydate = result.toString(); 
				}
			}
		}
		return activitydate;
	}
	public ActivityLocation()
	{
	}	

	public ActivityLocation(ActivityLocation al)
	{
		this.creation_type = al.creation_type;
		this.decomposed = al.decomposed;
		this.description = al.description;
		this.entered_ogb_code = al.entered_ogb_code;
		this.entered_point = al.entered_point;
		this.entered_polygon = al.entered_polygon;
		this.entrytype = al.entrytype;
		this.lat = al.lat;
		this.lng = al.lng;
		this.no_activity = al.no_activity;
		this.polygon_latlngs = al.polygon_latlngs;
	}
	
	@Transient
	String[] formatStrings = {"d/M/y", "yyyy-MM-dd"};
	protected Date tryParse(String dateString)
	{
	    for (String formatString : formatStrings)
	    {
	        try
	        {
	            return new SimpleDateFormat(formatString).parse(dateString);
	        }
	        catch (Exception e) {
	        	Logger.info("Error parsing date: "+e.toString());
	        	//if the date is not valid flag it as an error?
	        }
	    }

	    return null;
	}
	/**
	 * Setting the activity date field also populates the list of activitydates for this item... 
	 * @param activitydate
	 */
	public void setActivitydate(String activitydate) {
		try {
			HashMap<String,Integer> hm = new HashMap<String,Integer>(); 
			this.activitydate = activitydate;
			if (!activitydate.equals("")) {
				
				String dates[] = this.getActivitydate().split(",");
				List<ActivityLocationDate> dateList = new ArrayList<ActivityLocationDate>();
				for (int i=0; i<dates.length; i++) {
					ActivityLocationDate ald = new ActivityLocationDate();
					
					Date dt = null;
					dt = tryParse(dates[i].trim());
					if (dt!=null && !hm.containsKey(dt.toString()))
					{
						hm.put(dt.toString(), 1);
						ald.setActivity_date(dt);
						ald.setActivitylocation(this);
						dateList.add(ald);
					}
				}
				this.setActivitydates(dateList);
			}
		} catch (Exception e) {
			Logger.error("Error setting activity date: "+e.getMessage());
		}
	}

	/**
	 * Gets the oil and gas block code
	 * @return
	 */
	public String getEntered_ogb_code() {
		return entered_ogb_code;
	}
	/**
	 * Sets the oil and gas block code
	 * @param entered_ogb_code
	 */
	public void setEntered_ogb_code(String entered_ogb_code) {
		this.entered_ogb_code = entered_ogb_code;
	}
	/**
	 * Gets the entered point
	 * @return
	 */
	public Point getEntered_point() {
		return entered_point;
	}
	/**
	 * Sets the entered point
	 * @param entered_point
	 */
	public void setEntered_point(Point entered_point) {
		this.entered_point = entered_point;
	}
	/**
	 * Gets the entered polygon
	 * @return
	 */
	public Polygon getEntered_polygon() {
		return entered_polygon;
	}
	/**
	 * Sets the entered polygon
	 * @param entered_polygon
	 */
	public void setEntered_polygon(Polygon entered_polygon) {
		this.entered_polygon = entered_polygon;
	}
	
	/**
	 * Gets the creation type
	 * @return
	 */
	public String getCreation_type() {
		if (this.creation_type==null) {
			if (this.getId() != null) {
				//Check if this is a good idea - getting hold of the persisted data
				ActivityLocation alDb = JPA.em().find(ActivityLocation.class, this.getId());
				this.setCreation_type(alDb.getCreation_type());
			} else {
				if (this.getAa().isSavingAsClosed()) {
					this.setCreation_type("Additional");
				} else {
					this.setCreation_type("Proposed");
				}
			}
		}
		return creation_type;
	}
	/**
	 * Sets the creation type
	 * @param creation_type
	 */
	public void setCreation_type(String creation_type) {
		this.creation_type = creation_type;
	}
	/**
	 * Checks for no activity for a location
	 * @return
	 */
	public Boolean getNo_activity() {
		if (no_activity != null)
			return no_activity;
		if (this.getEntered_polygon()!=null) {
			return true;
		}
		return no_activity;
	}
	/**
	 * Sets whether there is no activity
	 * @param no_activity
	 */
	public void setNo_activity(Boolean no_activity) {
		this.no_activity = no_activity;
	}
	
	/**
	 * Gets the latitude
	 * @return
	 */
	public Double getLat() {
		if (this.lat==null && this.getEntered_point()!=null) {
			latlngFromPoint();
		}
		return lat;
	}
	/**
	 * Sets the latitude
	 * @param lat
	 */
	public void setLat(Double lat) {
		this.lat = lat;
		this.latlngToPoint();
	}
	
	/**
	 * Converts the latitude/longitude to a point
	 */
	private void latlngToPoint() {
		//if lat and lng are both set, set the entered_point value too...
		if (this.getLat()!=null && this.getLng()!=null) {
			WKTReader wktReader = new WKTReader();
			try {
				Geometry geom = wktReader.read("Point(" + Double.toString(this.getLng())
					    + " " + Double.toString(this.getLat()) + ")");
				if (geom.getGeometryType().equals("Point")) {
					geom.setSRID(4326);
					if (geom.isValid()) {
						this.entered_point = (Point) geom;
					}
					
				}
			} catch (ParseException e) {
				Logger.error("Error converting lat long: "+e.getMessage());
			}
		}
	}
	/**
	 * Converts from a point to latitude/longitude
	 */
	private void latlngFromPoint() {
		if (this.entered_point!=null) {
			if (this.entered_point.getGeometryType().equals("Point")) {
				this.lat = this.entered_point.getY();
				this.lng = this.entered_point.getX();
			}
		}
	}
	
	/**
	 * Gets the longitude
	 * @return
	 */
	public Double getLng() {
		if (this.lng==null && this.getEntered_point()!=null) {
			latlngFromPoint();
		}
		return lng;
	}
	/**
	 * Sets the longitude
	 * @param lng
	 */
	public void setLng(Double lng) {
		this.lng = lng;
		this.latlngToPoint();
	}
	
	/**
	 * Gets a list of activity dates
	 * @return
	 */
    public List<ActivityLocationDate> getActivitydates() {
		return activitydates;
	}
    /**
     * Sets the activity dates
     * @param activitydates
     */
	public void setActivitydates(List<ActivityLocationDate> activitydates) {
		this.activitydates = activitydates;
	}
	/**
	 * Gets the entry type
	 * @return
	 */
	public String getEntrytype() {
		return entrytype;
	}
	/**
	 * Sets the entry type
	 * @param entrytype
	 */
	public void setEntrytype(String entrytype) {
		this.entrytype = entrytype;
	}
	
	/**
	 * Gets the description
	 * @return
	 */
	public String getDescription() {
		StringBuffer res = new StringBuffer("");
		if (!(this.getLat()==null)) {
			res.append(Messages.get("location.type.latlng"));
			res.append(" ");
			res.append(Double.toString(this.getLat()));
			res.append(" / ");
			res.append(Double.toString(this.getLng()));
		} else if (this.getEntered_ogb_code() != null && !this.getEntered_ogb_code().equals("")) {
			res.append(Messages.get("location.type.ogb"));
			res.append(" ");
			res.append(this.getEntered_ogb_code());
		} else if (this.getEntered_polygon() != null) {
			res.append(Messages.get("location.type.polygon"));
			res.append(" ");
			res.append(this.getPolygon_latlngs());
		}
		return res.toString();
	}
	/**
	 * Sets the description
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Gets the id
	 * @return
	 */
	public Long getId() {
		return id;
	}
	/**
	 * Sets the id
	 * @param id
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * Gets whether the location is "decomposed"
	 * @return
	 */
	public Boolean getDecomposed() {
		return decomposed;
	}
	/**
	 * Sets the location to be decomposed
	 * @param decomposed
	 */
	public void setDecomposed(Boolean decomposed) {
		this.decomposed = decomposed;
	}
	/**
	 * Gets the associated application
	 * @return
	 */
	@JsonBackReference("activity-activitylocation")
	public ActivityApplication getAa() {
		return aa;
	}
	/**
	 * Sets the associated application
	 * @param aa
	 */
	@JsonBackReference("activity-activitylocation")
	public void setAa(ActivityApplication aa) {
		this.aa = aa;
	}	
    
	/**
	 * Gets the polygon as a string of latitude and longitudes
	 * @return
	 */
    public String getPolygon_latlngs() {
    	if (this.polygon_latlngs == null) {
    		this.polygon_latlngs = this.polyToString();
    	}
		return polygon_latlngs;
	}
    
    /**
     * Builds a string using one point at a time converting polygon points
     * @param sb a string separated by spaces
     * @param ptData point data
     */
    private void appendPointText(StringBuilder sb, String ptData) {
    	String[] comps = ptData.trim().split(" ");
		if (comps.length == 2) {
			if (sb.length()>0) {
				sb.append(", ");
			}
			sb.append(comps[1].trim());
			sb.append(" / ");
			sb.append(comps[0].trim());
		} else {
			Logger.debug("Point with incorrect data: " + ptData);
		}
    }
    
    /**
     * Builds a string using one point at a time converting polygon points
     * @param sb a string separated by slashes
     * @param ptData point data
     */
    private void appendPointData(StringBuilder sb, String ptData) {
    	String[] comps = ptData.split("/");
		if (comps.length == 2) {
			if (sb.length()>0) {
				sb.append(", ");
			}
			sb.append(comps[1].trim());
			sb.append(" ");
			sb.append(comps[0].trim());
		} else {
			Logger.debug("Point with incorrect data: " + ptData);
		}
    }
    
    /**
     * Convert polygon text to a string of lat / lng values 
     * @return
     */
    public String polyToString() {
    	StringBuilder sb = new StringBuilder();
    	if (this.entered_polygon!=null) {
			String polyText = this.entered_polygon.toText();
			polyText = polyText.substring(polyText.indexOf("((") + 2, polyText.length() - 2);
			
			List<String> points = Arrays.asList(polyText.split(","));
			Iterator<String> it = points.iterator();
			while (it.hasNext()) {
				String ptData = it.next();
				appendPointText(sb, ptData);
			}
		}
    	return sb.toString();
    }
    
    /**
     * Convert polygon lat / lng values string into WKT format polygon 
     * @return
     */
    private String polyAsGeomString() {
    	//convert from the format passed in to a polygon to be stored...
    	StringBuilder sb = new StringBuilder();
		List<String> points = Arrays.asList(this.polygon_latlngs.split(","));
		//Now each point should be <lat_value> / <lng_value>
		//polygon entry must have a minimum of three points
		if (points.size()>2) {
			Iterator<String> it = points.iterator();
			
			while (it.hasNext()) {
				String ptData = it.next();
				appendPointData(sb, ptData);
			}
			if (!(points.get(0).trim().equals(points.get(points.size()-1).trim()))) {
				//Close the polygon...
				appendPointData(sb, points.get(0));
			}
			sb.insert(0, "POLYGON((");
			sb.append("))");
		}
		return sb.toString();
    }
    /**
     * Data entry for polygons allows a string value to be submitted containing
     * the lat / lng points around the polygon e.g. "48 / 1, 49 / 1, 49 / 2, 48 /2"
     * This must be converted into a (valid) polygon value.
     * 
     * @param polygon_latlngs the sequence of points (lat/lng) describing the polygon
     */
	public void setPolygon_latlngs(String polygon_latlngs) {
		this.polygon_latlngs = polygon_latlngs;
		if (!polygon_latlngs.equals("")) {
			WKTReader wktReader = new WKTReader();
			try {
				Geometry geom = wktReader.read(polyAsGeomString());
				if (geom.getGeometryType().equals("Polygon")) {
					geom.setSRID(4326);
					if (geom.isValid()) {
						this.entered_polygon = (Polygon) geom;
					}
					
				}
			} catch (ParseException e) {
				Logger.error("Error setting lat long: "+e.getMessage());
			}
		}
	}
	@PrePersist
	void preInsert() 
	{
		if (this.creation_type==null || this.creation_type.compareTo("")==0)
		{
			this.setCreation_type("Proposed");  // set default value for creation_type when created
		}
	}
	/**
     * Update this activity location and related activity date records.
     */
    public void update() {
    	this.checkAgainstAreas();
    	
    	if (this.getId()==null) 
    	{
    		JPA.em().persist(this);
    	} else {
    		//When we're updating, we need to remove any existing date info from the
    		//database and add the new values.  This is due to the way that date entry 
    		//is handled in the form, with a single field.
    		//Only if the current entity is not managed.
    		if (!JPA.em().contains(this)) { 
	    		ActivityLocation al = JPA.em().find(ActivityLocation.class, this.getId());
	    		if (al.getActivitydates()!=null) {
	    			Iterator<ActivityLocationDate> it = al.getActivitydates().iterator();
	    			while (it.hasNext()) {
	    				ActivityLocationDate ald = it.next();
	    				JPA.em().remove(ald);
	    			}
	    		}
    		}
    		JPA.em().merge(this);
    	}
    	//Add the date values created from the form.
    	if (this.getActivitydates() != null && (this.getNo_activity()==null || !this.getNo_activity())) 
    	{
    		HashMap<String, Integer> hm = new HashMap<String, Integer>();
        	Iterator<ActivityLocationDate> it = this.getActivitydates().iterator();
	        while(it.hasNext()) {
	        	ActivityLocationDate ad = it.next();
	        	if (ad.getActivity_date()!=null && !hm.containsKey(ad.getActivity_date().toString())) 
	        	{
	        		hm.put(ad.getActivity_date().toString(), 1);
	        		ad.setActivitylocation(this);
	        	}
	        }
	        JPA.em().flush();
    	}
    }
    
	/**
	 * If the current entry is not a polygon entry, see if it matches against
	 * any proposed polygon entries for this activity application.  If it does match, then we
	 * will set it as a "Proposed" creation type, even if this item is created at close out.
	 * In this case we also set the "Decomposed" flag.
	 */
    private void checkAgainstAreas() {
		if (this.getEntered_polygon()!=null) {
			return;
		}
		//As we don't allow the entry fields to be updated for existing items lookup 
		//the values stored in the database
		ActivityLocation alCheck = this;
		//check if the current item has already been saved..
		if (this.getId()!=null) {
	    	alCheck = JPA.em().find(ActivityLocation.class, this.getId());
		}
		
		if (alCheck.getAa()!=null && alCheck.getEntered_ogb_code()!=null && !alCheck.getEntered_ogb_code().equals("")) {
			OilAndGasBlock ogb = OilAndGasBlock.findByCode(alCheck.getEntered_ogb_code()).get(0);
			if (ogb!=null) {
				Polygon polygon = ogb.getGeom();
				TypedQuery<ActivityLocation> query = JPA.em().createNamedQuery("ActivityLocation.findProposedPolygonEntriesByPolygon", ActivityLocation.class);
				query.setParameter("polygon", polygon);
				query.setParameter("aa_id", alCheck.getAa().getId());
				List<ActivityLocation> results = query.getResultList();
				if (!results.isEmpty()) {
					this.setCreation_type("Proposed");
					this.setDecomposed(true);
				}
			}
		}
		
		if (alCheck.getAa()!=null && alCheck.getLat()!= null && alCheck.getLng()!=null) {
			//Look for intersecting polygon entries...
			TypedQuery<ActivityLocation> query = JPA.em().createNamedQuery("ActivityLocation.findProposedPolygonEntriesByPoint", ActivityLocation.class);
			query.setParameter("point", alCheck.getEntered_point());
			query.setParameter("aa_id", alCheck.getAa().getId());
			List<ActivityLocation> results = query.getResultList();
			if (!results.isEmpty()) {
				this.setCreation_type("Proposed");
				this.setDecomposed(true);
			}
		}
		
	}
	/**
	 * validate
	 * 
	 * Custom validation for the ActivityLocation entity.
	 * @return list of validation errors
	 */
	public List<ValidationError> validate() 
	{
		List<ValidationError> errors = new ArrayList<ValidationError>();
		
		try
		{
			ActivityApplication aa = this.getAa();
			if (aa != null) {
		    	if (aa.getId() != null && aa.getStatus()==null) {
		    		aa = JPA.em().find(ActivityApplication.class, aa.getId());
		    	}
		    }
			
			if (this.getEntered_ogb_code()!=null && !this.getEntered_ogb_code().equals("")) {
	    		//Block code
				List<OilAndGasBlock> blocks = OilAndGasBlock.findByCode(this.getEntered_ogb_code());
	    		if (blocks.isEmpty()) {
	    			errors.add(new ValidationError("entered_ogb_code",  Messages.get("validation.entered_ogb_code.invalid")));
	    		}
			}
			if (this.getPolygon_latlngs()!=null && !this.getPolygon_latlngs().equals("")) {
				WKTReader wktReader = new WKTReader();
				try {
					Geometry geom = wktReader.read(polyAsGeomString());
					if (geom.getGeometryType().equals("Polygon")) {
						geom.setSRID(4326);
						if (!geom.isValid()) {
							errors.add(new ValidationError("polygon_latlngs",  Messages.get("validation.polygon_latlngs.invalid")));
						}
						//Check that the polygon falls within the bounds
						Geometry validArea = wktReader.read(VALID_AREA_POLYGON);
						validArea.setSRID(4326);
						if (!geom.within(validArea)) {
							errors.add(new ValidationError("polygon_latlngs", Messages.get("validation.polygon_latlngs.bounds")));
						}
					}
				} catch (ParseException e) {
					// 
					errors.add(new ValidationError("polygon_latlngs",  Messages.get("validation.polygon_latlngs.parseerror")));
				}
			}
			
			if ((aa!=null && aa.isSavingAsClosed()) || (getId()==null && getCreation_type().equals("Additional"))) 
			{
				//for closing, there must be dates or this item must be marked as no activity...
				if (this.getCreation_type().equals("Proposed") && (this.getNo_activity() == null)) {
					errors.add(new ValidationError("no_activity",  Messages.get("validation.required")));
				}
				if (this.getNo_activity() == null || !this.getNo_activity()) {
					if (this.getActivitydates() == null || this.getActivitydates().isEmpty()) {
						errors.add(new ValidationError("activitydate", Messages.get("validation.appcloseout.datesrequired")));
					} else {
						//Validate the activity date entries...
						Iterator<ActivityLocationDate> it = this.getActivitydates().iterator();
						while (it.hasNext()) {
							ActivityLocationDate ald = it.next();
							ald.setActivitylocation(this);
							List<ValidationError> locationdate_errors = ald.validate();
							if (locationdate_errors!=null) {
								Iterator<ValidationError> itErr = locationdate_errors.iterator();
								while (itErr.hasNext()) {
									ValidationError err=itErr.next();
									errors.add(new ValidationError("activitydate", err.message()));
								}
							}
						}
					}
				}
			}			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Logger.error("Error in validate: "+e.toString());
			errors.add(new ValidationError("",  Messages.get("error.something.went.wrong")));
		}
		return errors.isEmpty() ? null : errors;
	}
}
