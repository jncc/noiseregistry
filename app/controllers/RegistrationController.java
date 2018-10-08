package controllers;

import java.sql.Timestamp;
import java.util.Date;

import models.AppUserRegistration;
import models.AppUserDetails;
import models.AppUser;
import models.Organisation;
import play.Logger;
import play.api.PlayException;
import play.data.Form;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.*;
import play.twirl.api.Html;
import utils.AppConfigSettings;
import utils.MailSettings;
import views.html.*;

import javax.mail.*;
import javax.mail.internet.*;

public class RegistrationController extends Controller {

	static Form<AppUserRegistration> userForm = Form.form(AppUserRegistration.class);
	static Form<AppUserDetails> detailsForm = Form.form(AppUserDetails.class);
	
	/**
	 * Shows the UI for adding a user
	 * @return the form
	 */
	@Transactional
	public static Result add()
	{
		//Get the form object...
		return ok(
			    userform.render(AppUser.findByEmail(session("email")), userForm, Messages.get("userform.title_new"))
			  );
	}
	
	/**
	 * Saves the user
	 * @return confirmation page
	 */
	@Transactional
	public static Result save()
	{
		Form<AppUserRegistration>  filledForm = userForm.bindFromRequest();
		if(filledForm.hasErrors()) {
			return badRequest(
				userform.render(AppUser.findByEmail(session("email")), filledForm, Messages.get("userform.title_new"))
		    );
		} else {
			try {
				String sEmail = filledForm.get().getEmail_address();
				if (sEmail!=null)
				{
					AppUser au = AppUser.findByEmail(sEmail);
					if (au!=null)
					{
						filledForm.reject(Messages.get("userform.save.failed"));
						return badRequest(
								userform.render(AppUser.findByEmail(session("email")), filledForm, Messages.get("userform.title_new"))
					    );						
					}
				}
				AppUserRegistration aur = filledForm.get();
				aur.save();
				//flush the transaction so that any exceptions from the db layer are raised...
				JPA.em().flush();
				
				//Send the user a verification email...
				AppUser au = AppUser.findByEmail(aur.getEmail_address());
				sendVerificationMail(au);			
				return redirect(routes.RegistrationController.confirmadd(Long.toString(au.getId())));
				
			} catch (Exception e) {
				Logger.error("Save error: "+e.getMessage());

				filledForm.reject(Messages.get("userform.save.failed"));
				return badRequest(
						userform.render(AppUser.findByEmail(session("email")), filledForm, Messages.get("userform.title_new"))
			    );
			}
			
		}
	}
	
	/**
	 * UI showing the list of unverified users to resend mails to
	 * @return page showing the unverified users
	 */
	@Transactional(readOnly=true)
	public static Result unverifiedUsers()
	{
		AppUser au = AppUser.findByEmail(session("email"));
		
		if (au != null && au.getOrgRole().equals(AppUser.OVERALL_ADMIN)) {
			return ok(adminunverifiedusers.render(AppUser.findByEmail(session("email")),AppUser.getUnverifiedUsers()));
		}
		
		return unauthorized(views.html.errors.unauthorised.render(au, "HOME"));
	}
	
	@Transactional(readOnly=true)
	public static Result resendverifmail(Long id)
	{
		AppUser au = AppUser.findByEmail(session("email"));
		
		if (au != null && au.getOrgRole().equals(AppUser.OVERALL_ADMIN)) {		
			AppUser user = JPA.em().find(AppUser.class,id);
			
			if (user == null)
			{
				throw new PlayException("User is null", String.format("User %d was not found", id));
			}
			RegistrationController.sendVerificationMail(user);
			return RegistrationController.unverifiedUsers();
		}
		
		return unauthorized(views.html.errors.unauthorised.render(au, "HOME"));
	}
	
	@Transactional(readOnly=false)
	public static Result adminauthorise(Long id)
	{
		AppUser au = AppUser.findByEmail(session("email"));
		
		if (au != null && au.getOrgRole().equals(AppUser.OVERALL_ADMIN)) {
			AppUser user = JPA.em().find(AppUser.class,id);
			
			if (user == null)
			{
				throw new PlayException("User is null", String.format("User %d was not found", id));
			}
			user.setVerification_status(AppUser.VERIFIED);
			JPA.em().merge(user);
			return RegistrationController.unverifiedUsers();
		}
		
		return unauthorized(views.html.errors.unauthorised.render(au, "HOME"));
	}
	
	/**
	 * Send a verification email
	 * @param au the user
	 */
	@Transactional(readOnly=false)
	public static boolean sendVerificationMail(AppUser au) 
	{
		au.setDate_last_sent_verification(new Timestamp(System.currentTimeMillis()));
		if (JPA.em().getTransaction().isActive())
		{
			JPA.em().merge(au);
		}
		else
		{
			JPA.em().getTransaction().begin();
			JPA.em().merge(au);
			JPA.em().getTransaction().commit();
		}
		
		Html mailBody = views.html.email.verificationmail.render(au, request().host(), true);
		Html mailAlt = views.html.email.verificationmail.render(au, request().host(), false);
		
		String sSubject = Messages.get("registration.mail.subject");	
		
		try 
		{
			InternetAddress[] addresses = {new InternetAddress(au.getEmail_address())};
			return MailSettings.send(mailBody, mailAlt, sSubject, addresses, false, false, false);		
		} catch (Exception e)
		{
			Logger.error("Send verification email error: "+e.toString());
			return false;
		}
		
	}
	
	/**
	 * Checks the verification token
	 * @param ver_token
	 * @return confirmation
	 */
	@Transactional
	public static Result verify(String ver_token)
	{
		//Look for the verification token in the unverified user records
		boolean res = AppUser.verify(ver_token);
		if (res) {
	    	String activeTab="HOME";
	        return ok(views.html.finished.render(AppUser.findByEmail(session("email")), "verification.successpage_title", "verification.success", activeTab, routes.ApplicationController.home()));
		} else {
	    	String activeTab="HOME";
	        return ok(views.html.finished.render(AppUser.findByEmail(session("email")), "verification.errorpage_title", "verification.error", activeTab, routes.ApplicationController.home()));

		}
	}
	
	/**
	 * Shows the UI to confirm the addition of a user
	 * @param id not used 
	 * @return confirmation page
	 */
	@Transactional(readOnly=true)
	public static Result confirmadd(String id)
	{
    	String activeTab="HOME";
        return ok(views.html.finished.render(AppUser.findByEmail(session("email")), "registration.confirmation_title", "registration.success", activeTab, routes.ApplicationController.home()));
        
	}
	
	/**
	 * Shows the UI allowing a user to be edited
	 * @return page ready for editing
	 */
	@Transactional
	@Security.Authenticated(SecuredController.class)
	public static Result edit()
	{
		AppUser au = AppUser.findByEmail(request().username());
		Form<AppUserDetails> filledForm = detailsForm.fill(au.toAppUserDetails());
	    return ok(
	            mydetails.render(AppUser.findByEmail(session("email")), filledForm, Messages.get("userform.title_mydetails"), au.getId())
	        );
	}
	
	/**
	 * Shows a static version of the user details
	 * @return user detail page
	 */
	@Transactional(readOnly=true)
	@Security.Authenticated(SecuredController.class)
	public static Result read()
	{
		AppUser au = AppUser.findByEmail(request().username());
		return ok(userread.render(au));
	}
	
	/**
	 * Updates the user with the submitted details
	 * @param id the user id 
	 * @return confirmation page
	 */
	@Transactional
	@Security.Authenticated(SecuredController.class)
	public static Result update(Long id)
	{
		AppUser au = AppUser.findByEmail(request().username());
		
		Form<AppUserDetails>  filledForm = detailsForm.bindFromRequest();

		if (au != null && au.getId().equals(id)) {
			if(filledForm.hasErrors()) {
				return badRequest(
						mydetails.render(au, filledForm, Messages.get("userform.title_mydetails"), id)
			    );
			} else {
				filledForm.get().update(id);
				session("email", filledForm.get().getEmail_address());
				return redirect(routes.RegistrationController.confirmupdate());
			}
		}
		
		return unauthorized(views.html.errors.unauthorised.render(au, "HOME"));
	}
	
	@Transactional(readOnly=true)
	public static Result confirmupdate()
	{
    	String activeTab="HOME";
        return ok(views.html.finished.render(AppUser.findByEmail(session("email")), "changeuserdetails.confirmation_title", "changeuserdetails.success", activeTab, routes.RegistrationController.read()));

	}
}
