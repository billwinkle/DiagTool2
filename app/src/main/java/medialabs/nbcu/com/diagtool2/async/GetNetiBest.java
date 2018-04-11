package medialabs.nbcu.com.diagtool2.async;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import medialabs.nbcu.com.diagtool2.util.HttpUtils;

/**
 * Created by billwinkle on 2/3/17.
 */

public class GetNetiBest extends AsyncTask<Void, Void, String> {
    IGetNetiBestAsyncListner mListner;

    String location;
    String distance;
    String login;
    String pass;
    double lat;
    double lon;

    private final String TAG = "[GetNetiBest LOGGER ";

    public GetNetiBest(IGetNetiBestAsyncListner listner, double llat, double llon) {
        mListner = listner;
        lat = llat;
        lon=  llon;
    }

    @Override
    protected String doInBackground(Void... params) {

        String res="";

        try {
            String apiurl = "getnetibest4.jsp?lat=" + lat + "&lon=" + lon;
            String jres = HttpUtils.requestContent(apiurl);
            JSONArray rawdata = new JSONObject(jres).getJSONArray("data");

            Log.d(TAG, "data len: " + rawdata.length() + " jres: " + jres);
            if (rawdata.length() > 0) {
                JSONObject rdata = rawdata.getJSONObject(0);
                login    = rdata.getString("login");
                pass     = rdata.getString("pass");
                distance = rdata.getString("distance");
                location = rdata.getString("srvlocation");
                res = login + ":" + pass + ":" + distance + ":" + location;
            }

        } catch (Exception e) {
            return res;
        }

        return res;

    }

    @Override
    protected void onPostExecute(final String res) {
        if (mListner != null && res != null) {
            mListner.asyncGetNetiBestComplete(res);
        } else {
            //TODO
        }
    }

}
