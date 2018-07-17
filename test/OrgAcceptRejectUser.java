import static org.junit.Assert.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import play.db.jpa.*;
import play.test.FakeApplication;
import play.test.Helpers;
import scala.Option;
import models.AppUserRegistration;
import models.AppUser;
import models.OrgUser;
import models.NoiseProducer;
import models.Organisation;


/**
*
* Simple (JUnit) tests that can call all parts of a play app.
* If you are interested in mocking a whole application, see the wiki for more details.
*
*/
public class OrgAcceptRejectUser  {
	private static String contact_email="test@testorg.com";
	private static String contact_name="Test User";
	private static String contact_phone="0123456789";
	private static String organisation_name="Test Organisation";
	private static String password="Passw0Rd!";
	private static String email="user@company.com";
	private static String email2="user2@company.com";
	private static String phone="0123456789";
	private static String phone2="0123456789";
	private static String fullname="John Doe";
	private static String fullname2="John Doe2";
	private static NoiseProducer np=null;
	private static AppUser au = null;
	
	
	@PersistenceContext
	private static EntityManager em;
	
	@BeforeClass
	public static void setUp() {
		final FakeApplication app = Helpers.fakeApplication();
		Helpers.start(app);
		Option<JPAPlugin> jpaPlugin = app.getWrappedApplication().plugin(JPAPlugin.class);
		em = jpaPlugin.get().em("default");
		JPA.bindForCurrentThread(em);
		em.getTransaction().begin();
		
		//ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	    //validator = factory.getValidator();
		
		np = new NoiseProducer();
		Organisation org = new Organisation();
		org.setContact_email(contact_email);
		org.setContact_name(contact_name);
		org.setContact_phone(contact_phone);
		org.setOrganisation_name(organisation_name);
		
		np.setOrganisation(org);
		
		AppUserRegistration aur = new AppUserRegistration();
		aur.setEmail_address(email);
		aur.setPassword_entry(password);
		aur.setPassword_confirm(password);
		aur.setFullname(fullname);
		aur.setPhone(phone);
		
		aur.save();
		au = AppUser.findByEmail(email);
		AppUser.verify(au.getVerification_token());
		
		np.save(au);
		em.flush();
		
		np.getId();
	}
	
	@AfterClass
	public static void tearDown()
	{
		em.getTransaction().rollback();
		JPA.bindForCurrentThread(null);
		if (em!=null)
			em.close();
	}
	@Test
	public void test_userOptions()
	{
		Organisation org = np.getOrganisation();
		OrgUser ou = OrgUser.findOU(au.getId(), org.getId());
		
		// first user of org should be verified admin
		assertTrue(ou.getStatus()=="verified");
		assertTrue(ou.isAdministrator());

		// as this is a new user it should be unverified and not an administrator
		ou.save();

		AppUserRegistration aur = new AppUserRegistration();
		aur.setEmail_address(email2);
		aur.setPassword_entry(password);
		aur.setPassword_confirm(password);
		aur.setFullname(fullname2);
		aur.setPhone(phone2);
		
		aur.save();
		AppUser au2 = AppUser.findByEmail(email2);
		AppUser.verify(au2.getVerification_token());
		au2.save();
		em.flush();
		
		org.addUser(au2);
		OrgUser ou2 = OrgUser.findOU(au2.getId(), org.getId());
		
		assertFalse(ou2.getStatus()=="verified");
		assertFalse(ou2.isAdministrator());
		
		ou2.setAdministrator(true);
		ou2.save();

		assertFalse(ou2.getStatus()=="verified");
		assertTrue(ou2.isAdministrator());

		ou2.setStatus("verified");
		ou2.save();
		
		assertTrue(ou2.getStatus()=="verified");
		assertTrue(ou2.isAdministrator());
	}
}
