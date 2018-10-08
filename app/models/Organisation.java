package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityNotFoundException;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;

import org.hibernate.annotations.NamedNativeQueries;
import org.hibernate.annotations.NamedNativeQuery;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;

import play.Logger;
import play.data.validation.Constraints.Required;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import utils.AppConfigSettings;
import utils.MailSettings;

import com.fasterxml.jackson.annotation.JsonIgnore;

import play.i18n.Messages;
import play.twirl.api.Html;

import javax.mail.*;
import javax.mail.internet.*;

@NamedQueries({
	@NamedQuery(name="Organisation.findAll", query="from Organisation order by organisation_name"),
	@NamedQuery(name="Organisation.findUsers", query="from OrgUser as ou where ou.org.id = :id order by ou.au.fullname"),
	@NamedQuery(name="Organisation.findUser", query="from OrgUser as ou where ou.id = :id"),
	@NamedQuery(name="Organisation.findAdmins", query="from OrgUser where org = :org and administrator = true"),
	@NamedQuery(name="Organisation.findByName", query="from Organisation where lower(organisation_name) = lower(:org)"),
	@NamedQuery(name="Organisation.findRegulator", query="from Regulator as reg where reg.organisation=:org"),
	@NamedQuery(name="Organisation.findNoiseProducer", query="from NoiseProducer as np where np.organisation=:org"),
	@NamedQuery(name="Organisation.findById", query="from Organisation as o where o.id = :id order by o.id"),
	@NamedQuery(name="Organisation.findAllByIds", query="from Organisation as o where o.id in :ids order by o.id"),

	@NamedQuery(name="Organisation.findNoiseProducersByIds", query="from NoiseProducer as np where np.organisation.id IN :ids")	
})

@NamedNativeQueries({
	@NamedNativeQuery(name="Organisation.findNonUsers",
			query="select * from appuser "
					+ "where appuser.id not in (select appuser_id from orguser where organisation_id = :id)"
					+ "order by fullname",
			resultClass=AppUser.class),
	@NamedNativeQuery(name="Organisation.findUsersNotInAnyOrg",
			query="select * from appuser "
					+ "where appuser.id not in (select appuser_id from orguser)"
					+ "order by fullname",
			resultClass=AppUser.class),
	@NamedNativeQuery(name="Organisation.myAdminOrganisations",
			query="select * from organisation inner join orguser on orguser.organisation_id = organisation.id "
					+ "inner join appuser on appuser.id = orguser.appuser_id where appuser.email_address=:email and orguser.administrator "
					+ "inner join noiseproducer on noiseproducer.organisation_id = organisation.id "
					+ "order by organisation_name",
			resultClass=Organisation.class),
	@NamedNativeQuery(name="Organisation.myOrganisations",
			query="select * from organisation inner join orguser on orguser.organisation_id = organisation.id "
					+ "inner join appuser on appuser.id = orguser.appuser_id where appuser.email_address=:email "
					+ "order by organisation_name",
			resultClass=Organisation.class),
})



@Entity
@Table(name="organisation")
public class Organisation implements Comparable<Organisation>
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "organisation_seq_gen")
    @SequenceGenerator(name = "organisation_seq_gen", sequenceName = "organisation_id_seq")
    @Column(columnDefinition = "serial")
    protected Long id;
    
    @Required
    @Column(length=100)
    @Length(max=100, message="validation.field_too_long")
    protected String organisation_name;
        
    @Required
    @Column(length=50)
    @Length(max=50, message="validation.field_too_long")
    protected String contact_name;
    
    @Required
    @Column(length=50)
    @Email
    @Length(max=50, message="validation.field_too_long")
    protected String contact_email;
    
    @Transient
    protected Boolean accepts_email;
    
    @Required
    @Column(length=20)
    @Length(max=20, message="validation.field_too_long")
    protected String contact_phone;

    @Column
    protected Boolean administrator = new Boolean(false);
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
	 * Gets the name of the organisation
	 * @return
	 */
	public String getOrganisation_name() {
		return organisation_name;
	}

	/**
	 * Sets the name of the organisation
	 * @param organisation_name
	 */
	public void setOrganisation_name(String organisation_name) {
		this.organisation_name = organisation_name;
	}
	/**
	 * Gets the contact name
	 * @return
	 */
	public String getContact_name() {
		return contact_name;
	}

	/**
	 * Sets the contact name
	 * @param contact_name
	 */
	public void setContact_name(String contact_name) {
		this.contact_name = contact_name;
	}

	/**
	 * Gets the contact email
	 * @return
	 */
	public String getContact_email() {
		return contact_email;
	}

	/**
	 * Sets the contact email
	 * @param contact_email
	 */
	public void setContact_email(String contact_email) {
		this.contact_email = contact_email;
	}

	/**
	 * Gets the contact phone number
	 * @return
	 */
	public String getContact_phone() {
		return contact_phone;
	}

	/**
	 * Sets the contact phone number
	 * @param contact_phone
	 */
	public void setContact_phone(String contact_phone) {
		this.contact_phone = contact_phone;
	}

	/**
	 * Gets whether the organisation accepts email notifications
	 * @return
	 */
	public Boolean getAccepts_email() {
		if (accepts_email == null)
			return new Boolean(false);
		return accepts_email;
	}

	/**
	 * Sets whether the organisation accepts email notifications
	 * @param accepts_email
	 */
	public void setAccepts_email(Boolean accepts_email) {
		this.accepts_email = accepts_email;
	}
	/**
	 * Gets whether the organisation is administrative
	 * @return
	 */
	public Boolean isAdministrator() {
		if (administrator==null)
			return new Boolean(false);
		return administrator;
	}

	/**
	 * Sets whether the organisation is administrative
	 * @param administrator
	 */
	public void setAdministrator(Boolean administrator) {
		this.administrator = administrator;
	}

	/**
	 * Gets a list of regulators
	 * @return
	 */
	public static List<Organisation> findRegulators() {
		TypedQuery<Organisation> query = JPA.em().createNamedQuery("Organisation.findRegulators", Organisation.class);
		
		List<Organisation> results = query.getResultList();
		return results;
	}
	
	/**
	 * Gets an organisation user given its id 
	 * @param id id of the organisation user
	 * @return The OrgUser
	 */
	public static OrgUser findUser(Long id) 
	{
		TypedQuery<OrgUser> query = JPA.em().createNamedQuery("Organisation.findUser", OrgUser.class).setParameter("id",id);
		List<OrgUser> results = query.getResultList();
		
		if (results.size()>0)
			return results.get(0);

		OrgUser ou = new OrgUser();
		
		return ou;
	}
	/**
	 * Finds organisations with the same name  
	 * @param name name of the organisation
	 * @return The list of Organisations
	 */
	public static List<Organisation> findByName(String name) 
	{
		TypedQuery<Organisation> query = JPA.em().createNamedQuery("Organisation.findByName", Organisation.class).setParameter("org",name);
		
		return query.getResultList();
	}
	
	public static Boolean organisationNameExists(String name) {
		TypedQuery<Organisation> query = JPA.em().createNamedQuery("Organisation.findByName", Organisation.class);
		query.setParameter("org", name);
		
		List<Organisation> ol = query.getResultList();
		
		if (ol.isEmpty()) {
			return false;
		}
		return true;
	}
	
	/**
	 * Gets those organisations that aren't listed as regulators
	 * @return
	 */
	public static List<Organisation> findNonRegulators() {
		TypedQuery<Organisation> query = JPA.em().createNamedQuery("Organisation.findNonRegulators", Organisation.class);
		
		List<Organisation> results = query.getResultList();
		return results;
	}

	/**
	 * Gets the list of administrators for this organisation 
	 * @return
	 */
	public List<OrgUser> findAdmins() {
		TypedQuery<OrgUser> query = JPA.em().createNamedQuery("Organisation.findAdmins", OrgUser.class);
		query.setParameter("org", this);
		List<OrgUser> results = query.getResultList();
		return results;
	}
	
	/**
	 * Gets the list of users for this organisation 
	 * @return
	 */
	public List<OrgUser> findUsers() {
		List<OrgUser> results = new ArrayList<OrgUser>();
				
		try {
			TypedQuery<OrgUser> query = JPA.em().createNamedQuery("Organisation.findUsers", OrgUser.class).setParameter("id",this.getId());
		
			results = query.getResultList();
		} catch(Exception e) {
			Logger.error("error finding users: "+e.toString());
		}
		
		return results;
	}

	/**
	 * Saves the organisation to the database
	 */
	@Transactional
    public void save() 
	{
		JPA.em().merge(this);
		Regulator reg = this.getRegulator();
		if (reg != null) {
			if (this.getAccepts_email()==null) {
				reg.setAccepts_email(false);
			} else {
				reg.setAccepts_email(this.getAccepts_email());
			}
			JPA.em().merge(reg);
		}
	}
	
	/**
	 * Gets a List of users (0 or 1) that are administrators of the organisation with the given email 
	 * address  
	 * @param email the email address
	 * @return
	 */
	public static List<Organisation> getMyAdminOrganisations(AppUser au)
	{
		List<Organisation> results = new ArrayList<Organisation>();
		Organisation org = null;
		
		try 
		{
			if (au.getOrgRole()=="OVERALL_ADMIN")
			{
				List<Organisation> orgAll = findAll(); 
				Iterator <Organisation> itorg = orgAll.iterator();
				while (itorg.hasNext())
				{
					org = itorg.next();
					results.add(org);
				}
			}
			else
			{
				List <OrgUser> liou = au.getOu();
				Iterator <OrgUser> itou = liou.iterator();
				while (itou.hasNext())
				{
					OrgUser ou = itou.next();
					
					org = ou.getOrg();
					if (ou.isAdministrator())
						results.add(org);
				}
			}
		}
		catch (Exception e) {
			Logger.error("Error getting my admin organisations: "+e.toString());
		}
		Collections.sort(results);		
		return results;
	}
	
	/**
	 * Gets the list of organisations to which the user with the email address belongs 
	 * @param email email address of user
	 * @return 
	 */
	public static List<Organisation> getMyOrganisations(String email)
	{
		List<Organisation> results = new ArrayList<Organisation>();
		
		try 
		{
			TypedQuery<Organisation> query = JPA.em().createNamedQuery("Organisation.myOrganisations", Organisation.class);
			
			query.setParameter("email", email);

			results = query.getResultList();
		}
		catch (Exception e) {
			Logger.error("Error getting my otganisations: "+e.toString());
		}
		
		return results;
	}
	
	/**
	 * Gets the list of users who are not currently members of the organisation. 
	 * If no parameter is provided, gets a list of users that are not part of any organisation
	 * @param id id of the organistion
	 * @return 
	 */
	public static List<AppUser> findNonUsers(Long id)
	{
		
		List<AppUser> results = new ArrayList<AppUser>();		
		try 
		{
			TypedQuery<AppUser> query;
			if (id!=null) 
			{
				query = JPA.em().createNamedQuery("Organisation.findNonUsers", AppUser.class);
				query.setParameter("id", id);
			}
			else 
			{
				query = JPA.em().createNamedQuery("Organisation.findUsersNotInAnyOrg", AppUser.class);
			}

			results = query.getResultList();
		}
		catch (Exception e) {
			Logger.error("Error finding non-users: "+e.toString());
		}
		
		return results;
	}

	/**
	 * Gets all organisations
	 * @return
	 */
	public static List<Organisation> findAll() {
		TypedQuery<Organisation> query = JPA.em().createNamedQuery("Organisation.findAll", Organisation.class);
		
		List<Organisation> results = query.getResultList();
		return results;
	}
	
	/**
	 * Gets an organisation by id
	 * @return
	 */
	public static Organisation find(long id) {
		TypedQuery<Organisation> query = JPA.em().createNamedQuery("Organisation.findById", Organisation.class);
		query.setParameter("id", id);
		
		return query.getSingleResult();
	}
	
	/**
	 * Gets a list of organisations by id
	 * @return
	 */
	public static List<Organisation> findByIdList(List<Long> ids) {
		TypedQuery<Organisation> query = JPA.em().createNamedQuery("Organisation.findALLByIds", Organisation.class);
		query.setParameter("ids", ids);
		
		List<Organisation> organisations = query.getResultList();
		
		if (organisations.size() != ids.size()) {
			throw new EntityNotFoundException(String.format("Not all ids in list [%s] exist as organisations", ids.stream().map(Object::toString).collect(Collectors.joining(", "))));
		}
		
		return query.getResultList();
	}
	
	@JsonIgnore
	public boolean isRegulator() {
		TypedQuery<Regulator> query = JPA.em().createNamedQuery("Organisation.findRegulator", Regulator.class);
		query.setParameter("org", this);
		List<Regulator> results = query.getResultList();
		if (results.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * Try and get the regulator associated with this organisation (null if there isn't one)
	 * @return
	 */
	@JsonIgnore
	public Regulator getRegulator() {
		TypedQuery<Regulator> query = JPA.em().createNamedQuery("Organisation.findRegulator", Regulator.class);
		query.setParameter("org", this);
		List<Regulator> results = query.getResultList();
		if (results.isEmpty()) {
			return null;
		} else {
			return results.get(0);
		}
	}

	/**
	 * Gets whether this organisation is a NoiseProducer
	 * @return
	 */
	@JsonIgnore
	public boolean isNoiseProducer() {
		TypedQuery<NoiseProducer> query = JPA.em().createNamedQuery("Organisation.findNoiseProducer", NoiseProducer.class);
		query.setParameter("org", this);
		List<NoiseProducer> results = query.getResultList();
		if (results.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * Try to get the NoiseProducer associated with this organisation
	 * @return
	 */
	@JsonIgnore
	public NoiseProducer getNoiseProducer() {
		TypedQuery<NoiseProducer> query = JPA.em().createNamedQuery("Organisation.findNoiseProducer", NoiseProducer.class);
		query.setParameter("org", this);
		List<NoiseProducer> results = query.getResultList();
		if (results.isEmpty()) {
			return null;
		} else {
			return results.get(0);
		}
	}
	
	/**
	 * Gets a list of activityapplications by the given status
	 * @param status
	 * @return
	 */
	public List<ActivityApplication> findApplicationsByStatus(String status) {
		List<ActivityApplication> results = ActivityApplication.findByStatus(status, this);
		
		return results;
	}
	
	/**
	 * Adds a new user to the organisation
	 * @param au the new user
	 */
	public void addUser(AppUser au)
	{
		OrgUser newOrgUser = new OrgUser();
		newOrgUser.setAu(au);
		newOrgUser.setOrg(this);
		newOrgUser.setStatus(OrgUser.UNVERIFIED);
		newOrgUser.save();
	}
    
	@Override
	public int compareTo(Organisation o) {
		return getOrganisation_name().compareToIgnoreCase(o.getOrganisation_name());
	}

	/**
	 * Emails details of changes to an organisation to organisation users
	 * @param org - the organisation
	 * @param sHost - the host name
	 * @param au - the user 
	 * @return success or failure
	 */
	public boolean sendChanges(Organisation org, String sHost, AppUser au) 
	{
		List <OrgUser> liou = findUsers();
		if (getOrganisation_name().equals(org.getOrganisation_name()) && 
				getContact_name().equals(org.getContact_name()) &&
				getContact_email().equals(org.getContact_email()) &&
				getContact_phone().equals(org.getContact_phone()) )
			return true; // nothing to send
		try {
			Session session = MailSettings.getSession();
			
			List<InternetAddress> lia = new ArrayList<InternetAddress>();
			HashMap <String,String>hmAdd = new HashMap<String,String>();
			Iterator <OrgUser> it = liou.iterator();
			while (it.hasNext())
			{
				OrgUser ou = it.next();
				if (ou.getAu().getId().longValue()!=au.getId().longValue())
					hmAdd.put(ou.getAu().getEmail_address(),ou.getAu().getEmail_address());
			}
			Iterator <String> ithm = hmAdd.keySet().iterator();
			while (ithm.hasNext())
			{
				String sAdd = ithm.next();
				lia.add(new InternetAddress(sAdd));
			}
			if (!au.getEmail_address().equals(org.getContact_email()))
				lia.add(new InternetAddress(org.getContact_email()));

			if (!getContact_email().equals(org.getContact_email()))
				lia.add(new InternetAddress(getContact_email()));
									
			if (lia.size()==0)
				return true;
			int iCnt = 0;
			InternetAddress[] addresses = new InternetAddress[lia.size()];
			Iterator <InternetAddress> itia = lia.iterator();
			while (itia.hasNext())
			{
				InternetAddress ia = itia.next();
				addresses[iCnt++] = ia; 
			}
			
			String sSubject = Messages.get("orgchanges.mail.subject");
			
			String overrideAddressReg = AppConfigSettings.getConfigString("regulatorOverrideAddress", "email.regulator_override_address");
			String overrideAddressNP = AppConfigSettings.getConfigString("noiseproducerOverrideAddress", "email.noiseproducer_override_address");

			Html mailBody = views.html.email.orgchanges.render(this, org, sHost, overrideAddressNP, lia, true);
			Html mailAlt = views.html.email.orgchanges.render(this, org, sHost, overrideAddressNP, lia, false);
			
			return MailSettings.send(mailBody, mailAlt, sSubject, addresses, true, true, false);		
		 } catch (MessagingException me) {
			 Logger.error("Error sending organisation changes: "+me.toString());
			 me.printStackTrace();
		 }
		return false;
	}
}
