package medialabs.nbcu.com.diagtool2.async;

import org.json.JSONArray;

/**
 * Created by Bill on 1/25/17.
 */

public interface IGetNetiDataAsyncListner {
    public void asyncGetNetiDataComplete(JSONArray res);
}
