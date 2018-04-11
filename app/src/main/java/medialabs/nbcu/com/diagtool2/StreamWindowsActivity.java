package medialabs.nbcu.com.diagtool2;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by billwinkle on 2/10/17.
 */

//public class StreamWindowsActivity extends AppCompatActivity
  // implements GestureDetector.OnGestureListener, View.OnKeyListener,
  //     GestureDetector.OnDoubleTapListener

public class StreamWindowsActivity
        extends AppCompatActivity
    implements StreamWindowFragment.StreamWindowFragmentListner

{

    private final String TAG = "[StreamWidowsA]LOGGER";
    protected static Context mAppContext;
    HashMap<String,Double> windows = new HashMap<String,Double>();
    private GestureDetectorCompat gDetector;

    StreamWindowFragment window_main;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stream_windows2);


        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        Log.d(TAG, " DISPLAY  height:: " + dpHeight + " Width: " + dpWidth);

        mAppContext = getApplicationContext();

       // this.gDetector = new GestureDetectorCompat(this,this);
       // gDetector.setOnDoubleTapListener(this);
    try {
        StreamWindowFragment window1 =
                (StreamWindowFragment)
                        getSupportFragmentManager().findFragmentById(R.id.stream1_window);


        window1.setChannel(0);
        window1.setPlayerName("WindowPlayer1");
        window1.setChannelName("MSNBC");

        StreamWindowFragment window2 =
                (StreamWindowFragment)
                        getSupportFragmentManager().findFragmentById(R.id.stream2_window);

        window2.setChannel(1);
        window2.setPlayerName("WindowPlayer2");
        window2.setChannelName("CNBC");


        StreamWindowFragment window3 =
                (StreamWindowFragment)
                        getSupportFragmentManager().findFragmentById(R.id.stream3_window);


        window3.setChannel(3);
        window3.setPlayerName("WindowPlayer3");
        window3.setChannelName("NBCSN");


        StreamWindowFragment window4 =
                (StreamWindowFragment)
                        getSupportFragmentManager().findFragmentById(R.id.stream4_window);


        window4.setChannel(2);
        window4.setPlayerName("WindowPlayer4");
        window4.setChannelName("WNBC");


        window_main =
                (StreamWindowFragment)
                        getSupportFragmentManager().findFragmentById(R.id.stream_main);

        window_main.setChannel(1);
        window_main.setChannelName("CNBC");


        //height:: 552.0 Width: 960.0

        Double h = (int) dpHeight * 1.26;
        int h1 = h.intValue();

        Double w = (int) dpWidth * 1.17;
        int w1 = w.intValue();

        if (dpHeight < 400) {
            h1 = 900;
            w1 = 1400;
        }
        //window_main.setMainWindow(h1,w1);
        window_main.setMainWindow(800, 1200);
        window_main.setAudioUnMute();

        window_main.setPlayerName("WindowMain");
    } catch (Exception e) {
        Log.d(TAG, "StreamWindowsActivity -fault: " + e);
    }

    }


    /*
    StreamSelected

     */
    @Override
    public void onVideoSelected(int mCurrentChannel, String mPlayerName, String chanName)
    {
        String msg = "SWITCH MAIN " + mCurrentChannel + " player: " + mPlayerName + " chan:" + chanName;
        Log.d(TAG, msg);
        window_main.setChannelName(chanName);
        window_main.changeChannel(mCurrentChannel);

    }



}