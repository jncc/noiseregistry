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

import play.db.jpa.*;
import play.test.FakeApplication;
import play.test.Helpers;
import scala.Option;
import models.AppUserRegistration;
import models.AppUserDetails;
import models.AppUser;

public class UserEditDetailsTest {
	private static Validator validator;
	
	private static String password="Passw0Rd!";
	private static String email="user@company.com";
	private static String phone="0123456789";
	private static String fullname="John Doe";
	private static String newPhone="9876543210";
	private static String newFullname="Jane Doe";
	
	
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
	
	@AfterClass
	public static void tearDown()
	{
		em.getTransaction().rollback();
		JPA.bindForCurrentThread(null);
		if (em!=null)
			em.close();
	}
	
	@Test 
	public void test_userDetails() {
		try {
			AppUserDetails aud = AppUser.findByEmail(email).toAppUserDetails();
			
			assertEquals(aud.getFullname(), fullname);
			assertEquals(aud.getPhone(), phone);

		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test 
	public void test_updateUserDetails() {
		try {
			AppUser au = AppUser.findByEmail(email);
			
			AppUserDetails aud = au.toAppUserDetails();
			aud.setFullname(newFullname);
			aud.setPhone(newPhone);
			aud.update(au.getId());
			
			au = AppUser.findByEmail(email);
			
			AppUserDetails aud2 = au.toAppUserDetails();
			assertEquals(aud2.getFullname(), newFullname);
			assertEquals(aud2.getPhone(), newPhone);
			aud2.setFullname(fullname);
			aud2.setPhone(phone);
			aud2.update(au.getId());
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void test_updateInvalidUserDetails() {
		try {
			AppUser au = AppUser.findByEmail(email);
			AppUserDetails aud = au.toAppUserDetails();
			aud.setFullname(null);
			aud.setPhone(null);
			
			Set<ConstraintViolation<AppUserDetails>> constraintViolations =
				      validator.validate( aud );
			
			assertNotNull(constraintViolations);
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
}
