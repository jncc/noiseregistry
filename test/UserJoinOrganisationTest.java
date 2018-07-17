import static org.junit.Assert.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.validation.Validator;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.db.jpa.*;
import play.test.FakeApplication;
import play.test.Helpers;
import play.Logger;
import scala.Option;
import models.AppUserRegistration;
import models.AppUser;
import models.NoiseProducer;
import models.Organisation;
import models.OrgUser;


public class UserJoinOrganisationTest {
	private static String password="Passw0Rd!";
	private static String email="user@company.com";
	private static String phone="0123456789";
	private static String fullname="John Doe";
	private static String password2="Passw0Rd!";
	private static String email2="user2@company.com";
	private static String phone2="0123456789";
	private static String fullname2="John Doe2";
	
	private static Validator validator;
	
	@PersistenceContext
	private static EntityManager em;
	
	@BeforeClass
	public static void setUp() {
		final FakeApplication app = Helpers.fakeApplication();
		Helpers.start(app);
		Option<JPAPlugin> jpaPlugin = app.getWrappedApplication().plugin(JPAPlugin.class);
		em = jpaPlugin.get().em("default");
		JPA.bindForCurrentThread(em);
	}

	@Before
	public void setUp2() {
		
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

		AppUserRegistration aur2 = new AppUserRegistration();
		aur2.setEmail_address(email2);
		aur2.setPassword_entry(password2);
		aur2.setPassword_confirm(password2);
		aur2.setFullname(fullname2);
		aur2.setPhone(phone2);
		
		aur2.save();
		
		AppUser au2 = AppUser.findByEmail(email2);
		AppUser.verify(au2.getVerification_token());		
		
		NoiseProducer np1 = new NoiseProducer();
		Organisation org1 = new Organisation();
		org1.setOrganisation_name("Org1");
		org1.setContact_email("testemail@company.com");
		org1.setContact_name("contact name");
		org1.setContact_phone("01234");
		em.persist(org1);
		np1.setOrganisation(org1);
		em.persist(np1);
		
		NoiseProducer np2 = new NoiseProducer();
		Organisation org2 = new Organisation();
		org2.setOrganisation_name("Org2");
		org2.setContact_email("testemail2@company.com");
		org2.setContact_name("contact name2");
		org2.setContact_phone("012342");
		em.persist(org2);
		np2.setOrganisation(org2);
		em.persist(np2);
		
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
	public void test_join() {
		try {
			AppUser au = AppUser.findByEmail(email);
			List<NoiseProducer> nps = NoiseProducer.findAll();
			
			OrgUser ou = new OrgUser();
			ou.setAu(au);
			//Logger.error("Joining to org: " + nps.get(0).getOrganisation().getOrganisation_name());
			ou.setOrg(nps.get(0).getOrganisation());
			ou.setStatus(OrgUser.UNVERIFIED);
			Set<ConstraintViolation<OrgUser>> constraintViolations =
				      validator.validate( ou );
			assertTrue(constraintViolations.isEmpty());
			
			ou.save();
			em.flush();
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}

	@Test
	public void test_adminremove() {
		try {
			AppUser au = AppUser.findByEmail(email);
			List<NoiseProducer> nps = NoiseProducer.findAll();
			
			OrgUser ou = new OrgUser();
			OrgUser outmp;
			ou.setAu(au);
			Organisation org = nps.get(0).getOrganisation();
			//Logger.error("Joining to org: " + org.getOrganisation_name());
			ou.setOrg(nps.get(0).getOrganisation());
			ou.setStatus(OrgUser.UNVERIFIED);
			Set<ConstraintViolation<OrgUser>> constraintViolations =
				      validator.validate( ou );
			assertTrue(constraintViolations.isEmpty());			
			em.persist(ou);
			em.flush();

			boolean bFound = false;
			List<OrgUser> lou = org.findUsers();
			ListIterator<OrgUser> lit = lou.listIterator();
			while (lit.hasNext())
			{
				outmp = lit.next();
				if (outmp.getId().longValue()==ou.getId().longValue())
					bFound = true;
			}
			
			assertTrue(bFound);
			
			ou.delete();
			bFound = false;
			lou = org.findUsers();
			lit = lou.listIterator();
			while (lit.hasNext())
			{
				outmp = lit.next();
				if (outmp.getId().longValue()==ou.getId().longValue())
					bFound = true;
			}
			
			assertFalse(bFound);			
						
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}
	
	@Test
	public void test_joinAlreadyMember() {
		try {
			AppUser au = AppUser.findByEmail(email);
			List<NoiseProducer> nps = NoiseProducer.findAll();
			
			OrgUser ou = new OrgUser();
			ou.setAu(au);
			ou.setOrg(nps.get(0).getOrganisation());
			ou.setStatus(OrgUser.UNVERIFIED);
			Set<ConstraintViolation<OrgUser>> constraintViolations =
				      validator.validate( ou );
			assertTrue(constraintViolations.isEmpty());
			ou.save();
			em.flush();
			
			ou = new OrgUser();
			ou.setAu(au);
			ou.setOrg(nps.get(0).getOrganisation());
			ou.setStatus(OrgUser.UNVERIFIED);
			
			constraintViolations =
				      validator.validate( ou );
			assertTrue(constraintViolations.isEmpty());
			try {
				ou.save();
				em.flush();
				fail("Saved duplicate!");
			} catch (PersistenceException pe) {
				//This is expected...
				Logger.error("Caught expected exception: " + pe.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	/**
	 * 
	 */
	@Test
	public void test_joinMultiple() {
		try {
			AppUser au = AppUser.findByEmail(email);
			List<NoiseProducer> nps = NoiseProducer.findAll();
			
			OrgUser ou = new OrgUser();
			ou.setAu(au);
			ou.setOrg(nps.get(0).getOrganisation());
			ou.setStatus(OrgUser.UNVERIFIED);
			Set<ConstraintViolation<OrgUser>> constraintViolations =
				      validator.validate( ou );
			assertTrue(constraintViolations.isEmpty());
			ou.save();
			em.flush();
			
			ou = new OrgUser();
			ou.setAu(au);
			ou.setOrg(nps.get(1).getOrganisation());
			ou.setStatus(OrgUser.UNVERIFIED);
			
			constraintViolations =
				      validator.validate( ou );
			
			assertTrue(constraintViolations.isEmpty());
			ou.save();
			em.flush();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
