package controllers;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import models.AppUser;
import models.AppUserResetPassword;
import models.AppUserChangePassword;

import play.Logger;
import play.data.Form;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.twirl.api.Html;
import utils.AppConfigSettings;
import utils.MailSettings;

public class ChangePasswordController  extends Controller {
	static Form<AppUserChangePassword> passwordForm = Form.form(AppUserChangePassword.class);
	
	static Form<AppUserResetPassword> resetForm = Form.form(AppUserResetPassword.class);
	
	/**
	 * Index page for changing password
	 * @return change password page
	 */
	@Transactional(readOnly=true)
	@Security.Authenticated(SecuredController.class)
    public static Result index() {
        return ok(views.html.changepassword.render(AppUser.findByEmail(session("email")), passwordForm, Messages.get("changepasswordform.title")));
    }

	/**
	 * Saving a new password
	 * @return page confirming changed password or error
	 */
    @Transactional
    @Security.Authenticated(SecuredController.class)
    public static Result save() {
    	
        Form<AppUserChangePassword> filledForm = passwordForm.bindFromRequest();
        if (filledForm.hasErrors()) {
        	//Need to set the rollback only to avoid potential issues with the AppUser entity
        	JPA.em().getTransaction().setRollbackOnly();
            return badRequest(views.html.changepassword.render(AppUser.findByEmail(session("email")), filledForm, Messages.get("changepasswordform.title")));
        } else {
        	filledForm.get().update();
            return redirect(routes.ChangePasswordController.confirmchange());
        }
    }
    
    /**
     * Shows the page confirming the changed password
     * @return Confirmation page
     */
    @Transactional
    public static Result confirmchange() {
    	String activeTab="HOME";
        return ok(views.html.finished.render(AppUser.findByEmail(session("email")), "changepassword.confirmation_title", "changepassword.success", activeTab, routes.RegistrationController.read()));
    }
    /**
     * Show the UI page for forgotten password
     * @return Forgotten password page
     */
    @Transactional(readOnly=true)
    public static Result forgot() {
    	return ok(views.html.resetpassword.render(AppUser.findByEmail(session("email")), resetForm, Messages.get("resetpasswordform.title")));
    }
    
    /**
     * Action for forgotten password including generating a new random password
     * @return confirmation page
     */
    @Transactional
    public static Result generate() {
    	Form<AppUserResetPassword> filledForm = resetForm.bindFromRequest();
    	if (filledForm.hasErrors()) {
    		return badRequest(views.html.resetpassword.render(AppUser.findByEmail(session("email")), filledForm, Messages.get("resetpasswordform.title")));
    	} else {
    		String newPwd = filledForm.get().resetPassword();
    		String email = filledForm.get().getEmail();
    		//email the new password...
    		sendResetPasswordMail(AppUser.findByEmail(email), email, newPwd);
    		return redirect(routes.ChangePasswordController.confirmreset());
    	}
    	
    }
    /**
     * UI page to ask for password reset
     * @return page allowing user to reset password
     */
    @Transactional(readOnly=true)
    public static Result confirmreset() {
    	String activeTab="HOME";
        return ok(views.html.finished.render(AppUser.findByEmail(session("email")), "resetpassword.confirmation_title", "resetpassword.success", activeTab, routes.ApplicationController.home()));
    }
    
    /**
     * Send the newly created password to the user
     * @param au The user to mail with the new passord
     * @param email the email address 
     * @param pwd the new password
     */
  	public static void sendResetPasswordMail(AppUser au, String email, String pwd) 
  	{
  		Html mailBody = views.html.email.passwordresetmail.render(au, pwd, AppConfigSettings.getConfigString("hostname", "application.hostname"), true);
  		Html mailAlt = views.html.email.passwordresetmail.render(au, pwd, AppConfigSettings.getConfigString("hostname", "application.hostname") ,false);

  		String sSubject = Messages.get("resetpassword.mail.subject");
  		try {
  			InternetAddress[] addresses = {new InternetAddress(email)};
  			MailSettings.send(mailBody, mailAlt, sSubject, addresses, false, false, false);
  		}
  		catch (Exception e) {
  			Logger.error("Error sending password reset: "+e.toString());
  		}  		
  	}
}
