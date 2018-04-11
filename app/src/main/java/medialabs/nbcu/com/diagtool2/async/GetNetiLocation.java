package medialabs.nbcu.com.diagtool2.async;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import medialabs.nbcu.com.diagtool2.data.StreamLocalData;
import medialabs.nbcu.com.diagtool2.util.HttpUtils;

/**
 * Created by Bill on 1/31/17.
 */

public class GetNetiLocation extends AsyncTask<Void, Void, String> {
    IGetNetiLocAsyncListner mListner;
    String mIp;
    private final String TAG = "[GetNetiLoc] LOGGER ";

    public GetNetiLocation(IGetNetiLocAsyncListner listner, String ip) {
        mListner = listner;
        mIp = ip;
    }

    @Override
    protected String doInBackground(Void... params) {

        String res="";
        if (!StreamLocalData.user.equals("")) {
            mIp = StreamLocalData.ip;
            Log.d(TAG, "Found ip address: " + mIp);
        }

        try {
            String apiurl = "getnetiloc.jsp?ip=" + mIp;
            String jres = HttpUtils.requestContent(apiurl);
            JSONArray rawdata = new JSONObject(jres).getJSONArray("data");

            Log.d(TAG, "data len: " + rawdata.length() + " jres: " + jres);
            if (rawdata.length() > 0) {
                JSONObject rdata = rawdata.getJSONObject(0);
                res = rdata.getString("srvlocation");
            }

        } catch (Exception e) {
            return res;
        }

        return res;

    }

    @Override
    protected void onPostExecute(final String res) {
        if (mListner != null && res != null) {
            mListner.asyncGetNetiLocComplete(res);
        } else {
            //TODO
        }
    }

}
