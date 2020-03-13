# Liquibase

Liquibase is used for version control of both schema and data database changes. 

## Requirements

- Liquibase 3.3.0 was used for databases changes (not tested with more upto date version, but should work): http://www.liquibase.org/download/index.html
- PostgreSQL JDBC driver (currently tested against postgresql-42.2.10). It can be downloaded from: http://jdbc.postgresql.org/download.html 

## To update database changes 

Once downloaded and installed the Liquibase update can be run and rerun at any time.  Liquibase records which changes have been executed on the database and so updates will always only be applied if they haven't already been applied to this database and will be applied in the correct order.

Before running ensure that you know the following:

1) Path to `postgresql-42.2.10.jar`
2) Path to relevant `changelog.xml` file **Note: This is currently a complete mess**
3) PostgreSQL server IP / domain name
4) Database name
5) Database username (*user must have rights to change the database schema*)
6) Password (*user must have rights to change the database schema*)
7) Schema name

The command should be run on one line from the `..\public\docs\schema_changes directory`

    /path/to/liquibase.bat  
        --driver=org.postgresql.Driver 
        --classpath=/path/to/postgresql-42.2.10.jar 
        --changeLogFile=/path/to/schema_changes/changelog.xml 
        --url="jdbc:postgresql://{db_host}:{db_port}/{database}" 
        --username=xxxx 
        --password=xxxx 
        --defaultSchemaName=jncc 
        update

The command should return with `SUCCESS`.