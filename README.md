JNCC Noise Registry Play application
================================================

This file will be packaged with your application when using `activator dist`.

Installation
------------

Installation instructions are contained in:
INSTALL.txt

Detailed Documentation
----------------------

For more detailed documentation, check the public\docs folder:
1) developer.txt
This contains information regarding how to set up the environment for further development
including configuration of Eclipse and plugins

2) liquibase.txt
Instructions for how to locate the latest version of liquibase and how to ensure the 
database schema matches the code version.

3) openshift.txt
Instructions for uploading and running the application on OpenShift servers.

4) postgres.txt
Initial configuration of the PostgreSQL database used with this application.

5) war.txt
Creation and deployment of a .war file for use on Tomcat.

6) configurableitems.txt
How to change the text of the static pages.

Other than the initial database creation all schema changes are contained in the schema_changes
folder.
