package medialabs.nbcu.com.diagtool2.data;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
/**
 * Created by Bill on 1/25/17.
 */


public class TestData {

    private final String TAG = "[TestData] LOGGER ";
    public ArrayList<HashMap> raw = new ArrayList<HashMap>();
    public int hitcount=0;


    public void loadData (JSONArray rawdata)
    {
        if (hitcount!=0)
            raw = new ArrayList<HashMap>();

        hitcount = rawdata.length();
        for (int i=0; i < rawdata.length(); i++) {
            try {
                JSONObject rdata = rawdata.getJSONObject(i);
                HashMap in = new HashMap();
                in.put("streamurl", rdata.getString("streamurl"));
                in.put("serveraddr", rdata.getString("serveraddr"));
                in.put("streamid", rdata.getString("streamid"));
                in.put("user", rdata.getString("user"));
                in.put("location", rdata.getString("location"));
                in.put("playdelay", rdata.getString("playdelay"));
                in.put("ts", rdata.getString("ts"));
                in.put ("channelid",rdata.getString("channelid"));
                in.put ("srvlocation",rdata.getString("srvlocation"));
                raw.add(in);

            } catch (Exception e) {
                Log.d(TAG, "ERROR processing row: " + i + " e:" + e);
            }
        }
    }

}
