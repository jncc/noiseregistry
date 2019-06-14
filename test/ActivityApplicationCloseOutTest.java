import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import models.ActivityApplication;
import models.ActivityApplicationCloseOut;
import models.ActivityLocation;
import models.ActivityLocationDate;
import models.ActivityPiling;
import models.AppUser;
import models.AppUserRegistration;
import models.NoiseProducer;
import models.Organisation;
import models.Regulator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.test.FakeApplication;
import play.test.Helpers;
import scala.Option;

public class ActivityApplicationCloseOutTest {

	private static String password="Passw0Rd!";
	private static String email="user@company.com";
	private static String phone="0123456789";
	private static String fullname="John Doe";
	
	private static String org_name="My Noise Producer";
	private static String org_contactname="Company Director";
	private static String org_contactemail="director@company.com";
	private static String org_contactphone="0123456789";
	
	private static String org_name2="My Regulator";
	private static String org_contactname2="Company Director2";
	private static String org_contactemail2="director2@company.com";
	private static String org_contactphone2="01234567892";
	
	private static int reg_closeoutdays = 120;
	
	private static NoiseProducer np;
	private static Regulator reg;
	
	private static double validLat = 49.1233;
	private static double validLng = -1.234;
	
	private static double invalidLat = 44.123;
	private static double invalidLng = 8.754;
	
	private static double validLat2 = 50.1233;
	private static double validLng2 = 1.234;
	
	//Valid lat/ lng 3 is used to define a point outside a polygon drawn
	//using lat lngs 1 and 2
	private static double validLat3 = 49.0233;
	private static double validLng3 = 1.234;
	
	private AppUser au = null;
	
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
		
		au = AppUser.findByEmail(email);
		AppUser.verify(au.getVerification_token());
		
		np = new NoiseProducer();
		Organisation org = new Organisation();
		org.setOrganisation_name(org_name);
		org.setContact_email(org_contactemail);
		org.setContact_name(org_contactname);
		org.setContact_phone(org_contactphone);
		np.setOrganisation(org);
		
		np.save(AppUser.findByEmail(email));
		
		reg = new Regulator();
		Organisation org2 = new Organisation();
		org2.setOrganisation_name(org_name2);
		org2.setContact_email(org_contactemail2);
		org2.setContact_name(org_contactname2);
		org2.setContact_phone(org_contactphone2);
		reg.setOrganisation(org2);
		reg.setCloseoutdays(reg_closeoutdays);
		
		reg.save();
		
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
	
	private ActivityApplication getValidAA() {
		ActivityApplication aa = new ActivityApplication();
		aa.setDate_start(new Date());
		aa.setDate_end(new Date(aa.getDate_start().getTime() + 86400000));
		aa.setDuration(1);
		aa.setNoiseproducer(np);
		aa.setRegulator(reg);
		aa.setActivitytype_id(3L);
		aa.setNon_licensable(false);
		aa.setActivityPiling(new ActivityPiling());
		aa.getActivityPiling().setMax_hammer_energy(123);
		
		return aa;
	}
	
	@Test
	public void test_ActivityApplicationCloseOutNoLocations()
	{
		try {
			ActivityApplication aa = getValidAA();
			
			Set<ConstraintViolation<ActivityApplication>> constraintViolations =
				      validator.validate( aa );
			
			//Logger.error(constraintViolations.toString());
			assertTrue(constraintViolations.isEmpty());
			
			aa.save(au);
			assertTrue(aa.getStatus().equals("Proposed"));
			//Logger.error("New application: " + Long.toString(aa.getId()));
			
			ActivityApplicationCloseOut aaco = new ActivityApplicationCloseOut();
			
			Map<String, String> m = new HashMap<String, String>();
			m.put("max_hammer_energy_actual", "7");
			m.put("sound_pressure_level_actual","8");
			m.put("sound_exposure_level_actual","9");
			
			ActivityApplication.closeOut(aaco, aa.getId(), false, m, false);
			
			assertTrue(aa.getStatus().equals("Closed"));
			assertNotNull(aa.getDate_closed());
			
			//Logger.error("Date closed: " + aa.getDate_closed());
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			fail (e.getMessage());
		}
	}
	private String getValidPolygon() {
		StringBuilder sb = new StringBuilder();
		sb.append(Double.toString(validLat));
		sb.append(" / ");
		sb.append(Double.toString(validLng));
		sb.append(", ");
		sb.append(Double.toString(validLat2));
		sb.append(" / ");
		sb.append(Double.toString(validLng));
		sb.append(", ");
		sb.append(Double.toString(validLat2));
		sb.append(" / ");
		sb.append(Double.toString(validLng2));
		sb.append(", ");
		sb.append(Double.toString(validLat));
		sb.append(" / ");
		sb.append(Double.toString(validLng2));
		
		return sb.toString();
	}
	@Test
	public void test_ActivityApplicationWithPolygonLocation()
	{
		try {
			ActivityApplication aa = getValidAA();
			
			//create a valid location entry...
			ActivityLocation al = new ActivityLocation();
			al.setCreation_type("Proposed");
			al.setPolygon_latlngs(getValidPolygon());
			
			List<ActivityLocation> activitylocations = new ArrayList<ActivityLocation>();
			activitylocations.add(al);
			aa.setActivitylocations(activitylocations);
			
			Set<ConstraintViolation<ActivityApplication>> constraintViolations =
				      validator.validate( aa );
			//Logger.error(constraintViolations.toString());
			assertTrue(constraintViolations.isEmpty());
			
			aa.save(au);
			
			assertTrue(aa.getStatus().equals("Proposed"));
			//Logger.error("New application: " + Long.toString(aa.getId()));
			
			ActivityApplicationCloseOut aaco = new ActivityApplicationCloseOut();
			aaco.setActivitylocations(aa.getActivitylocations());
			
			//Add activity at a point that falls within the polygon limits
			ActivityLocation al2 = new ActivityLocation();
			al2.setAa(aa);
			al2.setCreation_type("Additional");
			al2.setLat((validLat + validLat2)/2);
			al2.setLng((validLng + validLng2)/2);
			al2.setNo_activity(true);
			al2.update();
			assertTrue(al2.getCreation_type().equals("Proposed"));
			
			//Add activity at a point outside the polygon limits
			ActivityLocation al3 = new ActivityLocation();
			al3.setAa(aa);
			al3.setCreation_type("Additional");
			al3.setLat(validLat3);
			al3.setLng(validLng3);
			al3.update();
			assertTrue(al3.getCreation_type().equals("Additional"));
			
			//For this location, date(s) must be provided
			List<ActivityLocationDate> dts = new ArrayList<ActivityLocationDate>();
			ActivityLocationDate ald = new ActivityLocationDate();
			ald.setActivity_date(new Date(aa.getDate_end().getTime()));
			ald.setActivitylocation(al2);
			JPA.em().persist(ald);
			dts.add(ald);
			
			al3.setActivitydates(dts);
			al3.update();
			
			//Add the locations to the list
			aaco.getActivitylocations().add(al2);
			aaco.getActivitylocations().add(al3);
			
			Map<String, String> m = new HashMap<String, String>();
			m.put("max_hammer_energy_actual", "7");
			m.put("sound_pressure_level_actual","8");
			m.put("sound_exposure_level_actual","9");
			
			ActivityApplication.closeOut(aaco, aa.getId(), false, m, false);
			
			//Check that the constraint violations are all ok
			constraintViolations =
				      validator.validate( aa );
			
			assertTrue(constraintViolations.isEmpty());
			
			//Check that the custom validate method returns no errors
			assertNull(aa.validate());
			
			assertTrue(aa.getStatus().equals("Closed"));
			assertNotNull(aa.getDate_closed());
			assertNotNull(aa.getActivitylocations());
			assertEquals(3, aa.getActivitylocations().size());

		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			fail (e.getMessage());
		}
	}
	
	@Test
	public void test_ActivityApplicationCloseOutWithLocationsInvalidDate()
	{
		try {
			ActivityApplication aa = getValidAA();
			
			//create a valid location entry...
			ActivityLocation al = new ActivityLocation();
			al.setCreation_type("Proposed");
			al.setLat(validLat);
			al.setLng(validLng);
			
			List<ActivityLocation> activitylocations = new ArrayList<ActivityLocation>();
			activitylocations.add(al);
			aa.setActivitylocations(activitylocations);
			
			Set<ConstraintViolation<ActivityApplication>> constraintViolations =
				      validator.validate( aa );
			//Logger.error(constraintViolations.toString());
			assertTrue(constraintViolations.isEmpty());
			
			aa.save(au);
			
			assertTrue(aa.getStatus().equals("Proposed"));
			//Logger.error("New application: " + Long.toString(aa.getId()));
			
			ActivityApplicationCloseOut aaco = new ActivityApplicationCloseOut();
			aaco.setActivitylocations(aa.getActivitylocations());
			
			ActivityLocation al2 = new ActivityLocation();
			al2.setCreation_type("Additional");
			al2.setLat(validLat);
			al2.setLng(validLng);
			al2.update();
			
			List<ActivityLocationDate> dts = new ArrayList<ActivityLocationDate>();
			ActivityLocationDate ald = new ActivityLocationDate();
			ald.setActivity_date(new Date(aa.getDate_end().getTime() + 86400000));
			ald.setActivitylocation(al2);
			JPA.em().persist(ald);
			dts.add(ald);
			
			al2.setActivitydates(dts);
			
			aaco.getActivitylocations().add(al2);
			aaco.getActivitylocations().get(0).setNo_activity(true);
			
			Map<String, String> m = new HashMap<String, String>();
			m.put("max_hammer_energy_actual", "7");
			m.put("sound_pressure_level_actual","8");
			m.put("sound_exposure_level_actual","9");
			
			ActivityApplication.closeOut(aaco, aa.getId(), false, m, false);
			
			//Check that the constraint violations are all ok
			constraintViolations =
				      validator.validate( aa );
			
			assertTrue(constraintViolations.isEmpty());
			
			//Check that the custom validate method returns errors...
			assertNotNull(aa.validate());
			
			assertTrue(aa.getStatus().equals("Closed"));
			assertNotNull(aa.getDate_closed());
			assertNotNull(aa.getActivitylocations());
			assertEquals(2, aa.getActivitylocations().size());

		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			fail (e.getMessage());
		}
	}
	@Test
	public void test_ActivityApplicationCloseOutWithLocationsValidDate()
	{
		try {
			ActivityApplication aa = getValidAA();
			
			//create a valid location entry...
			ActivityLocation al = new ActivityLocation();
			al.setCreation_type("Proposed");
			al.setLat(validLat);
			al.setLng(validLng);
			
			List<ActivityLocation> activitylocations = new ArrayList<ActivityLocation>();
			activitylocations.add(al);
			aa.setActivitylocations(activitylocations);
			
			Set<ConstraintViolation<ActivityApplication>> constraintViolations =
				      validator.validate( aa );
			//Logger.error(constraintViolations.toString());
			assertTrue(constraintViolations.isEmpty());
			
			aa.save(au);
			
			assertTrue(aa.getStatus().equals("Proposed"));
			//Logger.error("New application: " + Long.toString(aa.getId()));

			//Mark the first location as no activity...
			aa.getActivitylocations().get(0).setNo_activity(true);
			ActivityApplicationCloseOut aaco = new ActivityApplicationCloseOut();
			aaco.setActivitylocations(aa.getActivitylocations());
			
			//create a second activity location that we will record a date for...
			ActivityLocation al2 = new ActivityLocation();
			al2.setCreation_type("Additional");
			al2.setAa(aa);
			al2.setLat(validLat);
			al2.setLng(validLng);
			al2.update();
			
			List<ActivityLocationDate> dts = new ArrayList<ActivityLocationDate>();
			ActivityLocationDate ald = new ActivityLocationDate();
			ald.setActivity_date(new Date(aa.getDate_start().getTime() + 60000));
			ald.setActivitylocation(al2);
			JPA.em().persist(ald);
			dts.add(ald);
			
			al2.setActivitydates(dts);
			
			aaco.getActivitylocations().add(al2);
			
			Map<String, String> m = new HashMap<String, String>();
			m.put("max_hammer_energy_actual", "7");
			m.put("sound_pressure_level_actual","8");
			m.put("sound_exposure_level_actual","9");
			
			ActivityApplication.closeOut(aaco, aa.getId(), false, m, false);
			
			//Check that the constraint violations are all ok
			constraintViolations =
				      validator.validate( aa );
			
			//Logger.error(constraintViolations.toString());
			//Logger.error(Json.toJson(aa).toString());
			assertTrue(constraintViolations.isEmpty());
			
			//Check that the custom validate method returns no errors...
			assertNull(aa.validate());
			
			assertTrue(aa.getStatus().equals("Closed"));
			assertNotNull(aa.getDate_closed());
			assertNotNull(aa.getActivitylocations());
			assertEquals(2, aa.getActivitylocations().size());
			//Logger.error("First location: " + aa.getActivitylocations().get(0).getDescription());
			//Logger.error("Date closed: " + aa.getDate_closed());
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			fail (e.getMessage());
		}
	}
	
	@Test
	public void test_ActivityApplicationInterimCloseOutWithLocationsValidDate()
	{
		try {
			ActivityApplication aa = getValidAA();
			
			//create a valid location entry...
			ActivityLocation al = new ActivityLocation();
			al.setCreation_type("Proposed");
			al.setLat(validLat);
			al.setLng(validLng);
			
			List<ActivityLocation> activitylocations = new ArrayList<ActivityLocation>();
			activitylocations.add(al);
			aa.setActivitylocations(activitylocations);
			
			Set<ConstraintViolation<ActivityApplication>> constraintViolations =
				      validator.validate( aa );
			//Logger.error(constraintViolations.toString());
			assertTrue(constraintViolations.isEmpty());
			
			aa.save(au);
			
			assertTrue(aa.getStatus().equals("Proposed"));
			//Logger.error("New application: " + Long.toString(aa.getId()));

			//Mark the first location as no activity...
			aa.getActivitylocations().get(0).setNo_activity(true);
			ActivityApplicationCloseOut aaco = new ActivityApplicationCloseOut();
			aaco.setActivitylocations(aa.getActivitylocations());
			
			//create a second activity location that we will record a date for...
			ActivityLocation al2 = new ActivityLocation();
			al2.setCreation_type("Additional");
			al2.setAa(aa);
			al2.setLat(validLat);
			al2.setLng(validLng);
			al2.update();
			
			List<ActivityLocationDate> dts = new ArrayList<ActivityLocationDate>();
			ActivityLocationDate ald = new ActivityLocationDate();
			ald.setActivity_date(new Date(aa.getDate_start().getTime() + 60000));
			ald.setActivitylocation(al2);
			JPA.em().persist(ald);
			dts.add(ald);
			
			al2.setActivitydates(dts);
			
			aaco.getActivitylocations().add(al2);
			
			Map<String, String> m = new HashMap<String, String>();
			m.put("max_hammer_energy_actual", "7");
			m.put("sound_pressure_level_actual","8");
			m.put("sound_exposure_level_actual","9");
			
			ActivityApplication.closeOut(aaco, aa.getId(), true, m, false);
			
			//Check that the constraint violations are all ok
			constraintViolations =
				      validator.validate( aa );
			
			//Logger.error(constraintViolations.toString());
			//Logger.error(Json.toJson(aa).toString());
			assertTrue(constraintViolations.isEmpty());
			
			//Check that the custom validate method returns no errors...
			assertNull(aa.validate());
			
			assertTrue(aa.getStatus().equals("Interim Close-out"));
			assertNull(aa.getDate_closed());
			assertNotNull(aa.getActivitylocations());
			assertEquals(2, aa.getActivitylocations().size());
			
			//Save again as closed this time...
			ActivityApplication.closeOut(aaco, aa.getId(), false, m, false);
			
			assertNull(aa.validate());
			
			assertTrue(aa.getStatus().equals("Closed"));
			assertNotNull(aa.getDate_closed());
			assertNotNull(aa.getActivitylocations());
			assertEquals(2, aa.getActivitylocations().size());
			
			try {
				//Try to save again as closed...
				ActivityApplication.closeOut(aaco, aa.getId(), false, m, false);
				fail("Close of an already closed application - this should not be allowed");
			} catch (Exception e) {
				//
			}
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			fail (e.getMessage());
		}
	}
	
	@Test
	public void test_ActivityApplicationCloseOutWithInvalidLocations()
	{
		try {
			ActivityApplication aa = getValidAA();
			
			aa.save(au);
			assertTrue(aa.getStatus().equals("Proposed"));
			//Logger.error("New application: " + Long.toString(aa.getId()));
			
			//Create the invalid location entry
			ActivityLocation al = new ActivityLocation();
			al.setCreation_type("Additional");
			al.setLat(invalidLat);
			al.setLng(invalidLng);
			al.setNo_activity(false);
			
			ActivityApplicationCloseOut aaco = new ActivityApplicationCloseOut();
			List<ActivityLocation> activitylocations = new ArrayList<ActivityLocation>();;
			activitylocations.add(al);
			aaco.setActivitylocations(activitylocations);
			
			Set<ConstraintViolation<ActivityApplicationCloseOut>> constraintViolations =
				      validator.validate( aaco );
			//Logger.error(constraintViolations.toString());
			assertFalse(constraintViolations.isEmpty());
			//If the location entry is not valid, it should not be persisted, so the 
			//end result should be a closed activity application with an null list of locations
			Map<String, String> m = new HashMap<String, String>();
			m.put("max_hammer_energy_actual", "7");
			m.put("sound_pressure_level_actual","8");
			m.put("sound_exposure_level_actual","9");
			
			ActivityApplication.closeOut(aaco, aa.getId(), false, m, false);
			assertTrue(aa.getStatus().equals("Closed"));
			assertNotNull(aa.getDate_closed());
			assertNull(aa.getActivitylocations());
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			fail (e.getMessage());
		}
		
	}
}
