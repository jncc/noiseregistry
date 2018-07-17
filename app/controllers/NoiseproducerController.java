package controllers;


import java.util.List;
import java.util.Map;

import models.AppUser;
import models.NoiseProducer;
import models.Organisation;
import models.Regulator;
import play.Logger;
import play.data.Form;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.Http.RequestBody;
import views.html.*;


@Security.Authenticated(SecuredController.class)
public class NoiseproducerController extends Controller {

	static Form<NoiseProducer> appForm = Form.form(NoiseProducer.class);
	
	/**
	 * UI page allowing new Noise Producers
	 * @return Add noise producer page
	 */
	@Transactional(readOnly=true)
	public static Result add() 
	{
		return ok(organisationnpedit.render(AppUser.findByEmail(session("email")), appForm, null));
	}
	
	/**
	 * Allows the saving of a noise producer
	 * @return confirmation page 
	 */
	@Transactional
	public static Result save()
	{
		Form<NoiseProducer>  filledForm = appForm.bindFromRequest();
		if(filledForm.hasErrors()) {
			return badRequest(organisationnpedit.render(AppUser.findByEmail(session("email")), filledForm, null));
		} 
		
		NoiseProducer np = filledForm.get();
		AppUser au = AppUser.findByEmail(session("email"));
		if (np.getOrganisation().getId()!=null)
		{
			//being edited by admin user
			Organisation orgOnDisk = JPA.em().find(Organisation.class, np.getOrganisation().getId());		
			orgOnDisk.sendChanges(np.getOrganisation(), request().host(), au);
			np.update();
			return OrganisationController.adminorgs();
		} else {
			// being created, possibly by standard user
			
			// try to find an organisation with the same name
			List<Organisation> liorg = Organisation.findByName(filledForm.data().get("organisation.organisation_name"));
			if (liorg.size()>0)
			{
				np = NoiseProducer.getFromOrganisation(liorg.get(0));
				if (np == null)
					return badRequest("You cannot use this name for a noise producer");
				return OrganisationController.addUserToOrgSameName(np.getOrganisation().getId());
			}
			np.save(au);
			String activeTab="HOME";
	    	return ok(finished.render(au, "organisation.confirmsave.title", "organisation.confirmsave.message", activeTab, routes.OrganisationController.adminorgs()));			
		}
	}

	/**
	 * Allows editing of an organisation
	 * @param id the organisation to be edited
	 * @return the page containing the data to be edited
	 */
	@Transactional(readOnly=true)
	public static Result edit(String id) 
	{
		NoiseProducer np = JPA.em().find(NoiseProducer.class, Long.parseLong(id));
		if (OrganisationController.userHasAdminAccessToOrganisation(np.getOrganisation().getId().longValue()))
		{
			Form<NoiseProducer> filledForm = appForm.fill(np);
			return ok(organisationnpedit.render(AppUser.findByEmail(session("email")), filledForm , np));
		}
		String activeTab="HOME";
        return status(403,index.render(AppUser.findByEmail(session("email")), activeTab));
	}
}
