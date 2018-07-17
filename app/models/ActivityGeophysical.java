package models;

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
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="activitygeophysical")
public class ActivityGeophysical
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "activitygeophysical_seq_gen")
    @SequenceGenerator(name = "activitygeophysical_seq_gen", sequenceName = "activitygeophysical_id_seq")
    @Column(columnDefinition = "serial")
    protected Long id;   
    
    //@JsonBackReference("activity-activitygeophysical")
    @JsonIgnore
    @OneToOne(optional=false)
    @JoinColumn(name="activityapplication_id",referencedColumnName="id")
    protected ActivityApplication aa;
    
	@Column(length=10)
	@NotBlank(message="validation.required")
	protected String source;
	
	@Min(value=1, message="validation.min")
	@NotNull(message="validation.required")
	protected Integer frequency;
	
	@Min(value=1, message="validation.min")
	@Max(value=500, message="validation.max")
	@NotNull(message="validation.required")
	protected Integer sound_pressure_level;
	
	@Min(value=1, message="validation.min")
	@Max(value=500, message="validation.max")
	@NotNull(message="validation.required")	
	protected Integer sound_exposure_level;
	
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
	 * Constructor
	 */
	public ActivityGeophysical() {
		super();
	}
}