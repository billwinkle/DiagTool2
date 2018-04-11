package medialabs.nbcu.com.diagtool2.async;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import medialabs.nbcu.com.diagtool2.data.StreamLocalData;
import medialabs.nbcu.com.diagtool2.util.HttpUtils;

/**
 * Created by Bill on 1/25/17.
 */

public class GetNetiData extends AsyncTask<Void, Void, JSONArray> {
    IGetNetiDataAsyncListner mListner;
    String mUser;
    private final String TAG = "[GetNetiData] LOGGER ";

    public GetNetiData(IGetNetiDataAsyncListner listner, String user) {
        mListner = listner;
       // mUser = "b4c6deafae11cb1c";
        mUser = user;
    }

    @Override
    protected JSONArray doInBackground(Void... params) {
        JSONArray defres = new JSONArray();  // empty json array

        if (!StreamLocalData.user.equals("")) {
            mUser = StreamLocalData.user;
            Log.d(TAG, "Found user: " + mUser);
        }

        try {
            String apiurl = "getnetitest.jsp?user=" + mUser;
            String res = HttpUtils.requestContent(apiurl);
            return new JSONObject(res).getJSONArray("data");

        } catch (Exception e) {
            return defres;
        }

    }

    @Override
    protected void onPostExecute(final JSONArray res) {
        if (mListner != null && res != null) {
            mListner.asyncGetNetiDataComplete(res);
        } else {
            //TODO
        }
    }

}
