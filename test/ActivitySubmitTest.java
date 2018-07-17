import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.text.SimpleDateFormat;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import models.ActivityApplication;
import models.AppUser;
import models.AppUserLogin;
import models.AppUserRegistration;
import models.NoiseProducer;
import models.Organisation;
import models.Regulator;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import controllers.routes;
import play.Logger;
import play.data.Form;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.test.FakeApplication;
import play.test.Helpers;
import scala.Option;
import static play.test.Helpers.fakeRequest;
import play.mvc.Http.HeaderNames;
import static play.test.Helpers.callAction;
import play.mvc.Result;
import static play.test.Helpers.status;
import static play.test.Helpers.header;

/**
 * Integreation tests for the ActivityApplication submission
 * 
 * @note The way the persistence manager works, these tests will leave data in the test database.
 */
public class ActivitySubmitTest {
	private static String password="Passw0Rd!";
	private static String email="user@company.com";
	private static String phone="0123456789";
	private static String fullname="John Doe";
	private static NoiseProducer np = null;
	private static Regulator reg = null;
	
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
		
		AppUser au = AppUser.findByEmail(email);
		if (au==null)
		{
			AppUserRegistration aur = new AppUserRegistration();
			aur.setEmail_address(email);
			aur.setPassword_entry(password);
			aur.setPassword_confirm(password);
			aur.setFullname(fullname);
			aur.setPhone(phone);
			
			aur.save();
			au = AppUser.findByEmail(email);
			AppUser.verify(au.getVerification_token());
		}
		
		boolean bHaveNP = false;
		List<NoiseProducer> lnp = NoiseProducer.findAll();
		ListIterator<NoiseProducer> it = lnp.listIterator();
		while (it.hasNext())
		{
			NoiseProducer npit = it.next();
			if (npit.getOrganisation().getOrganisation_name().compareTo("noiseproducer")==0)
			{
				bHaveNP = true;
				np = npit;
			}
		}
		if (!bHaveNP)
		{
			Organisation orgNp = new Organisation();
			orgNp.setOrganisation_name("noiseproducer");
			orgNp.setContact_name("np contact");
			orgNp.setContact_email("np@np.com");
			orgNp.setContact_phone("0123456789");		
			np = new NoiseProducer();
			np.setOrganisation(orgNp);
			np.save();
		}
		
		boolean bHaveReg = false;
		List<Regulator> lreg = Regulator.findAll();
		ListIterator<Regulator> itr = lreg.listIterator();
		while (itr.hasNext())
		{
			Regulator regit = itr.next();
			if (regit.getOrganisation().getOrganisation_name().compareTo("regulator")==0)
			{
				bHaveReg = true;
				reg = regit;
			}
		}
		if (!bHaveReg)
		{
			Organisation orgReg = new Organisation();
			orgReg.setOrganisation_name("regulator");
			orgReg.setContact_name("reg contact");
			orgReg.setContact_email("reg@reg.com");
			orgReg.setContact_phone("0123456789");		
			reg = new Regulator();
			reg.setOrganisation(orgReg);
			reg.setAccepts_email(false);
			reg.save();
		}		
		
		em.getTransaction().commit();
	}
	
	@AfterClass
	public static void tearDown()
	{
		if (em.getTransaction().isActive())
		{
			em.getTransaction().rollback();
		}
		em.getTransaction().begin();

		AppUser au = AppUser.findByEmail(email);
		em.remove(au);
				
		em.getTransaction().commit();
		
		JPA.bindForCurrentThread(null);
		if (em!=null)
			em.close();
	}	
	
	@Test
	public void test_submitApplication() {
		try {
            Map<String, String> userData = new HashMap<String, String>();
            userData.put("email", email);
            userData.put("password", password);

            Result r;
            //javax.naming.spi.DirStateFactory.Result r;
            r = callAction(routes.ref.LoginController.authenticate(), 
            		fakeRequest().withFormUrlEncodedBody(Form.form(AppUserLogin.class).bind(userData).data()));       
            
            String cookies = header(HeaderNames.SET_COOKIE, r);
            Logger.error("cookies="+cookies);
            
            Map<String, String> aaData = new HashMap<String, String>();
            r = callAction(routes.ref.ActivityApplicationController.save(), 
            		fakeRequest().withHeader(HeaderNames.COOKIE,cookies).withFormUrlEncodedBody(Form.form(ActivityApplication.class).bind(aaData).data()));       
            assertEquals(400, status(r));  // bad data should give a 400 error            
            
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");

            String formatted = format1.format(cal.getTime());
            aaData.put("date_start", formatted);
            cal.add(Calendar.DATE, 100);
            formatted = format1.format(cal.getTime());
            aaData.put("date_end", formatted);
            aaData.put("duration", "3");
            aaData.put("noiseproducer.id", np.getId().toString());
            aaData.put("regulator.id", reg.getId().toString());
            aaData.put("non_licensable", Boolean.toString(false));
            aaData.put("activitytype_id", "3" );
            aaData.put("activityPiling.max_hammer_energy", "123");
            aaData.put("location_entry_type", "manual");
            aaData.put("use_parent_location", "no");
            
            r = callAction(routes.ref.ActivityApplicationController.save(), 
            		fakeRequest().withHeader(HeaderNames.COOKIE,cookies).withFormUrlEncodedBody(Form.form(ActivityApplication.class).bind(aaData).data()));       
            
            assertEquals(303, status(r));
            //assertTrue(Helpers.header("Location", r).contains("/activity/confirmadd"));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}	
}
