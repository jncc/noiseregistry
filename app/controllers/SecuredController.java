package controllers;

import play.mvc.*;
import play.mvc.Http.*;

import models.*;

public class SecuredController extends Security.Authenticator {

	/**
	 * Gets the email address for the current user
	 * @param ctx the context for the request
	 * @return the email address
	 */
	@Override
    public String getUsername(Context ctx) {
    	//Simple case - user has a logged in http session
    	if (ctx.session().containsKey("email")) {
    		return ctx.session().get("email");
    	}
    	
    	//No http session user, so check for auth-token...
        AppUser user = null;
        String[] authTokenHeaderValues = ctx.request().headers().get(LoginController.AUTH_TOKEN_HEADER);
        if ((authTokenHeaderValues != null) && (authTokenHeaderValues.length == 1) && (authTokenHeaderValues[0] != null)) {
            user = AppUser.findByAuthToken(authTokenHeaderValues[0]);
            if (user != null) {
                ctx.args.put("user", user);
                return user.getEmail_address();
            }
        }

        return null;
        
    }

	/**
	 * Action for unauthorized requests
	 * @param ctx the request context
	 * @return The redirected page
	 */
    @Override
    public Result onUnauthorized(Context ctx) {
    	if (ctx.request().accepts("text/html")) {
    		//Store the requested page in a temporary cookie value so that on authentication we can
    		//redirect the user to the page they tried to access.
    		String tgt = ctx.request().path();
    		String defHome = "/registration/read";
    		
    		if (!(tgt.equals(defHome))) {
    			ctx.response().setCookie(LoginController.AUTH_REDIRECT, tgt);
    		}
    		return redirect(routes.LoginController.index());
    	} else {
    		return unauthorized();
    	}
    }
}