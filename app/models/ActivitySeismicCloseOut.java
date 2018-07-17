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

import com.fasterxml.jackson.annotation.JsonIgnore;

import play.db.jpa.JPA;

@Entity
@Table(name="activityseismic")
public class ActivitySeismicCloseOut extends DefaultableActivity
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
	public ActivitySeismicCloseOut() 
	{
		super();
	}
	@Override
	public void populateDefaults() 
	{
		if (getMax_airgun_volume_actual()==null)
			setMax_airgun_volume_actual(getAa().getActivitySeismic().getMax_airgun_volume());
		if (getSound_pressure_level_actual()==null)
			setSound_pressure_level_actual(getAa().getActivitySeismic().getSound_pressure_level());
		if (getSound_exposure_level_actual()==null)
			setSound_exposure_level_actual(getAa().getActivitySeismic().getSound_exposure_level());
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
