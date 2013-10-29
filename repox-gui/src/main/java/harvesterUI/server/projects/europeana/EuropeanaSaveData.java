package harvesterUI.server.projects.europeana;

import eu.europeana.repox2sip.models.ProviderType;
import harvesterUI.server.dataManagement.dataSets.DataSetOperationsServiceImpl;
import harvesterUI.server.dataManagement.dataSets.Z39FileUpload;
import harvesterUI.server.projects.Light.LightSaveData;
import harvesterUI.server.userManagement.UserManagementServiceImpl;
import harvesterUI.server.util.PagingUtil;
import harvesterUI.server.util.Util;
import harvesterUI.shared.*;
import harvesterUI.shared.dataTypes.DataProviderUI;
import harvesterUI.shared.dataTypes.dataSet.DataSetTagUI;
import harvesterUI.shared.dataTypes.dataSet.DataSourceUI;
import harvesterUI.shared.dataTypes.dataSet.DatasetType;
import harvesterUI.shared.dataTypes.SaveDataResponse;
import harvesterUI.shared.mdr.TransformationUI;
import harvesterUI.shared.servletResponseStates.ResponseState;
import org.apache.log4j.Logger;
import org.dom4j.DocumentException;
import pt.utl.ist.repox.RepoxManagerEuropeana;
import pt.utl.ist.repox.dataProvider.*;
import pt.utl.ist.repox.dataProvider.dataSource.DataSourceTag;
import pt.utl.ist.repox.dataProvider.dataSource.IdProvided;
import pt.utl.ist.repox.externalServices.ExternalRestService;
import pt.utl.ist.repox.externalServices.ExternalServiceStates;
import pt.utl.ist.repox.metadataTransformation.MetadataTransformation;
import pt.utl.ist.repox.metadataTransformation.MetadataTransformationManager;
import pt.utl.ist.repox.oai.DataSourceOai;
import pt.utl.ist.repox.util.ConfigSingleton;
import pt.utl.ist.util.FileUtil;
import pt.utl.ist.util.exceptions.AlreadyExistsException;
import pt.utl.ist.util.exceptions.IncompatibleInstanceException;
import pt.utl.ist.util.exceptions.InvalidArgumentsException;
import pt.utl.ist.util.exceptions.ObjectNotFoundException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created to REPOX.
 * User: Edmundo
 * Date: 04-07-2011
 * Time: 13:35
 */
public class EuropeanaSaveData {
    private static final Logger log = Logger.getLogger(EuropeanaSaveData.class);

    public static String deleteDataProviders(List<DataProviderUI> dataProviderUIs) {
        for (DataProviderUI dataProvider : dataProviderUIs) {
            try {
                RepoxManagerEuropeana repoxManagerEuropeana = (RepoxManagerEuropeana)ConfigSingleton.getRepoxContextUtil().getRepoxManager();
                repoxManagerEuropeana.getDataManager().deleteDataProvider(dataProvider.getId());
            } catch (IOException e) {
                return MessageType.OTHER.name();
            } catch (ObjectNotFoundException e) {
                return MessageType.NOT_FOUND.name();
            }
            UserManagementServiceImpl.getInstance().removeDPFromUsers(dataProvider.getId());
        }
        return MessageType.OK.name();
    }

    // DATA SOURCES
    public static SaveDataResponse saveDataSource(boolean update, DatasetType type, String originalDSset, DataSourceUI dataSourceUI, int pageSize) throws ServerSideException{
        SaveDataResponse saveDataResponse = new SaveDataResponse();
        try {
            RepoxManagerEuropeana repoxManagerEuropeana = (RepoxManagerEuropeana)ConfigSingleton.getRepoxContextUtil().getRepoxManager();

            ResponseState urlStatus = Util.getUrlStatus(dataSourceUI);
            if(urlStatus != null){
                saveDataResponse.setResponseState(urlStatus);
                return saveDataResponse;
            }

            // Save metadata transformations
            MetadataTransformationManager metadataTransformationManager = ConfigSingleton.getRepoxContextUtil().getRepoxManager().getMetadataTransformationManager();
            Map<String, MetadataTransformation> metadataTransformations = new HashMap<String, MetadataTransformation>();
            for(TransformationUI transformationUI: dataSourceUI.getMetadataTransformations()) {
                MetadataTransformation loadedTransformation = metadataTransformationManager.loadMetadataTransformation(transformationUI.getIdentifier());
                metadataTransformations.put(transformationUI.getIdentifier(),loadedTransformation);
            }

            // Save external services
            List<ExternalRestService> externalRestServices = LightSaveData.saveExternalServices(dataSourceUI);

            if(update) {
                // Check if the id already exists
                DataSourceContainer dataSourceContainer = repoxManagerEuropeana.getDataManager().getDataSourceContainer(dataSourceUI.getDataSourceSet());
                DataSourceContainer originalDSC = repoxManagerEuropeana.getDataManager().getDataSourceContainer(originalDSset);
                if(dataSourceContainer != null && !originalDSC.getDataSource().getId().equals(dataSourceUI.getDataSourceSet())){
                    saveDataResponse.setResponseState(ResponseState.ALREADY_EXISTS);
                    return saveDataResponse;
                }

                DataSource createdDataSource = null;
                try{
                    if(type == DatasetType.OAI) {
                        createdDataSource = repoxManagerEuropeana.getDataManager().updateDataSourceOai(originalDSset,
                                dataSourceUI.getDataSourceSet(), dataSourceUI.getDescription(),
                                dataSourceUI.getNameCode(), dataSourceUI.getName(), dataSourceUI.getExportDirectory(),
                                dataSourceUI.getSchema(), dataSourceUI.getMetadataNamespace(),
                                dataSourceUI.getSourceMDFormat(), dataSourceUI.getOaiSource(),
                                dataSourceUI.getOaiSet(), metadataTransformations,externalRestServices,
                                dataSourceUI.getMarcFormat(),dataSourceUI.isUseLastUpdateDate());
                    } else if(type == DatasetType.SRU) {
                        createdDataSource = repoxManagerEuropeana.getDataManager().updateDataSourceSruRecordUpdate(originalDSset,
                                dataSourceUI.getDataSourceSet(), dataSourceUI.getDescription(),
                                dataSourceUI.getNameCode(), dataSourceUI.getName(), dataSourceUI.getExportDirectory(),
                                dataSourceUI.getSchema(), dataSourceUI.getMetadataNamespace(),
                                dataSourceUI.getSourceMDFormat(), metadataTransformations,externalRestServices,
                                dataSourceUI.getMarcFormat(),dataSourceUI.isUseLastUpdateDate());
                    } else if(type == DatasetType.FOLDER) {
                        if(dataSourceUI.getRetrieveStartegy().equals("pt.utl.ist.repox.marc.DataSourceFolder")) {
                            Map<String, String> namespaces = new HashMap<String, String>();
                            if(dataSourceUI.getRecordIdPolicy().equals("IdExtracted")) {
                                for(int i=0; i<dataSourceUI.getNamespaceList().size(); i+=2) {
                                    namespaces.put(dataSourceUI.getNamespaceList().get(i),
                                            dataSourceUI.getNamespaceList().get(i+1));
                                }
                            }
                            createdDataSource = repoxManagerEuropeana.getDataManager().updateDataSourceFolder(originalDSset, dataSourceUI.getDataSourceSet(),
                                    dataSourceUI.getDescription(),
                                    dataSourceUI.getNameCode(), dataSourceUI.getName(), dataSourceUI.getExportDirectory(),
                                    dataSourceUI.getSchema(), dataSourceUI.getMetadataNamespace(), dataSourceUI.getSourceMDFormat(),
                                    dataSourceUI.getIsoVariant(), dataSourceUI.getCharacterEncoding(),
                                    dataSourceUI.getRecordIdPolicy(), dataSourceUI.getIdXPath(),
                                    namespaces, dataSourceUI.getRecordRootName(), dataSourceUI.getDirPath(),
                                    metadataTransformations,externalRestServices,dataSourceUI.getMarcFormat(),dataSourceUI.isUseLastUpdateDate());
                        } else if(dataSourceUI.getRetrieveStartegy().equals("pt.utl.ist.repox.ftp.DataSourceFtp")) {
                            // Check FTP connection
                            if(dataSourceUI.getUser() != null && !dataSourceUI.getUser().isEmpty()) {
                                if(!FileUtil.checkFtpServer(dataSourceUI.getServer(),"Normal",dataSourceUI.getFolderPath(),
                                        dataSourceUI.getUser(),dataSourceUI.getPassword())){
                                    saveDataResponse.setResponseState(ResponseState.FTP_CONNECTION_FAILED);
                                    return saveDataResponse;
                                }
                            }

                            Map<String, String> namespaces = new HashMap<String, String>();
                            if(dataSourceUI.getRecordIdPolicy().equals("IdExtracted")) {
                                for(int i=0; i<dataSourceUI.getNamespaceList().size(); i+=2) {
                                    namespaces.put(dataSourceUI.getNamespaceList().get(i),
                                            dataSourceUI.getNamespaceList().get(i+1));
                                }
                            }
                            createdDataSource = repoxManagerEuropeana.getDataManager().updateDataSourceFtp(originalDSset,
                                    dataSourceUI.getDataSourceSet(), dataSourceUI.getDescription(),
                                    dataSourceUI.getNameCode(), dataSourceUI.getName(), dataSourceUI.getExportDirectory(),
                                    dataSourceUI.getSchema(), dataSourceUI.getMetadataNamespace(), dataSourceUI.getSourceMDFormat(),
                                    dataSourceUI.getIsoVariant(), dataSourceUI.getCharacterEncoding(),
                                    dataSourceUI.getRecordIdPolicy(), dataSourceUI.getIdXPath(),
                                    namespaces, dataSourceUI.getRecordRootName(), dataSourceUI.getServer(),
                                    dataSourceUI.getUser(), dataSourceUI.getPassword(), dataSourceUI.getFolderPath(),
                                    metadataTransformations,externalRestServices,dataSourceUI.getMarcFormat(),dataSourceUI.isUseLastUpdateDate());
                        } else if(dataSourceUI.getRetrieveStartegy().equals("pt.utl.ist.repox.ftp.DataSourceHTTP")) {
                            Map<String, String> namespaces = new HashMap<String, String>();
                            if(dataSourceUI.getRecordIdPolicy().equals("IdExtracted")) {
                                for(int i=0; i<dataSourceUI.getNamespaceList().size(); i+=2) {
                                    namespaces.put(dataSourceUI.getNamespaceList().get(i),
                                            dataSourceUI.getNamespaceList().get(i+1));
                                }
                            }
                            createdDataSource = repoxManagerEuropeana.getDataManager().updateDataSourceHttp(originalDSset,
                                    dataSourceUI.getDataSourceSet(), dataSourceUI.getDescription(),
                                    dataSourceUI.getNameCode(), dataSourceUI.getName(), dataSourceUI.getExportDirectory(),
                                    dataSourceUI.getSchema(), dataSourceUI.getMetadataNamespace(), dataSourceUI.getSourceMDFormat(),
                                    dataSourceUI.getIsoVariant(), dataSourceUI.getCharacterEncoding(),
                                    dataSourceUI.getRecordIdPolicy(), dataSourceUI.getIdXPath(),
                                    namespaces, dataSourceUI.getRecordRootName(), dataSourceUI.getHttpURL(),
                                    metadataTransformations,externalRestServices,dataSourceUI.getMarcFormat(),dataSourceUI.isUseLastUpdateDate());
                        }
                    } else if(type == DatasetType.Z39) {
                        // Harvest Method differences
                        if(dataSourceUI.getZ39HarvestMethod().equals("IdSequenceHarvester")) {
                            Map<String, String> namespaces = new HashMap<String, String>();
                            if(dataSourceUI.getRecordIdPolicy().equals("IdExtracted")) {
                                for(int i=0; i<dataSourceUI.getNamespaceList().size(); i+=2) {
                                    namespaces.put(dataSourceUI.getNamespaceList().get(i),
                                            dataSourceUI.getNamespaceList().get(i+1));
                                }
                            }
                            createdDataSource = repoxManagerEuropeana.getDataManager().updateDataSourceZ3950IdSequence(originalDSset,
                                    dataSourceUI.getDataSourceSet(), dataSourceUI.getDescription(),
                                    dataSourceUI.getNameCode(), dataSourceUI.getName(), dataSourceUI.getExportDirectory(),
                                    dataSourceUI.getSchema(), dataSourceUI.getMetadataNamespace(),
                                    dataSourceUI.getZ39Address(), dataSourceUI.getZ39Port(), dataSourceUI.getZ39Database(),
                                    dataSourceUI.getZ39User(), dataSourceUI.getZ39Password(), dataSourceUI.getZ39RecordSyntax(),
                                    dataSourceUI.getCharacterEncoding(), dataSourceUI.getZ39MaximumId(),
                                    dataSourceUI.getRecordIdPolicy(), dataSourceUI.getIdXPath(),
                                    namespaces, metadataTransformations,externalRestServices,dataSourceUI.isUseLastUpdateDate());
                        } else if(dataSourceUI.getZ39HarvestMethod().equals("IdListHarvester")) {
                            // check z3950 file upload
                            File z3950 = null;
                            if(!Z39FileUpload.ignoreUploadFile()) {
                                z3950 = Z39FileUpload.getZ39TempFile();
                                Z39FileUpload.deleteTempFile();
                            }

                            Map<String, String> namespaces = new HashMap<String, String>();
                            if(dataSourceUI.getRecordIdPolicy().equals("IdExtracted")) {
                                for(int i=0; i<dataSourceUI.getNamespaceList().size(); i+=2) {
                                    namespaces.put(dataSourceUI.getNamespaceList().get(i),
                                            dataSourceUI.getNamespaceList().get(i+1));
                                }
                            }
                            createdDataSource = repoxManagerEuropeana.getDataManager().updateDataSourceZ3950IdList(originalDSset,
                                    dataSourceUI.getDataSourceSet(), dataSourceUI.getDescription(),
                                    dataSourceUI.getNameCode(), dataSourceUI.getName(), dataSourceUI.getExportDirectory(),
                                    dataSourceUI.getSchema(), dataSourceUI.getMetadataNamespace(),
                                    dataSourceUI.getZ39Address(), dataSourceUI.getZ39Port(), dataSourceUI.getZ39Database(),
                                    dataSourceUI.getZ39User(), dataSourceUI.getZ39Password(), dataSourceUI.getZ39RecordSyntax(),
                                    dataSourceUI.getCharacterEncoding(), z3950 != null ? z3950.getAbsolutePath() : "",
                                    dataSourceUI.getRecordIdPolicy(), dataSourceUI.getIdXPath(),
                                    namespaces, metadataTransformations,externalRestServices,dataSourceUI.isUseLastUpdateDate());
                        } else if(dataSourceUI.getZ39HarvestMethod().equals("TimestampHarvester")) {
                            Format formatter = new SimpleDateFormat("yyyyMMdd");
                            String earliestDateString = formatter.format(dataSourceUI.getZ39EarlistDate());

                            Map<String, String> namespaces = new HashMap<String, String>();
                            if(dataSourceUI.getRecordIdPolicy().equals("IdExtracted")) {
                                for(int i=0; i<dataSourceUI.getNamespaceList().size(); i+=2) {
                                    namespaces.put(dataSourceUI.getNamespaceList().get(i),
                                            dataSourceUI.getNamespaceList().get(i+1));
                                }
                            }
                            createdDataSource = repoxManagerEuropeana.getDataManager().updateDataSourceZ3950Timestamp(originalDSset,
                                    dataSourceUI.getDataSourceSet(), dataSourceUI.getDescription(),
                                    dataSourceUI.getNameCode(), dataSourceUI.getName(), dataSourceUI.getExportDirectory(),
                                    dataSourceUI.getSchema(), dataSourceUI.getMetadataNamespace(),
                                    dataSourceUI.getZ39Address(), dataSourceUI.getZ39Port(), dataSourceUI.getZ39Database(),
                                    dataSourceUI.getZ39User(), dataSourceUI.getZ39Password(), dataSourceUI.getZ39RecordSyntax(),
                                    dataSourceUI.getCharacterEncoding(), earliestDateString,
                                    dataSourceUI.getRecordIdPolicy(), dataSourceUI.getIdXPath(),
                                    namespaces, metadataTransformations,externalRestServices,dataSourceUI.isUseLastUpdateDate());

                        }
                    }
                    // External Services Run Type
                    if(dataSourceUI.getExternalServicesRunType() != null)
                        createdDataSource.setExternalServicesRunType(
                                ExternalServiceStates.ContainerType.valueOf(dataSourceUI.getExternalServicesRunType()));

                    LightSaveData.replaceExportPathWithUpdatedId(originalDSset,dataSourceUI,createdDataSource);
                    ConfigSingleton.getRepoxContextUtil().getRepoxManager().getDataManager().setDataSetSampleState(dataSourceUI.isSample(),createdDataSource);

                    // Save Tags
                    createdDataSource.getTags().clear();
                    for(DataSetTagUI dataSetTagUI : dataSourceUI.getTags()){
                        createdDataSource.getTags().add(new DataSourceTag(dataSetTagUI.getName()));
                    }

                    ConfigSingleton.getRepoxContextUtil().getRepoxManager().getDataManager().saveData();
                    saveDataResponse.setPage(PagingUtil.getDataPage(createdDataSource.getId(),pageSize));
                    saveDataResponse.setResponseState(ResponseState.SUCCESS);
                } catch (ParseException e) {
                    saveDataResponse.setResponseState(ResponseState.OTHER);
                } catch (ObjectNotFoundException e) {
                    saveDataResponse.setResponseState(ResponseState.NOT_FOUND);
                } catch (InvalidArgumentsException e) {
                    saveDataResponse.setResponseState(ResponseState.INVALID_ARGUMENTS);
                } catch (IncompatibleInstanceException e) {
                    saveDataResponse.setResponseState(ResponseState.INCOMPATIBLE_TYPE);
                }
                return saveDataResponse;
            }
            else {
                // New Data Source
                // Check if the id already exists
                DataSourceContainer dataSourceContainerTest = repoxManagerEuropeana.getDataManager().getDataSourceContainer(dataSourceUI.getDataSourceSet());
                if(dataSourceContainerTest != null){
                    saveDataResponse.setResponseState(ResponseState.ALREADY_EXISTS);
                    return saveDataResponse;
                }

                DataSource createdDataSource = null;
                try {
                    if(type == DatasetType.OAI) {
                        createdDataSource = repoxManagerEuropeana.getDataManager().createDataSourceOai(dataSourceUI.getDataSetParent().getId(),
                                dataSourceUI.getDataSourceSet(), dataSourceUI.getDescription(),
                                dataSourceUI.getNameCode(), dataSourceUI.getName(), dataSourceUI.getExportDirectory(),
                                dataSourceUI.getSchema(), dataSourceUI.getMetadataNamespace(),
                                dataSourceUI.getSourceMDFormat(), dataSourceUI.getOaiSource(),
                                dataSourceUI.getOaiSet(), metadataTransformations,externalRestServices,dataSourceUI.getMarcFormat());
                    }else if(type == DatasetType.SRU) {
                        createdDataSource = repoxManagerEuropeana.getDataManager().createDataSourceSruRecordUpdate(dataSourceUI.getDataSetParent().getId(),
                                dataSourceUI.getDataSourceSet(), dataSourceUI.getDescription(),
                                dataSourceUI.getNameCode(), dataSourceUI.getName(), dataSourceUI.getExportDirectory(),
                                dataSourceUI.getSchema(), dataSourceUI.getMetadataNamespace(),
                                dataSourceUI.getSourceMDFormat(), metadataTransformations,externalRestServices,dataSourceUI.getMarcFormat());
                    } else if(type == DatasetType.FOLDER) {
                        if(dataSourceUI.getRetrieveStartegy().equals("pt.utl.ist.repox.marc.DataSourceFolder")) {
                            Map<String, String> namespaces = new HashMap<String, String>();
                            if(dataSourceUI.getRecordIdPolicy().equals("IdExtracted")) {
                                for(int i=0; i<dataSourceUI.getNamespaceList().size(); i+=2) {
                                    namespaces.put(dataSourceUI.getNamespaceList().get(i),
                                            dataSourceUI.getNamespaceList().get(i+1));
                                }
                            }
                            createdDataSource = repoxManagerEuropeana.getDataManager().createDataSourceFolder(dataSourceUI.getDataSetParent().getId(),
                                    dataSourceUI.getDataSourceSet(), dataSourceUI.getDescription(),
                                    dataSourceUI.getNameCode(), dataSourceUI.getName(), dataSourceUI.getExportDirectory(),
                                    dataSourceUI.getSchema(), dataSourceUI.getMetadataNamespace(), dataSourceUI.getSourceMDFormat(),
                                    dataSourceUI.getIsoVariant(), dataSourceUI.getCharacterEncoding(),
                                    dataSourceUI.getRecordIdPolicy(), dataSourceUI.getIdXPath(),
                                    namespaces, dataSourceUI.getRecordRootName(), dataSourceUI.getDirPath(),
                                    metadataTransformations,externalRestServices,dataSourceUI.getMarcFormat());
                        } else if(dataSourceUI.getRetrieveStartegy().equals("pt.utl.ist.repox.ftp.DataSourceFtp")) {
                            Map<String, String> namespaces = new HashMap<String, String>();
                            if(dataSourceUI.getRecordIdPolicy().equals("IdExtracted")) {
                                for(int i=0; i<dataSourceUI.getNamespaceList().size(); i+=2) {
                                    namespaces.put(dataSourceUI.getNamespaceList().get(i),
                                            dataSourceUI.getNamespaceList().get(i+1));
                                }
                            }
                            // Check FTP connection
                            if(dataSourceUI.getUser() != null && !dataSourceUI.getUser().isEmpty()) {
                                if(!FileUtil.checkFtpServer(dataSourceUI.getServer(), "Normal", dataSourceUI.getFolderPath(),
                                        dataSourceUI.getUser(), dataSourceUI.getPassword())){
                                    saveDataResponse.setResponseState(ResponseState.FTP_CONNECTION_FAILED);
                                    return saveDataResponse;
                                }
                            }

                            createdDataSource = repoxManagerEuropeana.getDataManager().createDataSourceFtp(dataSourceUI.getDataSetParent().getId(),
                                    dataSourceUI.getDataSourceSet(), dataSourceUI.getDescription(),
                                    dataSourceUI.getNameCode(), dataSourceUI.getName(), dataSourceUI.getExportDirectory(),
                                    dataSourceUI.getSchema(), dataSourceUI.getMetadataNamespace(), dataSourceUI.getSourceMDFormat(),
                                    dataSourceUI.getIsoVariant(), dataSourceUI.getCharacterEncoding(),
                                    dataSourceUI.getRecordIdPolicy(), dataSourceUI.getIdXPath(),
                                    namespaces, dataSourceUI.getRecordRootName(), dataSourceUI.getServer(),
                                    dataSourceUI.getUser(), dataSourceUI.getPassword(), dataSourceUI.getFolderPath(),
                                    metadataTransformations,externalRestServices,dataSourceUI.getMarcFormat());
                        } else if(dataSourceUI.getRetrieveStartegy().equals("pt.utl.ist.repox.ftp.DataSourceHTTP")) {
                            Map<String, String> namespaces = new HashMap<String, String>();
                            if(dataSourceUI.getRecordIdPolicy().equals("IdExtracted")) {
                                for(int i=0; i<dataSourceUI.getNamespaceList().size(); i+=2) {
                                    namespaces.put(dataSourceUI.getNamespaceList().get(i),
                                            dataSourceUI.getNamespaceList().get(i+1));
                                }
                            }
                            createdDataSource = repoxManagerEuropeana.getDataManager().createDataSourceHttp(dataSourceUI.getDataSetParent().getId(),
                                    dataSourceUI.getDataSourceSet(), dataSourceUI.getDescription(),
                                    dataSourceUI.getNameCode(), dataSourceUI.getName(), dataSourceUI.getExportDirectory(),
                                    dataSourceUI.getSchema(), dataSourceUI.getMetadataNamespace(), dataSourceUI.getSourceMDFormat(),
                                    dataSourceUI.getIsoVariant(), dataSourceUI.getCharacterEncoding(),
                                    dataSourceUI.getRecordIdPolicy(), dataSourceUI.getIdXPath(),
                                    namespaces, dataSourceUI.getRecordRootName(), dataSourceUI.getHttpURL(),
                                    metadataTransformations,externalRestServices,dataSourceUI.getMarcFormat());
                        }
                    } else if(type == DatasetType.Z39) {
                        // Harvest Method differences
                        if(dataSourceUI.getZ39HarvestMethod().equals("IdSequenceHarvester")) {
                            Map<String, String> namespaces = new HashMap<String, String>();
                            if(dataSourceUI.getRecordIdPolicy().equals("IdExtracted")) {
                                for(int i=0; i<dataSourceUI.getNamespaceList().size(); i+=2) {
                                    namespaces.put(dataSourceUI.getNamespaceList().get(i),
                                            dataSourceUI.getNamespaceList().get(i+1));
                                }
                            }
                            createdDataSource = repoxManagerEuropeana.getDataManager().createDataSourceZ3950IdSequence(dataSourceUI.getDataSetParent().getId(),
                                    dataSourceUI.getDataSourceSet(), dataSourceUI.getDescription(),
                                    dataSourceUI.getNameCode(), dataSourceUI.getName(), dataSourceUI.getExportDirectory(),
                                    dataSourceUI.getSchema(), dataSourceUI.getMetadataNamespace(),
                                    dataSourceUI.getZ39Address(), dataSourceUI.getZ39Port(), dataSourceUI.getZ39Database(),
                                    dataSourceUI.getZ39User(), dataSourceUI.getZ39Password(), dataSourceUI.getZ39RecordSyntax(),
                                    dataSourceUI.getCharacterEncoding(), dataSourceUI.getZ39MaximumId(),
                                    dataSourceUI.getRecordIdPolicy(), dataSourceUI.getIdXPath(),
                                    namespaces, metadataTransformations,externalRestServices);
                        } else if(dataSourceUI.getZ39HarvestMethod().equals("IdListHarvester")) {
                            // check z3950 file upload
                            File z3950 = null;
                            if(!Z39FileUpload.ignoreUploadFile()) {
                                z3950 = Z39FileUpload.getZ39TempFile();
                                Z39FileUpload.deleteTempFile();
                            }

                            Map<String, String> namespaces = new HashMap<String, String>();
                            if(dataSourceUI.getRecordIdPolicy().equals("IdExtracted")) {
                                for(int i=0; i<dataSourceUI.getNamespaceList().size(); i+=2) {
                                    namespaces.put(dataSourceUI.getNamespaceList().get(i),
                                            dataSourceUI.getNamespaceList().get(i+1));
                                }
                            }
                            createdDataSource = repoxManagerEuropeana.getDataManager().createDataSourceZ3950IdList(dataSourceUI.getDataSetParent().getId(),
                                    dataSourceUI.getDataSourceSet(), dataSourceUI.getDescription(),
                                    dataSourceUI.getNameCode(), dataSourceUI.getName(), dataSourceUI.getExportDirectory(),
                                    dataSourceUI.getSchema(), dataSourceUI.getMetadataNamespace(),
                                    dataSourceUI.getZ39Address(), dataSourceUI.getZ39Port(), dataSourceUI.getZ39Database(),
                                    dataSourceUI.getZ39User(), dataSourceUI.getZ39Password(), dataSourceUI.getZ39RecordSyntax(),
                                    dataSourceUI.getCharacterEncoding(), z3950.getAbsolutePath(),
                                    dataSourceUI.getRecordIdPolicy(), dataSourceUI.getIdXPath(),
                                    namespaces, metadataTransformations,externalRestServices);
                        } else if(dataSourceUI.getZ39HarvestMethod().equals("TimestampHarvester")) {
                            Format formatter = new SimpleDateFormat("yyyyMMdd");
                            String earliestDateString = formatter.format(dataSourceUI.getZ39EarlistDate());
                            Map<String, String> namespaces = new HashMap<String, String>();
                            if(dataSourceUI.getRecordIdPolicy().equals("IdExtracted")) {
                                for(int i=0; i<dataSourceUI.getNamespaceList().size(); i+=2) {
                                    namespaces.put(dataSourceUI.getNamespaceList().get(i),
                                            dataSourceUI.getNamespaceList().get(i+1));
                                }
                            }
                            createdDataSource = repoxManagerEuropeana.getDataManager().createDataSourceZ3950Timestamp(dataSourceUI.getDataSetParent().getId(),
                                    dataSourceUI.getDataSourceSet(), dataSourceUI.getDescription(),
                                    dataSourceUI.getNameCode(), dataSourceUI.getName(), dataSourceUI.getExportDirectory(),
                                    dataSourceUI.getSchema(), dataSourceUI.getMetadataNamespace(),
                                    dataSourceUI.getZ39Address(), dataSourceUI.getZ39Port(), dataSourceUI.getZ39Database(),
                                    dataSourceUI.getZ39User(), dataSourceUI.getZ39Password(), dataSourceUI.getZ39RecordSyntax(),
                                    dataSourceUI.getCharacterEncoding(), earliestDateString,
                                    dataSourceUI.getRecordIdPolicy(), dataSourceUI.getIdXPath(),
                                    namespaces, metadataTransformations,externalRestServices);
                        }
                    }
                } catch (SQLException e) {
                    saveDataResponse.setResponseState(ResponseState.ERROR_DATABASE);
                    return saveDataResponse;
                } catch (ObjectNotFoundException e) {
                    saveDataResponse.setResponseState(ResponseState.NOT_FOUND);
                    return saveDataResponse;
                } catch (AlreadyExistsException e) {
                    saveDataResponse.setResponseState(ResponseState.ALREADY_EXISTS);
                    return saveDataResponse;
                } catch (InvalidArgumentsException e) {
                    saveDataResponse.setResponseState(ResponseState.INVALID_ARGUMENTS);
                    return saveDataResponse;
                } catch (ParseException e) {
                    saveDataResponse.setResponseState(ResponseState.OTHER);
                    return saveDataResponse;
                }
                // External Services Run Type
                if(dataSourceUI.getExternalServicesRunType() != null)
                    createdDataSource.setExternalServicesRunType(
                            ExternalServiceStates.ContainerType.valueOf(dataSourceUI.getExternalServicesRunType()));

                createdDataSource.setExportDir(dataSourceUI.getExportDirectory());
                ConfigSingleton.getRepoxContextUtil().getRepoxManager().getDataManager().setDataSetSampleState(dataSourceUI.isSample(),createdDataSource);

                // Save Tags
                createdDataSource.getTags().clear();
                for(DataSetTagUI dataSetTagUI : dataSourceUI.getTags()){
                    createdDataSource.getTags().add(new DataSourceTag(dataSetTagUI.getName()));
                }

                ConfigSingleton.getRepoxContextUtil().getRepoxManager().getDataManager().saveData();
                saveDataResponse.setPage(PagingUtil.getDataPage(createdDataSource.getId(),pageSize));
                saveDataResponse.setResponseState(ResponseState.SUCCESS);
                return saveDataResponse;
            }
        }catch (DocumentException e) {
            saveDataResponse.setResponseState(ResponseState.OTHER);
            return saveDataResponse;
        } catch (IOException e) {
            saveDataResponse.setResponseState(ResponseState.OTHER);
            return saveDataResponse;
        }
    }

    public static SaveDataResponse saveDataProvider(boolean update, DataProviderUI dataProviderUI, int pageSize, String username) throws ServerSideException {

        DataManagerEuropeana europeanaManager = (DataManagerEuropeana)ConfigSingleton.getRepoxContextUtil().getRepoxManager().getDataManager();
        //String dpId;
        String homepage = dataProviderUI.getHomepage();
        URL url = null;
        SaveDataResponse saveDataResponse = new SaveDataResponse();

        String checkUrlResult = DataSetOperationsServiceImpl.checkURL(homepage);
        if(checkUrlResult != null){
            if(checkUrlResult.equals("URL_MALFORMED")){
                saveDataResponse.setResponseState(ResponseState.URL_MALFORMED);
                return saveDataResponse;
            }else if(checkUrlResult.equals("URL_NOT_EXISTS")){
                saveDataResponse.setResponseState(ResponseState.URL_NOT_EXISTS);
                return saveDataResponse;
            }else if(checkUrlResult.equals("SUCCESS"))
                try {
                    url = new URL(homepage);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    log.error(e.getMessage());
                }
        }

        if(update) {
            DataProviderEuropeana dataProvider = (DataProviderEuropeana)europeanaManager.getDataProvider(dataProviderUI.getId());
            if(dataProvider != null) {
                dataProvider.setCountry(dataProviderUI.getCountry());
                dataProvider.setName(dataProviderUI.getName());
                dataProvider.setDescription(dataProviderUI.getDescription());
                dataProvider.setDataSetType(ProviderType.valueOf(dataProviderUI.getType()));
                dataProvider.setNameCode(dataProviderUI.getNameCode());
                dataProvider.setHomePage(url);

                try {
                    dataProvider = (DataProviderEuropeana)europeanaManager.updateDataProvider(dataProvider.getId(), dataProviderUI.getName(),
                            dataProviderUI.getCountry(), dataProviderUI.getDescription(), dataProviderUI.getNameCode(), homepage, dataProviderUI.getType());
                    UserManagementServiceImpl.getInstance().addDPtoUser(username,dataProvider.getId());
                    saveDataResponse.setPage(PagingUtil.getDataPage(dataProvider.getId(), pageSize));
                    saveDataResponse.setResponseState(ResponseState.SUCCESS);
                } catch (ObjectNotFoundException e) {
                    saveDataResponse.setResponseState(ResponseState.NOT_FOUND);
                } catch (InvalidArgumentsException e) {
                    saveDataResponse.setResponseState(ResponseState.INVALID_ARGUMENTS);
                } catch (IOException e) {
                    saveDataResponse.setResponseState(ResponseState.OTHER);
                }

            }
        } else {
            try {
                DataProvider dataProvider = europeanaManager.createDataProvider(dataProviderUI.getParentAggregatorID(), dataProviderUI.getName(),
                        dataProviderUI.getCountry(), dataProviderUI.getDescription(), dataProviderUI.getNameCode(),
                        homepage, dataProviderUI.getType());
                UserManagementServiceImpl.getInstance().addDPtoUser(username,dataProvider.getId());
                saveDataResponse.setPage(PagingUtil.getDataPage(dataProvider.getId(),pageSize));
                saveDataResponse.setResponseState(ResponseState.SUCCESS);
            } catch (ObjectNotFoundException e) {
                saveDataResponse.setResponseState(ResponseState.NOT_FOUND);
            } catch (AlreadyExistsException e) {
                saveDataResponse.setResponseState(ResponseState.ALREADY_EXISTS);
            } catch (IOException e) {
                saveDataResponse.setResponseState(ResponseState.OTHER);
            } catch (InvalidArgumentsException e) {
                saveDataResponse.setResponseState(ResponseState.INVALID_ARGUMENTS);
            }
        }
        return saveDataResponse;
    }

    public static String deleteDataSources(List<DataSourceUI> dataSourceUIs) {
        Iterator<DataSourceUI> dataSourceUIIterator = dataSourceUIs.iterator();
        while (dataSourceUIIterator.hasNext()) {
            RepoxManagerEuropeana repoxManagerEuropeana = (RepoxManagerEuropeana)ConfigSingleton.getRepoxContextUtil().getRepoxManager();
            // todo use result
            try {
                repoxManagerEuropeana.getDataManager().deleteDataSourceContainer(dataSourceUIIterator.next().getDataSourceSet());
            } catch (IOException e) {
                return MessageType.OTHER.name();
            } catch (ObjectNotFoundException e) {
                return MessageType.NOT_FOUND.name();
            }
        }
        return MessageType.OK.name();
    }

    public static void addAllOAIURL(String url,String dataProviderID,String dsSchema,String dsNamespace,
                                    String dsMTDFormat, Map<String,List<String>> map,
                                    String name, String nameCode, String exportPath) {
        try {
            String finalExportPath;
            if(exportPath == null) {
                RepoxManagerEuropeana europeanaManager = (RepoxManagerEuropeana)ConfigSingleton.getRepoxContextUtil().getRepoxManager();
                finalExportPath = europeanaManager.getConfiguration().getExportDefaultFolder();
            } else
                finalExportPath = exportPath;

            List<String> sets = map.get("sets");
            List<String> setNames = map.get("setNames");

            DataManagerEuropeana europeanaManager = (DataManagerEuropeana)ConfigSingleton.getRepoxContextUtil().getRepoxManager().getDataManager();
            DataProviderEuropeana dataProviderEuropeana = (DataProviderEuropeana)europeanaManager.getDataProvider(dataProviderID);

            for (int i=0; i<sets.size(); i++) {
                String setSpec = sets.get(i);
                String setDescription = setNames.get(i);
                String setName = name + "_" + setSpec;
                String setNameCode = nameCode + "_" + setSpec;

                String setId = setSpec.replaceAll("[^a-zA-Z_0-9]", "_");

                DataSourceOai dataSourceOai = new DataSourceOai(dataProviderEuropeana, setId, setDescription,
                        dsSchema, dsNamespace, dsMTDFormat,
                        url, setSpec, new IdProvided(), new TreeMap<String, MetadataTransformation>());

                HashMap<String, DataSourceContainer> oldDataSourceContainers = dataProviderEuropeana.getDataSourceContainers();

                if(oldDataSourceContainers == null) {
                    dataProviderEuropeana.setDataSourceContainers(new HashMap<String, DataSourceContainer>());
                }

                boolean isDuplicate = false;
                if (oldDataSourceContainers != null) {
                    for (DataSourceContainer dataSourceContainer : oldDataSourceContainers.values()) {
                        DataSource oldDataSource = dataSourceContainer.getDataSource();
                        if (oldDataSource instanceof DataSourceOai
                                && ((DataSourceOai) oldDataSource).isSameDataSource(dataSourceOai)) {
                            isDuplicate = true;
                        }
                    }
                }

                if (!isDuplicate) {
                    while (ConfigSingleton.getRepoxContextUtil().getRepoxManager().getDataManager().getDataSourceContainer(dataSourceOai.getId()) != null) {
                        dataSourceOai.setId(dataSourceOai.getId() + "_new");
                    }

                    dataSourceOai.initAccessPoints();

                    DataSourceContainerEuropeana dataSourceContainerE = new DataSourceContainerEuropeana(dataSourceOai,
                            setNameCode,setName,finalExportPath);
                    dataProviderEuropeana.getDataSourceContainers().put(dataSourceOai.getId(),dataSourceContainerE);
                }
            }
            try {
                ConfigSingleton.getRepoxContextUtil().getRepoxManager().getDataManager().updateDataProvider(dataProviderEuropeana, dataProviderEuropeana.getId());
            } catch (ObjectNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            ConfigSingleton.getRepoxContextUtil().getRepoxManager().getAccessPointsManager().initialize(dataProviderEuropeana.getDataSourceContainers());
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

//        System.out.println("Done add all");
    }

    public static String getDirPathFtp(String dataSourceId){
        return ConfigSingleton.getRepoxContextUtil().getRepoxManager().getDataManager().getDirPathFtp(dataSourceId);
    }
}
