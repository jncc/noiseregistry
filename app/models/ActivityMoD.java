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
import javax.persistence.Transient;

import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;

import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.i18n.Messages;

@Entity
@Table(name="activitymod")
public class ActivityMoD extends DefaultableActivity
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "activitymod_seq_gen")
    @SequenceGenerator(name = "activitymod_seq_gen", sequenceName = "activitymod_id_seq")
    @Column(columnDefinition = "serial")
    protected Long id;   
    
    @JsonIgnore
	@OneToOne(optional=false)
    @JoinColumn(name="activityapplication_id",referencedColumnName="id")
    protected ActivityApplication aa;
    
	@Column(length=20)
	@NotBlank(message="validation.required")
	protected String source;
	
	@JsonIgnore
	@Transient
	protected String dummy;
	
	public String getDummy() {
		return dummy;
	}
	public void setDummy(String dummy) {
		this.dummy = dummy;
	}
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
	 * Sets the id
	 * @param id
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * Constructor
	 */
	public ActivityMoD() 
	{
		
	}
	/**
	 * Get the source types as a list for use in options
	 * @return
	 */
	public static LinkedHashMap<String, String> sourceOptions() {
		LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();

		options.put("ASW Sonar", sourceValue("ASW Sonar"));
        options.put("ASW Sonar (Check)", sourceValue("ASW Sonar (Check)"));
        options.put("Explosion", sourceValue("Explosion"));
        
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
		if (s.compareTo("ASW Sonar")==0)
			return Messages.get("field_option.asw_sonar");
		if (s.compareTo("ASW Sonar (Check)")==0)
			return Messages.get("field_option.asw_sonar_check");
		if (s.compareTo("Explosion")==0)
			return Messages.get("field_option.explosion");
		return Messages.get("field_option.unknown");
	}
	
	/**
	 * Saves the activity type to the database
	 */
	@Transactional
	public void save()
	{
		JPA.em().persist(this);
	}
	@Override
	public void populateDefaults() {
	}

	@Override
	public void mergeActuals(Map<String, String> m) {
	}
}