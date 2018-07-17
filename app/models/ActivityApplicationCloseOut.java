package models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.wordnik.swagger.annotations.ApiModelProperty;

import play.Logger;
import play.data.validation.ValidationError;
import play.data.validation.Constraints.MaxLength;
import play.db.jpa.JPA;
import play.i18n.Messages;

/**
 * Model class used to support the partial update of Activity Applications
 * with close out data (either interim close out or final).
 * 
 * This is needed so that the framework validation can be used with the bind from request
 * without having to resubmit the entire ActivityApplication object.
 */
@Entity
@Table(name="activityapplication")
public class ActivityApplicationCloseOut {

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "activityapplication_seq_gen")
    @SequenceGenerator(name = "activityapplication_seq_gen", sequenceName = "activityapplication_id_seq")
    @Column(columnDefinition = "serial")
	@ApiModelProperty(position=0)
    protected Long id;
	
    @JsonIgnore
    protected Long activitytype_id;
    
    @JsonIgnore
    @Column(length=500)
    @MaxLength(500)
    protected String supp_info;
    
    public String getSupp_info() {
		return supp_info;
	}
	public void setSupp_info(String supp_info) {
		this.supp_info = supp_info;
	}
	
	@Transient
    @JsonIgnore
    protected String csvupload;

	public String getCsvupload() {
		return csvupload;
	}
	public void setCsvupload(String csvupload) {
		this.csvupload = csvupload;
	}

	@Transient
	@ApiModelProperty(position=1)
	protected String interimcloseout;

	@JsonManagedReference("activity-activitylocation")
	@OneToMany(mappedBy="aa",
				targetEntity=ActivityLocation.class,
				fetch=FetchType.EAGER,
				cascade = {CascadeType.ALL})
	@OrderBy
	@Valid
	@ApiModelProperty(position=18)
	@Transient
    protected List<ActivityLocation> activitylocations; 
	
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
	@JsonManagedReference("activity-activityacousticdd")
    @OneToOne(mappedBy="aa",
    			targetEntity=ActivityAcousticDDCloseOut.class,
    			fetch=FetchType.LAZY,
    			optional=true,
    			orphanRemoval=true,
    			cascade = {CascadeType.ALL})
	@Valid
	@ApiModelProperty(position=11)
    protected ActivityAcousticDDCloseOut activityAcousticDD;
	
	@JsonManagedReference("activity-activityexplosives")
    @OneToOne(mappedBy="aa",
    			targetEntity=ActivityExplosivesCloseOut.class,
    			fetch=FetchType.LAZY,
    			optional=true,
    			orphanRemoval=true,
    			cascade = {CascadeType.ALL})
	@Valid
	@ApiModelProperty(position=12)
    protected ActivityExplosivesCloseOut activityExplosives;
	
	@JsonManagedReference("activity-activitymod")
    @OneToOne(mappedBy="aa",
    			targetEntity=ActivityMoDCloseOut.class,
    			fetch=FetchType.LAZY,
    			optional=true,
    			orphanRemoval=true,
    			cascade = {CascadeType.ALL})
	@Valid
	@ApiModelProperty(position=14)
    protected ActivityMoDCloseOut activityMoD; 	
	
	@JsonManagedReference("activity-activitymultibeames")
    @OneToOne(mappedBy="aa",
    			targetEntity=ActivityMultibeamESCloseOut.class,
    			fetch=FetchType.LAZY,
    			optional=true,
    			orphanRemoval=true,
    			cascade = {CascadeType.ALL})
	@Valid
	@ApiModelProperty(position=15)
    protected ActivityMultibeamESCloseOut activityMultibeamES;
	
	@JsonManagedReference("activity-activitypiling")
    @OneToOne(mappedBy="aa",
    			targetEntity=ActivityPilingCloseOut.class,
    			fetch=FetchType.LAZY,
    			optional=true,
    			orphanRemoval=true,
    			cascade = {CascadeType.ALL})
	@Valid
	@ApiModelProperty(position=16)
    protected ActivityPilingCloseOut activityPiling;
    
	@JsonManagedReference("activity-activityseismic")
    @OneToOne(mappedBy="aa",
    			targetEntity=ActivitySeismicCloseOut.class,
    			fetch=FetchType.LAZY,
    			optional=true,
    			orphanRemoval=true,
		    	cascade = {CascadeType.ALL})
    @Valid
    @ApiModelProperty(position=17)
    protected ActivitySeismicCloseOut activitySeismic;
	
	@JsonManagedReference("activity-activitysubbottomprofilers")
    @OneToOne(mappedBy="aa",
    			targetEntity=ActivitySubBottomProfilersCloseOut.class,
    			fetch=FetchType.LAZY,
    			optional=true,
    			orphanRemoval=true,
    			cascade = {CascadeType.ALL})
	@Valid
	@ApiModelProperty(position=13)
    protected ActivitySubBottomProfilersCloseOut activitySubBottomProfilers;
	
    protected String status;
    
    @Transient
    @JsonIgnore
    protected String mustconfirm="";
    
    public String getMustconfirm() {
		return mustconfirm;
	}
	public void setMustconfirm(String mustconfirm) {
		this.mustconfirm = mustconfirm;
	}
    
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public ActivityExplosivesCloseOut getActivityExplosives() {
		return activityExplosives;
	}

	public void setActivityExplosives(ActivityExplosivesCloseOut activityExplosives) {
		this.activityExplosives = activityExplosives;
	}

	public ActivityAcousticDDCloseOut getActivityAcousticDD() {
		return activityAcousticDD;
	}

	public void setActivityAcousticDD(ActivityAcousticDDCloseOut activityAcousticDD) {
		this.activityAcousticDD = activityAcousticDD;
	}

	public ActivityMoDCloseOut getActivityMoD() {
		return activityMoD;
	}

	public void setActivityMoD(ActivityMoDCloseOut activityMoD) {
		this.activityMoD = activityMoD;
	}

	public ActivityMultibeamESCloseOut getActivityMultibeamES() {
		return activityMultibeamES;
	}

	public void setActivityMultibeamES(ActivityMultibeamESCloseOut activityMultibeamES) {
		this.activityMultibeamES = activityMultibeamES;
	}

	public ActivityPilingCloseOut getActivityPiling() {
		return activityPiling;
	}

	public void setActivityPiling(ActivityPilingCloseOut activityPiling) {
		this.activityPiling = activityPiling;
	}

	public ActivitySeismicCloseOut getActivitySeismic() {
		return activitySeismic;
	}

	public void setActivitySeismic(ActivitySeismicCloseOut activitySeismic) {
		this.activitySeismic = activitySeismic;
	}

	public ActivitySubBottomProfilersCloseOut getActivitySubBottomProfilers() {
		return activitySubBottomProfilers;
	}

	public void setActivitySubBottomProfilers(
			ActivitySubBottomProfilersCloseOut activitySubBottomProfilers) {
		this.activitySubBottomProfilers = activitySubBottomProfilers;
	}
	
	/**
	 * Gets the interim closeout status
	 * @return
	 */
	public String getInterimcloseout() {
		return interimcloseout;
	}

	/**
	 * Sets the interim closeout status
	 * @param interimcloseout
	 */
	public void setInterimcloseout(String interimcloseout) {
		this.interimcloseout = interimcloseout;
	}

	/**
	 * Gets the activity locations
	 * @return
	 */
	public List<ActivityLocation> getActivitylocations() {
		return activitylocations;
	}

	/**
	 * Sets the activity locations
	 * @param activitylocations
	 */
	public void setActivitylocations(List<ActivityLocation> activitylocations) {
		this.activitylocations = activitylocations;
		Iterator<ActivityLocation> it = activitylocations.iterator();
		while (it.hasNext())
		{
			ActivityLocation al = it.next();
			if (al.getActivitydates()!=null && al.getActivitydates().size() > 0)
				al.setNo_activity(false);
		}
	}
	
	/**
	 * Gets the activity type as a number
	 * @return
	 */
	public Long getActivitytype_id() {
		return activitytype_id;
	}
	/**
	 * Sets the activity type
	 * @param activitytype_id
	 */
	public void setActivitytype_id(Long activitytype_id) {
		this.activitytype_id = activitytype_id;
	}
	
	public void populateActivityDefaults()
	{
		if (getActivitytype_id()==ActivityTypes.Seismic_Survey.toLong())
        {       	
        	this.getActivitySeismic().populateDefaults();        
        }
        else if (getActivitytype_id()==ActivityTypes.Sub_Bottom_Profilers.toLong())
        {
        	this.getActivitySubBottomProfilers().populateDefaults();
        }
        else if (getActivitytype_id()==ActivityTypes.Piling.toLong()) 
        {
        	this.getActivityPiling().populateDefaults();
        }
        else if (getActivitytype_id()==ActivityTypes.Explosives.toLong())
        {
        	this.getActivityExplosives().populateDefaults();
        }
        else if (getActivitytype_id()==ActivityTypes.Acoustic_Deterrent_Device.toLong()) 
        {
        	this.getActivityAcousticDD().populateDefaults();
        }
        else if (getActivitytype_id()==ActivityTypes.Multibeam_Echosounders.toLong()) 
        {
        	this.getActivityMultibeamES().populateDefaults();
        }
        else if (getActivitytype_id()==ActivityTypes.MoD.toLong()) 
        {
        	this.getActivityMoD().populateDefaults();
        }
	}
	
	/**
	 * Close out the application
	 * @param id the id of the application
	 * @throws Exception
	 */
	public String closeOut(Long id, Map<String, String> m,boolean bAllowStatusChange) throws Exception {
		//Perform the close out operation
		if (getInterimcloseout()!=null) {
			return ActivityApplication.closeOut(this, id, true, m, bAllowStatusChange);
		} else {
			return ActivityApplication.closeOut(this, id, false, m, bAllowStatusChange);
		}
	}

	/**
	 * validate
	 * 
	 * Custom validation for the ActivityApplicationCloseOut.  Attaches location records to the 
	 * parent ActivityApplication to ensure that the validation dependencies work correctly.
	 * 
	 */
	public List<ValidationError> validate() {
		List<ValidationError> errors = new ArrayList<ValidationError>();
		//Get hold of the parent activity application, as the detached locations submitted may not be linked...
		ActivityApplication aa = ActivityApplication.findById(this.getId());
		boolean bHasActivity = false;
		try
		{
			if (aa.getCsvclosing().booleanValue())
				return null;

			if (this.getActivitylocations() != null) {
				//for each activity location, make sure that either no activity has been set, or date(s) entered
				Iterator<ActivityLocation> it = this.getActivitylocations().iterator();
				while (it.hasNext()) {
					ActivityLocation al = it.next();
					//if the location has previously been stored, make sure that the parent activity application
					//matches our current application
					if (al.getId()!=null) {
						ActivityLocation alDb = JPA.em().find(ActivityLocation.class, al.getId());
						if (alDb.getAa() != aa) {
							throw new Exception("Attempt to update ActivityLocation from different ActivityApplication.");
						}
					}
					//Ensure that the location is attached to the parent ActivityApplication entity
					if (al.getAa()==null) {
						al.setAa(aa);
					}
					List<ValidationError> location_errors = al.validate();
					if (location_errors!=null) {
						Iterator<ValidationError> itErr = location_errors.iterator();
						while (itErr.hasNext()) {
							ValidationError err=itErr.next();
							errors.add(new ValidationError("activitylocations[" + this.getActivitylocations().indexOf(al) + "]." + err.key(), err.message()));
						}
					}
				}
				//If the locations and optional dates are valid, we require that there is at least one date value set
				if (errors.isEmpty()) {
					it = this.getActivitylocations().iterator();
					while (it.hasNext()) {
						ActivityLocation al = it.next();
						if ((al.getNo_activity()==null || !al.getNo_activity()) && al.getActivitydates()!=null) {
							if (!al.getActivitydates().isEmpty()) {
								bHasActivity = true;
							}
						}
					}
					if (!bHasActivity) {
						errors.add(new ValidationError("",  Messages.get("validation.appcloseout.noactivity")));
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
    public static ActivityApplicationCloseOut findById(Long id) {
    	TypedQuery<ActivityApplicationCloseOut> query = JPA.em().createQuery("from ActivityApplicationCloseOut aa where aa.id = :id", ActivityApplicationCloseOut.class);
		
    	query.setParameter("id", id);
    	List<ActivityApplicationCloseOut> results = query.getResultList();
    	if (!results.isEmpty()) {
    		return results.get(0);
    	}
    	return null;
    }	
}
