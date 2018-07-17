package models;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;

import play.db.jpa.JPA;
import play.i18n.Messages;

@Entity
@Table(name="activityseismic")
public class ActivitySeismic extends DefaultableActivity
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "activityseismic_seq_gen")
    @SequenceGenerator(name = "activityseismic_seq_gen", sequenceName = "activityseismic_id_seq")
    @Column(columnDefinition = "serial")
    protected Long id;   
    
    @JsonIgnore
	@OneToOne(optional=false)
    @JoinColumn(name="activityapplication_id",referencedColumnName="id")
    protected ActivityApplication aa;
    
	@Column(length=10)
	@NotBlank(message="validation.required")
	protected String survey_type;
	
	@Column(length=2)
	@NotBlank(message="validation.required")
	protected String data_type;
	
	@Column(length=50)
	protected String other_survey_type;

	@Min(value=1, message="validation.min")
	protected Integer max_airgun_volume;
	
	@Min(value=1, message="validation.min")
	@Max(value=500, message="validation.max")
	protected Integer sound_pressure_level;
	
	@Min(value=1, message="validation.min")
	@Max(value=500, message="validation.max")
	protected Integer sound_exposure_level;
	
	@Min(value=1, message="validation.min")
	protected Integer max_airgun_volume_actual;
	
	@Min(value=1, message="validation.min")
	@Max(value=500, message="validation.max")
	protected Integer sound_pressure_level_actual;
	
	@Min(value=1, message="validation.min")
	@Max(value=500, message="validation.max")
	protected Integer sound_exposure_level_actual;
	
	/**
	 * Gets the id
	 * @return
	 */
	public Long getId() {
		return id;
	}
	/**
	 * Gets the associated application
	 * @return
	 */
	public ActivityApplication getAa() {
		return aa;
	}
	/**
	 * Sets the application
	 * @param aa
	 */
	public void setAa(ActivityApplication aa) {
		this.aa = aa;
	}
	/**
	 * Gets the survey type
	 * @return
	 */
	public String getSurvey_type() {
		return survey_type;
	}
	/**
	 * Sets the survey type
	 * @param survey_type
	 */
	public void setSurvey_type(String survey_type) {
		this.survey_type = survey_type;
	}
	/**
	 * Gets the data type
	 * @return
	 */
	public String getData_type() {
		return data_type;
	}
	/**
	 * Sets the data type
	 * @param data_type
	 */
	public void setData_type(String data_type) {
		this.data_type = data_type;
	}
	/**
	 * Gets the other survey type
	 * @return
	 */
	public String getOther_survey_type() {
		return other_survey_type;
	}
	/**
	 * Sets the other survey type
	 * @param other_survey_type
	 */
	public void setOther_survey_type(String other_survey_type) {
		this.other_survey_type = other_survey_type;
	}
	/**
	 * Gets the maximum airgun volume
	 * @return
	 */
	public Integer getMax_airgun_volume() {
		return max_airgun_volume;
	}
	/**
	 * Sets the maximum airgun volume
	 * @param max_airgun_volume
	 */
	public void setMax_airgun_volume(Integer max_airgun_volume) {
		this.max_airgun_volume = max_airgun_volume;
	}
	/**
	 * Gets the sound pressure level
	 * @return
	 */
	public Integer getSound_pressure_level() {
		return sound_pressure_level;
	}
	/**
	 * Sets sound pressure level
	 * @param sound_pressure_level
	 */
	public void setSound_pressure_level(Integer sound_pressure_level) {
		this.sound_pressure_level = sound_pressure_level;
	}
	/**
	 * Gets the sound exposure level
	 * @return
	 */
	public Integer getSound_exposure_level() {
		return sound_exposure_level;
	}
	/**
	 * Sets the sound exposure level
	 * @param sound_exposure_level
	 */
	public void setSound_exposure_level(Integer sound_exposure_level) {
		this.sound_exposure_level = sound_exposure_level;
	}
	/**
	 * Gets the actual maximum airgun volume
	 * @return
	 */	
	public Integer getMax_airgun_volume_actual() {
		return max_airgun_volume_actual;
	}
	/**
	 * Sets the actual maximum airgun volume
	 * @param max_airgun_volume
	 */
	public void setMax_airgun_volume_actual(Integer max_airgun_volume_actual) {
		this.max_airgun_volume_actual = max_airgun_volume_actual;
	}
	/**
	 * Gets the actual sound pressure level
	 * @return
	 */
	public Integer getSound_pressure_level_actual() {
		return sound_pressure_level_actual;
	}
	/**
	 * Sets actual sound pressure level
	 * @param sound_pressure_level
	 */
	public void setSound_pressure_level_actual(Integer sound_pressure_level_actual) {
		this.sound_pressure_level_actual = sound_pressure_level_actual;
	}
	/**
	 * Gets the actual sound exposure level
	 * @return
	 */
	public Integer getSound_exposure_level_actual() {
		return sound_exposure_level_actual;
	}
	/**
	 * Sets the actual sound exposure level
	 * @param sound_exposure_level
	 */
	public void setSound_exposure_level_actual(Integer sound_exposure_level_actual) {
		this.sound_exposure_level_actual = sound_exposure_level_actual;
	}
	/**
	 * Sets the id
	 * @param id
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * Constructor
	 */
	public ActivitySeismic() 
	{
		
	}
	/**
	 * Get the survey types as a list for use in options
	 * @return
	 */
	public static LinkedHashMap<String, String> surveyTypeOptions() {
		LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();

		options.put("OBC/OBN", surveyTypeValue("OBC/OBN"));
        options.put("VSP", surveyTypeValue("VSP"));
        options.put("Site", surveyTypeValue("Site"));
        options.put("Regional", surveyTypeValue("Regional"));
        options.put("Reservoir", surveyTypeValue("Reservoir"));
        options.put("Route", surveyTypeValue("Route"));
        options.put("Other", surveyTypeValue("Other"));

    	return options;
	}
	/**
	 * Gets a message translated version as a string.  The list configured in this class
	 * should match the values in the database 
	 * @param s the survey type value as stored in the database
	 * @return
	 */
	public static String surveyTypeValue(String s)
	{
		if (s.compareTo("OBC/OBN")==0)
			return Messages.get("field_option.OBCOBN");
		if (s.compareTo("VSP")==0)
			return Messages.get("field_option.VSP");
		if (s.compareTo("Site")==0)
			return Messages.get("field_option.Site");
		if (s.compareTo("Regional")==0)
			return Messages.get("field_option.Regional");
		if (s.compareTo("Reservoir")==0)
			return Messages.get("field_option.Reservoir");
		if (s.compareTo("Route")==0)
			return Messages.get("field_option.Route");
		if (s.compareTo("Other")==0)
			return Messages.get("field_option.Other");
		return Messages.get("field_option.unknown");
	}
	/**
	 * Get the data types as a list for use in options
	 * @return
	 */
	public static LinkedHashMap<String, String> dataTypeOptions() {
		LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();

		options.put("2D", surveyTypeValue("2D"));
        options.put("3D", surveyTypeValue("3D"));
        options.put("4D", surveyTypeValue("4D"));
        
    	return options;
	}
	/**
	 * Gets a message translated version as a string.  The list configured in this class
	 * should match the values in the database 
	 * @param s the data type value as stored in the database
	 * @return
	 */
	public static String dataTypeValue(String s)
	{
		if (s.compareTo("2D")==0)
			return Messages.get("field_option.2D");
		if (s.compareTo("3D")==0)
			return Messages.get("field_option.3D");
		if (s.compareTo("4D")==0)
			return Messages.get("field_option.4D");
		return Messages.get("field_option.unknown");
	}
	@Override
	public void populateDefaults() 
	{
		if (getMax_airgun_volume_actual()==null)
			setMax_airgun_volume_actual(getMax_airgun_volume());
		if (getSound_pressure_level_actual()==null)
			setSound_pressure_level_actual(getSound_pressure_level());
		if (getSound_exposure_level_actual()==null)
			setSound_exposure_level_actual(getSound_exposure_level());
	}
	@Override
	public void mergeActuals(Map<String, String> m)
	{
		setSound_exposure_level_actual(getSafeInt(m,"activitySeismic.sound_exposure_level_actual"));
		setSound_pressure_level_actual(getSafeInt(m,"activitySeismic.sound_pressure_level_actual"));
		setMax_airgun_volume_actual(getSafeInt(m,"activitySeismic.max_airgun_volume_actual"));
		JPA.em().merge(this);
	}	
}
