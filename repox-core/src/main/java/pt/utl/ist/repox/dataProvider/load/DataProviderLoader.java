package pt.utl.ist.repox.dataProvider.load;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import pt.utl.ist.repox.dataProvider.DataProvider;
import pt.utl.ist.repox.dataProvider.DataSourceContainer;
import pt.utl.ist.repox.dataProvider.DataSourceContainerDefault;
import pt.utl.ist.repox.dataProvider.dataSource.IdProvided;
import pt.utl.ist.repox.oai.DataSourceOai;
import pt.utl.ist.repox.util.ConfigSingleton;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class DataProviderLoader {
    private static final Logger log = Logger.getLogger(DataProviderLoader.class);


    public List<DataProvider> loadDataProvidersFromREPOX(File dataProvidersFile) throws DocumentException, IOException, ParseException {
        return ConfigSingleton.getRepoxContextUtil().getRepoxManager().getDataManager().loadDataProvidersFromFile(dataProvidersFile, new File(ConfigSingleton.getRepoxContextUtil().getRepoxManager().getConfiguration().getRepositoryPath()));
    }



/* TEL EXAMPLE
 * <dataProviders xmlns="http://repox.ist.utl.pt/schemas/dataProviders" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://repox.ist.utl.pt/schemas/dataProviders repox_data_providers.xsd ">
  <dataProvider id="TEL" name="The European Library" country="-">
    <dataSource id="a0078" name="Kramerius - 19th and 20th century digitised periodicals and monographs from the Czech Republic">
      <oaiPmhDataSource>
        <baseUrl>http://kramerius.nkp.cz/kramerius/oai?metadataPrefix=oai_dc&amp;</baseUrl>
        <fullHarvestPeriodicity></fullHarvestPeriodicity>
        <IncrementalHarvestPeriodicity></IncrementalHarvestPeriodicity>
      </oaiPmhDataSource>
      <export>
        <fileSystemLocation>/solr/collections/a0078</fileSystemLocation>
        <periodicity></periodicity>
      </export>
    </dataSource>
 */

    public List<DataProvider> loadDataProvidersFromTEL(File dataProvidersFile) throws DocumentException, IOException {
        List<DataProvider> dataProviders = new ArrayList<DataProvider>();

        if(!dataProvidersFile.exists()) {
            return dataProviders;
        }

        SAXReader reader = new SAXReader();
        Document document = reader.read(dataProvidersFile);

        //List dataProviderElements = document.getRootElement().elements("dataProvider");
        List dataProviderElements = document.getRootElement().elements("provider");
        if(!dataProviderElements.isEmpty()) {
            for (Element currentDataProviderElement : (List<Element>) dataProviderElements) {
                String providerId = currentDataProviderElement.attributeValue("id");
                String providerName = currentDataProviderElement.attributeValue("name");
                String providerCountry = currentDataProviderElement.attributeValue("country");
                if(providerCountry != null) {
                    providerCountry = providerCountry.toLowerCase();
                }
                String providerDescription = currentDataProviderElement.attributeValue("description");

                HashMap<String, DataSourceContainer> dataSourceContainers = new HashMap<String, DataSourceContainer>();

                DataProvider provider = new DataProvider(providerId, providerName, providerCountry, providerDescription, dataSourceContainers);
                //for (Element currentDataSourceElement : (List<Element>) currentDataProviderElement.elements("dataSource")) {
                for (Element currentDataSourceElement : (List<Element>) currentDataProviderElement.elements("source")) {
                    String id = currentDataSourceElement.attributeValue("id");
                    String description = currentDataSourceElement.attributeValue("name");
                    if(description == null) {
                        description = "";
                    }
                    log.debug("DESCRIPTION: " + description);

                    //Create DataSource
                    Element oaiPmhSource = currentDataSourceElement.element("oaiPmhDataSource");
                    String urlString = oaiPmhSource.elementText("baseUrl");
                    DataSourceOai dataSource = null;

                    if(urlString != null && !urlString.isEmpty()) {
                        dataSource = getDataSourceOai(urlString, provider, id, description);
                    }
                    else {

                        dataSource = new DataSourceOai(provider, id, description, "http://www.openarchives.org/OAI/2.0/oai_dc.xsd",
                                "http://www.openarchives.org/OAI/2.0/", "oai_dc", urlString, "", new IdProvided(), null);
                    }

                    dataSourceContainers.put(dataSource.getId(), new DataSourceContainerDefault(dataSource));
                }

                dataProviders.add(provider);
            }
        }

        return dataProviders;
    }

    private DataSourceOai getDataSourceOai(String urlString, DataProvider provider, String id, String description)
            throws MalformedURLException {
        TreeMap<String, String> parametersMap = new TreeMap<String, String>();
        URL newURL = new URL(urlString);
        String oaiSource = "http://" + newURL.getHost() + (newURL.getPort() != -1 ? ":" + newURL.getPort() : "") + newURL.getPath();

        String serverQuery = newURL.getQuery();
        String[] parameters = serverQuery.split("&");
        for (String parameter : parameters) {
            int separatorIndex = parameter.indexOf("=");
            if(separatorIndex > 0) {
                String key = parameter.substring(0, separatorIndex);
                String value = parameter.substring(separatorIndex + 1);

                parametersMap.put(key, value);
            }
            else {
                // do nothing, no value
            }
        }

        String metadataFormat = parametersMap.get("metadataPrefix");
        if(metadataFormat == null) {
            metadataFormat = "oai_dc";
        }
        String oaiSet = parametersMap.get("set");

        log.debug("URL: " + oaiSource);
        log.debug("Set: " + oaiSet);
        log.debug("MF: " + metadataFormat);

        return new DataSourceOai(provider, id, description, "http://www.openarchives.org/OAI/2.0/oai_dc.xsd",
                "http://www.openarchives.org/OAI/2.0/", metadataFormat, oaiSource, oaiSet, new IdProvided(), null);
    }
}
