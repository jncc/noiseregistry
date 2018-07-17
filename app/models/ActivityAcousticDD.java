package models;

import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import play.db.jpa.JPA;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wordnik.swagger.annotations.ApiModelProperty;

@Entity
@Table(name="activityacousticdd")
public class ActivityAcousticDD extends DefaultableActivity
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "activityacousticdd_seq_gen")
    @SequenceGenerator(name = "activityacousticdd_seq_gen", sequenceName = "activityacousticdd_id_seq")
    @Column(columnDefinition = "serial")
    @ApiModelProperty(position=0)
    protected Long id;   
    
    @JsonIgnore
	@ManyToOne(optional=true)
    @JoinColumn(name="activityapplication_id",referencedColumnName="id")
    protected ActivityApplication aa;
    
    @Min(value=1, message="validation.min")
    @ApiModelProperty(position=1)
	protected Integer frequency;
    
    @Min(value=1, message="validation.min")
    @Max(value=500, message="validation.max")
    @ApiModelProperty(position=2)
	protected Integer sound_pressure_level;
    
    @Min(value=1, message="validation.min")
    @Max(value=500, message="validation.max")
    @ApiModelProperty(position=3)
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
     * Get the id of the activity type
     * @return the id
     */
	public Long getId() {
		return id;
	}
	/**
	 * Sets the id of the activity type
	 * @param id the new id
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * Gets the associated activityapplication
	 * @return the activity application
	 */
	public ActivityApplication getAa() {
		return aa;
	}
	/**
	 * Sets an associated activity application
	 * @param aa the ActivityApplication 
	 */
	public void setAa(ActivityApplication aa) {
		this.aa = aa;
	}
	/**
	 * Gets the frequency
	 * @return the frequency
	 */
	public Integer getFrequency() {
		return frequency;
	}
	/**
	 * Sets the frequency
	 * @param frequency the new frequency
	 */
	public void setFrequency(Integer frequency) {
		this.frequency = frequency;
	}
	/**
	 * Gets the sound pressure level
	 * @return the sound pressure level
	 */
	public Integer getSound_pressure_level() {
		return sound_pressure_level;
	}
	/**
	 * Sets the sound pressure level
	 * @param sound_pressure_level the new pressure level
	 */
	public void setSound_pressure_level(Integer sound_pressure_level) {
		this.sound_pressure_level = sound_pressure_level;
	}
	/**
	 * Gets the sound exposure level
	 * @return the sound exposure level
	 */
	public Integer getSound_exposure_level() {
		return sound_exposure_level;
	}
	/**
	 * Sets the sound exposure level
	 * @param sound_exposure_level the new exposure level
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
	public ActivityAcousticDD() {
		super();
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
		setSound_exposure_level_actual(getSafeInt(m,"activityAcousticDD.sound_exposure_level_actual"));
		setSound_pressure_level_actual(getSafeInt(m,"activityAcousticDD.sound_pressure_level_actual"));
		setFrequency_actual(getSafeInt(m,"activityAcousticDD.frequency_actual"));
		JPA.em().merge(this);	
	}	
	
}