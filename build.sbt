import com.github.play2war.plugin._

name := """noiseregistry"""

version := "1.0-SNAPSHOT"

Play2WarPlugin.play2WarSettings

Play2WarKeys.servletVersion := "2.5"

Play2WarKeys.filteredArtifacts ++= Seq(("javax.servlet", "servlet-api"), 
										("postgresql", "postgresql"),
										("org.postgresql", "postgresql"), 
										("org.postgis", "postgis-jdbc"),
										("javax.mail", "mail"),
										("javax.el", "el-api"),
										("org.apache.tomcat", "tomcat-servlet-api")
										)

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.11"

scalacOptions += "-feature"

resolvers += "Postgres Repository" at "http://mvnrepository.com/artifact"

resolvers += "Hibernate Spatial Repository" at "http://www.hibernatespatial.org/repository/"

resolvers += "CSV Commons" at "http://www.mirrorservice.org/sites/ftp.apache.org/"

javaOptions in Test += "-Dconfig.file=conf/test.conf"

javacOptions ++= Seq("-Xlint:unchecked")

libraryDependencies ++= Seq(
  "org.postgresql" % "postgresql" % "9.3-1102-jdbc41",
  javaJpa.exclude("org.hibernate.javax.persistence", "hibernate-jpa-2.0-api"),
  "org.hibernate" % "hibernate-entitymanager" % "4.3.7.Final",
  "org.hibernate" % "hibernate-spatial" % "4.3",
  "javax.mail" % "mail" % "1.4.3",
  cache,
  javaWs,
  "com.wordnik" %% "swagger-play2" % "1.3.10",
  "com.wordnik" %% "swagger-play2-utils" % "1.3.10",
  "javax.el" % "el-api" % "2.2",
  "org.glassfish.web" % "el-impl" % "2.2",
  "org.apache.commons" % "commons-csv" % "1.4"  
)

excludeFilter in (Assets, JshintKeys.jshint) := new FileFilter { 
	def accept(f: File) = ".*(govuk_frontend_toolkit|govuk_elements).*".r.pattern.matcher(f.getAbsolutePath).matches 
}
