import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import models.ActivityApplication;
import models.ActivityLocation;
import models.ActivityPiling;
import models.ActivityExplosives;
import models.AppUser;
import models.NoiseProducer;
import models.Organisation;
import models.Regulator;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.test.FakeApplication;
import play.test.Helpers;
import scala.Option;


/**
 * Unit tests for the ActivityApplication
 * 
 * @note persistence is tested in the ModelTest class so is not re-tested here.
 */
public class ActivityApplicationTest {
	
	private static NoiseProducer np;
	private static Regulator reg;
	
	private static double validLat = 49.1233;
	private static double validLng = -1.234;
	
	private static double validLat2 = 50.1233;
	private static double validLng2 = 1.234;
	
	private static AppUser au = null;

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
		
		np = new NoiseProducer();
		Organisation org = new Organisation();
		org.setOrganisation_name("test org");
		org.setContact_name("name");
		org.setContact_phone("1234");
		org.setContact_email("name@org.com");
		np.setOrganisation(org);
		np.save();

		reg = new Regulator();
		Organisation orgreg = new Organisation();
		orgreg.setOrganisation_name("test reg");
		orgreg.setContact_name("name");
		orgreg.setContact_phone("1234");
		orgreg.setContact_email("name@org.com");
		reg.setOrganisation(orgreg);
		reg.setCloseoutdays(180);
		reg.save();
		
		au = AppUser.findByEmail("user@company.com");
	}
	
	@AfterClass
	public static void tearDown()
	{
		em.getTransaction().rollback();
		JPA.bindForCurrentThread(null);
		if (em!=null)
			em.close();
	}	
	
	/**
	 * create coordinates for an invalid polygon
	 * @return String lat lng values
	 */
	private String getInvalidPolygon() {
		StringBuilder sb = new StringBuilder();
		sb.append(Double.toString(validLat));
		sb.append(" / ");
		sb.append(Double.toString(validLng));
		sb.append(", ");
		sb.append(Double.toString(validLat2));
		sb.append(" / ");
		sb.append(Double.toString(validLng2));
		sb.append(", ");
		sb.append(Double.toString(validLat2));
		sb.append(" / ");
		sb.append(Double.toString(validLng));
		sb.append(", ");
		sb.append(Double.toString(validLat));
		sb.append(" / ");
		sb.append(Double.toString(validLng2));
		
		return sb.toString();
	}
	/**
	 * create coordinates for a valid polygon
	 * @return String lat lng values
	 */
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
	public void test_setProposedActivityApplicationOrganisation() {
		try {
			ActivityApplication aa = new ActivityApplication();
			
			aa.setNoiseproducer(np);
			
			assertEquals(np, aa.getNoiseproducer());
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}	
	@Test
	public void test_setProposedActivityApplicationRegulator() {
		try {
			ActivityApplication aa = new ActivityApplication();
			
			aa.setRegulator(reg);
			
			assertEquals(reg, aa.getRegulator());
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	/*private ActivityApplication createApplicationActivity()
	{
		ActivityApplication aa = new ActivityApplication();
		Calendar cal = Calendar.getInstance();

		//set date values as they would be entered...
		aa.setDate_start_year(2014);
		aa.setDate_start_month(12);
		aa.setDate_start_day(1);
		
		//Remember that calendar takes months in the range 0-11
		cal.set(2014, 11, 02);
		aa.setDate_end(cal.getTime());
		
		cal.set(2014, 11, 05);
		aa.setDate_due(cal.getTime());
		
		ActivityPiling pil = new ActivityPiling();
		pil.setMax_hammer_energy(100);
		aa.setActivityPiling(pil);
		
		//aa.setActivity_type_id(3L);				
		
		aa.setDuration(5);
							
		aa.setDuration(2);

		return aa;
	}*/
	@Test
	public void test_setListApplication() 
	{
		try 
		{
			int iCount = 5;	
			
			ActivityApplication aa;
			for (int i = 0; i<iCount ; i++)
			{
				aa = getAA();
				aa.setNoiseproducer(np);			
				aa.setRegulator(reg);		
	
				aa.save(ActivityApplicationTest.au);
			}
			//liaa=ActivityApplication.findAll();
		}
		catch(Exception e)
		{
			fail(e.toString());
		}
		//assertEquals(iSize+iCount,liaa.size());
	}
	public static ActivityApplication getAA()
	{
		ActivityApplication aa = new ActivityApplication();
		Calendar cal = Calendar.getInstance();

		//set date values as they would be entered...
		aa.setDate_start_year(2014);
		aa.setDate_start_month(12);
		aa.setDate_start_day(1);
		
		//Remember that calendar takes months in the range 0-11
		cal.set(2014, 11, 02);
		aa.setDate_end(cal.getTime());
		
		cal.set(2014, 11, 05);
		aa.setDate_due(cal.getTime());
		
		aa.setDuration(5);
		
		ActivityPiling pil = new ActivityPiling();
		pil.setMax_hammer_energy(100);
		aa.setActivityPiling(pil);
				
		aa.setActivitytype_id(3L);
		
		aa.setNon_licensable(false);
		
		aa.setDuration(2);
		
		return aa;
	}
	@Test
	public void test_saveInvalidPolygonLocation() {
		
		
		ActivityApplication aa = getAA();
		aa.setNoiseproducer(np);			
		aa.setRegulator(reg);	

		//create a valid location entry...
		ActivityLocation al = new ActivityLocation();
		al.setCreation_type("Proposed");
		al.setPolygon_latlngs(getInvalidPolygon());
		
		List<ActivityLocation> activitylocations = new ArrayList<ActivityLocation>();
		activitylocations.add(al);
		aa.setActivitylocations(activitylocations);
		
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
	    
		Set<ConstraintViolation<ActivityApplication>> constraintViolations =
			      validator.validate( aa );
		assertTrue(constraintViolations.isEmpty());
		
		//Check that the custom validate method returns an error for the invalid polygon
		assertNotNull(aa.validate());
	}
	@Test
	public void test_saveValidPolygonLocation() {
		
		ActivityApplication aa = getAA();
		aa.setNoiseproducer(np);			
		aa.setRegulator(reg);	

		//create a valid location entry...
		ActivityLocation al = new ActivityLocation();
		al.setCreation_type("Proposed");
		al.setPolygon_latlngs(getValidPolygon());
		
		List<ActivityLocation> activitylocations = new ArrayList<ActivityLocation>();
		activitylocations.add(al);
		aa.setActivitylocations(activitylocations);
		
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
	    
		Set<ConstraintViolation<ActivityApplication>> constraintViolations =
			      validator.validate( aa );
		assertTrue(constraintViolations.isEmpty());
		
		//Check that the custom validate method returns an error for the invalid polygon
		assertNull(aa.validate());
	}
	@Test
	public void test_setCancelApplication() {
		try {
			ActivityApplication aa = getAA();
			aa.setNoiseproducer(np);			
			aa.setRegulator(reg);	

			aa.save(ActivityApplicationTest.au);
			em.flush();
			aa.cancel(ActivityApplicationTest.au);			
			
			assertEquals(aa.getStatus(), "Cancelled");
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	@Test
	public void test_saveDraftApplication() {
		try {	
			ActivityApplication aa = getAA();
			aa.setNoiseproducer(np);			
			aa.setRegulator(reg);	
			aa.setSaveasdraft("true");
			aa.save(ActivityApplicationTest.au);
			em.flush();
						
			assertEquals(aa.getStatus(), "Draft");
			
			aa.setNon_licensable(true);
			aa.setSaveasdraft("true");
			aa.update(aa.getId(),ActivityApplicationTest.au);
			
			assertEquals(aa.getStatus(), "Draft");
			
			aa.setSaveasdraft(null);
			aa.update(aa.getId(),ActivityApplicationTest.au);
			assertEquals(aa.getStatus(), "Proposed");
			
			//Finally, make sure that attempts to save a non-draft item fail with an exception
			try {
				aa.update(aa.getId(),ActivityApplicationTest.au);
				fail("Updated a Proposed item.");
			} catch (Exception e) {
				//This is expected!
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	@Test
	public void test_deleteDraftLocation() {
		try {	
			ActivityApplication aa = getAA();
			aa.setNoiseproducer(np);			
			aa.setRegulator(reg);	
			aa.setSaveasdraft("true");
			
			//create a valid location entry...
			ActivityLocation al = new ActivityLocation();
			al.setCreation_type("Proposed");
			al.setLat(validLat);
			al.setLng(validLng);
			
			ActivityLocation al2 = new ActivityLocation();
			al2.setCreation_type("Proposed");
			al2.setLat(validLat2);
			al2.setLng(validLng2);
			
			List<ActivityLocation> activitylocations = new ArrayList<ActivityLocation>();
			activitylocations.add(al);
			activitylocations.add(al2);
			aa.setActivitylocations(activitylocations);
						
			aa.save(ActivityApplicationTest.au);
			em.flush();
			assertEquals(2, aa.getActivitylocations().size());	
			assertEquals(aa.getStatus(), "Draft");
			
			//detach the object from the entity manager so that we have the same scenario
			//as when the framework binds an object from the request
			JPA.em().detach(aa);
			
			//remove the first location from the activity application
			aa.getActivitylocations().remove(0);
			
			aa.setSaveasdraft("true");
			aa.update(aa.getId(),ActivityApplicationTest.au);
			em.flush();
			
			assertEquals(aa.getStatus(), "Draft");
			assertEquals(1, aa.getActivitylocations().size());	
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	@Test
	public void test_linkedApplications() {
		try {
			ActivityApplication aa = getAA();
			aa.setNoiseproducer(np);			
			aa.setRegulator(reg);	
			aa.save(ActivityApplicationTest.au);
			em.flush();
			
			assertTrue(aa.getStatus()=="Proposed");
			
			ActivityApplication aa2 = aa.createLinked();
			assertNotNull(aa2);
			assertEquals(aa2.getParent(), aa);
			assertEquals(aa2.getNoiseproducer(), aa.getNoiseproducer());
			assertEquals(aa2.getRegulator(), aa.getRegulator());
			assertEquals(aa2.getDate_start(), aa.getDate_start());
			assertEquals(aa2.getDate_end(), aa.getDate_end());
			assertEquals(aa2.getDuration(), aa.getDuration());
			
			ActivityExplosives exp = new ActivityExplosives();
			exp.setTnt_equivalent(12.5);
			aa2.setActivityExplosives(exp);
					
			aa2.setActivitytype_id(4L);
			
			aa2.save(ActivityApplicationTest.au);
			em.flush();
			assertEquals(aa2.getDate_due(), aa.getDate_due());
			assertTrue(aa2.getStatus()=="Proposed");
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
