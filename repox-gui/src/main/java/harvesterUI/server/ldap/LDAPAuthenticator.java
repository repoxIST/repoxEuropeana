package harvesterUI.server.ldap;

/**
 * Created to Project REPOX
 * User: Edmundo
 * Date: 29-05-2012
 * Time: 18:24
 */

/*
 * $name:         GetAuthenticated.java
 * $description:  GetAuthenticated shows different kinds of bind.
 *                   -- anonymous bind
 *                   -- simple bind
 *                   -- simple bind with connection method
 *                   -- SSL bind
 ******************************************************************************/

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPException;
import harvesterUI.server.util.Util;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;

public class LDAPAuthenticator{

    private static Logger logger = Logger.getLogger(LDAPAuthenticator.class);


    public static boolean checkLDAPAuthentication(String ldapHost, String loginDN, String password){
        int ldapPort      = LDAPConnection.DEFAULT_PORT;
        LDAPConnection conn = new LDAPConnection();

        return simpleBind1(conn, ldapHost, ldapPort, loginDN, password);
    }


    private static boolean simpleBind1(LDAPConnection conn, String host,int port, String dn, String passwd) {
        try {
            Util.addLogEntry("Simple bind...",logger);
            // connect to the server
            conn.connect( host, port );
            // authenticate to the server
            try {
                conn.bind( LDAPConnection.LDAP_V3, dn, passwd.getBytes("UTF8") );
            } catch (UnsupportedEncodingException u){
                throw new LDAPException( "UTF8 Invalid Encoding",
                        LDAPException.LOCAL_ERROR,(String)null, u);
            }

            Util.addLogEntry("    User DN: " + dn ,logger);
            Util.addLogEntry((conn.isBound()) ?
                    "\tAuthenticated to the server ( simple )\n":
                    "\n\tNot authenticated to the server\n",logger);
            // disconnect with the server
            conn.disconnect();
        }catch( LDAPException e ) {
            Util.addLogEntry( "Error: " + e.toString(),logger);
            try {
                conn.disconnect();
            } catch (LDAPException e1) {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                return false;
            }
            return false;
        }
        return true;
    }
}
