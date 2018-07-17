package controllers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.HashMap;

import javax.mail.*;
import javax.mail.internet.*;
import javax.management.Query;

import models.ActivityApplication;
import models.AppUser;
import models.NoiseProducer;
import models.OrgUser;
import models.Organisation;
import models.Regulator;
import play.Logger;
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
import play.mvc.Http.RequestBody;

import javax.persistence.*;

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
		return ok(organisationregedit.render(AppUser.findByEmail(session("email")), frmReg, null));
	}	
	
	/**
	 * Allows editing of a regulator
	 * @param id the regulator to be edited
	 * @return the page containing the data to be edited
	 */
	@Transactional(readOnly=true)
	public static Result edit(String id) 
	{
		Regulator reg = JPA.em().find(Regulator.class, Long.parseLong(id));
		if (OrganisationController.userHasAdminAccessToOrganisation(reg.getOrganisation().getId().longValue()))
		{
			Form<Regulator> filledForm = frmReg.fill(reg);
			return ok(organisationregedit.render(AppUser.findByEmail(session("email")), filledForm , reg));
		}
		String activeTab="HOME";
        return status(403,index.render(AppUser.findByEmail(session("email")), activeTab));
	}
	
	/**
	 * Shows the UI for the regulator details
	 * @param id the id of the regulator
	 * @return page showing the regulator
	 */
	@Transactional(readOnly=true)
	public static Result read(String id)
	{
		long lid = Long.parseLong(id);
		
		Regulator reg = JPA.em().find(Regulator.class, lid);
		
		return ok(organisationregread.render(AppUser.findByEmail(session("email")), reg));
	}

	/** 
	 * Saves the regulator details
	 * @return confirmation
	 */
	@Transactional
	public static Result save() 
	{
		Form<Regulator> filledForm = frmReg.bindFromRequest();
		if(filledForm.hasErrors()) {
			Regulator reg = null;
			if (filledForm.data().containsKey("id")) 
				reg = JPA.em().find(Regulator.class, Long.parseLong(filledForm.data().get("id"))); 
			return badRequest(organisationregedit.render(AppUser.findByEmail(session("email")), filledForm, reg));
		}
		
		Regulator reg = filledForm.get();
		AppUser au = AppUser.getSystemUser(request().username());
		if (reg.getOrganisation().getId()!=null)
		{
			Organisation orgOnDisk = JPA.em().find(Organisation.class, reg.getOrganisation().getId());
			orgOnDisk.sendChanges(reg.getOrganisation(), request().host(), au);
			reg.update();
		} else {
			reg.save();
		}
		return OrganisationController.adminorgs();
	}
}
