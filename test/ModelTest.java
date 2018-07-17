import java.util.Calendar;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import play.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import models.ActivityAcousticDD;
import models.ActivityApplication;
import models.ActivityExplosives;
import models.ActivitySubBottomProfilers;
import models.ActivityMultibeamES;
import models.ActivityPiling;
import models.ActivitySeismic;
import models.ActivityType;
import models.AppUser;
import models.NoiseProducer;
import models.OilAndGasBlock;
import models.OrgUser;
import models.Organisation;
import models.ActivityLocation;
import models.Regulator;
import static org.fest.assertions.Assertions.*;
import static org.junit.Assert.fail;
import play.test.FakeApplication;
import play.test.Helpers;
import scala.Option;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ModelTest {
	private static EntityManager em;
	private static NoiseProducer np = null;
	private static Regulator reg = null;
	private static Organisation org = null;
	private static ActivityType at = null;
	private static ActivityLocation al = null;
	private static ActivityApplication aa = null;
	private static OilAndGasBlock oagb = null;
	//private static Liquibase liquibase = null;

	@BeforeClass
	public static void setUp()
	{
		final FakeApplication app = Helpers.fakeApplication();
		Helpers.start(app);
		Option<JPAPlugin> jpaPlugin = app.getWrappedApplication().plugin(JPAPlugin.class);
		em = jpaPlugin.get().em("default");
		JPA.bindForCurrentThread(em);

		em.getTransaction().begin();
		
		try
		{
		}
		catch (Exception e)
		{
			fail(e.toString());
		}
	}
	@AfterClass
	public static void tearDown()
	{
		try
		{
			//liquibase.rollback("teststart",(Contexts)null);
		}
		catch (Exception e) {}
		em.getTransaction().rollback();
		JPA.bindForCurrentThread(null);
		if (em!=null)
			em.close();
	}
	public void createAndRetrieveUser(EntityManager em, Organisation org) {

		try {
			AppUser au = new AppUser();
			au.setEmail_address("email");
			au.setFullname("username");
			au.setPhone("01234");
			OrgUser ou = new OrgUser();
			ou.setOrg(org);
			ou.setAu(au);
			
			em.persist(au);
		
			Query q = em.createQuery("Select count(id) from AppUser where email_address = 'email'");
			long count = (long)q.getSingleResult();
			assertThat(count==1);
			Logger.error("User count = "+count);
			
			au = new AppUser();
			au.setEmail_address("email2");
			au.setFullname("username2");
			au.setPhone("0123456678");
			ou = new OrgUser();
			ou.setOrg(org);
			ou.setAu(au);
			
			em.persist(au);
			
			q = em.createQuery("Select count(id) from AppUser");
			count = (long)q.getSingleResult();
			assertThat(count==2);
			Logger.error("User count2 = "+count);
		
		} catch(Exception e) {
			fail(e.toString());
		}
	}
	@Test
	public void test01_createActivityType() {
		try {
			// start with nothing in the table
			Query q = em.createQuery("Select count(id) from ActivityType");
			long count = (long)q.getSingleResult();
			assertThat(count==0);

			// Add some data to the table
			at = new ActivityType();
			at.setName("test activity");
			
			em.persist(at);
			em.flush();

			// check that we have one entry
			q = em.createQuery("Select count(id) from ActivityType");
			count = (long)q.getSingleResult();
			assertThat(count==1);
			
			// add another entry
			at = new ActivityType();
			at.setName("test activity2");
			
			em.persist(at);
			em.flush();			

			// check that we have the new entry
			q = em.createQuery("Select count(id) from ActivityType");
			count = (long)q.getSingleResult();
			assertThat(count==2);

			Logger.info(Long.toString(at.getId()));
		} catch(Exception e) {
			e.printStackTrace();
			fail(e.toString());
		} 
	}

	@Test
	public void test02_createOilAndGasBlock() {
		try {
			// start with nothing in the table
			Query q = em.createQuery("Select count(id) from OilAndGasBlock");
			long count = (long)q.getSingleResult();
			assertThat(count==0);

			// Add some data to the table
			oagb = new OilAndGasBlock();
			oagb.setBlock_code("999/99");
			oagb.setLessthan_five(false);
			oagb.setTw_code("C");
			oagb.setSplit_block(true);
			oagb.setQuadrant("999");
			oagb.setPoint_req(false);
			oagb.setAssignment_block_code("111/11");
		
			em.persist(oagb);
			em.flush();

			// check that we have one entry
			q = em.createQuery("Select count(id) from OilAndGasBlock");
			count = (long)q.getSingleResult();
			assertThat(count==1);
			
			// add another entry
			oagb = new OilAndGasBlock();
			oagb.setBlock_code("888/88");
			oagb.setLessthan_five(false);
			oagb.setTw_code("T");
			oagb.setSplit_block(true);
			oagb.setQuadrant("888");
			oagb.setPoint_req(false);
			oagb.setAssignment_block_code("111/11");
			
			em.persist(oagb);
			em.flush();			

			// check that we have the new entry
			q = em.createQuery("Select count(id) from OilAndGasBlock");
			count = (long)q.getSingleResult();
			assertThat(count==2);
			
			//createProposedActivity();
		
		} catch(Exception e) {
			e.printStackTrace();
			fail(e.toString());
		} 
	}
	
	@Test
	public void test03_createAndRetrieveOrganisation() {
		try {
			// start with nothing in the table
			Query q = em.createQuery("Select count(id) from Organisation");
			long count = (long)q.getSingleResult();
			assertThat(count==0);

			np = new NoiseProducer();
			org = new Organisation();
			org.setOrganisation_name("OrgName");
			org.setContact_email("testemail@company.com");
			org.setContact_name("contact name");
			org.setContact_phone("01234");
			em.persist(org);
			np.setOrganisation(org);
			em.persist(np);
			em.flush();
					
			assertThat(org.getId()!=0 && org.getId()!=null);
			
			q = em.createQuery("Select count(id) from Organisation where contact_email = 'testemail'");
			count = (long)q.getSingleResult();
			assertThat(count==1);
			
			createAndRetrieveUser(em, org);
			
			reg = new Regulator();
			Organisation orgReg = new Organisation();
			orgReg.setOrganisation_name("OrgName");
			orgReg.setContact_email("testemail@company.com");
			orgReg.setContact_name("contact name");
			orgReg.setContact_phone("01234");
			em.persist(orgReg);
			reg.setOrganisation(orgReg);
			em.persist(reg);
			em.flush();
		
		} catch(Exception e) {
			fail(e.toString());
		} 
	}
	@Test
	public void test04_createProposedActivity() {
		try {
			//createAndRetrieveOrganisation();
			
			// start with nothing in the table
			Query q = em.createQuery("select count(id) from ActivityLocation");
			long count = (long)q.getSingleResult();
			assertThat(count==0);
			
			assertThat(at!=null);

			Logger.info("at = "+Long.toString(at.getId()));
			Logger.info("org = "+org.getId().toString());
			
			assertThat(at.getId()>0);

			ActivityApplication aa = new ActivityApplication();

			aa.setNoiseproducer(np);
			aa.setRegulator(reg);
			aa.setNon_licensable(false);
			aa.setActivitytype_id(at.getId());
			aa.setStatus("Proposed");
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

			aa.setDuration(20);
			em.persist(aa);
			em.flush();			
			
			// Add some data to the table
			al = new ActivityLocation();
			al.setCreation_type("Proposed");
			//al.setOagb(oagb);
			al.setAa(aa);
			
			em.persist(al);
			em.flush();
			
			// check that we have one entry
			q = em.createQuery("Select count(id) from ActivityLocation");
			
			count = (long)q.getSingleResult();
			assertThat(count==1);
			
		} catch(Exception e) {
			e.printStackTrace();
			fail(e.toString());
		} 
	}
	@Test
	public void test04_Seismic()
	{
		// start with nothing in the table
		Query q = em.createQuery("Select count(id) from ActivitySeismic");
		long count = (long)q.getSingleResult();
		assertThat(count==0);

		ActivitySeismic as = new ActivitySeismic(); 
		as.setAa(aa);
		as.setSurvey_type("survey");
		as.setData_type("dt");
		as.setMax_airgun_volume(20);
		as.setOther_survey_type("other");
		as.setSound_exposure_level(10);
		as.setSound_pressure_level(15);
		em.persist(as);
		em.flush();

		q = em.createQuery("Select count(id) from ActivitySeismic");
		count = (long)q.getSingleResult();
		assertThat(count==1);
	}
	
	@Test
	public void test05_SubBottomProfilers()
	{
		// start with nothing in the table
		Query q = em.createQuery("Select count(id) from ActivitySubBottomProfilers");
		long count = (long)q.getSingleResult();
		assertThat(count==0);

		ActivitySubBottomProfilers ag = new ActivitySubBottomProfilers();
		ag.setAa(aa);
		ag.setFrequency(20);
		ag.setSound_exposure_level(25);
		ag.setSound_pressure_level(250);
		ag.setSource("source");
		em.persist(ag);
		em.flush();

		q = em.createQuery("Select count(id) from ActivitySubBottomProfilers");
		count = (long)q.getSingleResult();
		assertThat(count==1);
	}
	
	@Test
	public void test06_Piling()
	{
		// start with nothing in the table
		Query q = em.createQuery("Select count(id) from ActivityPiling");
		long count = (long)q.getSingleResult();
		assertThat(count==0);

		ActivityPiling ap = new ActivityPiling();
		ap.setAa(aa);
		ap.setMax_hammer_energy(2000);
		em.persist(ap);
		em.flush();

		q = em.createQuery("Select count(id) from ActivityPiling");
		count = (long)q.getSingleResult();
		assertThat(count==1);
	}
	
	@Test
	public void test07_Explosives()
	{
		// start with nothing in the table
		Query q = em.createQuery("Select count(id) from ActivityExplosives");
		long count = (long)q.getSingleResult();
		assertThat(count==0);
		
		ActivityExplosives ae = new ActivityExplosives();
		ae.setAa(aa);
		ae.setTnt_equivalent(400.0);
		em.persist(ae);
		em.flush();

		q = em.createQuery("Select count(id) from ActivityExplosives");
		count = (long)q.getSingleResult();
		assertThat(count==1);
	}
	
	@Test
	public void test08_Acoustic()
	{
		// start with nothing in the table
		Query q = em.createQuery("Select count(id) from ActivityAcousticDD");
		long count = (long)q.getSingleResult();
		assertThat(count==0);

		ActivityAcousticDD aadd = new ActivityAcousticDD();
		aadd.setAa(aa);
		aadd.setFrequency(50);
		aadd.setSound_exposure_level(52);
		aadd.setSound_pressure_level(56);
		em.persist(aadd);
		em.flush();		

		q = em.createQuery("Select count(id) from ActivityAcousticDD");
		count = (long)q.getSingleResult();
		assertThat(count==1);
	}
	@Test
	public void test09_Multibeam()
	{
		// start with nothing in the table
		Query q = em.createQuery("Select count(id) from ActivityMultibeamES");
		long count = (long)q.getSingleResult();
		assertThat(count==0);

		ActivityMultibeamES am = new ActivityMultibeamES();
		am.setAa(aa);
		am.setFrequency(596);
		am.setSound_exposure_level(300);
		am.setSound_exposure_level(395);
		em.persist(am);
		em.flush();		

		q = em.createQuery("Select count(id) from ActivityMultibeamES");
		count = (long)q.getSingleResult();
		assertThat(count==1);
	}
}
