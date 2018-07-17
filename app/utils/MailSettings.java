package utils;

import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import play.Logger;
import play.twirl.api.Html;

import java.util.Date;

import com.typesafe.config.ConfigFactory;

public class MailSettings {

	/**
	 * Helper to get the mail session object from JNDI if available (Tomcat instance deployments)
	 * or from the configuration (for Play standalone running)
	 * 
	 * @return javax.mail.Session
	 */
	public static Session getSession() {
		Session session = null;
		
		//Try to get context values (for tomcat deployments):
		Context initCtx;
		try {
			initCtx = new InitialContext();
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			session = (Session) envCtx.lookup("mail/Session");
			return session;
		} catch (NamingException e) {
			Logger.error("Error getting mail context: "+e.toString());
		}
		//no session from java naming context, so try using application config		
        Properties props = new Properties();
        props.put("mail.smtp.host", ConfigFactory.load().getString("mail.smtp.host")); 
        props.put("mail.smtp.port", ConfigFactory.load().getString("mail.smtp.port"));
        props.put("mail.smtp.user", ConfigFactory.load().getString("mail.smtp.user"));
        props.put("mail.smtp.auth", ConfigFactory.load().getString("mail.smtp.auth"));
        props.put("mail.smtp.starttls.enable", ConfigFactory.load().getString("mail.smtp.starttls.enable"));
        props.put("mail.smtp.starttls.required", ConfigFactory.load().getString("mail.smtp.starttls.required"));
        
        //props.put("mail.debug", "true");
        if (ConfigFactory.load().getString("mail.smtp.auth").equals("true")) {
	        session = Session.getInstance(props, new javax.mail.Authenticator() {
	            protected PasswordAuthentication getPasswordAuthentication() {
	                return new PasswordAuthentication(
	                		ConfigFactory.load().getString("mail.smtp.user"), ConfigFactory.load().getString("mail.smtp.password"));
	             }
	          });
        } else {
        	session = Session.getInstance(props);
        }
		
		return session;
	}
	
	public static boolean send(Html mailBody, Html mailAlt, String sSubject, 
			InternetAddress[] addresses, boolean bRegulator, boolean bNP, boolean bJNCC)
	{
		try {
			Session session = MailSettings.getSession();
	        MimeMessage message = new MimeMessage(session);
	        message.setSubject(sSubject);

			String overrideAddress = AppConfigSettings.getConfigString("regulatorOverrideAddress", "email.regulator_override_address");
			if (bRegulator && !overrideAddress.equals("")) 
			{
				Logger.error("Regulator email override is in effect - email will not be sent to the regulator address");
				addresses = new InternetAddress[1]; 
				addresses[0] = new InternetAddress(overrideAddress);
			}
			String overrideAddressNP = AppConfigSettings.getConfigString("noiseproducerOverrideAddress", "email.noiseproducer_override_address");
			if (bNP && !overrideAddressNP.equals("")) 
			{
				Logger.error("Noise producer email override is in effect - email will not be sent to the noise producer address");
				addresses = new InternetAddress[1]; 
				addresses[0] = new InternetAddress(overrideAddressNP);
			}
			String overrideAddressJNCC = AppConfigSettings.getConfigString("jnccOverrideAddress", "email.jncc_override_address");
			if (bJNCC && !overrideAddressJNCC.equals("")) 
			{
				Logger.error("JNCC email override is in effect - email will not be sent to JNCC");
				addresses = new InternetAddress[1]; 
				addresses[0] = new InternetAddress(overrideAddressJNCC);
			}

	        message.setRecipients(Message.RecipientType.TO, addresses);

	        String mailFrom = AppConfigSettings.getConfigString("sendMailFrom", "email.sendFrom");
			InternetAddress iamf = new InternetAddress(mailFrom);

	        message.setFrom(iamf);
	        message.setSentDate(new Date());
	        
	        Multipart multiPart = new MimeMultipart("alternative");
	        MimeBodyPart textPart = new MimeBodyPart();
	        textPart.setText(mailAlt.body(), "utf-8");

	        MimeBodyPart htmlPart = new MimeBodyPart();
	        htmlPart.setContent(mailBody.body(), "text/html; charset=utf-8");

	        multiPart.addBodyPart(textPart); // <-- first
	        multiPart.addBodyPart(htmlPart); // <-- second
	        message.setContent(multiPart);
	        
	        // set the message content here
	        Transport t = session.getTransport("smtp");
	        t.connect();
	        t.sendMessage(message, message.getAllRecipients());
	        t.close();
	        return true;
		 } catch (MessagingException me) {
			 Logger.error("Error sending mail: "+me.toString());
		     me.printStackTrace();
		 }
		return false;
	}

}
