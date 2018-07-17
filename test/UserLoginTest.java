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

public class UserLoginTest {
	private static String password="Passw0Rd!";
	private static String wrongPassword = "Passw0Rd!2";
	private static String email="user@company.com";
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
	public void test_successfulLogin() {
		try {
			assertNotNull(AppUser.authenticate(email, password));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void test_unsuccessfulLogin() {
		try {
			assertNull(AppUser.authenticate(email,  wrongPassword));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void test_registrationDateStored() {
		try {
			AppUser au = AppUser.authenticate(email, password);
			assertNotNull(au);
			assertNotNull(au.getDate_registered());
		} catch(Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void test_passwordEncypted() {
		try {
			AppUser au = AppUser.authenticate(email, password);
			assertNotNull(au);
			assertNotNull(au.getPassword());
			assertFalse(au.getPassword().equals(password));
			
		} catch(Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
