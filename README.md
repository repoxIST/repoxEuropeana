#REPOX Europeana SRC Code

1 - To configure REPOX some parameters must be set on the "configurations.properties" file under "repox-core\src\main\resources":

- Directory of the REPOX Repository (XML files)<br>
repository.dir C:/repoxdata/repository 
- Directory of the REPOX XML Dynamic Configuration Files (DataProviders, Statistics)<br>
xmlConfig.dir C:/repoxdata/configuration
- Directory for the temporary OAI-PMH requests<br>
oairequests.dir C:/repoxdata/[temp]OAI-PMH_Requests
- Directory for the temporary FTP requests<br>
ftprequests.dir C:/repoxdata/[temp]FTP_Requests
- Directory for the temporary HTTP requests<br>
httprequests.dir C:/repoxdata/[temp]HTTP_Requests
- Directory of the Database<br>
database.dir C:/repoxdata/repoxdb

administrator.email mail@gmail.com<br>
smtp.server smtp.gmail.com<br>
default.email mail@gmail.com<br>
administrator.email.pass zzzz<br>
smtp.port 465<br>
useMailSSLAuthentication = true<br>

- Derby Database:<br>
database.driverClassName org.apache.derby.jdbc.EmbeddedDriver<br>
database.embeddedDriver true<br>
database.url jdbc:derby:<br>
database.create true<br>

- Or if using a PostGres Database:
database.driverClassName org.postgresql.Driver<br>
database.url jdbc:postgresql://localhost/exampledb<br>
database.user postgres<br>
database.password postgres<br>

<br>
exportDefaultFolder= C:/Default_EXPORT_FOLDER<br>
<br>
ldapHost =<br>
ldapUserPrefix = <br>
ldapLoginDN = <br>
<br>
sendEmailAfterIngest = false<br>
useOAINamespace = false<br>
<br>

2 - Also set these properties for "Test-configuration.properties" under "repox-gui\src\test\resources".

==================================================================================================
Additional Information:<br>

1 - Configuration file imported through classpath (configuration. properties and log4j.properties):<br>

2 - When initiation the tomcat/jetty server, add the following arguments to the JVM classpath:<br>
	- Drepox.data.dir=/home/conf<br>
		The argument represents the folder where the configuration.properties is stored<br>
	- Drepox.log4j.configuration=file:///home/conf/log4j.properties<br>
		The argument represents the location of the log4j.properties file<br>
		
3 - REPOX External Services: https://docs.google.com/document/d/1XkXJA8HRFzIGXaWYIQ3xJ1aSvKaDsl0lLnXidKkNMRg/edit?usp=sharing
<br>
4 - REPOX REST architecture details: https://docs.google.com/spreadsheet/ccc?key=0As_Z9E2rQwrWdHJDb3V5WmREUmY3b2FCajVSNERPaUE&usp=sharing
