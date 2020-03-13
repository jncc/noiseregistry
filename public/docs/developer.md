# Development Environment Setup

This project was originally developed using the activator helper as part of Play Framework, this no longer functions and will only run against an Oracle JDK (without modification of the executable shell scripts).

## Pre-requisites

 - SBT (https://www.scala-sbt.org) - You will also need to insert custom repositories to cope with a https issue, described below
 - OpenJDK 8 (Windows users can use AdoptOpenJDK https://adoptopenjdk.net while linux users can just use their preferred OpenJDK distribution)
 - VS Code (with `Java Extension Pack` installed for debugging)
 - A local database to connect to (with the appropriate database inside), currently am running with the `kartoza/postgis:9.6-2.4` docker container

### SBT Repositories Maven Central fix

The Maven Central Repo no long responds to http (and does not redirect to https), so you will need to put some overrides into your SBT user config folders to cope with this. Afte the initial run of sbt (it should download and install v0.13.5 locally), you will need to create a `repositories` files in your `.sbt` user folder. This folder is typically in your home directory i.e. `C:\Users\{username}\.sbt` or `~/.sbt`, under that folder there should now be a folder `0.13`, in both the current root directory and the `0.13` folder create a file called `repositories` containing the following;

	[repositories]
	maven-central: https://repo1.maven.org/maven2/

## Configuration file

You can use the existing template at `conf/dev.conf` and modify that for your needs, generally you will only need to fill in the database connection strings `db.default.url`, `db.default.user` and `db.default.password` to get started, currently the config **does not accept SSL postgres connections** so we would recommend that you develop against a local database and deploy against a local database to prevent database traffic running in the clear over a network connection.

## Run locally

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

## Run locally with debugging support in VS Code (or any other java debugger)

To run the application with a JVM debugging point opend you can run the same command as above, but with an additional parameter;

	sbt "run -Dconfig.file=conf/dev.conf" -jvm-debug 9999

This exposes the port 9999 so you can attach a debugger to the process, there is a default debugger defined in the committed `.vscode` directory.

The following instructions should be used by a developer wishing to set up a development environment.

## Stopping the application

To stop the application, use the command `Ctrl + D ` and the app should shut down (N.B. `Ctrl + C` doesn't seem to work under powershell and windows command prompt at list).