package models;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityNotFoundException;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.validation.constraints.Min;
import javax.validation.*;
import javax.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import play.db.jpa.JPA;

@NamedQueries({
	@NamedQuery(name="Regulator.findAll", query="from Regulator order by organisation.organisation_name"),
	@NamedQuery(name = "Regulator.getFromOrganisation", query = "from Regulator where organisation=:org"),
	@NamedQuery(name = "Regulator.findAllByIds", query="from Regulator where organisation.id in :ids")
})

@Entity
@Table(name="regulator")
public class Regulator {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "regulator_seq_gen")
	@SequenceGenerator(name = "regulator_seq_gen", sequenceName = "regulator_id_seq")
	@Column(columnDefinition = "serial")
	protected Long id; 
	
    @OneToOne(cascade = {CascadeType.ALL})	
    @JoinColumn(name="organisation_id",referencedColumnName="id")
	@JsonIgnore
	@Valid
	protected Organisation organisation;
	
	@JsonIgnore
	@Min(value=1, message="validation.min")
	protected Integer closeoutdays;

	@JsonIgnore
	protected Boolean accepts_email = new Boolean(false);
	
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
	 * Gets the organisation
	 * @return
	 */
	public Organisation getOrganisation() {
		return organisation;
	}

	/**
	 * Sets the organisation
	 * @param organisation
	 */
	public void setOrganisation(Organisation organisation) {
		this.organisation = organisation;
	}

	/**
	 * Gets the closeout days
	 * @return
	 */
	public Integer getCloseoutdays() {
		return closeoutdays;
	}

	/**
	 * Sets the closeout days
	 * @param closeoutdays
	 */
	public void setCloseoutdays(Integer closeoutdays) {
		this.closeoutdays = closeoutdays;
	}
	
	/**
	 * Gets whether the regulator accepts email
	 * @return
	 */
	public Boolean getAccepts_email() {
		if (accepts_email == null)
			return new Boolean(false);
		return accepts_email;
	}

	/**
	 * Sets whether the regulator accepts email
	 * @param accepts_email
	 */
	public void setAccepts_email(Boolean accepts_email) {
		this.accepts_email = accepts_email;
	}

	/**
	 * Gets a list of regulators
	 * @return
	 */
	public static List<Regulator> findAll() {
		TypedQuery<Regulator> query = JPA.em().createNamedQuery("Regulator.findAll", Regulator.class);
		
		List<Regulator> results = query.getResultList();
		return results;
	}
	/**
	 * Saves the regulator to the database
	 */
	public void save() {
		if (!getAccepts_email())
			setAccepts_email(false); // just so as not null
		
		JPA.em().persist(this);
	}	
	
	/**
	 * Updates an existing regulator to the database
	 */
	public void update() {
		if (!getAccepts_email())
			setAccepts_email(false); // just so as not null
				
		JPA.em().merge(this);
	}
	
	/**
	 * Gets the regulators as an options list
	 * @return
	 */
	public static LinkedHashMap<String, String> getOptions() {
		List<Regulator> regs = findAll();
		LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
		if (!regs.isEmpty()) {
			for(Regulator reg: regs) {
            	options.put(Long.toString(reg.id), reg.getOrganisation().getOrganisation_name());
        	}
		}
		return options;
	}	
	/**
	 * Gets the Regulator for the organisation
	 * @param organisation
	 * @return
	 */
	public static Regulator getFromOrganisation(Organisation organisation) {
		TypedQuery<Regulator> query = JPA.em().createNamedQuery("Regulator.getFromOrganisation", Regulator.class);
		
    	query.setParameter("org", organisation);

    	if (query.getResultList().size() == 0)
    		return null;
    	
		Regulator reg = query.getResultList().get(0);
		return reg;
	}
	
	
	/**
	 * Try and get all regulators by a list of ids (throw error if all of those results do not exist as a regulator)
	 */
	public static List<Regulator> getRegulatorsByOrgIds(List<Long> ids) {
		TypedQuery<Regulator> query = JPA.em().createNamedQuery("Regulator.findAllByIds", Regulator.class);
		query.setParameter("ids", ids);
		
		List<Regulator> regs = query.getResultList();
		
		if (regs.size() != ids.size()) {
			throw new EntityNotFoundException(String.format("Not all ids in list [%s] exist as regulators", ids.stream().map(Object::toString).collect(Collectors.joining(", "))));
		}
		
		return regs;
	}
}