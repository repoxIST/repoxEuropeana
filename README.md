repoxEuropeana
==============

REPOX Europeana

To configure REPOX some parameters must be set on the "configurations.properties" file.



Additional Information:

Configuration file imported through classpath (configuration. properties and log4j.properties):

When initiation the tomcat/jetty server, add the following arguments to the JVM classpath:
	-Drepox.data.dir=/home/conf
		The argument represents the folder where the configuration.properties is stored
	-Drepox.log4j.configuration=file:///home/conf/log4j.properties
		The argument represents the location of the log4j.properties file
		
Configure REPOX External Services: https://docs.google.com/document/d/1XkXJA8HRFzIGXaWYIQ3xJ1aSvKaDsl0lLnXidKkNMRg/edit?usp=sharing
REPOX REST architecture details: https://docs.google.com/spreadsheet/ccc?key=0As_Z9E2rQwrWdHJDb3V5WmREUmY3b2FCajVSNERPaUE&usp=sharing