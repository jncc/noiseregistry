package utils;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import com.typesafe.config.ConfigFactory;
import play.Logger;

public class AppConfigSettings {

	/**
	 * Get a configuration string value either from the JNDI settings if available, or via the
	 * specified configuration file key otherwise.
	 * 
	 * example: AppConfigSettings.getConfigString("application/Hostname", "application.hostname")
	 * 
	 * @param jndiKey
	 * @param configKey
	 * @return
	 */
	public static String getConfigString(String jndiKey, String configKey) {
		
		Context initCtx;
		try {
			initCtx = new InitialContext();
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			String res = (String) envCtx.lookup(jndiKey);
			return res;
		} catch (NamingException e) {
			// could be in jndi or config so checkboth before outputting error
		}
		String res = ConfigFactory.load().getString(configKey);
		if (res==null || res.equals(""))
			Logger.info(jndiKey+" not found in jndi and "+configKey+" not found in conf file");
		return res;
	}
}
