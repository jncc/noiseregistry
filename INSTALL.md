# Production deployment

This document describes the required steps for the installation of a web application running under Apache Tomcat 9.

In addition to the Tomcat server, a Postgres 9.6 database server is required. The Postgres server can be on the same server as Tomcat or on another server. The Tomcat server must be able to access the Postgres server on the configured port for Postgres (the default is 5432) - any firewall(s) between the two servers must be configured appropriately.

1. Install an Apache Tomcat 9 server

	The Apache Tomcat 9 installation is available from: https://tomcat.apache.org/download-90.cgi

	Instructions on the installation process can be found at: http://tomcat.apache.org/tomcat-9.0-doc/setup.html

	Typically we will extract the service to a location on the working drive rather than install to Program Files under windows to avoid permissions issues, i.e.;

	C:\Software\Apache\Tomcat\9.0.30\...

	Remove all entries in the `webapps` folder.

2. Install Postgres 9.6 server 

	The Postgres server install is available from: http://www.postgresql.org/download/ 

3. Configure the Postgres database/schema/user

	A new database, schema and user are needed for the application - a sample SQL script to create these can be found in the file [public/docs/postgres.md](./public/docs/postgres.md) the password for the new user should be changed to a suitable value. The modified script can then be run from a psql command prompt on the server or via any additional Postgres tools installed (e.g. pgAdmin).

	The PostGIS extension must be enabled for the database. Details steps for installation and enabling PostGIS can be found at: http://postgis.net/install/
	
	Once PostGIS is installed the extension needs to be enabled for the database:

	```sql
	CREATE EXTENSION postgis;
	```

	Oil and gas block spatial fields use the WGS coordinate system and are of type GEOMETRY (polygon,4326).

4. Configure the Apache Tomcat server

	Three extra files are required.  These must be copied to the Tomcat "lib" folder under the main Tomcat application folder:
	
	1) Java library containing the Postgres JDBC Driver
		
		Name: `postgresql-42.2.10.jar`
		
		Download from: http://jdbc.postgresql.org/download.html	
	2) PostGIS jar file used by Hibernate
		
		Name: postgis-jdbc-1.5.2.jar

		Download from: http://www.hibernatespatial.org/repository/org/postgis/postgis-jdbc/1.5.2/postgis-jdbc-1.5.2.jar
	3) Java Mail libraries

		Name: javax.mail.jar

		Download from: https://java.net/projects/javamail/pages/Home

	The Tomcat server must be configured to allow the web application to access the Postgres database, mail server and to support different settings for 
	
## QA and Production environments.
 
The server.xml document is found in the "conf" folder under the main Tomcat application folder on Windows platforms. Extra resources and environment configuration settings are required, e.g., 

```xml
<GlobalNamingResources>
	<Resource name="jdbc/NoiseRegistryDS"
		username="<postgres_username>"
		password="<postgres_password>"
		url="jdbc:postgresql://<postgres_server>:<postgres_port>/<postgres_database>"
		auth="Container"
		type="javax.sql.DataSource"
		maxActive="100" 
		maxIdle="30" 
		maxWait="10000"
		driverClassName="org.postgresql.Driver"
		factory="org.apache.commons.dbcp.BasicDataSourceFactory"  
		removeAbandoned="true"
		removeAbandonedTimeout="20"
		logAbandoned="true" />
		
	<Resource name="mail/Session"
		type="javax.mail.Session"
		auth="Container"
		mail.smtp.host="<mail_server>"
		mail.smtp.port="<mail_port>" />
		
	<Environment name="sendMailFrom"
		type="java.lang.String"
		value="<system_email_address>" />
			
	<Environment name="externalHostname"
		type="java.lang.String"
		value="<external_hostname>:<external_port>" />

	<Environment name="regulatorOverrideAddress"
		type="java.lang.String"
		value="<mail_catch_address>" />
...
</GlobalNamingResources>
 ```

A JNDI resource named "jdbc/NoiseRegistryDS" should be created to link to the Postgres database.  This should be configured in the "GlobalNamingResources" section of the server.xml file. `<postgres_database>`, `<postgres_username>` and `<postgres_password>` should match the configuration created in step 3 above.

`<postgres_server>` should be the name or ip address of the server configured in step 2.

`<postgres_port>` is the port that the postgres server responds on - the default is 5432 so this should be used unless a different
port has been configured for Postgres.

A mail resource is required:

 - `<mail_server>` is the ip address or hostname of the email server to be used by the application
 - `<mail_port>` is the smtp port to connect on (the default is port 25)

In addition to the settings for the Postgres server and the mail server there are three further configuration settings:

a) The `sendMailFrom` setting is used to configure the email address that any mail generated by the system will be sent as.

b) The `externalHostname` setting is used to allow the application to create full URLs for pages when the current context does not contain an Http request (for example when scheduled tasks are run directly on the server). The DNS hostname and port number for the application must be entered in this setting.

c) The `regulatorOverrideAddress` setting is used to specify a single email address to be used as the "To" field for all regulator emails.

This is particularly for use in development and QA environments, where the creation of regulator entries with valid external email addresses could result in inappropriate emails being sent to the regulatory bodies.  For the production environment this value should be set to empty string ("").

### Database setup
 
 1. Create `jncc` database (Note: this could be any name technically, it just happens to have been the default one setup by the original contractors)
 2. Create `jncc` database user, giving it permissions to connect to the created database
 3. Run the `/public/docs/schema/schema_only.sql` into the database, this will create the basic tables and schema required
 4. Run the `/public/docs/schema_changes/ogbdata.sql` to import the Oil and Gas Blocks data

This should give you a basic working database with no data in it, you should be able to start a project connected to the database, however you will need to start setting up at least one administrative organistation, which requires database access.

#### Admin organisations

Admin access to the system is controlled via user membership of an admin organisation, organisations can only be promoted to an admin organisations via the database `administrator` boolean attribute on the `organisation` table.

### Install Liquibase

**NOTE**: Unless any new work occurs this section should be ignored in favour of the instructions in [Database setup](#database%20setup)
Install Liquibase from http://www.liquibase.org/

Adjust the command given in the docs\liquibase.txt file and run the following files from the schema_changes directory:
changelog2.xml
changelog3.xml

Both should indicate that they have run successfully.

### Deploy the application war file

This assumes that a war file ("ROOT.war") containing the application has been provided.  For instructions on creating a war file from the 
application source, see the document [public/docs/war.txt](./public/docs/war.md).

### Test the application

The Tomcat server should now respond on:
	http://<tomcat_server>:<tomcat_port>/

Where `<tomcat_server>` and `<tomcat_port>` are as described above.

## Deploy web proxy

Typically Tomcat will always be behind some sort of web proxy, we will not describe how to set up IIS, Apache, NGINX here but you will just need to proxy the relevant `<tomcat_port>`