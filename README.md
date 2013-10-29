repoxEuropeana
==============

REPOX Europeana

To configure REPOX some parameters must be set on the "configurations.properties" file under "repox-core\src\main\resources":

# Directory of the REPOX Repository (XML files)
repository.dir C:/repoxdata/repository
# Directory of the REPOX XML Dynamic Configuration Files (DataProviders, Statistics)
xmlConfig.dir C:/repoxdata/configuration
# Directory for the temporary OAI-PMH requests
oairequests.dir C:/repoxdata/[temp]OAI-PMH_Requests
# Directory for the temporary FTP requests
ftprequests.dir C:/repoxdata/[temp]FTP_Requests
# Directory for the temporary HTTP requests
httprequests.dir C:/repoxdata/[temp]HTTP_Requests
# Directory of the Database
database.dir C:/repoxdata/repoxdb

administrator.email mail@gmail.com
smtp.server smtp.gmail.com
default.email mail@gmail.com
administrator.email.pass zzzz
smtp.port 465

Derby Database:
database.driverClassName org.apache.derby.jdbc.EmbeddedDriver
database.embeddedDriver true
database.url jdbc:derby:
database.create true

Or if using a PostGres Database:
database.driverClassName org.postgresql.Driver
database.url jdbc:postgresql://localhost/exampledb
database.user postgres
database.password postgres

==================================================================================================
Additional Information:

Configuration file imported through classpath (configuration. properties and log4j.properties):

When initiation the tomcat/jetty server, add the following arguments to the JVM classpath:
	-Drepox.data.dir=/home/conf
		The argument represents the folder where the configuration.properties is stored
	-Drepox.log4j.configuration=file:///home/conf/log4j.properties
		The argument represents the location of the log4j.properties file
		
Configure REPOX External Services: https://docs.google.com/document/d/1XkXJA8HRFzIGXaWYIQ3xJ1aSvKaDsl0lLnXidKkNMRg/edit?usp=sharing
REPOX REST architecture details: https://docs.google.com/spreadsheet/ccc?key=0As_Z9E2rQwrWdHJDb3V5WmREUmY3b2FCajVSNERPaUE&usp=sharing