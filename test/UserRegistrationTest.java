import static org.junit.Assert.*;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import models.AppUserRegistration;

import org.junit.Before;
import org.junit.Test;


public class UserRegistrationTest {
	private static String password="Passw0Rd!";
	private static String wrongPassword = "Passw0Rd!2";
	private static String passwordWeak="password";
	private static String email="user@company.com";
	private static String phone="0123456789";
	private static String fullname="John Doe";
	
	private static Validator validator;
	
	@Before
	public void setup()
	{
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	    validator = factory.getValidator();
	}
	
	@Test
	public void test_unmatchedPassword() {
		try {
			AppUserRegistration au = new AppUserRegistration();
			au.setEmail_address(email);
			au.setPassword_entry(password);
			au.setPassword_confirm(wrongPassword);
			au.setPhone(phone);
			au.setFullname(fullname);
			//With non matching passwords, validate must fail
			assertNotNull(au.validate());
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}	
	}
	
	@Test
	public void test_weakPassword() {
		try {
			AppUserRegistration au = new AppUserRegistration();
			au.setEmail_address(email);
			au.setPassword_entry(passwordWeak);
			au.setPassword_confirm(passwordWeak);
			au.setPhone(phone);
			au.setFullname(fullname);
			//With matching weak passwords, validation must fail...
			
			Set<ConstraintViolation<AppUserRegistration>> constraintViolations =
				      validator.validate( au );
			//Logger.error(constraintViolations.toString());
			assertFalse(constraintViolations.isEmpty());
			//assertNotNull(au.validate());
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}	
	}
	
	@Test
	public void test_validPassword() {
		try {
			AppUserRegistration au = new AppUserRegistration();
			au.setEmail_address(email);
			au.setPassword_entry(password);
			au.setPassword_confirm(password);
			au.setPhone(phone);
			au.setFullname(fullname);
			//With matching weak passwords, validation must fail...
			assertNull(au.validate());
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}	
	}
}
