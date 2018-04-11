package medialabs.nbcu.com.diagtool2.data;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Bill on 1/25/17.
 */

public class StreamLocalData
{
      public static String user="";
      public static String login="";
      public static String pass;
      public static String distance;
      public static String location="";
      public static String ip="";
      public static double loc_lat=0;
      public static double loc_lon=0;
      public static int selected_location=0;
    /**
     *  0 - GPS
     *  1- Philadelphoa
     *  2- Seattle
     *  3- LA
     *  4- Round Robin
     */
      public static String[] city = {"GPS","Philadelphia","Seattle","Los Angeles","Cleveland"};
    // public static String controlip="http://65.51.185.114:82";
      // AWS
     // public static String controlip="http://65.51.185.114:83";

    //"http://52.14.195.244"
   // OHLOADBALANCE-1440028145.us-east-2.elb.amazonaws.com
    //public static String controlip="http://52.14.195.244";
      public static String controlip="http://OHLOADBALANCE-1440028145.us-east-2.elb.amazonaws.com";

      public static Double[] selected_lat ={0.0,39.9544294,47.6062,34.0522,41.4993,0.0};
      public static Double[] selected_lon ={0.0,-75.1685377,-122.3321,-118.2437,-81.6944,0.0};

      public static StreamChannel[] streams = new StreamChannel[4];

     private static SharedPreferences prefs;
     private static  AppCompatActivity prefapp;


    static public void  setPref (AppCompatActivity x)
    {
        prefapp=x;
        prefs=PreferenceManager.getDefaultSharedPreferences(x);
        initStreamChannel();
    }

      static public void  initStreamChannel ()
      {
          for (int i=0; i<streams.length; i++)
          {
              streams[i] = new StreamChannel();
              streams[i].channelid=i;
              switch (i) {
                  case 0:
                      streams[i].channelName="MSNBC";
                      streams[i].streamdelay= prefs.getInt("msnbc_delay_value", 0);
                      break;
                  case 1:
                      streams[i].channelName="CNBC";
                      streams[i].streamdelay= prefs.getInt("cnbc_delay_value", 0);
                      break;
                  case 2:
                      streams[i].channelName="WNBC";
                      streams[i].streamdelay= prefs.getInt("wnbc_delay_value", 0);
                      break;
                  case 3:
                      streams[i].channelName="NBCSN";
                      streams[i].streamdelay= prefs.getInt("nbcsn_delay_value", 0);
                      break;
               }
               streams[i].streamdelay=0;

          }
      }

      static public String getChName(int i)
      {
          return streams[i].channelName;
      }

    static public int  getDelay (int id)
      {
          String delayname = streams[id].channelName + "_delay_value";
          return prefs.getInt(delayname, 0);
      }

    static public void setStreamDelay (int id, int delay)
      {
          String delayname = streams[id].channelName + "_delay_value";
          streams[id].streamdelay=delay;
          SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(prefapp);
          SharedPreferences.Editor editor = prefs.edit();
          editor.putInt(delayname, delay);
          editor.commit();

      }

}
