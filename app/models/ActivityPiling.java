package models;

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

import play.db.jpa.JPA;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="activitypiling")
public class ActivityPiling extends DefaultableActivity
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "activitypiling_seq_gen")
    @SequenceGenerator(name = "activitypiling_seq_gen", sequenceName = "activitypiling_id_seq")
    @Column(columnDefinition = "serial")
    protected Long id;   
    
    @JsonIgnore
    @OneToOne(optional=false)
    @JoinColumn(name="activityapplication_id",referencedColumnName="id")
    protected ActivityApplication aa;
    
    @Min(value=1, message="validation.min")
	protected Integer max_hammer_energy;
    
    @Min(value=1, message="validation.min")
    @Max(value=500, message="validation.max")
	protected Integer sound_pressure_level;
    
	@Min(value=1, message="validation.min")
    @Max(value=500, message="validation.max")
	protected Integer sound_exposure_level;
    
    @Min(value=1, message="validation.min")
	protected Integer max_hammer_energy_actual;

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
	 * Sets the application
	 * @param aa
	 */
	public void setAa(ActivityApplication aa) {
		this.aa = aa;
	}

	/**
	 * Gets the maximum hammer energy
	 * @return
	 */
	public Integer getMax_hammer_energy() {
		return max_hammer_energy;
	}

	/**
	 * Sets the maximum hammer energy
	 * @param max_hammer_energy
	 */
	public void setMax_hammer_energy(Integer max_hammer_energy) {
		this.max_hammer_energy = max_hammer_energy;
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
	 * @param sound pressure level
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
	 * @param sound exposure level
	 */
	public void setSound_exposure_level(Integer sound_exposure_level) {
		this.sound_exposure_level = sound_exposure_level;
	}
	
	/**
	 * Gets the actual maximum hammer energy
	 * @return
	 */
    public Integer getMax_hammer_energy_actual() {
		return max_hammer_energy_actual;
	}
    
	/**
	 * Sets the actual maximum hammer energy
	 * @param max_hammer_energy
	 */
	
	public void setMax_hammer_energy_actual(Integer max_hammer_energy_actual) {
		this.max_hammer_energy_actual = max_hammer_energy_actual;
	}

	/**
	 * Gets the actual sound pressure level
	 * @return
	 */
	public Integer getSound_pressure_level_actual() {
		return sound_pressure_level_actual;
	}
	
	/**
	 * Sets the actual sound pressure level
	 * @param sound pressure level
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
	 * @param sound exposure level
	 */
	public void setSound_exposure_level_actual(Integer sound_exposure_level_actual) {
		this.sound_exposure_level_actual = sound_exposure_level_actual;
	}
	
	/**
	 * Constructor
	 */
	public ActivityPiling() {
		super();
	}

	@Override
	public void populateDefaults() {
		if (getMax_hammer_energy_actual()==null)
			setMax_hammer_energy_actual(getMax_hammer_energy());
		if (getSound_pressure_level_actual()==null)
			setSound_pressure_level_actual(getSound_pressure_level());
		if (getSound_exposure_level_actual()==null)
			setSound_exposure_level_actual(getSound_exposure_level());		
	}

	@Override
	public void mergeActuals(Map<String, String> m) {
		setSound_exposure_level_actual(getSafeInt(m,"activityPiling.sound_exposure_level_actual"));
		setSound_pressure_level_actual(getSafeInt(m,"activityPiling.sound_pressure_level_actual"));
		setMax_hammer_energy_actual(getSafeInt(m,"activityPiling.max_hammer_energy_actual"));
		JPA.em().merge(this);	
	}	
}