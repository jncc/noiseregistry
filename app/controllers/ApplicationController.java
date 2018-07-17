package controllers;

import models.AppUser;
import play.db.jpa.Transactional;
import play.mvc.*;
import views.html.*;

public class ApplicationController extends Controller {

	@Transactional(readOnly=true)
	/**
	 * Index page for the application
	 * @return Index page
	 */
    public static Result index() {
	   	String activeTab="HOME";
        return ok(index.render(AppUser.findByEmail(session("email")), activeTab));
    }
	
	/**
	 * Home page for the application
	 * @return Home page
	 */
	@Transactional(readOnly=true)
    public static Result home() {
    	AppUser au = AppUser.findByEmail(session("email"));
    	String activeTab="HOME";
        
    	if ((au!=null) && ((au.getUserOrgType()=="NOISEPRODUCER") || (au.getUserOrgType()=="REGULATOR"))){
    		return ok(home.render(AppUser.findByEmail(session("email")), activeTab));
        }
        return redirect(routes.RegistrationController.read());
    }
	
	/**
	 * Feedback page
	 * @return Feedback page
	 */
	@Transactional(readOnly=true)
    public static Result feedback() {
    	String activeTab="FEEDBACK";
        return ok(infopage.render(AppUser.findByEmail(session("email")), "feedback.message", activeTab));
    }
	
	/**
	 * Help page
	 * @return Help page
	 */
	@Transactional(readOnly=true)
    public static Result help() {
    	String activeTab="HOME";
        return ok(infopage.render(AppUser.findByEmail(session("email")), "helppage.message", activeTab));
    }
	
	/**
	 * Cookies page
	 * @return Cookies page
	 */
	@Transactional(readOnly=true)
    public static Result cookies() {
    	String activeTab="HOME";
        return ok(infopage.render(AppUser.findByEmail(session("email")), "cookies.message", activeTab));
    }
	
	/**
	 * Contact page
	 * @return contact page
	 */
	@Transactional(readOnly=true)
    public static Result contact() {
    	String activeTab="HOME";
        return ok(infopage.render(AppUser.findByEmail(session("email")), "contact.message", activeTab));
    }
	/**
	 * Terms and conditions page
	 * @return terms and conditions page
	 */
	@Transactional(readOnly=true)
    public static Result terms() {
    	String activeTab="HOME";
        return ok(infopage.render(AppUser.findByEmail(session("email")), "termsandconditions.message", activeTab));
    }
   
}
