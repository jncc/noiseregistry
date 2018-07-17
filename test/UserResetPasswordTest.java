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
import models.AppUserResetPassword;

public class UserResetPasswordTest {
	private static String password="Passw0Rd!";
	private static String email="user@company.com";
	private static String wrongEmail="user2@company.com";
	private static String phone="0123456789";
	private static String fullname="John Doe";
	
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
	public void test_resetPasswordWrongEmail() {
		try {
			AppUserResetPassword rp = new AppUserResetPassword();
			rp.setEmail(wrongEmail);
			String newPassword = rp.resetPassword();
			assertNull(newPassword);
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void test_resetPasswordCorrectEmail() {
		try {
			AppUserResetPassword rp = new AppUserResetPassword();
			rp.setEmail(email);
			String genPassword = rp.resetPassword();
			assertNotNull(genPassword);
			
			AppUser au = AppUser.authenticate(email, genPassword);
			assertNotNull(au);
			
			assertFalse(au.getPassword().equals(genPassword));
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
}
