# WAR File Generation

The Play Framework application does not need any servlet container to run the application.  However, in order to generate a WAR file and deploy this to Tomcat 9 follow the steps below.  This assumes that both a Tomcat 9 server and a PostgreSQL database are installed and configured (see [INSTALL.md](../../INSTALL.md)).

You will need to update the `application.secret` config parameter in the `conf/application.conf` file as this is the only variable that can't easilly be configured from the Tomcat host. You can generate a new one and modify the `applicaiton.conf` or if needed extract it from the deployed war artefact;

From the root of the project in a terminal run, this will update the `conf/applicaiton.conf` file with the new secret:

    sbt "play-update-secret"

Replace the "changeme" value in the `application.conf` to the generated value **before** compiling to war as the `application.conf` will be compiled into the warfile.

Then from the root of the project in a terminal run:

    sbt war

This generates the .war file in the `target` directory.  

To allow the application to run as the root ("/") context rename the generated war file as "ROOT.war".
