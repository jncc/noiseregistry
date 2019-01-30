package controllers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

import javax.mail.*;
import javax.mail.internet.*;

import models.ActivityApplication;
import models.AppUser;
import models.NoiseProducer;
import models.OrgUser;
import models.Organisation;
import models.Regulator;
import play.Logger;
import play.api.PlayException;
import play.data.Form;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.*;
import play.twirl.api.Html;
import play.data.DynamicForm;
import utils.AppConfigSettings;
import utils.MailSettings;
import views.html.*;
import utils.AppConfigSettings;

@Security.Authenticated(SecuredController.class)
public class OrganisationController extends Controller {

	static Form<Organisation> appForm = Form.form(Organisation.class);
	static Form<Regulator> frmReg = Form.form(Regulator.class);
	static Form<OrgUser> joinForm = Form.form(OrgUser.class);
	
	/**
	 * Shows the UI allowing creation of a new organisation
	 * @return page for new organisation
	 */
	public static Result add() 
	{
		//should never happen - only add NoiseProducers or Regulators
		// TODO: Why is this even here then?
		String activeTab="HOME";
		return status(403,index.render(AppUser.findByEmail(session("email")), activeTab));
	}

	/**
	 * Allows editing of an organisation
	 * @param id the organisation to be edited
	 * @return the page containing the data to be edited
	 */
	@Transactional(readOnly=true)
	public static Result edit(String id) 
	{
		Organisation org = JPA.em().find(Organisation.class, Long.parseLong(id));
		if (org != null && userHasAdminAccessToOrganisation(org.getId().longValue()))
		{
			if (org.isRegulator()) 
			{
				Regulator reg = org.getRegulator();
				return RegulatorController.edit(reg.getId().toString());
			}
			NoiseProducer np = org.getNoiseProducer();
			return NoiseproducerController.edit(np.getId().toString());
		}
		
		return unauthorized(views.html.errors.unauthorised.render(AppUser.findByEmail(session("email")), "HOME"));
	}
	
	/**
	 * UI showing the list of organisations to which the user is the administrator
	 * @return page showing the organisations
	 */
	@Transactional(readOnly=true)
	public static Result adminorgs()
	{
		AppUser au = AppUser.findByEmail(session("email"));

		return ok(adminorganisations.render(au, Organisation.getMyAdminOrganisations(au)));
	}
	
	/**
	 * Shows the UI for organisation user
	 * @param id the organisation user id
	 * @return page allowing the user to be edited
	 */
	@Transactional(readOnly=true)
	public static Result getuser(String id) 
	{
		OrgUser ou = Organisation.findUser(Long.parseLong(id));

		if (ou != null && userHasAdminAccessToOrganisation(ou.getOrg().getId()))
		{
			Form<OrgUser> f = Form.form(OrgUser.class).fill(ou);
			return ok(organisationuser.render(AppUser.findByEmail(session("email")), ou , ou.getOrg().getId(), f));
		}
		
		return unauthorized(views.html.errors.unauthorised.render(AppUser.findByEmail(session("email")), "HOME")); // user must have admin rights to organisation
	}
	
	/**
	 * Checks whether the user is an administrator of an organisation
	 * @param lid the id of the organisation
	 * @return
	 */
	@Transactional(readOnly=true)
	public static boolean userHasAdminAccessToOrganisation(long lid)
	{
		AppUser au = AppUser.findByEmail(session("email"));

		List<Organisation> liorg = Organisation.getMyAdminOrganisations(au);
		ListIterator<Organisation> it = liorg.listIterator();
		while (it.hasNext())
		{
			Organisation org = it.next();
			if (org.getId()==lid)
				return true;
		}
		return false;
	}
	
	/**
	 * Shows the UI for the organisation details
	 * @param id the id of the organisation
	 * @return page showing the organisation
	 */
	@Transactional(readOnly=true)
	public static Result read(String id)
	{
		long lid = Long.parseLong(id);
		
		if (userHasAdminAccessToOrganisation(lid))
		{
			Organisation org = JPA.em().find(Organisation.class, lid);
			if (org.isRegulator()) 
			{
				Regulator reg = org.getRegulator();
				return ok(organisationregread.render(AppUser.findByEmail(session("email")), reg));
			} 
			else 
			{
				NoiseProducer np = org.getNoiseProducer();
				List<ActivityApplication> aas = ActivityApplication.findIncompleteByNoiseProducer(np);	
				return ok(organisationnpread.render(AppUser.findByEmail(session("email")), org, aas));
			}
		}

		return unauthorized(views.html.errors.unauthorised.render(AppUser.findByEmail(session("email")), "HOME")); // user must have admin rights to organisation
	}
	
	/**
	 * Page showing list of noise producers
	 * @return Page showing list of noise producers
	 */
	@Transactional(readOnly=true)
	public static Result list()
	{
		AppUser au = AppUser.findByEmail(session("email"));
		List<NoiseProducer> nps = new ArrayList<NoiseProducer>();
		
		if (!au.isRegulatorMember()) {
			nps = NoiseProducer.findAll();
		}
		
		return ok(organisationselect.render(au, nps));
	}
	
	/**
	 * UI allowing the user to request join access
	 * @param orgId The organisation which the user is attempting to join
	 * @return page showing the request
	 */
	@Transactional(readOnly=true)
	public static Result join(Long orgId) 
	{
		AppUser au = AppUser.findByEmail(session("email"));
		Organisation org = JPA.em().find(Organisation.class, orgId);
		return ok(organisationjoin.render(au, org));
	}
	
	private static  Result addUserToDB(Long orgId, boolean bDirect)
	{
		String activeTab="HOME";

		Organisation org = JPA.em().find(Organisation.class, orgId);
		AppUser au = AppUser.findByEmail(session("email"));

		try {
			org.addUser(au);
			JPA.em().flush();
			
			sendAdminJoinNotification(org, au);
			
		} catch (Exception e) {
			//User may already be a member of the selected organisation?
			return badRequest(finished.render(au, "organisation.joinrequest.title", "organisation.joinrequest.errormessage", activeTab, routes.RegistrationController.read()));
		}
		String sMessage = "organisation.joinrequest.confirmmessage";
		if (!bDirect)
			sMessage = "organisation.joinrequestsamename.confirmmessage";
		return ok(finished.render(au, "organisation.joinrequest.title", sMessage, activeTab, routes.RegistrationController.read()));					
	}
	
	/**
	 * Request to join organisation
	 * @param orgId the id of the organisation to add the user to
	 * @return confirmation of the request
	 */
	@Transactional
	public static Result addUser(Long orgId)
	{
		return addUserToDB(orgId, true);
	}
	
	public static Result addUserToOrgSameName(Long orgId)
	{
		return addUserToDB(orgId, false);
	}
	
	/** 
	 * Saves the organisation details
	 * @return confirmation
	 */
	@Transactional
	public static Result save() 
	{
		//should never happen - only save NoiseProducers or Regulators
		String activeTab="HOME";
		return status(403,index.render(AppUser.findByEmail(session("email")), activeTab));
    }
	
	/**
	 * Removes a user from an organisation
	 * @return organisation details page
	 */
	@Transactional
	public static Result deleteuser() 
	{
		Form<OrgUser> filledForm = Form.form(OrgUser.class).bindFromRequest();
		
		Map<String, String> map = filledForm.data();
		OrgUser ou = JPA.em().find(OrgUser.class, Long.parseLong((String)map.get("id")));
		
		if (ou != null && userHasAdminAccessToOrganisation(ou.getOrg().getId())) {
			sendUserRemovalMail(ou);
			ou.delete();
			return redirect(routes.OrganisationController.read(ou.getOrg().getId().toString()));
		}
		
		return unauthorized(views.html.errors.unauthorised.render(AppUser.getSystemUser(request().username()), "HOME"));
	}

	/**
	 * Saves changes to an organisation user
	 * May send email to user
	 * @return appropriate data
	 */
	@Transactional
	public static Result saveuser() 
	{
		Form<OrgUser> filledForm = Form.form(OrgUser.class).bindFromRequest();		
		Map<String, String> map = filledForm.data();

		OrgUser ou = JPA.em().find(OrgUser.class, Long.parseLong((String)map.get("id")));
		
		if (ou != null && userHasAdminAccessToOrganisation(ou.getOrg().getId())) {
			if (map.containsKey("action") && ((String)map.get("action")).compareTo("delete")==0) {
				return ok(organisationconfirmdelete.render(AppUser.findByEmail(session("email")), ou));				
			}
	
			if(filledForm.hasErrors()) {
				return badRequest(organisationuser.render(AppUser.findByEmail(session("email")), ou , ou.getOrg().getId(), filledForm));
			} else {
				if (ou!=null)
				{
					if (map.get("administrator")!=null && ((String)map.get("administrator")).compareToIgnoreCase("true")==0) {
						ou.setAdministrator(true);
					} else {
						ou.setAdministrator(false);
					}
					
					if (map.get("status")!=null) {
						if (((String)map.get("status")).compareToIgnoreCase("reject")==0) {
							sendRejectionToUser(ou,map);
							ou.delete();
						} else {
							if (ou.getStatus().compareToIgnoreCase((String)map.get("status"))!=0) {
								sendAcceptToUser(ou,map);
							}
							ou.setStatus((String)map.get("status"));
							ou.save();
						}
					}					
				}
			}
			
			Long lorgid = ou.getOrg().getId();
	    	return redirect(routes.OrganisationController.read(lorgid.toString()));
		}
		
		return unauthorized(views.html.errors.unauthorised.render(AppUser.getSystemUser(request().username()), "HOME"));
    }

	/**
	 * Sends rejection email to user
	 * @param ou the organisation user
	 * @param map additional data
	 */
	private static void sendRejectionToUser(OrgUser ou, Map<String, String> map) {
		AppUser au = ou.getAu();

		try {
			InternetAddress[] addresses = {new InternetAddress(au.getEmail_address())};
	
			Html mailBody = views.html.email.userreject.render(ou, AppConfigSettings.getConfigString("hostname", "application.hostname"),((String)map.get("reject_reason")),true);
			Html mailAlt = views.html.email.userreject.render(ou, AppConfigSettings.getConfigString("hostname", "application.hostname"),((String)map.get("reject_reason")),false);
			
			String sSubject = Messages.get("userreject.mail.subject");
			
			MailSettings.send(mailBody, mailAlt, sSubject, addresses, false, false, true);		
			
		} catch (Exception e) {
			Logger.error("Send rejection to user error: "+e.toString());
		}
	}

	/**
	 * Sends acceptance email to user
	 * @param ou the organisation user
	 * @param map additional data
	 */
	private static void sendAcceptToUser(OrgUser ou, Map<String, String> map) {
		AppUser au = ou.getAu();
		
		
		try {
			InternetAddress[] addresses = {new InternetAddress(au.getEmail_address())};
			String sSubject = Messages.get("useraccept.mail.subject");
			Html mailBody = views.html.email.useraccept.render(ou, AppConfigSettings.getConfigString("hostname", "application.hostname"), true);
			Html mailAlt = views.html.email.useraccept.render(ou, AppConfigSettings.getConfigString("hostname", "application.hostname"), false);

			MailSettings.send(mailBody, mailAlt, sSubject, addresses, false, false, true);
		}
		catch (Exception e)	{
			Logger.error("Send accept to user error: "+e.toString());
		}			
	}
	
	/**
	 * Sends the organisation administrator join confirmation
	 * @param org the organisation
	 * @param au the user
	 */
	private static void sendAdminJoinNotification(Organisation org, AppUser au) {
		List<OrgUser> admins = org.findAdmins();
		if (!admins.isEmpty()) {
				
			
			try {       			
				Html mailBody = views.html.email.userjoinrequest.render(org, au, AppConfigSettings.getConfigString("hostname", "application.hostname"), true);
				Html mailAlt = views.html.email.userjoinrequest.render(org, au, AppConfigSettings.getConfigString("hostname", "application.hostname"), false);
				
				String sSubject = Messages.get("userjoinrequest.mail.subject");
				
		        //Add each admin user to the email "to" field...
		        Iterator<OrgUser> it = admins.iterator();
		        InternetAddress[] addresses = new InternetAddress[admins.size()];
		        int i = 0;
		        while (it.hasNext()) {      	
		        	addresses[i++] = new InternetAddress(it.next().getAu().getEmail_address());
		        }
		        
				MailSettings.send(mailBody, mailAlt, sSubject, addresses, false, false, true);        
			}
			catch (Exception e) {
				Logger.error("Send admin join notification error: "+e.toString());
			}
		}
	}
	/**
	 * Sends the user a removal confirmation email
	 * @param ou the organisation user
	 */
	public static void sendUserRemovalMail(OrgUser ou) 
	{
		AppUser au = ou.getAu();
		Html mailBody = views.html.email.userremovalmail.render(ou, AppConfigSettings.getConfigString("hostname", "application.hostname"), true);
		Html mailAlt = views.html.email.userremovalmail.render(ou, AppConfigSettings.getConfigString("hostname", "application.hostname"), false);
		
		Html mailBodyAdmin = views.html.email.userremovalmailadmin.render(ou, AppConfigSettings.getConfigString("hostname", "application.hostname"), true);
		Html mailBodyAdminAlt = views.html.email.userremovalmailadmin.render(ou, AppConfigSettings.getConfigString("hostname", "application.hostname"), false);
		
		try {
			InternetAddress[] addresses = {new InternetAddress(au.getEmail_address())};
	        
			String sSubject = Messages.get("userremoval.mail.subject");
			MailSettings.send(mailBody, mailAlt, sSubject, addresses, false, false, true);		

	        List<OrgUser> lau = au.getOrgAdmins(ou);
	        InternetAddress[] adminaddresses = new InternetAddress[lau.size()];
	        ListIterator<OrgUser> it = lau.listIterator();
	        int i = 0;
	        while (it.hasNext())
	        {
	        	OrgUser ouA = it.next();
	        	adminaddresses[i++]=new InternetAddress(ouA.getAu().getEmail_address());
	        }
	        sSubject = Messages.get("userremovaladmin.mail.subject");
			MailSettings.send(mailBodyAdmin, mailBodyAdminAlt, sSubject, adminaddresses, false, false, true);		
		 } catch (MessagingException me) {
			 	Logger.error("Send user removal mail error: "+me.toString());
		        me.printStackTrace();
		 }
	}
	/**
	 * @return Page showing other organisations that could be merged into this
	 */
	@Transactional(readOnly=true)
	public static Result mergeUI(String id)
	{
		AppUser au = AppUser.getSystemUser(request().username());
		
		if (au.getOrgRole().equals(AppUser.OVERALL_ADMIN)) {
			List<Organisation> liOrgs = new ArrayList<Organisation>();
			Organisation orgThis = JPA.em().find(Organisation.class,Long.parseLong(id));
			if (orgThis != null)
			{
				if (orgThis.isRegulator())
				{
					List<Regulator> liReg = Regulator.findAll();
					Iterator <Regulator> itr = liReg.iterator();
					while (itr.hasNext())
						liOrgs.add(itr.next().getOrganisation());
				}
				else
				{
					List <NoiseProducer> liNP = NoiseProducer.findAll();
					Iterator <NoiseProducer> itnp = liNP.iterator();
					while (itnp.hasNext())
						liOrgs.add(itnp.next().getOrganisation());
				}
				Iterator <Organisation> it = liOrgs.iterator();
				while (it.hasNext())
				{
					Organisation org = it.next();
					if (org.getId().toString().equals(id))
						it.remove();
				}
			}
			return ok(organisationmerge.render(au,liOrgs,orgThis));
		}
		
		String activeTab="HOME";
		return status(403,index.render(AppUser.findByEmail(session("email")), activeTab));
	}
	
	/**
	 * UI for the confirmation of organisation merging
	 * @param id - the id of the organisation to merge into
	 */
	@Transactional(readOnly=true)
	public static Result mergeOrgsConfirm(Long id)
	{
		AppUser au = AppUser.getSystemUser(request().username());
		
		if (au.getOrgRole().equals(AppUser.OVERALL_ADMIN)) {		
			Organisation orgInto = JPA.em().find(Organisation.class,id);
			DynamicForm requestData = Form.form().bindFromRequest();
			Map <String,String> data = requestData.data();
			
			List<Organisation> liOrgs = Organisation.findAll();
			Iterator <Organisation> it = liOrgs.iterator();
			while (it.hasNext())
			{
				Organisation org = it.next();
				if (org.getId()==id || !data.containsKey("mergeorgs"+org.getId().toString()))
					it.remove();
			}
			return ok(organisationmergeconfirm.render(au, id, liOrgs, orgInto));
		}
		
		String activeTab="HOME";
		return status(403,index.render(AppUser.findByEmail(session("email")), activeTab));
	}
	
	/**
	 * UI for the merging of organisations
	 * @param id - the id of the organisation
	 */
	@Transactional(readOnly=false)
	public static Result mergeOrgs(Long id)
	{
		try {
			if (AppUser.getSystemUser(request().username()).getOrgRole() == AppUser.OVERALL_ADMIN) {
				List <NoiseProducer> lNP = new ArrayList<NoiseProducer>();
				List <Long> lNPId = new ArrayList<Long>();
				List <Regulator> lReg = new ArrayList<Regulator>();
				List <Long> lRegId = new ArrayList<Long>();
				List <Organisation> lOrgs = new ArrayList<Organisation>();
				List <Long> lOrgsOther = new ArrayList<Long>();
				
				javax.persistence.Query query = null;
				DynamicForm requestData = Form.form().bindFromRequest();
				Map <String,String> data = requestData.data();
				
				lOrgsOther = data.keySet().stream()
						.filter(o -> o.contains("mergeorgs"))
						.map(o -> Long.parseLong(o.replace("mergeorgs", "")))
						.filter(i -> i != id)
						.collect(Collectors.toList());					
				
				if (lOrgsOther.isEmpty()) {
					throw new IllegalArgumentException("No organisations selected to merge with target organisations");
				}
				
				Organisation orgThis = Organisation.find(id);
				
				if (orgThis == null) {
					throw new NullPointerException(String.format("Merge target organisation %d does not exist", id));
				}
											
				// Change owner / regulator of activities
				if (orgThis.isNoiseProducer()) {
					// Change owner of activities owned by merging noise producers to the target noise producer
					lNP = NoiseProducer.getNoiseProducersByOrgIds(lOrgsOther);
					lNPId = lNP.stream().map(np -> np.getId()).collect(Collectors.toList()); // Should just use initial list
					lOrgs = lNP.stream().map(np -> np.getOrganisation()).collect(Collectors.toList());
					
					NoiseProducer np = orgThis.getNoiseProducer();
					query = JPA.em().createQuery(
						"UPDATE ActivityApplication SET noiseproducer_id = :npid where noiseproducer in :npreplace");
					query.setParameter("npid",np.getId());
					query.setParameter("npreplace",lNP);
					
					query.executeUpdate();
				} else if (orgThis.isRegulator()) {
					// Change regulator of activities be regulated by merging regulators to the target regulator				
					lReg = Regulator.getRegulatorsByOrgIds(lOrgsOther);
					lRegId = lReg.stream().map(reg -> reg.getId()).collect(Collectors.toList()); // Should just use initial list
					lOrgs = lReg.stream().map(reg -> reg.getOrganisation()).collect(Collectors.toList());
					
					Regulator reg = orgThis.getRegulator();
					
					lReg.forEach(regulator -> {
						ActivityApplication.updateRegulatorCloseoutDays(regulator, reg.getCloseoutdays());
					});
					
					query = JPA.em().createQuery("UPDATE ActivityApplication SET regulator_id = :regid where regulator in :regreplace");
					query.setParameter("regid",reg.getId());
					query.setParameter("regreplace",lReg);
					
					query.executeUpdate();				
				} else {
					throw new IllegalArgumentException(String.format("Merge target [%d] was not a noise producer or a regulator", id));
				}
				
				// Extract list of users in original target organisation
				List <OrgUser> liOTOU = orgThis.findUsers();
				HashMap <Long,Boolean> hmOuThis = new HashMap<Long,Boolean>();
				liOTOU.forEach(ou -> hmOuThis.put(ou.getAu().getId(), true));
				
				// Collect merge organisation users
				HashMap <AppUser,Boolean> hmOu = new HashMap<AppUser,Boolean> ();
				List <OrgUser> liou = OrgUser.getUsersInOrgs(lOrgs);
				Iterator <OrgUser> itou = liou.iterator();
				while (itou.hasNext())
				{
					OrgUser ou = itou.next();
	
					if (hmOu.containsKey(ou.getAu()))
					{
						if (ou.isAdministrator())
							hmOu.put(ou.getAu(), ou.isAdministrator());
					}
					else
					{
						hmOu.put(ou.getAu(), ou.isAdministrator());
					}
				}
				// so now we have a list of all users at all of the organisations to be merged
				query = JPA.em().createQuery(
						"delete from OrgUser where organisation_id in (:orgs)");
				query.setParameter("orgs",lOrgs);
				query.executeUpdate();
				
				// for each old user create one record for the merged org if it wasn't in the old org
				Iterator <AppUser> itHmOu = hmOu.keySet().iterator();
				
				while (itHmOu.hasNext())
				{
					AppUser auNew = itHmOu.next();
					if (!hmOuThis.containsKey(auNew.getId()))
					{
						OrgUser ou = new OrgUser();
						ou.setAu(auNew);
						ou.setOrg(orgThis);
						ou.setAdministrator(false);
						ou.setStatus("unverified");
						ou.save();
					}
				}
				
				// ok now we need to delete the orgs to be merged
				if (orgThis.isNoiseProducer())
				{
					if (lNPId.size() > 0)
					{
						query = JPA.em().createQuery(
								"delete from NoiseProducer where id in (:npsdel)");
						query.setParameter("npsdel",lNPId);
						query.executeUpdate();
					}
				}
				else
				{
					if (lRegId.size() > 0)
					{
						query = JPA.em().createQuery(
								"delete from Regulator where id in (:regsdel)");
						query.setParameter("regsdel",lRegId);
						query.executeUpdate();
					}
				}
				
				if (lOrgsOther.size() > 0)
				{
					query = JPA.em().createQuery("delete from Organisation where id in (:orgsdel)");					
					query.setParameter("orgsdel", lOrgsOther);
					query.executeUpdate();
				}
				
				return OrganisationController.adminorgs();
			}
		} catch (Exception ex) {
			PlayException pex = new PlayException(ex.getMessage(), ex.getMessage(), ex);
			play.Logger.error(pex.title, pex);
			String activeTab="ERROR";
			return badRequest(views.html.errors.error.render(AppUser.getSystemUser(request().username()), AppConfigSettings.getConfigString("sendMailFrom", "email.sendFrom"), activeTab, pex.id));
		}
		
		String activeTab="HOME";
		return status(403,index.render(AppUser.findByEmail(session("email")), activeTab));
	}

	/**
	 * @return Page showing other organisations that could be merged into this
	 */
	@Transactional(readOnly=true)
	public static Result addUserByAdminUI(Long id)
	{		
		AppUser au = AppUser.getSystemUser(request().username());
		
		if (au.getOrgRole().equals(AppUser.OVERALL_ADMIN)) {
			Organisation orgThis = JPA.em().find(Organisation.class,id);
			List<AppUser> liau = Organisation.findNonUsers(null);
			
			return ok(organisationadduser.render(au, liau, orgThis));
		}
		
		return unauthorized(views.html.errors.unauthorised.render(au, "HOME"));
	}
	
	/**
	 * @return Page showing other organisations that could be merged into this
	 */
	@Transactional(readOnly=false)
	public static Result addUserByAdmin(Long id)
	{	
		AppUser au_actioner = AppUser.getSystemUser(request().username());
		
		if (au_actioner.getOrgRole().equals(AppUser.OVERALL_ADMIN)) {
			DynamicForm requestData = Form.form().bindFromRequest();
			Map <String,String> data = requestData.data();
	
			AppUser au = JPA.em().find(AppUser.class,Long.parseLong(data.get("appuser.id")));
	
			Organisation orgThis = JPA.em().find(Organisation.class,id);
			
			if (au != null && orgThis != null) {			
				OrgUser ou = new OrgUser();
				ou.setOrg(orgThis);
				ou.setAu(au);
				ou.setAdministrator(false);
				ou.setStatus(OrgUser.VERIFIED);
				
				JPA.em().persist(ou);
				
				return adminorgs();
			}
		}
		
		return unauthorized(views.html.errors.unauthorised.render(au_actioner, "HOME"));
	}
}
