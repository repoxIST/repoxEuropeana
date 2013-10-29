/* Client.java - created on 19 de Abr de 2013, Copyright (c) 2011 The European Library, all rights reserved */
package harvesterUI.server.sru;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Iterator;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.w3c.dom.Document;

/**
 * 
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 19 de Abr de 2013
 */
public class TestClient {
    static String testUrl="http://127.0.0.1:8888/srurecordupdate/recUpdate";

    public static void testUpdateRequest() throws Exception {
        SOAPConnection connection;

        SOAPConnectionFactory connectionFactory =
                    SOAPConnectionFactory.newInstance();
            connection = connectionFactory.createConnection();
        
        String outString ="";

        try {
            Document soapMessageDom = XmlUtil.parseDomFromFile(new File("example3.xml"));
            SOAPMessage outgoingMessage = SoapUtil.toSOAPMessage(soapMessageDom);


            URL client = new URL(testUrl);

            outString += "SOAP outgoingMessage sent\n";

            SOAPMessage incomingMessage = connection.
                call(outgoingMessage, client);

            if (incomingMessage != null) {
                ByteArrayOutputStream incomingFile = new ByteArrayOutputStream();
                incomingMessage.writeTo(incomingFile);
                incomingFile.close();
                outString +=
                    "SOAP outgoingMessage received:\n" + new String(incomingFile.toByteArray());
            }

            System.out.println(outString);
        } catch(Throwable e) {
            e.printStackTrace();
        }

    }
}
