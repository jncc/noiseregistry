package models;

import java.util.LinkedHashMap;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
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
import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonIgnore;

import play.Logger;
import play.db.jpa.JPA;

@NamedQueries({
	@NamedQuery(name = "NoiseProducer.getFromOrganisation", query = "from NoiseProducer where organisation=:org"),
	@NamedQuery(name = "NoiseProducer.findAll", query="from NoiseProducer order by organisation.organisation_name"),
})
@Entity
@Table(name="noiseproducer")
public class NoiseProducer implements Comparable<NoiseProducer> {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "noiseproducer_seq_gen")
	@SequenceGenerator(name = "noiseproducer_seq_gen", sequenceName = "noiseproducer_id_seq")
	@Column(columnDefinition = "serial")
	protected Long id; 
	
    @OneToOne(cascade = {CascadeType.ALL})	
	@JsonIgnore
    @JoinColumn(name="organisation_id",referencedColumnName="id")
    @Valid
	protected Organisation organisation;

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
	 * Gets the NoiseProducers for the organisation
	 * @param organisation
	 * @return
	 */
	public static NoiseProducer getFromOrganisation(Organisation organisation) {
		TypedQuery<NoiseProducer> query = JPA.em().createNamedQuery("NoiseProducer.getFromOrganisation", NoiseProducer.class);
		
    	query.setParameter("org", organisation);

    	if (query.getResultList().size() == 0)
    		return null;
    	
		NoiseProducer np = query.getResultList().get(0);
		return np;
	}

	/**
	 * Gets all of the NoiseProducers
	 * @return
	 */
	public static List<NoiseProducer> findAll() {
		TypedQuery<NoiseProducer> query = JPA.em().createNamedQuery("NoiseProducer.findAll", NoiseProducer.class);
		
		List<NoiseProducer> results = query.getResultList();
		return results;
	}
	
	/**
	 * Get the Noise Producers for use in options lists
	 * @param au the context of the user
	 * @return
	 */
	public static LinkedHashMap<String, String> getOptions(AppUser au) {
		
		List<NoiseProducer> nps = au.findNoiseProducers();
		LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
		if (!nps.isEmpty()) {
			for(NoiseProducer np: nps) {
	            options.put(Long.toString(np.id), np.getOrganisation().getOrganisation_name());
	        }
		}
		return options;
	}
	
	/**
	 * Set a user to be an organisation administrator
	 * @param au
	 */
	private void setOrgAdmin(AppUser au) {
		//having saved the new organisation, set the current user to be the first admin user...
		OrgUser orguser = new OrgUser();
		orguser.setOrg(this.getOrganisation());
		orguser.setStatus("verified");
		orguser.setAu(au);
		orguser.setAdministrator(true);
		JPA.em().persist(orguser);
	}
	
	/**
	 * AppUser should be passed to the save method on first save so that the first
	 * administrator gets set correctly at creation. 
	 * @param au
	 */
	public void save(AppUser au) {
		this.save();
		this.setOrgAdmin(au);
	}

	/**
	 * AppUser should be passed to the save method on first save so that the first
	 * administrator gets set correctly at creation.  This method is here for testing only.
	 * 
	 */
	public void save() {
		if (!this.organisation.isAdministrator())
			this.organisation.setAdministrator(false); // just so as not null
		JPA.em().persist(this);
	}
	
	/**
	 * Update the NoiseProducr in the database
	 */
    public void update() {
    	if (!this.organisation.isAdministrator())
			this.organisation.setAdministrator(false); // just so as not null
    	    	
    	JPA.em().merge(this);
    }
    
	@Override
	public int compareTo(NoiseProducer o) {
		return getOrganisation().getOrganisation_name().compareToIgnoreCase(o.getOrganisation().getOrganisation_name());
	}
}
