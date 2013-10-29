package pt.utl.ist.repox.statistics;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import pt.utl.ist.repox.dataProvider.DataManagerEuropeana;
import pt.utl.ist.repox.dataProvider.DataProvider;
import pt.utl.ist.repox.dataProvider.DataSourceContainer;
import pt.utl.ist.repox.dataProvider.dataSource.IdExtracted;
import pt.utl.ist.repox.dataProvider.dataSource.IdGenerated;
import pt.utl.ist.repox.dataProvider.dataSource.IdProvided;
import pt.utl.ist.repox.marc.DataSourceDirectoryImporter;
import pt.utl.ist.repox.oai.DataSourceOai;
import pt.utl.ist.repox.util.ConfigSingleton;
import pt.utl.ist.repox.util.TimeUtil;
import pt.utl.ist.repox.z3950.DataSourceZ3950;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class StatisticsManagerEuropeana implements StatisticsManager {
    private static final Logger log = Logger.getLogger(StatisticsManagerEuropeana.class);

    private File configurationFile;

    public File getConfigurationFile() {
        return configurationFile;
    }

    public void setConfigurationFile(File configurationFile) {
        this.configurationFile = configurationFile;
    }

    public StatisticsManagerEuropeana(File configurationFile) {
        super();
        this.configurationFile = configurationFile;
    }

    public RepoxStatistics generateStatistics(List<String> dataProviderIds) throws IOException, DocumentException, SQLException {
        int dataSourcesIdExtracted = 0;
        int dataSourcesIdGenerated = 0;
        int dataSourcesIdProvided = 0;

        int dataProviders = 0;
        int dataSourcesOai = 0;
        int dataSourcesZ3950 = 0;
        int dataSourcesDirectoryImporter = 0;

        Map<String, MetadataFormatStatistics> dataSourcesMetadataFormats = new TreeMap<String, MetadataFormatStatistics>();

        Map<String, Integer> countriesRecords = new TreeMap<String, Integer>();
        int recordsTotal = 0;

        List<DataProvider> allDataProviders = ConfigSingleton.getRepoxContextUtil().getRepoxManager().getDataManager().getDataProviders();

        for(DataProvider dataProvider : allDataProviders) {
            if(dataProviderIds != null && !dataProviderIds.contains(dataProvider.getId()))
                continue;

            dataProviders++;

            for(DataSourceContainer dataSourceContainer : dataProvider.getDataSourceContainers().values()) {
                if(dataSourceContainer.getDataSource() instanceof DataSourceOai) {
                    dataSourcesOai++;
                }
                else if(dataSourceContainer.getDataSource() instanceof DataSourceZ3950) {
                    dataSourcesZ3950++;
                }
                else if(dataSourceContainer.getDataSource() instanceof DataSourceDirectoryImporter) {
                    dataSourcesDirectoryImporter++;
                }

                if(dataSourceContainer.getDataSource().getRecordIdPolicy() instanceof IdProvided) {
                    dataSourcesIdProvided++;
                }
                else if(dataSourceContainer.getDataSource().getRecordIdPolicy() instanceof IdExtracted) {
                    dataSourcesIdExtracted++;
                }
                else if(dataSourceContainer.getDataSource().getRecordIdPolicy() instanceof IdGenerated) {
                    dataSourcesIdGenerated++;
                }
                else {
                    throw new RuntimeException("DataSource of unsupported class:" + dataSourceContainer.getDataSource().getClass().getName());
                }

                MetadataFormatStatistics metadataFormatStatistics = dataSourcesMetadataFormats.get(dataSourceContainer.getDataSource().getMetadataFormat());
                if(metadataFormatStatistics == null) {
                    dataSourcesMetadataFormats.put(dataSourceContainer.getDataSource().getMetadataFormat(),
                            new MetadataFormatStatistics(1,ConfigSingleton.getRepoxContextUtil().getRepoxManager().
                                    getRecordCountManager().getRecordCount(dataSourceContainer.getDataSource().getId()).getCount()));
                }
                else {
                    metadataFormatStatistics.addCollectionNumber();
                    metadataFormatStatistics.addRecordNumber(ConfigSingleton.getRepoxContextUtil().getRepoxManager().
                            getRecordCountManager().getRecordCount(dataSourceContainer.getDataSource().getId()).getCount());
                }

                int dataSourceCount = 0;

                if(ConfigSingleton.getRepoxContextUtil().getRepoxManager().getRecordCountManager().getRecordCount(dataSourceContainer.getDataSource().getId()) != null){
                    dataSourceCount = ConfigSingleton.getRepoxContextUtil().getRepoxManager().getRecordCountManager().getRecordCount(dataSourceContainer.getDataSource().getId()).getCount();
                }

                DataProvider dataProviderParent = ConfigSingleton.getRepoxContextUtil().getRepoxManager().getDataManager().getDataProviderParent(dataSourceContainer.getDataSource().getId());
                if(dataProviderParent.getCountry() != null) {
                    int countryRecordsTotal = dataSourceCount;

                    if(countriesRecords.get(dataProviderParent.getCountry()) != null) {
                        countryRecordsTotal += countriesRecords.get(dataProviderParent.getCountry());
                    }

                    countriesRecords.put(dataProviderParent.getCountry(), countryRecordsTotal);
                }

                recordsTotal += dataSourceCount;
            }
        }

        int dataSourcesTotal = dataSourcesOai + dataSourcesDirectoryImporter + dataSourcesZ3950;
        float recordsAvgDataSource = (dataSourcesTotal == 0 ? 0 : (float) recordsTotal / (float) dataSourcesTotal);
        float recordsAvgDataProvider = (allDataProviders.size() == 0 ? 0 : (float) recordsTotal / (float) allDataProviders.size());

        return new RepoxStatisticsEuropeana(dataSourcesIdExtracted, dataSourcesIdGenerated, dataSourcesIdProvided,
                ((DataManagerEuropeana)ConfigSingleton.getRepoxContextUtil().getRepoxManager().getDataManager()).getAggregatorsEuropeana().size(),
                dataProviders, dataSourcesOai, dataSourcesZ3950, dataSourcesDirectoryImporter, dataSourcesMetadataFormats, recordsAvgDataSource,
                recordsAvgDataProvider, countriesRecords, recordsTotal);
    }

    public synchronized Document getStatisticsReport(RepoxStatistics repoxStatistics) throws IOException {
        RepoxStatisticsEuropeana repoxStatisticsEuropeana = (RepoxStatisticsEuropeana)repoxStatistics;
        Document document = DocumentHelper.createDocument();

        Element rootNode = document.addElement("repox-statistics");
        rootNode.addAttribute("generationDate", DateFormatUtils.format(repoxStatisticsEuropeana.getGenerationDate(), TimeUtil.LONG_DATE_FORMAT_TIMEZONE));

        rootNode.addElement("dataSourcesIdExtracted").setText(String.valueOf(repoxStatisticsEuropeana.getDataSourcesIdExtracted()));
        rootNode.addElement("dataSourcesIdGenerated").setText(String.valueOf(repoxStatisticsEuropeana.getDataSourcesIdGenerated()));
        rootNode.addElement("dataSourcesIdProvided").setText(String.valueOf(repoxStatisticsEuropeana.getDataSourcesIdProvided()));
        rootNode.addElement("aggregators").setText(String.valueOf(repoxStatisticsEuropeana.getAggregators()));
        rootNode.addElement("dataProviders").setText(String.valueOf(repoxStatisticsEuropeana.getDataProviders()));
        rootNode.addElement("dataSourcesOai").setText(String.valueOf(repoxStatisticsEuropeana.getDataSourcesOai()));
        rootNode.addElement("dataSourcesZ3950").setText(String.valueOf(repoxStatistics.getDataSourcesZ3950()));
        rootNode.addElement("dataSourcesDirectoryImporter").setText(String.valueOf(repoxStatisticsEuropeana.getDataSourcesDirectoryImporter()));
        if(repoxStatistics.getDataSourcesMetadataFormats() != null && !repoxStatistics.getDataSourcesMetadataFormats().isEmpty()) {
            Element dataSourcesMetadataFormatsElement = rootNode.addElement("dataSourcesMetadataFormats");
            for (Entry<String, MetadataFormatStatistics> currentFormat : repoxStatistics.getDataSourcesMetadataFormats().entrySet()) {
                Element currentDSMF = dataSourcesMetadataFormatsElement.addElement("dataSourcesMetadataFormat");
                currentDSMF.addElement("metadataFormat").setText(currentFormat.getKey());
                currentDSMF.addElement("dataSources").setText(String.valueOf(currentFormat.getValue().getCollectionNumber()));
                currentDSMF.addElement("records").setText(String.valueOf(currentFormat.getValue().getRecordNumber()));
            }
        }

        rootNode.addElement("recordsAvgDataSource").setText(String.valueOf(repoxStatisticsEuropeana.getRecordsAvgDataSource()));
        rootNode.addElement("recordsAvgDataProvider").setText(String.valueOf(repoxStatisticsEuropeana.getRecordsAvgDataProvider()));
        Element countriesRecordsElement = rootNode.addElement("countriesRecords");
        if(repoxStatisticsEuropeana.getCountriesRecords() != null && !repoxStatisticsEuropeana.getCountriesRecords().isEmpty()) {
            for (Entry<String, Integer> currentCountry : repoxStatisticsEuropeana.getCountriesRecords().entrySet()) {
                Element currentCR = countriesRecordsElement.addElement("countryRecords");
                currentCR.addAttribute("country", currentCountry.getKey());
                currentCR.addElement("records").setText(currentCountry.getValue().toString());
            }
        }
        rootNode.addElement("recordsTotal").setText(String.valueOf(repoxStatisticsEuropeana.getRecordsTotal()));

        return document;
    }
}
