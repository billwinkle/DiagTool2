package medialabs.nbcu.com.diagtool2.async;

import android.net.Uri;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;
import medialabs.nbcu.com.diagtool2.util.HttpUtils;

/**
 * Created by Bill on 1/24/17.
 */

public class AddLogInfoTask extends AsyncTask<Void, Void, JSONArray> {
    IAsyncListner mListner;
    String mStreamURL="";
    String mServerAddr="";
    String mStreamId="";
    String mProto="";
    String mUser="";
    String mBitRateList="";
    int mPort=0;
    int mMaxAbr=0;
    double mFrameRate=0;
    double mBitRate=0;
    double mLocLong=0;
    double mLocLat=0;
    double mPlayDelay=0;
    String mLocation="";

    public AddLogInfoTask()
    {


    }

    public void setAsyncListener (IAsyncListner listner)
    {
        mListner = listner;
    }

    public void setDataElements(String url, String addr, String id, String proto,
                                String user, String ratelist, int port, int max, double frate,
                                double brate, double lng, double lat, double delay, String loc )
    {

        mStreamURL =url;
        mServerAddr=addr;
        mStreamId=id;
        mProto=proto;
        mUser=user;
        mBitRateList=ratelist;
        mPort=port;
        mMaxAbr=max;
        mFrameRate=frate;
        mBitRate=brate;
        mLocLong=lng;
        mLocLat=lat;
        mPlayDelay=delay;
        mLocation=loc;

    }



    public String getLogString ()
    {
        String param = "";
        param = mStreamURL +"|" + mServerAddr +"|" + mStreamId +"|" +mProto +"|"
                + mUser +"|" + mBitRateList +"|" + mPort +"|" + mMaxAbr +"|"
                + mFrameRate +"|" +mBitRate + "|" + mLocLong + "|" +mLocLat +"|"
                + mPlayDelay + "|" + mServerAddr + "|" + mLocation;


        return param;
    }
    /*
    create table NetiTest (
          streamurl    varchar (225) not null,
          serveraddr    varchar (125) not null,
          serverport   int,
          streamid     varchar (50) not null,
          proto        varchar (50) not null,
          user         varchar (50) not null,
          location     varchar (100) ,
          loc_long     double,
          loc_lat      double,
          playdelay    double,
          maxabr       int,
          framerate    double,
          bitrate      double,
          bitratelist  varchar(100),
          ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP
  );

     */


    @Override
    protected JSONArray doInBackground(Void... params) {
        try {
            String p= Uri.encode(getLogString());
            String apiurl = "addnetitest.jsp?param=" +p;
            String res = HttpUtils.requestContent(apiurl);
            return new JSONObject(res).getJSONArray("appi");
        } catch (Exception e) {
            return null;
        }

    }

    @Override
    protected void onPostExecute(final JSONArray res) {
        if (mListner != null && res != null) {
            mListner.asyncComplete();
        } else {
            //TODO
        }
    }

}


