package utils;

import static org.junit.Assert.*;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.junit.Test;

import utils.PasswordHash;

public class PasswordHashTest {

	private static String password="p\r\nassw0Rd!";
	private static String wrongPassword = "p\r\nassw0Rd!2";
	
	@Test
	public void test_Hashing() {
		try {
			String hash = PasswordHash.createHash(password);
			String secondHash = PasswordHash.createHash(password);
			// Hashes must be different for the same password value
			assertFalse(hash.equalsIgnoreCase(secondHash));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void test_WrongPassword() {
		String hash;
		try {
			hash = PasswordHash.createHash(password);
			assertFalse(PasswordHash.validatePassword(wrongPassword, hash)); 
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}
	
	@Test 
	public void test_CorrectPassword() {
		String hash;
		try {
			hash = PasswordHash.createHash(password);
			assertTrue(PasswordHash.validatePassword(password, hash));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
        
	}
	
}
