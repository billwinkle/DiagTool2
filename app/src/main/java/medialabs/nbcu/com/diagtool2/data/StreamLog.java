package medialabs.nbcu.com.diagtool2.data;

/**
 * Created by billwinkle on 3/28/17.
 */

import com.netinsight.sye.framework.model.SyeChannel;
import com.netinsight.sye.framework.model.SyeChannelInfo;
import com.netinsight.sye.framework.model.SyeStats;
import com.netinsight.sye.framework.model.SyeTrack;
import com.netinsight.sye.framework.player.SyeMediaPlayer;
import com.netinsight.sye.framework.player.SyeMediaPlayerEvent;
import com.netinsight.sye.framework.player.SyeSession;
import com.netinsight.sye.framework.player.SyeSessionEvent;
import java.net.URI;
import android.net.Uri;
import android.provider.Settings;

import java.util.List;

import android.util.Log;
import medialabs.nbcu.com.diagtool2.async.*;

public class StreamLog
{
    private static final String TAG = "[StreamLog]LOGGER ";
    static public String serveraddress="";
    static public String location=""; // comes from an async call to getnetidata

    String mAndroidUser="";  // from StreamLocalData
    static double loc_lat;          // from StreamLocalData
    static double loc_lon;          // from StreamLocalData


    /*
       li.setDataElements(streamInfo.getEndpointV4(),mServerAddress,Integer.toString(streamInfo.getmChannelIdNumeric()),proto,user,channel.getChannelId(),
                    srvport,maxabr,vrate,brate,loc_lon,loc_lat,delay, mLocation);
     */
    static public int channelId=-1;
    static public long delay=-1;
    static public int srvport=-1;
    static public int vrate=-1;
    static public int maxabr=-1;
    static public int brate=-1;
    static public String proto="";
    static public String channelid="";
    static public String srvurl="";


    private static int  getMaxBitrateTrackId(List<SyeTrack> trackList) {
        int maxBitrate = 0;
        int trackId = 0;
        if(!trackList.isEmpty()) {
            for(SyeTrack t : trackList) {
                int bitrate = t.getBitrate();
                if(bitrate > maxBitrate) {
                    maxBitrate = bitrate;
                    trackId = t.getId();
                }
            }
        }

        assert(trackId != 0);
        return trackId;
    }

    // example how to use the stream informations
    public static void logStreamInfo(SyeChannel channel, SyeSession syeSession)
    {


        SyeChannelInfo streamInfo = null;
        List<SyeTrack> mTrackList = null;

        if(channel!= null) {
            streamInfo = channel.getChannelInfo();
            mTrackList = channel.getTrackList();

        }

        if(streamInfo != null) {

            Log.d(TAG, "channel:" + channel.getChannelId());

          /*
            URI tmpUrl = null;
            try {
                tmpUrl = new URI(streamInfo.getEndpointV4());
            } catch (Exception e) {
                //e.printStackTrace();
                Log.d(TAG, "logStreamInfo URI ERROR: " + e);
            }

            if(tmpUrl != null) {
                serveraddress = tmpUrl.getHost();
                srvport = tmpUrl.getPort();
                proto = tmpUrl.getScheme();
            }
 */

            delay = streamInfo.getDelay();
            maxabr = getMaxBitrateTrackId(mTrackList);
            vrate = 0;
            brate = 0;
            srvport = 0;

            if(syeSession != null) {
                SyeStats stats = syeSession.GetStats();

                brate = stats.getBitrate();
                vrate = stats.getVideoRate();

            }

            String user = StreamLocalData.user;
            srvurl = streamInfo.getEndpointV4();


            try {
              if (srvurl != null) {
                  String[] info = srvurl.split(":");
                  serveraddress = info[0];
                  srvport = Integer.valueOf(info[1]);
              }
              else
                  srvurl="NOT RETURNED!!!!!!";
            } catch (Exception e) {
                Log.d(TAG, "logStreamInfo SRV ERROR: " + e);
            }

            StreamLocalData.ip = serveraddress;

            loc_lat = StreamLocalData.loc_lat;
            loc_lon = StreamLocalData.loc_lon;
            /*
            logStreamInfo ERROR: java.lang.NullPointerException: Attempt to invoke virtual method 'int java.lang.String.length()' on a null object reference
03-28 12:20:45.055 14818-14818/medialabs.nbcu.com.diagtool2 D/[StreamLog]LOGGER: logStreamInfo Channel: ch2 URL:null address:
03-28 12:20:45.055 14818-14818/medialabs.nbcu.com.diagtool2 D/[StreamLog]LOGGER: logStreamInfo ID: 0 Proto:
03-28 12:20:45.055 14818-14818/medialabs.nbcu.com.diagtool2 D/[StreamLog]LOGGER: logStreamInfo delay: 0 maxabr: 0
03-28 12:20:45.055 14818-14818/medialabs.nbcu.com.diagtool2 D/[StreamLog]LOGGER: logStreamInfo user: 4d88701a544a0724 Server Port: 0
03-28 12:20:45.055 14818-15062/medialabs.nbcu.com.diagtool2 D/[GetNetiLoc] LOGGER: Found ip address:
             */

            Log.d(TAG, "logStreamInfo Channel: " + channel.getChannelId() + " URL:" + srvurl + " address:" + serveraddress);
            Log.d(TAG, "logStreamInfo ID: " + streamInfo.getmChannelIdNumeric() + " Proto: " + proto);
            Log.d(TAG, "logStreamInfo delay: " + delay + " maxabr: " + maxabr);
            Log.d(TAG, "logStreamInfo user: " + user + " Server Port: " + srvport);

            channelid=channel.getChannelId();
            channelId=streamInfo.getmChannelIdNumeric();


        }
    }

}
