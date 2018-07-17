package models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;

import org.hibernate.annotations.NamedNativeQueries;
import org.hibernate.annotations.NamedNativeQuery;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;

import play.Logger;
import play.data.format.Formats;
import play.data.validation.Constraints.Required;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wordnik.swagger.annotations.ApiModelProperty;

@NamedQueries({
	@NamedQuery(name = "AuditTrail.findByTableId", query = "from AuditTrail where tablename=:tablename and fk_id=:id)"),
})

@Entity
@Table(name="audittrail")
public class AuditTrail
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "audittrail_seq_gen")
    @SequenceGenerator(name = "audittrail_seq_gen", sequenceName = "audittrail_id_seq")
    @Column(columnDefinition = "serial")
    protected Long id;
    
    @Required
    @Column(length=50)
    @Length(max=50, message="validation.field_too_long")
    protected String who;
        
    @JsonIgnore
	@Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    protected Timestamp date_when;
    
    @Required
    @Column(length=100)
    @Length(max=100, message="validation.field_too_long")
    protected String tablename;
    
    @Required
    @Column(length=100)
    @Length(max=100, message="validation.field_too_long")
    protected String event;
    
    @Required
    protected Long fk_id;
    
    @Required
    @Column(length=1500)
    @Length(max=1500, message="validation.field_too_long")
    protected String reason;
    
    AuditTrail() {
    	this.date_when = new Timestamp(new java.util.Date().getTime());
    }
    
	/**
	 * Writes a message to the audit trail
	 * @param au - the user
	 * @param id - the id of the object
	 * @param sEvent - the event to log
	 * @param sReason - the reason for the event
	 * @param sTable - the object tablename
	 * @return List of applications with the given status
	 */
    public static void WriteAudit(AppUser au,long id,String sEvent,String sReason,String sTable)
    {
    	AuditTrail at = new AuditTrail();
    	if (au==null)
    		at.setWho("unknown");
    	else
    		at.setWho(au.getEmail_address());
    	at.setEvent(sEvent);
    	at.setReason(sReason);
    	at.setTablename(sTable);
    	at.setFk_id(new Long(id));
    	JPA.em().persist(at);    	
    }
    
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
	 * Gets 
	 * @return
	 */
	public String getWho() {
		return who;
	}

	/**
	 * Sets 
	 * @param 
	 */
	public void setWho(String who) {
		this.who = who;
	}
	
	public Date getDate_when() {
		return date_when;
	}
	/**
	 * Gets 
	 * @return
	 */
	public String getTablename() {
		return tablename;
	}

	/**
	 * Sets 
	 * @param 
	 */
	public void setTablename(String tablename) {
		this.tablename = tablename;
	}

	/**
	 * Gets 
	 * @return
	 */
	public String getEvent() {
		return event;
	}

	/**
	 * Sets 
	 * @param 
	 */
	public void setEvent(String event) {
		this.event = event;
	}

	
	public Long getFk_id() {
		return fk_id;
	}
	public void setFk_id(Long fk_id) {
		this.fk_id = fk_id;
	}
	/**
	 * Gets 
	 * @return
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * Sets 
	 * @param 
	 */
	public void setReason(String reason) {
		this.reason = reason;
	}
	
	/**
	 * Finds all audit by id of an object
	 * @param sTable - the object type
	 * @param id - the id of the object
	 * @return List of audit trails
	 */
	public static List<AuditTrail> findByTableId(String sTable,Long id)
	{
    	TypedQuery<AuditTrail> query = JPA.em().createNamedQuery("AuditTrail.findByTableId", AuditTrail.class);
		
    	query.setParameter("id", id);
    	query.setParameter("tablename", sTable);
    	List<AuditTrail> results = query.getResultList();
    	return results;
	}
}
