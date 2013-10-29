package harvesterUI.server.util;

import harvesterUI.server.RepoxServiceImpl;

/**
 * Created to REPOX project.
 * User: Edmundo
 * Date: 23/04/12
 * Time: 17:11
 */
public class PagingUtil {

    public static int getDataPage(String id, int pageSize){
        return RepoxServiceImpl.getProjectManager().getDataPage(id, pageSize);
    }

}
