package controllers;

import java.util.ArrayList;
import java.util.Arrays;

import models.AppUser;
import models.NoiseProducer;
import models.Organisation;
import models.Regulator;
import play.data.Form;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.*;
import views.html.*;
import utils.AppConfigSettings;

@Security.Authenticated(SecuredController.class)
public class RegulatorController extends Controller {

	static Form<Regulator> frmReg = Form.form(Regulator.class);
		
	/**
	 * UI page allowing new regulators
	 * @return Add regulator
	 */
	@Transactional(readOnly=true)
	public static Result add() 
	{
		AppUser au = AppUser.getSystemUser(request().username());

		if (au.getOrgRole().equals(AppUser.OVERALL_ADMIN)) {		
			return ok(organisationregedit.render(AppUser.findByEmail(session("email")), frmReg, null));
		}

		String activeTab="HOME";
        return unauthorized(views.html.errors.unauthorised.render(AppUser.findByEmail(session("email")), activeTab));
	}	
	
	/**
	 * Allows editing of a regulator
	 * @param id the regulator to be edited
	 * @return the page containing the data to be edited
	 */
	@Transactional(readOnly=true)
	public static Result edit(String id) 
	{
		AppUser au = AppUser.getSystemUser(request().username());

		if (au.getOrgRole().equals(AppUser.OVERALL_ADMIN)) {
			Regulator reg = JPA.em().find(Regulator.class, Long.parseLong(id));
			if (reg != null) {
				Form<Regulator> filledForm = frmReg.fill(reg);
				return ok(organisationregedit.render(AppUser.findByEmail(session("email")), filledForm , reg));
			}
		}
		
		String activeTab="HOME";
        return unauthorized(views.html.errors.unauthorised.render(AppUser.findByEmail(session("email")), activeTab));
	}
	
	/**
	 * Shows the UI for the regulator details
	 * @param id the id of the regulator
	 * @return page showing the regulator
	 */
	@Transactional(readOnly=true)
	public static Result read(String id)
	{
		AppUser au = AppUser.getSystemUser(request().username());
		
		if (au.getOrgRole().equals(AppUser.OVERALL_ADMIN)) {
			Regulator reg = JPA.em().find(Regulator.class,  Long.parseLong(id));
			if (reg != null) {
				// If regulator exists and user is a superuser
				return ok(organisationregread.render(AppUser.findByEmail(session("email")), reg));
			}			
		}

		String activeTab="HOME";
        return unauthorized(views.html.errors.unauthorised.render(AppUser.findByEmail(session("email")), activeTab));
	}

	/** 
	 * Saves the regulator details
	 * @return confirmation
	 */
	@Transactional
	public static Result save() 
	{
		AppUser au = AppUser.getSystemUser(request().username());
		
		if (au.getOrgRole().equals(AppUser.OVERALL_ADMIN)) {
			Form<Regulator> filledForm = frmReg.bindFromRequest();
			if(filledForm.hasErrors()) {
				Regulator reg = null;
				if (filledForm.data().containsKey("id")) 
					reg = JPA.em().find(Regulator.class, Long.parseLong(filledForm.data().get("id"))); 
				return badRequest(organisationregedit.render(AppUser.findByEmail(session("email")), filledForm, reg));
			}
			
			Regulator reg = filledForm.get();
			if (reg.getOrganisation().getId() != null)
			{		
				if (reg.getId() != null) {
					Regulator regOnDisk = JPA.em().find(Regulator.class, reg.getId());
					if (regOnDisk != null
							&& regOnDisk.getOrganisation().getId().equals(reg.getOrganisation().getId())
							&& OrganisationController.userHasAdminAccessToOrganisation(reg.getOrganisation().getId())) {				
						Organisation orgOnDisk = regOnDisk.getOrganisation();
						
						// Prevent an organisation with the same name from being created from an existing organisation
						if (!orgOnDisk.getOrganisation_name().equals(reg.getOrganisation().getOrganisation_name()) 
								&& Organisation.organisationNameExists((reg.getOrganisation().getOrganisation_name()))) {
							filledForm.reject(Messages.get("validation.organisation.name_exists"));
							return badRequest(organisationregedit.render(AppUser.findByEmail(session("email")), filledForm, reg));
						}
						
						orgOnDisk.sendChanges(reg.getOrganisation(), AppConfigSettings.getConfigString("hostname", "application.hostname"), au);
						reg.getOrganisation().setAdministrator(orgOnDisk.isAdministrator());
						reg.update();
						return OrganisationController.adminorgs();
					} 
				}
			} else {
				reg.getOrganisation().setAdministrator(false);
				reg.save();
				return OrganisationController.adminorgs();
			}
		}
		
		// Probably should be unauthorised so it doesn't leak details unnecessarily   
		return unauthorized(views.html.errors.unauthorised.render(au, "HOME"));
	}
}
