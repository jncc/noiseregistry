import static org.junit.Assert.*;

import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import play.db.jpa.*;
import play.test.FakeApplication;
import play.test.Helpers;
import scala.Option;
import models.AppUserRegistration;
import models.AppUser;
import models.AppUserChangePassword;

public class UserChangePasswordTest {
	private static String password="Passw0Rd!";
	private static String newPassword = "Passw0Rd!2";
	private static String passwordWeak="password";
	private static String email="user@company.com";
	private static String phone="0123456789";
	private static String fullname="John Doe";
	
	@PersistenceContext
	private static EntityManager em;
	
	private static Validator validator;
	
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
		
		AppUser au = AppUser.findByEmail(email);
		AppUser.verify(au.getVerification_token());
		
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	    validator = factory.getValidator();
		
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
	public void test_changeToInvalidPassword() {
		AppUserChangePassword cp = new AppUserChangePassword();
		cp.setEmail_address(email);
		cp.setCurrent_password(password);
		cp.setPassword_entry(passwordWeak);
		cp.setPassword_confirm(passwordWeak);
		
		Set<ConstraintViolation<AppUserChangePassword>> constraintViolations =
			      validator.validate( cp );
		//Logger.error(constraintViolations.toString());
		assertFalse(constraintViolations.isEmpty());
		
		//assertNotNull(cp.validate());
		
	}
	
	@Test
	public void test_changeToUnmatchedPasswords() {
		try {
			AppUserChangePassword cp = new AppUserChangePassword();
			cp.setEmail_address(email);
			cp.setCurrent_password(password);
			cp.setPassword_entry(newPassword);
			cp.setPassword_confirm(password);
			cp.update();
			assertNotNull(cp.validate());
			//em.flush();
			
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
			
	@Test
	public void test_changeToValidPassword() {
		try {
			AppUserChangePassword cp = new AppUserChangePassword();
			cp.setEmail_address(email);
			cp.setCurrent_password(password);
			cp.setPassword_entry(newPassword);
			cp.setPassword_confirm(newPassword);
			
			assertNull(cp.validate());
			cp.update();
			
			AppUser au = AppUser.authenticate(email, newPassword);
			assertNotNull(au);
			
			//Change it back to the original value, so that other tests will work
			cp.setCurrent_password(newPassword);
			cp.setPassword_entry(password);
			cp.setPassword_confirm(password);
			
			assertNull(cp.validate());
			cp.update();
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void test_passwordEncypted() {
		try {
			try {
				AppUserChangePassword cp = new AppUserChangePassword();
				cp.setEmail_address(email);
				cp.setCurrent_password(password);
				cp.setPassword_entry(newPassword);
				cp.setPassword_confirm(newPassword);
				
				assertNull(cp.validate());
				cp.update();
				
				AppUser au = AppUser.authenticate(email, newPassword);
				assertNotNull(au);
				
				assertFalse(au.getPassword().equals(newPassword));
				
				//Change it back to the original value, so that other tests will work
				cp.setCurrent_password(newPassword);
				cp.setPassword_entry(password);
				cp.setPassword_confirm(password);
				
				assertNull(cp.validate());
				cp.update();
			} catch (Exception e) {
				fail(e.getMessage());
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
