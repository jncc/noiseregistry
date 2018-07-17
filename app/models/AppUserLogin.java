package models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import play.Logger;
import play.i18n.Messages;


public class AppUserLogin {

	protected String email;
	protected String password;
	@JsonIgnore
	protected String redirectTo;
	
    protected AppUser au;

    /**
     * gets the email address
     * @return
     */
    public String getEmail() {
		return email;
	}

    /**
     * Sets the email address
     * @param email
     */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Gets the password
	 * @return
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password
	 * @param password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Gets the redirected page (when a user tries to hit a page that requires authorisation
	 * they are directed to login then redirected to this page)
	 * @return
	 */
	public String getRedirectTo() {
		return redirectTo;
	}

	/**
	 * Sets the redirected page
	 * @param redirectTo
	 */
	public void setRedirectTo(String redirectTo) {
		this.redirectTo = redirectTo;
	}

	/**
	 * Gets the associated AppUser object
	 * @return
	 */
	@JsonIgnore
	public AppUser getAu() {
		return au;
	}

	/**
	 * Sets the AppUser
	 * @param au
	 */
	protected void setAu(AppUser au) {
		this.au = au;
	}

	/**
	 * Validates the user
	 * Returns a string if invalid and null otherwise
	 * @return
	 */
	public String validate() {
		this.au = AppUser.authenticate(this.email, this.password);
        if (this.au == null) {
        	//Log the failed authentication attempt...
        	Logger.error("Invalid authentication attempt for user: " + this.email);
            return Messages.get("loginform.error.invalid");
        }
        if (this.au.getVerification_status().compareToIgnoreCase(AppUser.VERIFIED) !=0 ) {
        	Logger.error("Attempted unverified user login: " + this.email);
            return Messages.get("loginform.error.unverified");
        }
        return null;
    }

}
