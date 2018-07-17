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

import play.db.jpa.JPA;
import play.i18n.Messages;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="activitysubbottomprofilers")
public class ActivitySubBottomProfilers extends DefaultableActivity
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "activitysubbottomprofilers_seq_gen")
    @SequenceGenerator(name = "activitysubbottomprofilers_seq_gen", sequenceName = "activitysubbottomprofilers_id_seq")
    @Column(columnDefinition = "serial")
    protected Long id;   
    
    @JsonIgnore
    @OneToOne(optional=false)
    @JoinColumn(name="activityapplication_id",referencedColumnName="id")
    protected ActivityApplication aa;
    
	@Column(length=10)
	@NotBlank(message="validation.required")
	protected String source;
	
	@Min(value=1, message="validation.min")
	protected Integer frequency;
	
	@Min(value=1, message="validation.min")
	@Max(value=500, message="validation.max")
	protected Integer sound_pressure_level;
	
	@Min(value=1, message="validation.min")
	@Max(value=500, message="validation.max")
	protected Integer sound_exposure_level;

	@Min(value=1, message="validation.min")
	protected Integer frequency_actual;
	
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
	 * Sets the id
	 * @param id
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * Gets the associated application
	 * @return
	 */
	public ActivityApplication getAa() {
		return aa;
	}
	/**
	 * Sets the associated application
	 * @param aa
	 */
	public void setAa(ActivityApplication aa) {
		this.aa = aa;
	}
	/**
	 * Gets the source
	 * @return
	 */
	public String getSource() {
		return source;
	}
	/**
	 * Sets the source
	 * @param source
	 */
	public void setSource(String source) {
		this.source = source;
	}
	/**
	 * Gets the frequency
	 * @return
	 */
	public Integer getFrequency() {
		return frequency;
	}
	/**
	 * Sets the frequency
	 * @param frequency
	 */
	public void setFrequency(Integer frequency) {
		this.frequency = frequency;
	}
	/**
	 * Gets the sound pressure level
	 * @return
	 */
	public Integer getSound_pressure_level() {
		return sound_pressure_level;
	}
	/**
	 * Sets the sound pressure level
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
	 * Gets the actual frequency
	 * @return the frequency
	 */
	public Integer getFrequency_actual() {
		return frequency_actual;
	}
	/**
	 * Sets the actual frequency
	 * @param frequency the new frequency
	 */
	public void setFrequency_actual(Integer frequency_actual) {
		this.frequency_actual = frequency_actual;
	}
	/**
	 * Gets the actual sound pressure level
	 * @return the sound pressure level
	 */
	public Integer getSound_pressure_level_actual() {
		return sound_pressure_level_actual;
	}
	/**
	 * Sets the actual sound pressure level
	 * @param sound_pressure_level the new pressure level
	 */
	public void setSound_pressure_level_actual(Integer sound_pressure_level_actual) {
		this.sound_pressure_level_actual = sound_pressure_level_actual;
	}
	/**
	 * Gets the actual sound exposure level
	 * @return the sound exposure level
	 */
	public Integer getSound_exposure_level_actual() {
		return sound_exposure_level_actual;
	}
	/**
	 * Sets the actual sound exposure level
	 * @param sound_exposure_level the new exposure level
	 */
	public void setSound_exposure_level_actual(Integer sound_exposure_level_actual) {
		this.sound_exposure_level_actual = sound_exposure_level_actual;
	}
	/**
	 * Constructor
	 */
	public ActivitySubBottomProfilers() {
		super();
	}
	/**
	 * Get the source types as a list for use in options
	 * @return
	 */
	public static LinkedHashMap<String, String> sourceOptions() {
		LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();

		options.put("Pinger", sourceValue("Pinger"));
        options.put("Boomer", sourceValue("Boomer"));
        options.put("Sparker", sourceValue("Sparker"));
        options.put("Chirp", sourceValue("Chirp"));

    	return options;
	}
	/**
	 * Gets a message translated version as a string.  The list configured in this class
	 * should match the values in the database 
	 * @param s the source value as stored in the database
	 * @return
	 */
	public static String sourceValue(String s)
	{
		if (s.compareTo("Pinger")==0)
			return Messages.get("field_option.pinger");
		if (s.compareTo("Boomer")==0)
			return Messages.get("field_option.boomer");
		if (s.compareTo("Sparker")==0)
			return Messages.get("field_option.sparker");
		if (s.compareTo("Chirp")==0)
			return Messages.get("field_option.chirp");
		return Messages.get("field_option.unknown");
	}
	@Override
	public void populateDefaults() {
		if (getFrequency_actual()==null)
			setFrequency_actual(getFrequency());
		if (getSound_pressure_level_actual()==null)
			setSound_pressure_level_actual(getSound_pressure_level());
		if (getSound_exposure_level_actual()==null)
			setSound_exposure_level_actual(getSound_exposure_level());		
	}

	@Override
	public void mergeActuals(Map<String, String> m) {
		setSound_exposure_level_actual(getSafeInt(m,"activitySubBottomProfilers.sound_exposure_level_actual"));
		setSound_pressure_level_actual(getSafeInt(m,"activitySubBottomProfilers.sound_pressure_level_actual"));
		setFrequency_actual(getSafeInt(m,"activitySubBottomProfilers.frequency_actual"));
		JPA.em().merge(this);	
	}
}