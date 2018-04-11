package medialabs.nbcu.com.diagtool2;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

/*
import com.netinsight.redstone.rsframework.model.RSChannel;
import com.netinsight.redstone.rsframework.model.RSStats;
import com.netinsight.redstone.rsframework.model.RSStream;
import com.netinsight.redstone.rsframework.player.RSMediaPlayer;
import com.netinsight.redstone.rsframework.player.RSMediaPlayerEvent;
*/

import com.netinsight.sye.framework.model.SyeChannel;
import com.netinsight.sye.framework.model.SyeChannelInfo;
import com.netinsight.sye.framework.model.SyeStats;
import com.netinsight.sye.framework.model.SyeTrack;
import com.netinsight.sye.framework.player.SyeMediaPlayer;
import com.netinsight.sye.framework.player.SyeMediaPlayerEvent;
import com.netinsight.sye.framework.player.SyeSession;
import com.netinsight.sye.framework.player.SyeSessionEvent;

import java.util.ArrayList;
import java.util.List;

import medialabs.nbcu.com.diagtool2.data.StreamLocalData;

/**
 * Created by billwinkle on 2/10/17.
 */

/*
public class StreamWindowFragment
        extends Fragment
        implements GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener,
        TextureView.SurfaceTextureListener,RSMediaPlayerEvent
*/
/*
public class StreamWindowFragment
        extends Fragment
        implements
        View.OnTouchListener, View.OnKeyListener,
        TextureView.SurfaceTextureListener,RSMediaPlayerEvent
*/
public class StreamWindowFragment
        extends Fragment
        implements
        TextureView.SurfaceTextureListener, SyeMediaPlayerEvent, SyeSessionEvent
{

   // private static TextureView streamwindow;

    private final String TAG = "[StreamWinFrag]LOGGER";

    private TextureView mTextureView;
    //private RelativeLayout mainView;
   // private ProgressDialog progress;
    protected static Context mAppContext;
    private GestureDetectorCompat gDetector;

    // SDK interface
    //private SyeMediaPlayer rsMediaPlayer;
    //public ArrayList<SyeChannel> mRsChannels;
    private SyeMediaPlayer syeMediaPlayer;
    private SyeSession syeSession;
    public ArrayList<SyeChannel> mSyeChannels;

    // settings for the RSMediaPlayer
    private String mBaseUrl;

    // Touch support
    float mLastScrollingX;
    float mStartScrollingX;

    // surface scaling
    float scaleW;
    float scaleH;
    float panHeight;

    int mainWidth;
    int mainHeight;

    int minBRate=100;
    int maxBRate=300;

    // Channels
    int mCurrentChannel;
    String serverAddress="";
    String mPlayerName="FragPlayer";
    String mChannelName="";

    boolean channelSet=false;
    boolean audioMuted=true;
    boolean isMainWindow=false;

    StreamWindowFragmentListner mListner;

    SyeChannel channel = (SyeChannel) null;


    public void setChannelName(String n)
    {
        mChannelName=n;
        Log.i(TAG, "CHANNEL NAME: " +mChannelName);
    }

    public void setChannel (int c)
    {
        Log.i(TAG, "setChannel " + c);
        mCurrentChannel=c;
        channelSet=true;

    }

    public void setAudioUnMute ()
    {
        audioMuted=false;
    }

    public void setPlayerName (String p)
    {
        mPlayerName=p;
    }


    public interface StreamWindowFragmentListner {
        void onVideoSelected(int mCurrentChannel, String mPlayerName, String chanName);
    }


    public void setMainWindow(int height, int width)
    {
        isMainWindow=true;
        mainHeight = height;
        mainWidth  = width;

        Log.i(TAG, "setMainWindow:  w: " + mainWidth + " h: " + mainHeight);
        mTextureView.setLayoutParams(new LinearLayout.LayoutParams(mainWidth,mainHeight));
    }

    public void setAudio ()
    {
       // if (audioMuted)
            //rsMediaPlayer.setVolume(0);
//com.netinsight.sye.framework.player.SyeSession.setVolume(java.lang.String, int)
      //  else
          //  rsMediaPlayer.setVolume(100);
        /** TODO **/
        if (!isMainWindow) {
            syeSession.setMaxVideoBitrate(maxBRate);
            syeSession.setMinVideoBitrate(minBRate);
            syeSession.setStartVideoBitrate(minBRate);
        }

        if (audioMuted)
            syeSession.setVolume("audio_renderer", 0);
        else
            syeSession.setVolume("audio_renderer", 100);
    }


    // initiate settings for the RSMediaPlayer
    private void SyeInitSettings() {

        // feel the base URL here
        //mBaseUrl = "http://demo.neti.systems:3001";
        // mBaseUrl = "http://65.51.185.114:82";

        mBaseUrl = StreamLocalData.controlip;


    }



    @Override
    public void onAttach(Context context) {
        mAppContext = context;
        mListner = (StreamWindowFragmentListner) context;

        if (context instanceof StreamWindowFragmentListner) {
            mListner = (StreamWindowFragmentListner) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement StreamWindowFragmentListner");
        }
       // this.gDetector = new GestureDetectorCompat(mAppContext,this);
       // gDetector.setOnDoubleTapListener(this);
        super.onAttach(context);
        Log.i(TAG, "Attached context -   " + this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stream_fragment,
                container, false);

        mTextureView = (TextureView) view.findViewById(R.id.stream_window);


        SyeInitSettings();



        mTextureView.setOpaque(false);
        mTextureView.setSurfaceTextureListener(this);


        mTextureView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {

                Log.d(TAG, "GOT onTouch in window: " + mPlayerName);


                        CharSequence text = "Motion on player: "  + mPlayerName + " channel: " + mCurrentChannel;
                        //CharSequence text = "Got Touch on window ";
                        Log.d(TAG, String.valueOf(text));

                        int duration = Toast.LENGTH_SHORT;

                     //   Toast toast = Toast.makeText(mAppContext, text, duration);
                    //    toast.show();

                  mListner.onVideoSelected(mCurrentChannel,mPlayerName,mChannelName);

                return false;
            }
        });



        return view;
    }

    public void changeChannel (int chid)
    {
        setAudio ();
        //rsMediaPlayer.startPlay(channel);

       // channel = mRsChannels.get(chid);
        //rsMediaPlayer.startPlay(channel);
        SyeChannel channel = mSyeChannels.get(chid);

       // CharSequence text = "Channel_" + channel.getChannelId() + " " + channel.getChannelEpgId();
        CharSequence text = "Viewing " + mChannelName;
        Toast.makeText(mAppContext, text, Toast.LENGTH_SHORT).show();



        boolean ret = syeSession.prepare(channel);
        assert(ret == true);
       // return;
    }

    /*
    private void startPlayChannel(RSChannel channel) {
        Log.i(TAG, "startPlayChannel");
        // optional: rsMediaPlayer.startPlay(channel);
        //rsMediaPlayer.startPlay(channel.getChannelId());
        //rsMediaPlayer.setVolume(0);
        setAudio ();
        rsMediaPlayer.startPlay(channel);
    }
*/



    //////////////////////////////// RSMediaPlayer callback///////////////////////////////////////

    private void playbackStarted(String instanceName)
    {
        Log.d(TAG, "SyeMediaPlayerEvent playbackStarted for player=" + instanceName);
        setAudio ();

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                try {
                    mTextureView.setVisibility(View.VISIBLE);
                    Matrix m1 = new Matrix();
                    m1.postScale(scaleW, scaleH);
                    m1.postTranslate(0, panHeight);

                    Log.d(TAG, "Transla scaleW=" + scaleW + " scaleH=" + scaleH);
                    mTextureView.setTransform(m1);
                }catch (Exception e) {
                    Log.d(TAG, "PLAYBACK Exception: " + e);
                }

              //  progress.dismiss();
            }
        });

        /** TODO **/
        if (!isMainWindow) {
            syeSession.setMaxVideoBitrate(maxBRate);
            syeSession.setMinVideoBitrate(minBRate);
            syeSession.setStartVideoBitrate(minBRate);
        }

        SyeChannel channel = mSyeChannels.get(mCurrentChannel);
        logStreamInfo(channel);
    }

    private void logStreamInfo(SyeChannel channel) {

        setAudio();
        Log.d(TAG, "channel:" + channel.getChannelId());
        SyeChannelInfo streamInfo = null;
        List<SyeTrack> mTrackList = null;
        if (channel != null) {
            streamInfo = channel.getChannelInfo();
            mTrackList = channel.getTrackList();
        }


    }

/*
    // invoked when the decoder has detected a resolution change (onVideoFormatChanged callback)
    private void adjustAspectRatio(int videoWidth, int videoHeight) {

        int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

        double aspectRatio = (double) videoHeight / videoWidth;

        int newWidth, newHeight;
        if (screenHeight  > (int) (screenWidth * aspectRatio)) {
            // limited by narrow width; restrict height
            newWidth = screenWidth;
            newHeight = (int) (screenWidth * aspectRatio);
        } else {
            // limited by short height; restrict width
            newWidth = (int) (screenHeight  / aspectRatio);
            newHeight = screenHeight ;
        }
        int x = (screenWidth - newWidth) / 2;
        int y = (screenHeight  - newHeight) / 2;
        Log.v(TAG, "video=" + videoWidth + "x" + videoHeight +
                " screen=" + screenWidth + "x" + screenHeight +
                " newView=" + newWidth + "x" + newHeight +
                " offset=" + x + "," + y);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(newWidth, newHeight);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mTextureView.setLayoutParams(params);

    }

    // Callback when playback has stopped
    public void playbackStopped(String instanceName) {
        Log.d(TAG, "RSMediaPlayerEvent playbackStopped threadId=" + Thread.currentThread().getId());
    }

    // Callback when something has gone wrong on server or decoder side
    public void playbackNetworkFailure(String instanceName) {
        Log.d(TAG, "RSMediaPlayerEvent playbackNetworkFailure from instance " + instanceName );
    }
*/


    // Callback when login to backend failed
    public void loginFailed(String userName) {
        Log.d(TAG, "RSMediaPlayerEvent loginFailed for user " + userName);

        CharSequence text = "Login with username '" + userName + "' failed";
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(mAppContext, text, duration);
        toast.show();

    }

    public void onChangedPlayerState(String playerName, PlayerState state) {
        Log.d(TAG, "onChangedPlayerState " + playerName + ": " + state.toString());

        switch(state) {
            case idle: initSyePlayer(playerName); break;
            case initialized:
                mSyeChannels = syeMediaPlayer.getChannelList();
                if (mSyeChannels == null || mSyeChannels.size() < 1) return;

                syeSession = syeMediaPlayer.createSession("session1");

               // mCurrentChannel = 0;
                final SyeChannel startChannel = mSyeChannels.get(mCurrentChannel);
                syeSession.prepare(startChannel);


                break;
        }
    }

    // init the settings of the player
    void initSyePlayer(String playerName) {

        boolean clientSetDelayOn = false;
        int delayValue = 0;

        SyeMediaPlayer.playerSettings settings = syeMediaPlayer.getSettings();
        settings.setABRMode(1); // stateful

        settings.setDelayFromClient(clientSetDelayOn, delayValue);
        //settings.setStatsCallback(false, 1000); // get CB every second
        syeMediaPlayer.configure(settings);
    }

    // event or error in the player
    public void onSyeMediaPlayerError(String playerName, SyeMediaPlayerEvent.Error error, String message) {
        Log.e(TAG, "onError for " + playerName + " error=" + error + "(" + message + ")");

        switch (error) {
            case LOGIN_FAILED: loginFailed(message); break;
            case GET_CHANNELS_REQUEST_FAILED:
                Toast.makeText(mAppContext, "Problem connecting to backend", Toast.LENGTH_SHORT).show();
                //finish(); // should stop the fragment
                break;
        }
    }

    // triggered every second if enabled in the settings of the player
    public void onStats(String sessionName, final SyeStats stats) {
        if(sessionName.equals("session1")) {
            Log.d(TAG, "getNetworkTime = " + syeSession.getNetworkTime());
            Log.d(TAG, "stats: damaged_frames     : " + stats.getDamagedFrames());
            Log.d(TAG, "stats: TS retransmit requests : " + stats.getNbRetransmitRequests());
            Log.d(TAG, "stats: TS packets retransmitted  : " + stats.getNbRetransmitPackets());
            Log.d(TAG, "stats: TS Lost            : " + stats.getLostPackets());
            Log.d(TAG, "stats: bitrate : " + stats.getBitrate());
            Log.d(TAG, "stats: Video Rate         : " + stats.getVideoRate() + " fps");
        }
    }




    //////////////////////////////// SyeSession callbacks ///////////////////////////////////////
    // state machine callback
    public void onChangedSessionState(String sessionName, sessionState state, String media_type) {
        Log.d(TAG, "onChangedSessionState " + sessionName + ": " + state.toString() + " " + (media_type != null ? media_type : ""));

        switch(state) {
            case prepared:
                boolean status = false;
                status = syeSession.createDefaultVideoRenderer("video_renderer", mTextureView);
                if(status == false) {
                    Log.w(TAG, "failed to create the video renderer");
                }

                status = syeSession.createDefaultAudioRenderer("audio_renderer");
                if(status == false) {
                    Log.w(TAG, "failed to create the audio renderer");
                }
                syeSession.Play();
                break;
        }
    }

    // triggered everytime an audio or video track has changed (ABR)
    public void onActiveTrackChanged(String sessionName, String media_type, int id) {
        Log.d(TAG, "SyeMediaPlayerEvent callback: activeTrackChanged for session " + sessionName + " and " + media_type + " type");
        SyeChannel activeChannel = syeSession.getActiveChannel();
        SyeTrack activeAudioTrack = syeSession.getActiveAudioTrack();
        SyeTrack activeVideoTrack = syeSession.getActiveVideoTrack();
        int activeAudioTrackId = syeSession.getActiveAudioTrackId();
        int activeVideoTrackId = syeSession.getActiveVideoTrackId();
    }

    // triggered by any type of callback
    public void eventCallback(String name, int nb, String msg) {
        Log.d(TAG, "eventCallback for " + name + ": " + Integer.toHexString(nb) + " \"" + msg + "\"");
    }

    // triggered when fatal error
    public void onError(String name, int nb, String msg) {
        Log.e(TAG, "onError for " + name + ": " + Integer.toHexString(nb) + " \"" + msg + "\"");
    }


    public synchronized void onChangedRendererState(String rendererName, SyeSessionEvent.rendererState state) {
        Log.d(TAG, "onChangedRendererState " + rendererName + ": " + state.toString());

        switch(state) {
            case started:
                if(rendererName.equals("video_renderer")) {
                    playbackStarted(rendererName);
                }
                break;
        }
    }


    // callback when the format of the video frame has changed
    public void onVideoFormatChanged(String instanceName, final int width, final int height) {
        setAudio ();
        Log.d(TAG, "SyeMediaPlayerEvent videoFormatChanged from instance " + instanceName );
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                //adjustAspectRatio(width, height);
            }
        });
    }





    //////////////////////////////// TextureView callback///////////////////////////////////////

    //////////////////////////////// TextureView callback///////////////////////////////////////
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture st, int width, int height) {

        scaleW = (float) 1;
        scaleH = (float) 1;
        panHeight = 0;

        // one way to get username and password
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //String userName = prefs.getString("myLogin", "");
        //String password = prefs.getString("myPassword", "");
       /// String userName = "player";
       // String password = "neti";


        /* Sye player is created and given the baseURL and the Sye user/password */
        //String baseURL = prefs.getString("base_url", "");

       //String baseURL = "http://52.14.195.244";

        //syeMediaPlayer = new SyeMediaPlayer("PLAYER_1", this, mAppContext, baseURL, userName, password);

        /* Sye player is created and given the baseURL and the credentials for authentification */
        //String credentials = "this is the app developer credentials";
        //syeMediaPlayer = new SyeMediaPlayer("PLAYER_1", this, mAppContext, baseURL, credentials);


        int mWidth = mTextureView.getWidth();
        int mHeight =mTextureView.getHeight();

        Log.d(TAG, mPlayerName + " onSurface callback size = " + width + "x" + height);
        Log.d(TAG, mPlayerName + " mWidth " + mWidth + " mHeight: " + mHeight);

        double x = mTextureView.getTranslationX();
        double y = mTextureView.getTranslationY();
        Log.d(TAG, mPlayerName + " x " + x + " y: " + y);


        scaleW = (float) 1;
        scaleH = (float) 1;
        panHeight = 0;


      //  String userName = StreamLocalData.login;
       // String password = StreamLocalData.pass;

        String userName = StreamLocalData.login;
        String password = StreamLocalData.pass;

        /* Sye player is created and given the baseURL and the Sye user/password */
        //String baseURL = prefs.getString("base_url", "");

        //String baseURL = "http://52.14.195.244";
        String baseURL = StreamLocalData.controlip;

        Log.d(TAG, mPlayerName + " audioMuted: " + audioMuted);
        Log.d(TAG, mPlayerName + " CHANNEL: " + mChannelName + " user: " +userName + " pass: " + password + " control:" + baseURL);

        /* Sye media player is created  */
       // syeMediaPlayer = new SyeMediaPlayer("PLAYER_1", this, mAppContext, mBaseUrl, "bill", "54U26h89");
        syeMediaPlayer = new SyeMediaPlayer("PLAYER_1", this, mAppContext, mBaseUrl, userName, password);

        Log.d(TAG, mPlayerName + "started FragPlayer");





    }


    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture st, int width, int height) {
        // ignore
        Log.d(TAG, "FragPlayer  width=" + width + " height=" + height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture st) {
        Log.d(TAG, "####onSurfaceTextureDestroyed");
        if (syeMediaPlayer != null) {
            syeMediaPlayer.deleteAllSessions();
            syeMediaPlayer = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // ignore
    }







}