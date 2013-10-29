package harvesterUI.client.panels.overviewGrid;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridView;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import harvesterUI.client.HarvesterUI;
import harvesterUI.client.panels.browse.BrowseFilterPanel;
import harvesterUI.client.panels.dataSourceView.DataSetViewInfo;
import harvesterUI.client.panels.grid.DataGridContainer;
import harvesterUI.client.panels.overviewGrid.columnRenderes.*;
import harvesterUI.client.panels.overviewGrid.contextMenus.OverviewGridContextMenus;
import harvesterUI.client.panels.overviewGrid.tree.MainGridTree;
import harvesterUI.client.servlets.dataManagement.DataManagementServiceAsync;
import harvesterUI.client.servlets.dataManagement.FilterServiceAsync;
import harvesterUI.client.util.ServerExceptionDialog;
import harvesterUI.client.util.UtilManager;
import harvesterUI.shared.ProjectType;
import harvesterUI.shared.dataTypes.AggregatorUI;
import harvesterUI.shared.dataTypes.DataContainer;
import harvesterUI.shared.dataTypes.DataProviderUI;
import harvesterUI.shared.dataTypes.dataSet.DataSourceUI;
import harvesterUI.shared.filters.FilterQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created to REPOX Project.
 * User: Edmundo
 * Date: 23-03-2012
 * Time: 15:16
 */
public class MainGrid extends DataGridContainer<DataContainer> {

    private ContentPanel mainGridPanel;
    private CheckBoxSelectionModel mainGridSelectionModel;
    private TreeStore<ModelData> store;
    private DataManagementServiceAsync dataManagementService;

    private BrowseFilterPanel browseFilterPanel;

    private ContentPanel viewDsInfoPanel;
    private ColumnConfig countryColumn;
    private BorderLayout mainBorderLayout;
    private MainGridTopToolbar topToolbar;

    private int selectedItemIndex = -1;

    public MainGrid() {
        dataManagementService = (DataManagementServiceAsync) Registry.get(HarvesterUI.DATA_MANAGEMENT_SERVICE);

        createMainPanel();
        createMainGrid();
//        createPagingToolbar();
//        createTopToolbar();
        createFiltersPanel();
    }

    private void createMainGrid(){
        store = new TreeStore<ModelData>();

        mainDataGrid = new MainGridTree();

        BorderLayoutData data = new BorderLayoutData(Style.LayoutRegion.CENTER);
        mainGridPanel.add(mainDataGrid, data);
    }

    @Override
    public void loadGridData(PagingLoadConfig config){
        List<FilterQuery> filterQueries = browseFilterPanel.getAllQueries();

        UtilManager.maskCentralPanel(HarvesterUI.CONSTANTS.loadingMainData());
//        getPagingToolBar().showRefreshIconRunning(true);
        AsyncCallback<DataContainer> callback = new AsyncCallback<DataContainer>() {
            public void onFailure(Throwable caught) {
                new ServerExceptionDialog("Failed to get response from server",caught.getMessage()).show();
            }
            public void onSuccess(DataContainer mainDataParent) {
                store.removeAll();
                store.add(mainDataParent.getChildren(),true);

                // TODO Load
//                mainDataGrid.expandAll();

                selectPreviouslySelectedItem();
                resetScrollBarPos();

                mainDataGrid.getDataProvider().getList().add(mainDataParent);

//                getPagingToolBar().loadPagingInfo();
//                getPagingToolBar().showRefreshIconRunning(false);
                UtilManager.unmaskCentralPanel();
            }
        };
        FilterServiceAsync service = (FilterServiceAsync) Registry.get(HarvesterUI.FILTER_SERVICE);
        service.getFilteredData(filterQueries,config.getOffset(),config.getLimit(),HarvesterUI.UTIL_MANAGER.getLoggedUserName(), callback);
    }

    public void refreshGrid(){
//        pagingToolBar.refresh();
    }

    private void createPagingToolbar(){
        RpcProxy<PagingLoadResult<DataContainer>> proxy = new RpcProxy<PagingLoadResult<DataContainer>>() {
            @Override
            public void load(Object loadConfig, AsyncCallback<PagingLoadResult<DataContainer>> callback) {
                PagingLoadConfig pagingLoadConfig = (PagingLoadConfig) loadConfig;
                pagingLoadConfig.set("isFiltered",browseFilterPanel.isFilterApplied());
                dataManagementService.getPagingData((PagingLoadConfig) loadConfig,callback);
            }
        };

        PagingLoader<PagingLoadResult<DataProviderUI>> loader = new BasePagingLoader<PagingLoadResult<DataProviderUI>>(proxy);
        loader.setRemoteSort(true);
    }

    private void createMainPanel(){
        mainGridPanel = new ContentPanel(){
            @Override
            protected void onResize(int width, int height) {
                super.onResize(width,height);
//                mainGridPanel.layout(true);
//                mainDataGrid.repaint();
//                mainDataGrid.getView().refresh(false);
            }
        };

        mainBorderLayout = new BorderLayout();
        mainGridPanel.setLayout(mainBorderLayout);
        mainGridPanel.setHeaderVisible(false);

        browseFilterPanel = (BrowseFilterPanel) Registry.get("browseFilterPanel");

        createViewDSInfoPanel();
    }

    private void createViewDSInfoPanel(){
        BorderLayoutData data = new BorderLayoutData(Style.LayoutRegion.EAST, 475, 100, 750);
        data.setMargins(new Margins(1, 1, 1, 5));
        data.setCollapsible(true);

        viewDsInfoPanel = new ContentPanel();
        viewDsInfoPanel.setLayout(new CenterLayout());
        viewDsInfoPanel.setLayoutOnChange(true);
//        filterPanel.setAnimCollapse(true);
        viewDsInfoPanel.setId("eastPanel");
        viewDsInfoPanel.setBodyBorder(false);
        viewDsInfoPanel.setIcon(HarvesterUI.ICONS.view_info_icon());
        viewDsInfoPanel.setHeading(HarvesterUI.CONSTANTS.viewDataSetInformation());
        LabelToolItem noDsSelectedLabel = new LabelToolItem(HarvesterUI.CONSTANTS.noDataSetSelected());
        noDsSelectedLabel.setStyleName("noDataSetSelected");
        viewDsInfoPanel.add(noDsSelectedLabel);

        mainBorderLayout.addListener(Events.Expand, new Listener<BorderLayoutEvent>() {
            @Override
            public void handleEvent(BorderLayoutEvent be) {
                if (be.getRegion().equals(Style.LayoutRegion.EAST))
                    setOnExpandDataSet();
            }
        });

        mainGridPanel.add(viewDsInfoPanel, data);
    }

    private void createTopToolbar(){
        topToolbar = new MainGridTopToolbar(this);
        mainGridPanel.setTopComponent(topToolbar);
    }

    private void createFiltersPanel(){
        BorderLayoutData data = new BorderLayoutData(Style.LayoutRegion.WEST, 300, 300, 350);
        data.setMargins(new Margins(1, 5, 1, 1));
        data.setCollapsible(true);

        mainGridPanel.add(browseFilterPanel, data);
    }


    /*********************************************************
                View Info Side Panel Functions
     **********************************************************/
    private void editPanelInfo(DataSourceUI dataSourceUI){
        // Only do when visible to enhance performance
        if(viewDsInfoPanel.isExpanded()){
            DataSetViewInfo dataSetViewInfo = new DataSetViewInfo();
            dataSetViewInfo.createForm(dataSourceUI);
            viewDsInfoPanel.removeAll();
            viewDsInfoPanel.setLayout(new FitLayout());
            viewDsInfoPanel.add(dataSetViewInfo);
            viewDsInfoPanel.setHeading(dataSetViewInfo.getHeading());
        }
    }

    private void emptyDataSetPanel(){
        // Only do when visible to enhance performance
        if(viewDsInfoPanel.isExpanded()){
            viewDsInfoPanel.removeAll();
            LabelToolItem noDsSelectedLabel = new LabelToolItem(HarvesterUI.CONSTANTS.noDataSetSelected());
            noDsSelectedLabel.setStyleName("noDataSetSelected");
            viewDsInfoPanel.setLayout(new CenterLayout());
            viewDsInfoPanel.setHeading(HarvesterUI.CONSTANTS.viewDataSetInformation());
            viewDsInfoPanel.add(noDsSelectedLabel);
        }
    }

    private void setOnExpandDataSet(){
        // TODO
//        if(mainDataGrid.getSelectionModel().getSelectedItem() != null){
//            selectedItemIndex = mainDataGrid.getStore().indexOf(mainDataGrid.getSelectionModel().getSelectedItem());
//            if(mainDataGrid.getSelectionModel().getSelectedItem() instanceof DataProviderUI) {
//                DataProviderUI dataProviderUI = (DataProviderUI) mainDataGrid.getSelectionModel().getSelectedItem();
//                if(dataProviderUI.getDataSourceUIList().size() == 1)
//                    editPanelInfo(dataProviderUI.getDataSourceUIList().get(0));
//            } else if(mainDataGrid.getSelectionModel().getSelectedItem() instanceof DataSourceUI)
//                editPanelInfo((DataSourceUI)mainDataGrid.getSelectionModel().getSelectedItem());
//        }
    }

    /*********************************************************
                        Scrolling Functions
     **********************************************************/
    private void selectPreviouslySelectedItem() {
        if(selectedItemIndex < store.getModels().size() && selectedItemIndex >= 0)
            mainGridSelectionModel.select(selectedItemIndex, true);
    }

    public void resetScrollBarPos(){
        try{
//            mainDataGrid.getView().getScroller().setScrollTop(scrollBarY);
        }catch (NullPointerException e){

        }
    }

    /*********************************************************
                            Public Functions
     **********************************************************/

    public ContentPanel getMainGridPanel() {
        return mainGridPanel;
    }

//    public TreeGrid<DataContainer> getMainDataGrid() {
//        return mainDataGrid;
//    }

//    public MyPagingToolBar getPagingToolBar() {
//        return pagingToolBar;
//    }

    public TreeStore<ModelData> getStore() {
        return store;
    }

    public BrowseFilterPanel getBrowseFilterPanel() {
        return browseFilterPanel;
    }

    public MainGridTopToolbar getTopToolbar() {
        return topToolbar;
    }
}
