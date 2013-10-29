package pt.utl.ist.repox.z3950.test;

import org.dom4j.DocumentException;
import org.jzkit.search.provider.iface.IRQuery;
import org.jzkit.search.provider.iface.SearchException;
import org.jzkit.search.provider.iface.Searchable;
import org.jzkit.search.provider.z3950.SimpleZAuthenticationMethod;
import org.jzkit.search.provider.z3950.Z3950ServiceFactory;
import org.jzkit.search.util.RecordModel.InformationFragment;
import org.jzkit.search.util.ResultSet.IRResultSet;
import org.jzkit.search.util.ResultSet.IRResultSetException;
import org.jzkit.search.util.ResultSet.IRResultSetStatus;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import pt.utl.ist.repox.dataProvider.DataManagerDefault;
import pt.utl.ist.repox.dataProvider.DataProvider;
import pt.utl.ist.repox.dataProvider.DataSourceContainer;
import pt.utl.ist.repox.dataProvider.DataSourceContainerDefault;
import pt.utl.ist.repox.dataProvider.dataSource.IdGenerated;
import pt.utl.ist.repox.marc.CharacterEncoding;
import pt.utl.ist.repox.task.exception.IllegalFileFormatException;
import pt.utl.ist.repox.util.ConfigSingleton;
import pt.utl.ist.repox.z3950.*;
import pt.utl.ist.util.DateUtil;
import pt.utl.ist.util.exceptions.AlreadyExistsException;
import pt.utl.ist.util.exceptions.ObjectNotFoundException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;

public class Test {
    private enum HarvestType { timestamp, idList, idSequence }

    Searchable currentSearchable = null;
    Z3950ServiceFactory factory;
    ApplicationContext appContext;

    private Target target;
    private Date earliestTimestamp;
    private File idListFile;
    private Long maxId;

    public Test(Target target, Date earliestTimestamp, File idListFile, Long maxId) {
        super();
        this.target = target;
        this.earliestTimestamp = earliestTimestamp;
        this.idListFile = idListFile;
        this.maxId = maxId;
    }

    public HarvestMethod getHarvestMethod(HarvestType harvestType) {
        switch (harvestType) {
            case timestamp:
                return new TimestampHarvester(target, earliestTimestamp);
            case idList:
                return new IdListHarvester(target, idListFile);
            case idSequence:
                return new IdSequenceHarvester(target, maxId);

            default:
                throw new RuntimeException("Unknown Harvest Type");
        }
    }

    public DataSourceZ3950 createDummyDataSource(HarvestMethod harvestMethod) throws IOException, DocumentException, SQLException, ClassNotFoundException, NoSuchMethodException, IllegalFileFormatException, ParseException, AlreadyExistsException {

        HashMap<String, DataSourceContainer> dataSourceContainers = new HashMap<String, DataSourceContainer>();

        DataProvider dummyDP = new DataProvider("tempDP", "tempDP", null, "temporary Data Provider - delete", dataSourceContainers);
        DataSourceZ3950 dataSourceZ3950 = new DataSourceZ3950(dummyDP, "tempZ3950", "tempZ3950", "", "", harvestMethod, new IdGenerated(), null);

        dataSourceContainers.put(dataSourceZ3950.getId(), new DataSourceContainerDefault(dataSourceZ3950));

        ((DataManagerDefault)ConfigSingleton.getRepoxContextUtil().getRepoxManagerTest().getDataManager()).addDataProvider(dummyDP);
        dataSourceZ3950.initAccessPoints();
        ConfigSingleton.getRepoxContextUtil().getRepoxManagerTest().getAccessPointsManager().initialize(dummyDP.getDataSourceContainers());

        return dataSourceZ3950;
    }

    private void deleteDummyDataSource(DataSourceZ3950 dataSourceZ3950) throws IOException, DocumentException, ClassNotFoundException, NoSuchMethodException, IllegalFileFormatException, SQLException, ParseException, ObjectNotFoundException {
        DataProvider dataProviderParent = ConfigSingleton.getRepoxContextUtil().getRepoxManager().getDataManager().getDataProviderParent(dataSourceZ3950.getId());
        String dataProviderId = dataProviderParent.getId();
        ConfigSingleton.getRepoxContextUtil().getRepoxManagerTest().getDataManager().deleteDataProvider(dataProviderId);
    }


    public void init() {
        connect();
    }

    protected void connect() {
        appContext = new ClassPathXmlApplicationContext( "ZHarvesterApplicationContext.xml" );

        factory = new Z3950ServiceFactory(target.getAddress(), target.getPort());
        factory.setApplicationContext(appContext);
        factory.setDefaultRecordSyntax(target.getRecordSyntax());
        factory.setDefaultElementSetName("F");
//	        factory.getRecordArchetypes().put("Default","sutrs:Resource:F");
        if(target.getUser() != null && !target.getUser().equals("")) {
            SimpleZAuthenticationMethod auth_method = new SimpleZAuthenticationMethod(3, target.getUser(), null, target.getPassword());
            factory.setAuthMethod(auth_method);
        }

    }

    protected IRResultSet runQuery(String queryString) {
        int queryRetries = 3;

        int tries=0;
        while (tries < queryRetries){
            tries++;

            IRQuery query = new IRQuery();
            query.collections.add(target.getDatabase());
            query.query = new org.jzkit.search.util.QueryModel.PrefixString.PrefixString(queryString);

            IRResultSet result;
            try {
                if(currentSearchable==null) {
                    currentSearchable = factory.newSearchable();
                    currentSearchable.setApplicationContext(appContext);
                }
                result = currentSearchable.evaluate(query);

                result.waitForStatus(IRResultSetStatus.COMPLETE | IRResultSetStatus.FAILURE, 30000);

            } catch (IRResultSetException ex) {
                continue;
            } catch (SearchException ex) {
                currentSearchable = null;
                continue;
            }
            if(result!=null && result.getStatus()!=IRResultSetStatus.FAILURE)
                return result;

            else if(result!=null && result.getStatus()==IRResultSetStatus.FAILURE)
                System.out.println("Search failure: " + result.getResultSetInfo());
            else
                System.out.println("Search failure - uknown error");
        }
        return null;
    }

}
