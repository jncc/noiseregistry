import static org.junit.Assert.*;

import java.util.Set;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import models.ActivityLocation;
import models.AppUser;
import models.AppUserRegistration;
import models.NoiseProducer;
import models.Organisation;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import play.db.jpa.*;
import play.test.FakeApplication;
import play.test.Helpers;
import scala.Option;

public class OrgCreateTest {
	
	private static String password="Passw0Rd!";
	private static String email="user@company.com";
	private static String phone="0123456789";
	private static String fullname="John Doe";
	
	private static String org_name="My Noise Producer";
	private static String org_contactname="Company Director";
	private static String org_contactemail="director@company.com";
	private static String org_contactphone="0123456789";
	
	private static String org_name2="My Noise Producer2";
	private static String org_contactname2="Company Director2";
	private static String org_contactemail2="director2@company.com";
	private static String org_contactphone2="01234567892";
	
	private static Validator validator;
	
	@PersistenceContext
	private static EntityManager em;
	
	@BeforeClass
	public static void setUp() {
		final FakeApplication app = Helpers.fakeApplication();
		Helpers.start(app);
		Option<JPAPlugin> jpaPlugin = app.getWrappedApplication().plugin(JPAPlugin.class);
		em = jpaPlugin.get().em("default");
		
	}

	@Before
	public void setUp2() {
		JPA.bindForCurrentThread(em);
		em.getTransaction().begin();
		AppUserRegistration aur = new AppUserRegistration();
		aur.setEmail_address(email);
		aur.setPassword_entry(password);
		aur.setPassword_confirm(password);
		aur.setFullname(fullname);
		aur.setPhone(phone);
		
		aur.save();
		
		AppUser au = AppUser.findByEmail(email);
		AppUser.verify(au.getVerification_token());
		
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	    validator = factory.getValidator();
	}
	
	@After
	public void tearDown2() {
		em.getTransaction().rollback();
	}
	@AfterClass
	public static void tearDown() {
		JPA.bindForCurrentThread(null);
		if (em!=null)
			em.close();
	}
	
	@Test
	public void test_createNoiseProducer() {
		try {
			int npstart = NoiseProducer.findAll().size();
			NoiseProducer np = new NoiseProducer();
			Organisation org = new Organisation();
			org.setOrganisation_name(org_name);
			org.setContact_email(org_contactemail);
			org.setContact_name(org_contactname);
			org.setContact_phone(org_contactphone);
			np.setOrganisation(org);
			
			Set<ConstraintViolation<NoiseProducer>> constraintViolations =
				      validator.validate( np );
			assertTrue(constraintViolations.isEmpty());
			
			AppUser au = AppUser.findByEmail(email);
			np.save(au);
			em.flush();
			
			List<Organisation> orgs = Organisation.findAll();
			assertNotNull(orgs);
			assertFalse(orgs.isEmpty());
			
			//Organisation org2 = orgs.get(0);
			Organisation org2 = JPA.em().find(Organisation.class, np.getOrganisation().getId());
			assertEquals(org2.getOrganisation_name(), org_name);
			assertEquals(org2.getContact_email(), org_contactemail);
			assertEquals(org2.getContact_name(), org_contactname);
			assertEquals(org2.getContact_phone(), org_contactphone);
			
			assertEquals(NoiseProducer.findAll().size(), npstart+1);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void test_createMultipleNoiseProducers() {
		try {
			int npstart = NoiseProducer.findAll().size();
			
			NoiseProducer np = new NoiseProducer();
			Organisation org = new Organisation();
			org.setOrganisation_name(org_name);
			org.setContact_email(org_contactemail);
			org.setContact_name(org_contactname);
			org.setContact_phone(org_contactphone);
			np.setOrganisation(org);
			
			Set<ConstraintViolation<NoiseProducer>> constraintViolations =
				      validator.validate( np );
			assertTrue(constraintViolations.isEmpty());
			
			np.save(AppUser.findByEmail(email));
			em.flush();
			
			
			np = new NoiseProducer();
			org = new Organisation();
			org.setOrganisation_name(org_name2);
			org.setContact_email(org_contactemail2);
			org.setContact_name(org_contactname2);
			org.setContact_phone(org_contactphone2);
			np.setOrganisation(org);
			
			constraintViolations =
				      validator.validate( np );
			assertTrue(constraintViolations.isEmpty());
			
			np.save(AppUser.findByEmail(email));
			em.flush();
			
			assertEquals(NoiseProducer.findAll().size(), npstart + 2);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
