/**
 * Integration tests for the regulator interface.
 * 
 * Depends on the ActivityApplicationTest class to generate partially complete activity application records
 * that are used to test the access to items for the user's regulator.
 */
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.contentType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import models.ActivityApplication;
import models.ActivityApplicationCloseOut;
import models.AppUser;
import models.AppUserRegistration;
import models.NoiseProducer;
import models.OrgUser;
import models.Organisation;
import models.Regulator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.test.WithBrowser;
import play.i18n.Messages;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.test.FakeApplication;
import play.test.Helpers;
import play.twirl.api.Content;
import scala.Option;

public class RegulatorTest extends WithBrowser {
	private static String password="Passw0Rd!";
	private static String email="user@company.com";
	private static String phone="0123456789";
	private static String fullname="John Doe";

	private static String org_name="My Regulator";
	private static String org_contactname="Company Director";
	private static String org_contactemail="director@company.com";
	private static String org_contactphone="01234567892";
	
	private static String org_name2="My Regulator2";
	private static String org_contactname2="Company Director2";
	private static String org_contactemail2="director2@company.com";
	private static String org_contactphone2="01234567892";
	
	private static String org_name3="My Noise Producer";
	private static String org_contactname3="Company Director3";
	private static String org_contactemail3="director3@company.com";
	private static String org_contactphone3="01234567892";
	
	private static int reg_closeoutdays = 120;
	
	private static NoiseProducer np;
	private static Regulator reg;
	private static Regulator reg2;
	
	private AppUser au = null;

	@PersistenceContext
	private static EntityManager em;
	

	@Before
	public void setUp() {
		final FakeApplication app = Helpers.fakeApplication();
		Helpers.start(app);
						
		Option<JPAPlugin> jpaPlugin = app.getWrappedApplication().plugin(JPAPlugin.class);
		em = jpaPlugin.get().em("default");
		JPA.bindForCurrentThread(em);
		em.getTransaction().begin();
		AppUserRegistration aur = new AppUserRegistration();
		aur.setEmail_address(email);
		aur.setPassword_entry(password);
		aur.setPassword_confirm(password);
		aur.setFullname(fullname);
		aur.setPhone(phone);
		
		aur.save();
		
		au = AppUser.findByEmail(email);
		AppUser.verify(au.getVerification_token());
		
		reg = new Regulator();
		Organisation org = new Organisation();
		org.setOrganisation_name(org_name);
		org.setContact_email(org_contactemail);
		org.setContact_name(org_contactname);
		org.setContact_phone(org_contactphone);
		org.setAdministrator(false);
		org.setAccepts_email(false);
		reg.setOrganisation(org);
		reg.setCloseoutdays(reg_closeoutdays);
		
		reg.save();
		
		reg2 = new Regulator();
		org = new Organisation();
		org.setOrganisation_name(org_name2);
		org.setContact_email(org_contactemail2);
		org.setContact_name(org_contactname2);
		org.setContact_phone(org_contactphone2);
		org.setAdministrator(false);
		org.setAccepts_email(false);
		reg2.setOrganisation(org);
		reg2.setCloseoutdays(reg_closeoutdays);
		
		reg2.save();
		
		np = new NoiseProducer();
		org = new Organisation();
		org.setOrganisation_name(org_name3);
		org.setContact_email(org_contactemail3);
		org.setContact_name(org_contactname3);
		org.setContact_phone(org_contactphone3);
		org.setAdministrator(false);
		org.setAccepts_email(false);
		np.setOrganisation(org);
		
		np.save();
		
		OrgUser ou = new OrgUser();
		ou.setAu(au);
		ou.setOrg(reg.getOrganisation());
		ou.setStatus("verified");
		
		ou.save();
		
		List<OrgUser> ous = new ArrayList<OrgUser>();
		ous.add(ou);
		au.setOu(ous);		
	}
	@After
	public void tearDown()
	{
		em.getTransaction().rollback();
		JPA.bindForCurrentThread(null);
		if (em!=null)
			em.close();
	}
	
	@Test
	public void test_RegulatorHome()
	{
		String activeTab="HOME";
    	AppUser au = AppUser.getSystemUser(email);
    	//Logger.error(au.getUserOrgType());
    	assertEquals("REGULATOR", au.getUserOrgType());
        Content html = views.html.home.render(au, activeTab);
        assertEquals("text/html", contentType(html));
        //Logger.error(contentAsString(html));
        //Regulator home page should include links to the proposed and completed activity applications,
        //but no link to create a new application - only noise producers get to create these items.
        
        assertThat(contentAsString(html), not(containsString(Messages.get("actions.create_new_activity"))));
        assertThat(contentAsString(html), containsString(Messages.get("actions.view_proposed_activities_regulator")));
        assertThat(contentAsString(html), containsString(Messages.get("actions.view_completed_activities")));
	}
	
	@Test
	public void test_RegulatorUpdateAcceptsEmail()
	{
		AppUser au = AppUser.getSystemUser(email);
    	//Logger.error(au.getUserOrgType());
    	assertEquals("REGULATOR", au.getUserOrgType());
    	
    	Organisation myOrg = JPA.em().find(Organisation.class,  reg.getOrganisation().getId());
    	myOrg.setAccepts_email(true);
    	myOrg.save();
    	assertEquals(myOrg.getRegulator().getAccepts_email().booleanValue(), true);
    	myOrg.setAccepts_email(false);
    	myOrg.save();
    	assertEquals(myOrg.getRegulator().getAccepts_email().booleanValue(), false);
    	
	}
	@Test
	public void test_RegulatorAccessToAA()
	{
    	AppUser au = AppUser.getSystemUser(email);
    	//Logger.error(au.getUserOrgType());
    	assertEquals("REGULATOR", au.getUserOrgType());
    	
    	//ensure that the user cannot access the activity application for another regulator
    	Long id = createProposedAA(reg2);
    	assertNull(ActivityApplication.findById(id));
    	
    	//ensure that the user can access the activity application for their own regulator
    	Long idProp = createProposedAA(reg);
    	assertNotNull(ActivityApplication.findById(idProp));
    	
    	//ensure the user cannot see draft applications...
    	id = createDraftAA(reg);
    	assertNull(ActivityApplication.findById(id));
    	
		Map<String, String> m = new HashMap<String, String>();
		m.put("max_hammer_energy_actual", "7");
		m.put("sound_pressure_level_actual","8");
		m.put("sound_exposure_level_actual","9");
		
    	//ensure that the user can see closed applications...
    	try {
    		ActivityApplication.closeOut(new ActivityApplicationCloseOut(), idProp, false, m, false);
    	} catch (Exception e) {
    		fail(e.getMessage());	
    	}
    	ActivityApplication aa = ActivityApplication.findById(idProp);
    	
    	assertNotNull(aa);
    	assertEquals("Closed", aa.getStatus());
    	
    	
	}
	
	/***
	 * 
	 * This test withdrawn as it now uses the referer in the template and that doesn't play nicely with the test
	 * 
	 */
	/*@Test 
	public void test_RegulatorViewOrganisation()
	{
		AppUser au = AppUser.getSystemUser(email);
    	//Logger.error(au.getUserOrgType());
    	assertEquals("REGULATOR", au.getUserOrgType());
    	//ensure that the user can access the activity application for their own regulator
    	Long id = createProposedAA(reg);
    	ActivityApplication aa = ActivityApplication.findById(id);
    	assertNotNull(aa);
    	
    	Content html = views.html.activityformread.render(au, aa, "read");
    	
        assertEquals("text/html", contentType(html));
        //Make sure that the rendered content includes the Organisation details (noise producer name, email, phone)
        assertThat(contentAsString(html), containsString(Messages.get("activityform.field_heading.noiseproducer_name")));
        assertThat(contentAsString(html), containsString(Messages.get("activityform.field_heading.noiseproducer_email")));
        assertThat(contentAsString(html), containsString(Messages.get("activityform.field_heading.noiseproducer_phone")));
	}*/
	
	private Long createProposedAA(Regulator reg) {
		//get a (mostly) populated activity application...
		ActivityApplication aa = ActivityApplicationTest.getAA();
		//set the np and reg to our values
		aa.setNoiseproducer(np);
		aa.setRegulator(reg);
		
		aa.save(au);
		
		return aa.getId();
	}
	
	private Long createDraftAA(Regulator reg) {
		//get a (mostly) populated activity application...
		ActivityApplication aa = ActivityApplicationTest.getAA();
		//set the np and reg to our values
		aa.setNoiseproducer(np);
		aa.setRegulator(reg);
		aa.setSaveasdraft("true");
		aa.save(au);
		
		return aa.getId();
	}
}
