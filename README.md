# JNCC Marine Noise Registry Service

## Production Deployment

Installation instructions are contained in: [INSTALL.md](./INSTALL.md)

## Developer documentation

This project was originally developed using the activator helper as part of Play Framework, this no longer functions and will only run against an Oracle JDK (without modification of the executable shell scripts).

### Pre-requisites

 - SBT (https://www.scala-sbt.org)
 - OpenJDK 8 (Windows users should use AdoptOpenJDK https://adoptopenjdk.net while linux users can use their preferred OpenJDK distribution)
 - VS Code (with `Java Extension Pack` installed for debugging)
 - A local database to connect to (with the appropriate database inside)

### Configuration file

You can use the existing template at `conf/dev.conf` and modify that for your needs, generally you will only need to fill in the database connection strings `db.default.url`, `db.default.user` and `db.default.password` to get started, currently the config **does not accept SSL postgres connections** so we would recommend that you develop against a local database and deploy against a local database to prevent database traffic running in the clear over a network connection.

### Run locally

Run SBT to setup the initial project (the current project still relies on v0.13.5), this could take some time on the first run.

	sbt

Once the initial downloads are completed you can run a compile to grab dependencies and run through an initial compilation;

	sbt compile

Assuming no errors occur you should be able to run the application with the following;

	sbt "run -Dconfig.file=conf/dev.conf" -jvm-debug 9999

Substituting `conf/dev.conf` with the path to your configuration file setup for your dev environment.

You should be able to browse to the following address;

	http://localhost:9000

This may take some time as the initial run does not set up required database connections, etc... until the first request occurs, so you may be waiting for a few minutes after the first request, after which the response time should be more regular.

### Run locally with debugging support in VS Code

To run the application with a JVM debugging point opend you can run the same command as above, but with an additional parameter;

    sbt "run -Dconfig.file=conf/dev.conf" -jvm-debug 9999

This exposes the port 9999 so you can attach a debugger to the process, there is a default debugger defined in the committed `.vscode` directory.

The following instructions should be used by a developer wishing to set up a development environment.

### Packaging as a WAR file for deployment

To package for deployment run sbt with the war argument from the root of the project;

`sbt war`

This will create the folder `./war` and the folder `./target` in the main project folder if they do not already exist and create a war package in the `./target/` directory, currently outputs with the following name: `noiseregistry-1.0-SNAPSHOT.war`.

Drop this new file into your environment's Tomcat `webapps` directory as `ROOT.war`.

### Further documentation

For more detailed documentation, check the [public\docs](./public/docs) folder:

1) [developer.md](./public/docs/developer.md) - This contains information regarding how to set up the environment for further development including configuration of Eclipse and plugins. **This has been superseeded in part by the section above**

2) [liquibase.md](./public/docs/developer.md) - Instructions for how to locate the latest version of liquibase and how to ensure the database schema matches the code version.

3) openshift.txt - Instructions for uploading and running the application on OpenShift servers.

4) [postgres.md](./public/docs/postgres.md) - Initial configuration of the PostgreSQL database used with this application.

5) [war.md](./public/docs/war.md) - Creation and deployment of a .war file for use on Tomcat. **This has been superseeded in part by the section above**

6) configurableitems.txt - How to change the text of the static pages.

Other than the initial database creation all schema changes are contained in the schema_changes folder.
