package models;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;

import javax.persistence.*;

import play.data.validation.Constraints.Pattern;
import play.Logger;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import utils.PasswordHash;

@Entity
@Table(name = "appuser")
@NamedQueries({
		@NamedQuery(name = "AppUser.findVerifiedByEmail", query = "from AppUser where lower(email_address)=lower(:email)"),
		@NamedQuery(name = "AppUser.getUnverifiedUsers", query = "from AppUser where verification_status='Unverified' order by fullname"),
		@NamedQuery(name = "AppUser.findByEmail", query = "from AppUser where lower(email_address)=lower(:email)"),
		@NamedQuery(name = "AppUser.findByVerificationToken", query = "from AppUser where verification_token=:token"),
		@NamedQuery(name = "AppUser.findByAuthToken", query = "from AppUser where auth_token=:token"),
		@NamedQuery(name = "AppUser.findUnverifiedByEmail", query = "from AppUser where (verification_status is null or verification_status!='Verified') and email_address=:email"),
		@NamedQuery(name = "AppUser.findOrgAdmins", query = "from OrgUser as ou where administrator is true and ou.org.id=:org_id")})
public class AppUser {

	public static final String VERIFIED = "Verified";
	public static final String UNVERIFIED = "Unverified";
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "appuser_seq_gen")
	@SequenceGenerator(name = "appuser_seq_gen", sequenceName = "appuser_id_seq")
	@Column(columnDefinition = "serial")
	protected Long id;

	@Column(length = 50)
	@Required
	protected String fullname;

	@Required
	@Column(length = 50, unique = true)
	protected String email_address;

	@Column(length = 20)
	@Required
	protected String phone;

	//The pattern used here must match the PASSWORD_PATTERN field set below which gets used to
	//generate passwords automatically.
	@Transient
	@MinLength(8)
	@Pattern(value="((?=.*\\d)(?=.*[A-Z])(.)*)", message="error.passwords_strength")
	protected String password_entry;

	@Transient
	protected String password_confirm;

	protected String password;

	protected Timestamp date_registered;

	protected Timestamp date_last_login;

	protected String verification_token;

	protected String verification_status;

	protected String auth_token;
	
	protected Timestamp auth_token_expiry;
	
	protected Timestamp date_last_sent_verification;
	
	@OneToMany(mappedBy = "au", targetEntity = OrgUser.class, fetch = FetchType.EAGER)
	protected List<OrgUser> ou;
	// Regular expression to validate password:
	// (?=.*\\d) => must contain a digit
	// (?=.*[A-Z]) => must contain an uppercase character
	private static final String PASSWORD_PATTERN = "((?=.*\\d)(?=.*[A-Z])(.)*)";
	private static final int AUTH_TOKEN_EXPIRY_MINS = 30;
	
	public static final String OVERALL_ADMIN = "OVERALL_ADMIN";
	public static final String ADMIN = "ADMIN";
	public static final String MEMBER = "MEMBER";
			
	/**
	 * Gets a user from the email address
	 * @param email address to search for
	 * @return The user matching the email address
	 */
	@Transactional (readOnly=true)
	public static AppUser findByEmail(String email) 
	{
		if (email == null || email.equals("")) // we won't find these
			return null;
		
		TypedQuery<AppUser> query = JPA.em().createNamedQuery(
				"AppUser.findByEmail", AppUser.class);
		query.setParameter("email", email);
		List<AppUser> results = query.getResultList();
		if (results.isEmpty()) {
			return null;
		}
		return (results.get(0));

	}

	/**
	 * Get the list of organisation user entries for this user (allows the user to be 
	 * associated with organisations)
	 * @return
	 */
	public List<OrgUser> getOu() {
		return ou;
	}

	/**
	 * Set the organisation users for this user
	 * @param ou
	 */
	public void setOu(List<OrgUser> ou) {
		this.ou = ou;
	}

	/**
	 * Get the id
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
	 * Get the full name
	 * @return
	 */
	public String getFullname() {
		return fullname;
	}

	/**
	 * Sets the full name
	 * @param fullname
	 */
	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	/**
	 * Gets the email address
	 * @return
	 */
	public String getEmail_address() {
		return email_address;
	}

	/**
	 * Sets the email address
	 * @param email_address
	 */
	public void setEmail_address(String email_address) {
		this.email_address = email_address;
	}

	/** 
	 * Gets the phone number
	 * @return
	 */
	public String getPhone() {
		return phone;
	}

	/**
	 * Sets the phone number
	 * @param phone
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}

	/**
	 * Gets the date registered
	 * @return
	 */
	public Timestamp getDate_registered() {
		return date_registered;
	}

	/**
	 * Sets the date registered
	 * @param date_registered
	 */
	public void setDate_registered(Timestamp date_registered) {
		this.date_registered = date_registered;
	}

	/**
	 * Gets the date last logged in
	 * @return
	 */
	public Timestamp getDate_last_login() {
		return date_last_login;
	}
	/**
	 * Gets the date last sent a verification email
	 * @return
	 */
	public Timestamp getDate_last_sent_verification() {
		return date_last_sent_verification;
	}
	
	/**
	 * Sets the date last logged in
	 * @param date_last_login
	 */
	public void setDate_last_login(Timestamp date_last_login) {
		this.date_last_login = date_last_login;
	}
	/**
	 * Sets the date last sent a verification
	 * @param date_last_login
	 */
	public void setDate_last_sent_verification(Timestamp date_last_sent_verification) {
		this.date_last_sent_verification = date_last_sent_verification;
	}
	
	/**
	 * Gets the password
	 * @return
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password
	 * @param password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Gets the password entry
	 * @return
	 */
	public String getPassword_entry() {
		return password_entry;
	}

	/**
	 * Sets the password entry
	 * @param password_entry
	 */
	public void setPassword_entry(String password_entry) {
		this.password_entry = password_entry;
	}

	/**
	 * Gets the password confirmation
	 * @return
	 */
	public String getPassword_confirm() {
		return password_confirm;
	}

	/**
	 * Sets the password confirmation
	 * @param password_confirm
	 */
	public void setPassword_confirm(String password_confirm) {
		this.password_confirm = password_confirm;
	}

	/**
	 * Gets the verification token
	 * @return
	 */
	public String getVerification_token() {
		return verification_token;
	}

	/**
	 * Sets the verification token
	 * @param verification_token
	 */
	public void setVerification_token(String verification_token) {
		this.verification_token = verification_token;
	}

	/**
	 * Gets the verification status
	 * @return
	 */
	public String getVerification_status() {
		return verification_status;
	}

	/**
	 * Sets the verification status
	 * @param verification_status
	 */
	public void setVerification_status(String verification_status) {
		this.verification_status = verification_status;
	}

	/**
	 * Setting items prior to save
	 */
	@PrePersist
	public void preSave() {
		date_registered = new Timestamp(new java.util.Date().getTime());
		verification_status = AppUser.UNVERIFIED;
		verification_token = generateToken();
	}

	/**
	 * Gets a verified user by email
	 * @param email email address to find
	 * @return the user
	 */
	public static AppUser findVerifiedByEmail(String email) {

		TypedQuery<AppUser> query = JPA.em().createNamedQuery(
				"AppUser.findVerifiedByEmail", AppUser.class);
		query.setParameter("email", email);
		List<AppUser> results = query.getResultList();
		if (results.isEmpty()) {
			return null;
		}
		return (results.get(0));
	}

	/**
	 * Gets an unverified user by email 
	 * @param email email address to find
	 * @return the user
	 */
	private static AppUser findUnverifiedByEmail(String email) {
		TypedQuery<AppUser> query = JPA.em().createNamedQuery(
				"AppUser.findUnverifiedByEmail", AppUser.class);
		query.setParameter("email", email);
		List<AppUser> results = query.getResultList();
		if (results.isEmpty()) {
			return null;
		}
		return (results.get(0));
	}
	/**
	 * authenticate
	 * 
	 * Attempt to authenticate the user by email address and password
	 * 
	 * @param email
	 * @param password
	 * @return matching AppUser entity or null
	 */
	public static AppUser authenticate(String email, String password) 
	{
		AppUser au = AppUser.findVerifiedByEmail(email);
		if (au == null) {
			return null;
		}
		try {
			if (PasswordHash.validatePassword(password, au.getPassword())) {
				if (au.getVerification_status().equals(AppUser.VERIFIED))
				{
					au.date_last_login = new Timestamp(
						new java.util.Date().getTime());
					if (!au.getVerification_token().equals("")) {
						au.setVerification_token("");
					}
					JPA.em().merge(au);
				}
				return au;
			}
		} catch (NoSuchAlgorithmException e) {
			Logger.error(e.getMessage());
		} catch (InvalidKeySpecException e) {
			Logger.error(e.getMessage());
		}
		
		return null;
	}

	/**
	 * Saves the user to the database
	 */
	public void save() {
		JPA.em().persist(this);
	}

	/**
	 * Updates the user record in the database
	 */
	public void update() {
		if (this.getId() == null) {
			if (this.getEmail_address() != null) {
				this.setId(AppUser.findByEmail(email_address).getId());
			}
		}
		JPA.em().merge(this);
	}
	
	/**
	 * Sets the verification token
	 * @param ver_token token to associate with the user
	 * @return true if successful
	 */
	public static boolean verify(String ver_token) {
		// first try and find the user record by the supplied token...
		TypedQuery<AppUser> query = JPA.em().createNamedQuery(
				"AppUser.findByVerificationToken", AppUser.class);
		query.setParameter("token", ver_token);
		List<AppUser> results = query.getResultList();
		if (results.isEmpty()) {
			return false;
		}
		AppUser au = results.get(0);
		try {
			au.setVerification_status(AppUser.VERIFIED);
			
			JPA.em().merge(au);
			return true;
		} catch (Exception e) {
			Logger.error(e.getMessage());
		}
		return false;
	}
	
	/**
	 * Sets the verification token based on email address
	 * @param email address of the user
	 * @return true if successful
	 */
	public boolean verifyByEmail(String email) {
		// first try and find the user record by the supplied token...
		TypedQuery<AppUser> query = JPA.em().createNamedQuery(
				"AppUser.findByEmail", AppUser.class);
		query.setParameter("email", email);
		List<AppUser> results = query.getResultList();
		if (results.isEmpty()) {
			return false;
		}
		AppUser au = results.get(0);
		try {
			au.setVerification_status(AppUser.VERIFIED);
			
			JPA.em().merge(au);
			return true;
		} catch (Exception e) {
			Logger.error(e.getMessage());
		}
		return false;
	}

	/**
	 * Constructor
	 */
	public AppUser() {
		super();
	}
	
	/**
	 * Determines whether the user is an administrator, member or nothing in an organisation
	 * @return
	 */
	public String getOrgRole() 
	{
		/* Does user have admin access to any organisation */

		String sReturn = ""; 
		Iterator <OrgUser> it = ou.iterator();
		while (it.hasNext())
		{
			if (sReturn.equals(""))
				sReturn = MEMBER;
			
			OrgUser orguser = it.next();
			if (orguser.isAdministrator())
			{
				sReturn = ADMIN;
			
				if (orguser.getOrg().isAdministrator())
					return OVERALL_ADMIN;
			}
		}
		
		return sReturn;
	}
	/**
	 * Determines whether the user is an administrator, member or nothing in a specific organisation
	 * @param org the organisation to check
	 * @return
	 */
	public String getOrgRoleForOrg(Organisation org) 
	{
		String sOrgRole = getOrgRole(); 
		if (sOrgRole.equals(OVERALL_ADMIN))
			return OVERALL_ADMIN;
		
		List lOrgs = Organisation.getMyAdminOrganisations(this);
		if (lOrgs.contains(org))
			return ADMIN;
		
		if (sOrgRole == MEMBER)
			return MEMBER;
		
		return "";
	}
		
	/**
	 * Determines whether the organisation type is a noise producer or regulator
	 * @return
	 */
	public String getUserOrgType() {
		/* Users can belong to multiple orgs but only one org type - noise producer or regulator */

		if (this.isNoiseProducerMember()) 
			return "NOISEPRODUCER";
		if (this.isRegulatorMember()) 
			return "REGULATOR";
		return "NONE";
	}

	public static List<AppUser> getUnverifiedUsers() {
		TypedQuery<AppUser> query = JPA.em().createNamedQuery(
				"AppUser.getUnverifiedUsers", AppUser.class);

		List<AppUser> results = query.getResultList();

		return results;
	}
		
	/**
	 * Gets the organisation administators
	 * @param ou an organisation user
	 * @return List of administrators for the same organisation
	 */
	public List<OrgUser> getOrgAdmins(OrgUser ou) {
		TypedQuery<OrgUser> query = JPA.em().createNamedQuery(
				"AppUser.findOrgAdmins", OrgUser.class);

		query.setParameter("org_id", ou.getOrg().getId());
		List<OrgUser> results = query.getResultList();

		return results;
	}

	/**
	 * Determines whether the user is a member of a noise producer
	 * @return
	 */
	public boolean isNoiseProducerMember() {
		boolean res = false;
		
		if (this.getOu()==null) // user with no orguser record
			return false;
		
		if (!this.getOu().isEmpty()) {
			Iterator<OrgUser> iter = this.getOu().iterator();
			while(iter.hasNext() & !res) {
				if (iter.next().getOrg().isNoiseProducer()) {
					res = true;
				}
			}
		}
		return res;
	}
	
	/**
	 * Determines whether the user is a member of a regulator
	 * @return
	 */
	public boolean isRegulatorMember() {
		boolean res = false;
		
		if (this.getOu()==null) // user with no orguser record
			return false;

		if (!this.getOu().isEmpty()) {
			Iterator<OrgUser> iter = this.getOu().iterator();
			while(iter.hasNext() & !res) {
				if (iter.next().getOrg().isRegulator()) {
					res = true;
				}
			}
		}
		return res;
	}
	/**
	 * 
	 * @return String containing random token
	 */
	String generateToken() {
		SecureRandom random = new SecureRandom();
		byte[] token = new byte[16];
		random.nextBytes(token);
		BigInteger bi = new BigInteger(1, token);
		String res = bi.toString(16);

		return res;
	}

	/**
	 * Encrypts the password
	 */
	public void encryptPassword() {
		try {
			this.setPassword(PasswordHash.createHash(this.getPassword_entry()));
		} catch (NoSuchAlgorithmException e) {
			Logger.error(e.getMessage());
		} catch (InvalidKeySpecException e) {
			Logger.error(e.getMessage());
		}	
	}

	/**
	 * Resets the password
	 * @param email email of the user to reset the password of
	 * @return the new password
	 */
	public static String resetPassword(String email) {

		AppUser au = AppUser.findByEmail(email);
		if (au==null) {
			return null;
		}
		try {
			String newPassword = generateValidPassword();
			
			au.setPassword_entry(newPassword);
			au.setPassword_confirm(newPassword);
			
			au.encryptPassword();
			au.setPassword(au.getPassword());
			JPA.em().merge(au);

			return newPassword;
		} catch (Exception e) {
			Logger.error(e.getMessage());
			return null;
		}
	}

	/**
	 * Generate a new password
	 * @return The new password
	 */
	static String generateValidPassword() {
		String res = getRandomString(8);
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(PASSWORD_PATTERN);
		Matcher matcher = pattern.matcher(res);
		while (!matcher.matches()) {
			res = getRandomString(8);
			matcher = pattern.matcher(res);
		}
		return res;
	}

	/**
	 * Gets a pseudo random string
	 * @param length length of string to return
	 * @return the string
	 */
	private static String getRandomString(int length) {
		final String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJLMNOPQRSTUVWXYZ1234567890!";
		StringBuilder result = new StringBuilder();
		while (length > 0) {
			SecureRandom rand = new SecureRandom();
			result.append(characters.charAt(rand.nextInt(characters.length())));
			length--;
		}
		return result.toString();
	}

	/**
	 * Saves the registration details to the database
	 * @param appUserRegistration details to save
	 */
	public static void saveRegistration(AppUserRegistration appUserRegistration) {
		// Update any non-verified account, or create a new one
		boolean bNew = false;
		AppUser au = AppUser.findUnverifiedByEmail(appUserRegistration.getEmail_address());
		if (au == null) {
			bNew = true;
			au = new AppUser();
		}
		au.setEmail_address(appUserRegistration.getEmail_address());
		au.setPhone(appUserRegistration.getPhone());
		au.setFullname(appUserRegistration.getFullname());
		au.setPassword_entry(appUserRegistration.getPassword_entry());
		au.setPassword_confirm(appUserRegistration.getPassword_confirm());
		au.encryptPassword();
		if (bNew) {
			JPA.em().persist(au);
		} else {
			//reset the registration details and verification token...
			au.preSave();
			JPA.em().merge(au);
		}
	}

	/**
	 * Updates the user password
	 * @param appUserChangePassword model containing the password change data 
	 */
	public static void updatePassword(
			AppUserChangePassword appUserChangePassword) {
		// Get the authenticated user, then update the password
		AppUser au = AppUser.authenticate(appUserChangePassword.getEmail_address(), appUserChangePassword.getCurrent_password());
		if (au!=null) {
			au.setPassword_entry(appUserChangePassword.getPassword_entry());
			au.setPassword_confirm(appUserChangePassword.getPassword_confirm());
			au.encryptPassword();
			JPA.em().merge(au);
		}
	}

	
	/**
	 * Update the AppUser record using the details passed in.
	 * 
	 * The AppUserDetails provides support for partial updates 
	 * with validation and automatic form binding
	 * 
	 * @param appUserDetails
	 * @param id
	 */
	public static void updateDetails(AppUserDetails appUserDetails, Long id) {
		AppUser au = JPA.em().find(AppUser.class, id);
		if (au!=null) {
			au.setEmail_address(appUserDetails.getEmail_address());
			au.setFullname(appUserDetails.getFullname());
			au.setPhone(appUserDetails.getPhone());
			JPA.em().merge(au);
		}
	}

	/**
	 * Create a new AppUserDetails instance with values set from the
	 * current AppUser object.
	 * 
	 * @return AppUserDetails
	 */
	public AppUserDetails toAppUserDetails() {
		AppUserDetails aud = new AppUserDetails();
		aud.setEmail_address(this.getEmail_address());
		aud.setFullname(this.getFullname());
		aud.setPhone(this.getPhone());
		return aud;
	}

	/**
	 * Generate a list of noise producers that the current object is a verified member of.
	 * 
	 * @return List<NoiseProducer>
	 */
	public List<NoiseProducer> findNoiseProducers() {
		
		List<NoiseProducer> nps = new ArrayList<NoiseProducer>();
		if (!this.getOu().isEmpty()) {
			Iterator<OrgUser> iter = this.getOu().iterator();
			while (iter.hasNext()) {
				OrgUser ou = iter.next();
				if (ou.getStatus().equals(OrgUser.VERIFIED)) {
					NoiseProducer np = ou.getOrg().getNoiseProducer();
					
					if (np!=null) {
						nps.add(np);
					}
				}
			}
		}
		Collections.sort(nps);
		return nps;
	}
	
	/**
	 * Generate a list of regulators that the current object is a verified member of.
	 * 
	 * @return List<Regulator>
	 */
	public List<Regulator> findRegulators() {
		
		List<Regulator> regs = new ArrayList<Regulator>();
		if (!this.getOu().isEmpty()) {
			Iterator<OrgUser> iter = this.getOu().iterator();
			while (iter.hasNext()) {
				OrgUser ou = iter.next();
				if (ou.getStatus().equals(OrgUser.VERIFIED)) {
					Regulator reg = ou.getOrg().getRegulator();
					
					if (reg!=null) {
						regs.add(reg);
					}
				}
			}
		}
		return regs;
	}
	
	/**
	 * Get activity Applications for this user by the status of them
	 * @param status the status of the activity application to look for
	 * @param comparator the sort order
	 * @return List of ActivityApplications
	 */
	public List<ActivityApplication> findApplicationsByStatus(String status, Comparator<ActivityApplication> comparator)
	{
		List<ActivityApplication> laa = new ArrayList<ActivityApplication>();
		if (getOrgRole() == OVERALL_ADMIN)
		{
			laa = ActivityApplication.findAllByStatus(status);
		}
		else
		{
			List<OrgUser> lou = this.getOu();
			Iterator<OrgUser> it = lou.iterator();
			OrgUser ou;
			Organisation org;
			
			while (it.hasNext())
			{
				ou = it.next();
				
				if (ou.getStatus().compareTo(OrgUser.VERIFIED)==0)
				{
					org = ou.getOrg();
					laa.addAll(org.findApplicationsByStatus(status));
				}
			}
			//Use the appropriate sort order depending on the user type...
			if (comparator == null) {
				comparator = new ActivityApplication.NoiseProducerComparator();
			}
			Collections.sort(laa, comparator);
		}
		return laa;
	}

	/**
	 * Gets a user by the token
	 * @param token the token to look for
	 * @return the user
	 */
	public static AppUser findByAuthToken(String token) {
		// Lookup user by the auth token, checking expiry...
		TypedQuery<AppUser> query = JPA.em().createNamedQuery(
				"AppUser.findByAuthToken", AppUser.class);
		query.setParameter("token", token);
		List<AppUser> results = query.getResultList();
		if (results.isEmpty()) {
			return null;
		}
		AppUser res = results.get(0);
		if (res.auth_token_expiry.before(new Timestamp(new java.util.Date().getTime()))) {
			//Expiry has passed
			Logger.error("Auth token has expired!");
			res.clearAuthToken();
			return null;
		} else {
			Logger.info("Auth token has NOT expired!");
		}
		return res;
		
	}

	/**
	 * Generates a token that expires after 60 minutes 
	 * @return
	 */
	public String createAuthToken() {
		// Generate a token and expiry, update the user record and return the token.
		this.auth_token = generateToken();
		this.auth_token_expiry = new Timestamp(new java.util.Date().getTime() + (1000 * 60 * AUTH_TOKEN_EXPIRY_MINS));
		
		this.update();
		return this.auth_token;
	}
	
	/**
	 * clearAuthToken
	 * 
	 * Remove the auth token value and expiry from the appuser entry.
	 * @NOTE this will only work for transactions that are not readonly
	 */
	private void clearAuthToken() {
		this.auth_token = null;
		this.auth_token_expiry = null;
		JPA.em().merge(this);
	}
	
	public void logout() {
		this.clearAuthToken();
	}

	/**
	 * setUserFilters
	 * 
	 * For access to the database, set any filters in the session that are
	 * required for the conditional access.
	 */
	public void setUserFilters() 
	{
		if (getOrgRole() == OVERALL_ADMIN) // no restrictions for overall admin
			return;
		//
		org.hibernate.Session session = JPA.em().unwrap(org.hibernate.Session.class);
		if (session.getEnabledFilter("myNoiseProducers")!=null) {
			Logger.error("myNoiseProducers already enabled");
			return;
		}
		if (session.getEnabledFilter("myRegulators")!=null) {
			Logger.error("myRegulators already enabled");
			return;
		}
		
		List<Long> ids = new ArrayList<Long>();
		List<NoiseProducer> myNps = this.findNoiseProducers();
		if (!myNps.isEmpty()) {
			Iterator<NoiseProducer> it = myNps.iterator();
			while (it.hasNext())
			{
				ids.add(it.next().getId());
			}
			session.enableFilter("myNoiseProducers").setParameterList("np_id", ids);
		} else {
			//Not a noise producer, could be a regulator...
			List<Regulator> myRegs = this.findRegulators();
			if (!myRegs.isEmpty()) {
				Iterator<Regulator> it = myRegs.iterator();
				while (it.hasNext())
				{
					ids.add(it.next().getId());
				}
				session.enableFilter("myRegulators").setParameterList("reg_id", ids);
			}
		}
	    //if the user is neither regulator nor noise producer, set a filter to match nothing
		if (ids.isEmpty()) {
			session.enableFilter("myNoiseProducers").setParameter("np_id", -1L);
		}
	}

	/**
	 * 
	 * Setup the user object and set any session filters required for application security.
	 * 
	 * @param username
	 * @return
	 */
	public static AppUser getSystemUser(String username) {
		AppUser au = AppUser.findByEmail(username);
		if (JPA.em() != null && au!= null) {
			au.setUserFilters();
		} else {
			Logger.error("Not setting filters - no transaction");
		}
		return au;
	}
}