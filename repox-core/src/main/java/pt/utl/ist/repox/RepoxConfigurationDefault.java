/*
 * Created on 23/Mar/2006
 *
 */
package pt.utl.ist.repox;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;


/**
 * Represents all the global available to the application. Just for convenience.
 * Not to be used for having globals that are important for the model.
 *
 * @author Nuno Freire
 */
public class RepoxConfigurationDefault extends RepoxConfiguration {

    public RepoxConfigurationDefault(Properties configurationProperties) throws IOException {
        super(configurationProperties);
    }

}
