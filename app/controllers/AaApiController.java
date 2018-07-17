package controllers;

import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import models.ActivityApplication;
import models.AppUser;
import play.Logger;
import play.data.Form;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Result;
import play.mvc.Security;

import com.wordnik.swagger.annotations.*;

@Security.Authenticated(SecuredController.class)
@Api(value = "/aa1", description = "Operations on Activity Applications (aa1)")
public class AaApiController extends BaseApiController {
	static Form<ActivityApplication> appForm = Form.form(ActivityApplication.class);
	
	@Transactional(readOnly=true)
	@ApiOperation(value = "Finds Activity Applications by status",
            notes = "Returns all Activity Applications with a status value matching the parameter",
            nickname = "findByStatus",
            response = ActivityApplication.class,
            responseContainer = "List", httpMethod = "GET")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid status value")})
	public static Result findByStatus(
			@ApiParam(value = "Status values that need to be considered for filter", 
				required = true, 
				defaultValue = "Proposed", 
				allowableValues = "Proposed,Cancelled,Closed,Deleted", allowMultiple = false) 
			@QueryParam("status") String status)
	{
		AppUser au = new AppUser();
		if (!request().username().equals("")) {
			au = AppUser.findByEmail(request().username());
		}
		return JsonResponse(au.findApplicationsByStatus(status, null));
	}
	
	@Transactional(readOnly=true)
	@ApiOperation(value = "Find Activity Application by ID",
	            notes = "Returns an Activity Application",
	            nickname = "findById",
	            response = ActivityApplication.class, httpMethod = "GET")
	@ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid ID supplied"),
	            @ApiResponse(code = 404, message = "Activity Application not found")})
	public static Result getById(
			@ApiParam(value = "Id value that identifies the Activity Application", 
				required = true, 
				allowMultiple = false) 
			@PathParam("id") Long id)
	{
		ActivityApplication aa = JPA.em().find(ActivityApplication.class, id);
		if (aa==null) {
			return notFound();
		}
		return JsonResponse(aa);
	}
	
	@Transactional
	@ApiOperation(value="Create Activity Application",
				notes = "Creates a new Activity Application using the data supplied (if valid)",
				nickname = "createAA",
				response = ActivityApplication.class, httpMethod = "POST")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "body", value = "Activity Application details", required = true, dataType="models.ActivityApplication", paramType = "body"),
	})
	@ApiResponses(value = {@ApiResponse(code = 405, message = "Invalid input")})
	public static Result create()
	{
		Form<ActivityApplication>  filledForm = appForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(filledForm.errorsAsJson());
		} else {
			AppUser au = AppUser.getSystemUser(request().username());
			filledForm.get().save(au);
			JPA.em().flush();
			
			return JsonResponse(filledForm.get());
		}
	}
}
