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
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import play.db.jpa.JPA;
import play.db.jpa.Transactional;

@Entity
@Table(name="activitymod")
public class ActivityMoDCloseOut extends DefaultableActivity
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
	 * Sets the id
	 * @param id
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * Constructor
	 */
	public ActivityMoDCloseOut() 
	{
		
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