package pt.utl.ist.repox.recordPackage;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: GPedrosa
 * Date: 15/Mar/2010
 * Time: 17:35:09
 * To change this template use File | Settings | File Templates.
 */

public class RecordSAXParser extends DefaultHandler {

    public interface RecordHandler{
        public void handleRecord(Element record);
    }

    RecordHandler handler;

    //List<Element> records;

    private String rootNodeValue;
    private int actualLevel = 0;
    private int rootNodeLevel = 0;
    private boolean insideElement = false;
    private String currentCharacters;
    private Map<String, String> namespaces;

    /** Creates a new instance of RecordSAXParser */
    public RecordSAXParser(String rootNodeValue, RecordHandler handler) {
        this.rootNodeValue = rootNodeValue;
        this.handler=handler;
        this.namespaces = new HashMap<String, String>();
    }


    /*   public List<Element> getRecords(){
            return records;
        }
    */

    public void startDocument() throws SAXException {
//        records = new ArrayList<Element>();
    }

    public void endDocument() throws SAXException {
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException  {
        try{
            if(!insideElement && !qName.equals(rootNodeValue)){
                // get all namespaces
                for (int i = 0; i < attributes.getLength(); i++) {
                    // Get names and values for each attribute
                    String name = attributes.getQName(i);
                    if(name.startsWith("xmlns")){
                        namespaces.put(name, attributes.getValue(i));
                    }
                }
            }

            if (qName.equals(rootNodeValue) && !insideElement){
                currentCharacters = "";
                insideElement = true;
                rootNodeLevel = actualLevel;
            }
            if(insideElement){
                String att = "";
                for (int i = 0; i < attributes.getLength(); i++) {
                    // Get names and values for each attribute
                    String name = attributes.getQName(i);
                    String value = attributes.getValue(i);

                    value = value.replace("<", "&lt;");
                    value = value.replace(">", "&gt;");
                    value = value.replace("&", "&amp;");
                    value = value.replace("\"", "&quot;");
                    value = value.replace("\'", "&#039;");

                    //if(!name.equals("") && !name.contains("xsi")){
                    if(!name.equals("")){
                        att+= " " + name + "=\"" + value + "\"";
                    }
                }

                if(qName.equals(rootNodeValue)){
                    // add namespaces to record
                    String namespacesString = "";
                    for (String s : namespaces.keySet()) {
                        if(!att.contains(s)){
                            namespacesString = " " + s + "=\"" + namespaces.get(s) + "\"" + namespacesString;
                        }
                    }
                    currentCharacters+="<" + qName + att + namespacesString + ">";
                }
                else{
                    currentCharacters+="<" + qName + att + ">";
                }
            }
            actualLevel++;
        }catch(Exception e){
            e.printStackTrace();
            throw new SAXException(e);
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException  {
        try{
            actualLevel--;
            if (qName.equals(rootNodeValue) && rootNodeLevel == actualLevel){
                currentCharacters+="</" + qName + ">";

                //System.out.println("currentCharacters = " + currentCharacters);

                SAXReader reader = new SAXReader();
                // create dom with the record
                Document doc = reader.read(new StringReader(currentCharacters));

                handler.handleRecord(doc.getRootElement());
                // records.add(doc.getRootElement());
                insideElement = false;
            }
            if(insideElement){
                currentCharacters+="</" + qName + ">";
            }
        }catch(Exception e){
            e.printStackTrace();
            throw new SAXException(e);
        }
    }

    public void characters(char buf[], int offset, int len) throws SAXException {
        String value = new String(buf,offset,len);

        value = value.replace("<", "&lt;");
        value = value.replace(">", "&gt;");
        value = value.replace("&", "&amp;");
        value = value.replace("\"", "&quot;");
        value = value.replace("\'", "&#039;");
        currentCharacters+=value;
    }
}

