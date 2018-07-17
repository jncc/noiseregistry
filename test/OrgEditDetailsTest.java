import static org.junit.Assert.*;

import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import play.db.jpa.*;
import play.test.FakeApplication;
import play.test.Helpers;
import scala.Option;
import models.AppUserRegistration;
import models.AppUser;
import models.NoiseProducer;
import models.Organisation;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OrgEditDetailsTest {
	private static Validator validator;
	private static String contact_email="test@testorg.com";
	private static String contact_name="Test User";
	private static String contact_phone="0123456789";
	private static String organisation_name="Test Organisation";
	private static String password="Passw0Rd!";
	private static String email="user@company.com";
	private static String phone="0123456789";
	private static String fullname="John Doe";
	private static Long lid = null;
	private static String new_contact_email="anotheremail@another.com";
	private static String new_contact_name="A Different Name";
	private static String new_contact_phone="987654321";
	private static String new_organisation_name="Business PLC";
	
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
		
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	    validator = factory.getValidator();
		
		NoiseProducer np = new NoiseProducer();
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
		AppUser au = AppUser.findByEmail(email);
		AppUser.verify(au.getVerification_token());
		
		np.save(au);
		em.flush();
		
		org = np.getOrganisation();
		assertFalse(org==null);
		
		lid = np.getId();
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
	public void test_orgDetails() {
		try {
			NoiseProducer np = em.find(NoiseProducer.class, lid);
			
			assertEquals(np.getOrganisation().getContact_email(), contact_email);
			assertEquals(np.getOrganisation().getContact_name(), contact_name);
			assertEquals(np.getOrganisation().getContact_phone(), contact_phone);
			assertEquals(np.getOrganisation().getOrganisation_name(), organisation_name);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test 
	public void test_updateOrgDetails() {
		try {
			NoiseProducer np = em.find(NoiseProducer.class, lid);
			Organisation org = np.getOrganisation();
			
			org.setContact_email(new_contact_email);
			org.setContact_name(new_contact_name);
			org.setContact_phone(new_contact_phone);
			org.setOrganisation_name(new_organisation_name);
			np.update();
			
			np = em.find(NoiseProducer.class, lid);
			
			assertEquals(np.getOrganisation().getContact_email(), new_contact_email);
			assertEquals(np.getOrganisation().getContact_name(), new_contact_name);
			assertEquals(np.getOrganisation().getContact_phone(), new_contact_phone);
			assertEquals(np.getOrganisation().getOrganisation_name(), new_organisation_name);
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void test_updateInvalidOrgDetails() {
		try {
			NoiseProducer np = em.find(NoiseProducer.class, lid);
			Organisation org = np.getOrganisation();
			assertFalse(org==null);
			org.setContact_email(null);

			Set<ConstraintViolation<NoiseProducer>> cv2 =
				      validator.validate( np );
			
			assertNotNull(cv2);
			
			np = em.find(NoiseProducer.class, lid);
			org = np.getOrganisation();
			org.setContact_name(null);

			Set<ConstraintViolation<NoiseProducer>> cv3 =
				      validator.validate( np );
			
			assertNotNull(cv3);

			np = em.find(NoiseProducer.class, lid);
			org = np.getOrganisation();
			org.setContact_phone(null);

			Set<ConstraintViolation<NoiseProducer>> cv4 =
				      validator.validate( np );
			
			assertNotNull(cv4);

			np = em.find(NoiseProducer.class, lid);
			org = np.getOrganisation();
			org.setOrganisation_name(null);

			Set<ConstraintViolation<NoiseProducer>> cv5 =
				      validator.validate( np );
			
			assertNotNull(cv5);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
}
