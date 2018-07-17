package models;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import play.db.jpa.JPA;

@NamedQueries({
	@NamedQuery(name="ActivityType.findAll", query="from ActivityType order by name")
})
@Entity
@Table(name="activitytype")
public class ActivityType
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "activitytype_seq_gen")
    @SequenceGenerator(name = "activitytype_seq_gen", sequenceName = "activitytype_id_seq")
    @Column(columnDefinition = "serial")
    protected Long id;
    
    @Column(length=30)
    protected String name;
    
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
	 * Gets the name
	 * @return
	 */
	public String getName() {
		return name;
	}
	/**
	 * Sets the name
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
    
	/**
	 * retrieves the types from the database
	 * @return
	 */
	public static Map<String,String> options() {
		List<ActivityType> actTypes = JPA.em().createNamedQuery("ActivityType.findAll", ActivityType.class).getResultList();
        LinkedHashMap<String,String> options = new LinkedHashMap<String,String>();
        for(ActivityType at: actTypes) {
            options.put(Long.toString(at.id), at.name);
        }
        return options;
    }
	
	/**
	 * Gets an activity type by id
	 * @param id the id to be found
	 * @return the activity type
	 */
	public static ActivityType findById(int id) {
	    return JPA.em().find(ActivityType.class, id);
	}
	
	/**
	 * gets a list of all activity types
	 * @return
	 */
	public static List<ActivityType> findAll() {
		List<ActivityType> actTypes = JPA.em().createNamedQuery("ActivityType.findAll", ActivityType.class).getResultList();
        return actTypes;
	}
}