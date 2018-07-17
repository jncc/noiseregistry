package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import models.AppUserLogin;
import models.AppUser;
import play.data.Form;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.Cookie;
import play.mvc.Result;
import play.mvc.Security;
import play.i18n.Messages;

@Api(value = "/login", description = "Authentication Operations")
public class LoginController extends Controller {
	public final static String AUTH_TOKEN_HEADER = "X-AUTH-TOKEN";
	public final static String AUTH_REDIRECT = "redirectTo";
    public static final String AUTH_TOKEN = "authToken";
    
	static Form<AppUserLogin> loginForm = Form.form(AppUserLogin.class);

	/**
	 * Index page for login
	 * @return The login page
	 */
    public static Result index() {
    	AppUserLogin aul = new AppUserLogin();
    	//Pickup the redirectto value from the cookie, and discard the cookie - the redirectto value is
    	//contained in the form from here.
    	Cookie redirectCookie = request().cookie(AUTH_REDIRECT);
    	if (redirectCookie!=null) {
    		String redirectTo = redirectCookie.value();
    		aul.setRedirectTo(redirectTo);
    		response().discardCookie(AUTH_REDIRECT);
    	}
    	
    	Form<AppUserLogin> filledForm = loginForm.fill(aul);
        return ok(views.html.login.render(filledForm, Messages.get("loginform.title")));
    }

    /**
     * Authenticates a user from the POSTed email address and password
     * @return appropriate data
     */
    @ApiOperation(value = "Authenticate user by email address and password",
            notes = "Returns auth token to JSON clients.  For use in the Swagger UI, the returned auth_token value should be copied into the api_key field to provide authentication.",
            nickname = "authenticate",
            httpMethod = "POST")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "body", value = "User's email and password", required = true, dataType="models.AppUserLogin", paramType = "body"),
      })
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid username/ password")})
    @Transactional
    public static Result authenticate() {
    	Form<AppUserLogin> filledForm = loginForm.bindFromRequest();
    	AppUser au = null;
    	
        if (filledForm.hasErrors()) {
        	//Return errors in a format suitable for the request...
        	if (request().accepts("text/html")) {
        		return badRequest(views.html.login.render(filledForm, Messages.get("loginform.title")));
        	} else if (request().accepts("application/json")) {
        		return badRequest(filledForm.errorsAsJson());
        	} else {
        		return badRequest("Unsupported content type");
        	}
        	
        } else {
        	if (request().accepts("text/html")) {
        		//For success, set session context and redirect for browsers access
	            session().clear();
	            session("email", filledForm.get().getEmail());
	            au = filledForm.get().getAu();
	            String redirectTo = filledForm.get().getRedirectTo();
	            if ((au!=null) && ((au.getUserOrgType()=="NOISEPRODUCER") || (au.getUserOrgType()=="REGULATOR")))
	            {
	            	if (redirectTo.equals("")) {
	            		return redirect(routes.ApplicationController.home());
	            	} else {
	            		return redirect(redirectTo);
	            	}
	            }
	            return redirect(routes.RegistrationController.read());
        	} else if (request().accepts("application/json")) {
        		//For API access, generate an auth token and return it
        		au = filledForm.get().getAu();
        		String authToken=au.createAuthToken();
        		ObjectNode authTokenJson = Json.newObject();
                authTokenJson.put(AUTH_TOKEN, authToken);
                response().setCookie(AUTH_TOKEN, authToken);
                return ok(authTokenJson);
        	} else {
        		return badRequest("Unsupported content type");
        	}
        }
    }
    
    /**
     * Allows the user to logout
     * @return confirmation that the user has logged out
     */
    @Security.Authenticated(SecuredController.class)
    @Transactional
    @ApiOperation(value = "Log out the current user",
    	notes = "Logs out from http session and api token authentication.",
    	nickname = "logout",
    	httpMethod = "GET")
    public static Result logout()
    {
    	 response().discardCookie(AUTH_TOKEN); 
    	 AppUser au = AppUser.findByEmail(request().username());
    	 if (au!=null) {
    		 au.logout();
    	 }
    	 session().clear();
    	 
    	 if (request().accepts("text/html")) {
    		 return redirect(routes.ApplicationController.home());
    	 } else if (request().accepts("application/json")) {
    		 return ok(Json.toJson("Logged out"));
    	 } else {
    		 return badRequest("Unsupported content type");
    	 }

    }
}
