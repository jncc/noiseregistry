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
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="activityexplosives")
public class ActivityExplosivesCloseOut extends DefaultableActivity
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "activityexplosives_seq_gen")
    @SequenceGenerator(name = "activityexplosives_seq_gen", sequenceName = "activityexplosives_id_seq")
    @Column(columnDefinition = "serial")
    protected Long id;   
    
    @JsonIgnore
	@OneToOne(optional=false)
    @JoinColumn(name="activityapplication_id",referencedColumnName="id")
    protected ActivityApplication aa;
    
    @DecimalMin(value="0.1", message="validation.tnt_min")
    @Max(value=5000, message="validation.max")
	protected Double tnt_equivalent_actual;
	
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
	 * Gets the actual TNT equivalent
	 * @return
	 */
	public Double getTnt_equivalent_actual() {
		return tnt_equivalent_actual;
	}
	
	/**
	 * Sets the actual TNT equivalent
	 * @param tnt_equivalent
	 */
	public void setTnt_equivalent_actual(Double tnt_equivalent_actual) {
		this.tnt_equivalent_actual = tnt_equivalent_actual;
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
	public ActivityExplosivesCloseOut() {
		super();
	}

	@Override
	public void populateDefaults() {
		if (getTnt_equivalent_actual()==null)
			setTnt_equivalent_actual(getAa().getActivityExplosives().getTnt_equivalent());
		if (getSound_pressure_level_actual()==null)
			setSound_pressure_level_actual(getAa().getActivityExplosives().getSound_pressure_level());
		if (getSound_exposure_level_actual()==null)
			setSound_exposure_level_actual(getAa().getActivityExplosives().getSound_exposure_level());		
	}

	@Override
	public void mergeActuals(Map<String, String> m) {
		setSound_exposure_level_actual(getSafeInt(m,"activityExplosives.sound_exposure_level_actual"));
		setSound_pressure_level_actual(getSafeInt(m,"activityExplosives.sound_pressure_level_actual"));
		setTnt_equivalent_actual(getSafeDouble(m,"activityExplosives.tnt_equivalent_actual"));
	}
	
}