package medialabs.nbcu.com.diagtool2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.netinsight.sye.framework.model.SyeChannel;
import com.netinsight.sye.framework.model.SyeStats;
import com.netinsight.sye.framework.model.SyeTrack;
import com.netinsight.sye.framework.player.SyeMediaPlayer;
import com.netinsight.sye.framework.player.SyeMediaPlayerEvent;
import com.netinsight.sye.framework.player.SyeSession;
import com.netinsight.sye.framework.player.SyeSessionEvent;

import java.util.ArrayList;

import medialabs.nbcu.com.diagtool2.async.AddLogInfoTask;
import medialabs.nbcu.com.diagtool2.async.GetNetiLocation;
import medialabs.nbcu.com.diagtool2.async.IAsyncListner;
import medialabs.nbcu.com.diagtool2.async.IGetNetiLocAsyncListner;
import medialabs.nbcu.com.diagtool2.data.Keyword;
import medialabs.nbcu.com.diagtool2.data.StreamLocalData;
import medialabs.nbcu.com.diagtool2.data.StreamLog;

/*
import com.netinsight.redstone.rsframework.model.RSChannel;
import com.netinsight.redstone.rsframework.model.RSStats;
import com.netinsight.redstone.rsframework.model.RSStream;
import com.netinsight.redstone.rsframework.player.RSMediaPlayer;
import com.netinsight.redstone.rsframework.player.RSMediaPlayerEvent;
*/

/**
 * Created by Bill on 1/24/17.
 */

public class StreamActivity extends AppCompatActivity
        implements TextureView.SurfaceTextureListener, IGetNetiLocAsyncListner, IAsyncListner,
             View.OnTouchListener, View.OnKeyListener, SyeMediaPlayerEvent, SyeSessionEvent,KeywordListFragment.OnListFragmentInteractionListener,KeywordListFragment.OnGraphicsChangeEvent {

    private final String TAG = "[StreamActivity ]LOGGER";

    private TextureView mTextureView;
    private RelativeLayout mainView;
    private ProgressDialog progress;
    protected static Context mAppContext;

    private TextView mDelayValue;
    private Button mDelayButton;
    private SeekBar mStreamBar;


    // SDK interface
   // private RSMediaPlayer rsMediaPlayer;
   // public ArrayList<RSChannel> mRsChannels;

    // SDK interface
    private SyeMediaPlayer syeMediaPlayer;
    private SyeSession syeSession;
    private  SyeChannel thechannel;

    public ArrayList<SyeChannel> mSyeChannels;

    LocationManager mLocationManager;

    // settings for the RSMediaPlayer
    private String mBaseUrl;
    private int mFixedAbrLevel;

    // Touch support
    float mLastScrollingX;
    float mStartScrollingX;

    // surface scaling
    float scaleW;
    float scaleH;
    float panHeight;

    double loc_lat=0;
    double loc_lon=0;

    String mLocation ="";

    // Channels
    int mCurrentChannel;
    String startUpChannel;
    boolean audioMuted;
    String serverAddress="";
    String srvLocation="";
    StreamLog mStreamLog;

    int mStreamDelayValue=0;


    SyeChannel channel  = (SyeChannel)null;

    FloatingActionButton fab;

    //RELATED Graphics Stuff
    private static final String NO_MATCH_GRAPHICS_ID = "104692869";
    private static final Rect GRAPHICS_RECTANGLE =  new Rect(90, 480, 435, 944);
    private SurfaceHolder mSurfaceHolder;
    private String mCurrentGraphicsId = null;

    /*
    private void startPlayChannel(RSChannel channel) {
        Log.i(TAG, "startPlayChannel");
        // optional: rsMediaPlayer.startPlay(channel);
        //rsMediaPlayer.startPlay(channel.getChannelId());
        rsMediaPlayer.startPlay(channel);
    }
    */

    private void oneChannelUp() {
        if (mCurrentChannel < (mSyeChannels.size() - 1)) {
            mCurrentChannel++;
        } else {
            mCurrentChannel = 0;
        }
        changeChannel();
    }

    private void oneChannelDown() {
        if (mCurrentChannel > 0) {
            mCurrentChannel--;
        } else {
            mCurrentChannel = (mSyeChannels.size() - 1);
        }
        changeChannel();
    }

    public void changeChannel()
    {
/*
        for (int i = 0; i < mRsChannels.size(); i++) {
            RSChannel channel = mRsChannels.get(i);
            if (channel.getChannelId().equals(channelId)) {
                mCurrentChannel = i;
                startPlayChannel(channel);
                break;
            }
        }*/

       // logStreamInfo(SyeChannel channel, SyeSession syeSession)


        SyeChannel channel = mSyeChannels.get(mCurrentChannel);
        thechannel=channel;


       // logData ();

        CharSequence text = "Channel_" + channel.getChannelId() + " " + channel.getChannelEpgId();

        boolean ret = syeSession.prepare(channel);
        assert(ret == true);

        clearSurfaceAndStartKeywordListFragment();
        return;

    }

    public void setNewDelay()
    {
        SyeChannel channel = mSyeChannels.get(mCurrentChannel);
        thechannel=channel;
        boolean ret = syeSession.prepare(channel);
        return;
    }

    private void clearSurfaceAndStartKeywordListFragment() {
        clearRectangle();
        String channelName = getChannelName();
        KeywordListFragment keywordListFragment = KeywordListFragment.newInstance(channelName);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.keyword_list_fragment, keywordListFragment, channelName+ "KeywordList")
                .commit();
    }

    public void logData ()
    {
       // private SyeSession syeSession;
        //private  SyeChannel thechannel;
        // -- logging for the stream
        StreamLog.logStreamInfo(thechannel, syeSession);

        GetNetiLocation loc = new GetNetiLocation(this, StreamLog.serveraddress);
        loc.execute((Void) null);

    }

//////////////////////////////// Examples of remote control actions ///////////////////////////////////////

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        Log.d(TAG, "onKey called");
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        processKeyEvent(keyCode);
        Log.d(TAG, "Processing view keyevent code:" + keyCode);
        return super.onKeyDown(keyCode, event);
    }

    private void processKeyEvent(int keyCode) {
        if (mSyeChannels == null || mSyeChannels.size() < 1) {
            return;
        }

        if (keyCode == 19) {
            //one channel up
            oneChannelUp();
        } else if (keyCode == 20) {
            //one channel down
            oneChannelDown();
        }
    }

//////////////////////////////// Examples of touch screen actions ///////////////////////////////////////

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mStartScrollingX = event.getX();

                if (mCurrentGraphicsId!=null && GRAPHICS_RECTANGLE.contains((int) event.getX(), (int) event.getY()) && getChannelName().equalsIgnoreCase("cnbc")) {
                    String message = "opening story id:: " + mCurrentGraphicsId;
                    showAppToast(message);
                    openWebStory(mCurrentGraphicsId);
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:

                //When the gesture ends change channel if user moved video enough
                if ((mLastScrollingX) < -600) {
                    if (mSyeChannels == null) break;
                    oneChannelDown();
                    mTextureView.setVisibility(View.INVISIBLE);
                } else if ((mLastScrollingX) > 600) {
                    if (mSyeChannels == null) break;
                    // move one channel up
                    oneChannelUp();
                    mTextureView.setVisibility(View.INVISIBLE);
                }
                Matrix m1 = new Matrix();
                m1.postScale(scaleW, scaleH);
                m1.postTranslate(0, panHeight);
                mTextureView.setTransform(m1);

                break;

            case MotionEvent.ACTION_POINTER_DOWN: // second finger
                Log.d(TAG, "actio_ptr_down");
                if (audioMuted) {
                    // full volume set up
                    syeSession.setVolume("audio_renderer", 100);
                    audioMuted = false;
                } else {
                    audioMuted = true;
                    // muting
                    syeSession.setVolume("audio_renderer", 0);
                }

                break;
            case MotionEvent.ACTION_MOVE:
                mLastScrollingX = mStartScrollingX - event.getX();
                Matrix m = new Matrix();

                m.postScale(scaleW, scaleH);
                m.postTranslate((-mLastScrollingX), panHeight);
                mTextureView.setTransform(m);
                Log.d(TAG, "SCROLLING VIEW mLastScrollingX=" + mLastScrollingX);
                break;
        }
        return true;
    }

//////////////////////////////// Main Activity Callbacks and settings ///////////////////////////////////////

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // stopping all
        if (syeMediaPlayer != null) {
            syeMediaPlayer.deleteAllSessions();
            syeMediaPlayer = null;
        }
        Log.d(TAG, "Redstone demo app ended");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (progress.isShowing())
            progress.dismiss();

        // stopping all
        if (syeMediaPlayer != null) {
            syeMediaPlayer.deleteAllSessions();
            syeMediaPlayer = null;
        }
        Log.d(TAG, "LOGGER -  ONPause -- Redstone demo app paused");


    }

    @Override
    protected void onResume() {
        super.onResume();

        // progress.show();
        if (!progress.isShowing())
            progress.show();

        // forcing the video restart
        if (mTextureView.isAvailable()) {
            onSurfaceTextureAvailable(mTextureView.getSurfaceTexture(), mTextureView.getWidth(), mTextureView.getHeight());
        }

        clearSurfaceAndStartKeywordListFragment();

        Log.d(TAG, "Redstone demo app resumed");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // save the application context
        mAppContext = getApplicationContext();

        // Hints for the activity
        setHintsForTheActivity();

        setContentView(R.layout.activity_stream);

        fab = (FloatingActionButton) findViewById(R.id.streamfab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // Snackbar.make(view, "Connected to server: " + serverAddress + " " + srvLocation, Snackbar.LENGTH_LONG)
                Snackbar.make(view, "Connected to server: " + StreamLog.serveraddress + " " + StreamLog.location, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        mStreamBar   = (SeekBar) findViewById(R.id.streamBar);
        mDelayValue  = (TextView) findViewById(R.id.delayValue);
        mDelayButton = (Button) findViewById(R.id.delayButton);

        mDelayValue.setText("DELAY: " + Integer.toString(mStreamDelayValue) + " Chan: " + mCurrentChannel + " " + StreamLocalData.getChName(mCurrentChannel));

        mStreamBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
        {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
               mStreamDelayValue = progress * 100;
               mDelayValue.setText("DELAY: " +Integer.toString(mStreamDelayValue)  + " Chan: " +  mCurrentChannel + " " + StreamLocalData.getChName(mCurrentChannel));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}

            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        final IAsyncListner thislistner = this;
        mDelayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "Setting User Pref Delay channel: " + mCurrentChannel + " delay: " + mStreamDelayValue);
                StreamLocalData.setStreamDelay(mCurrentChannel, mStreamDelayValue);
//                StreamLocalData.setStreamDBDelay (mCurrentChannel, mStreamDelayValue, thislistner);
                initSyePlayer("delayplayer");
                //changeChannel();
                setNewDelay();
            }
        });



        // Add touch support
        mainView = (RelativeLayout) findViewById(R.id.stream_view);
        mainView.setOnTouchListener(this);
        mainView.setOnKeyListener(this);

        // settings
        //RSinitsettings();

        // Add one textureview to show video in
        mTextureView = (TextureView) findViewById(R.id.movie_texture_view);

       // mTextureView = (TextureView) findViewById(R.id.stream_view2);
        mTextureView.setOpaque(false);
        mTextureView.setSurfaceTextureListener(this);

        // Spinnerwheel
        progress = new ProgressDialog(this);
        progress.setTitle("");
        progress.setMessage("Accessing content - Please wait ...");
        progress.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (progress.isShowing()) {
                    processKeyEvent(keyCode);
                    Log.d(TAG, "Processing progressbar keyevent");
                    progress.dismiss();
                }
                return true;
            }
        });
        if (!progress.isShowing())
            progress.show();

        mCurrentChannel = 0;

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.transparentView);
        surfaceView.setZOrderOnTop(true);
        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback(){
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d("GraphicsCard", "surfaceDestroyed");
                mSurfaceHolder = holder;
                clearRectangle();
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d("GraphicsCard","surfaceCreated" );
                mSurfaceHolder = holder;
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.d("GraphicsCard","surfaceChanged" );
                mSurfaceHolder = holder;
            }
        });

        Log.d("StreamActivity", "Player started...");
    }



    public void drawGraphicsRectangle() {
        if(mSurfaceHolder ==null)
            return;
        Canvas canvas = mSurfaceHolder.lockCanvas();
        if (canvas != null) {
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            //border's properties
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.YELLOW);
            paint.setStrokeWidth(3);
            canvas.drawRect(GRAPHICS_RECTANGLE, paint);
            mSurfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public void clearRectangle(){
        if(mSurfaceHolder ==null)
            return;

        Canvas canvas = mSurfaceHolder.lockCanvas();
        if(canvas!=null) {
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            mSurfaceHolder.unlockCanvasAndPost(canvas);
        }
    }


    private String getChannelName() {
        return StreamLocalData.getChName(mCurrentChannel);
    }

    private void setHintsForTheActivity() {

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                +WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                +WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Display d = getWindow().getWindowManager().getDefaultDisplay();
        Log.d(TAG, "Current Display mode=" + d.toString());
        Log.d(TAG, "Current Display refresh rate=" + d.getRefreshRate());
    }

    // initiate settings for the RSMediaPlayer
    /*
    private void RSinitsettings() {

        // feel the base URL here
        //mBaseUrl = "http://demo.neti.systems:3001";
       // mBaseUrl = "http://65.51.185.114:82";

        mBaseUrl = StreamLocalData.controlip;
        // -1 is automatic ABR scaling
        mFixedAbrLevel = -1;
        // Abr mode is fixed to minimum value for old android devices (for experimentation, only KitKat and more have full support)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT && mFixedAbrLevel < 0) {
            mFixedAbrLevel = 1;
        }
    }

*/



//////////////////////////////// RSMediaPlayer callback///////////////////////////////////////



    // Callback when playback has stopped
    public void playbackStopped(String instanceName) {
        Log.d(TAG, "RSMediaPlayerEvent playbackStopped threadId=" + Thread.currentThread().getId());
    }

    // Callback when something has gone wrong on server or decoder side
    public void playbackNetworkFailure(String instanceName) {
        Log.d(TAG, "RSMediaPlayerEvent playbackNetworkFailure from instance " + instanceName );
    }


    /**
     * MyCode
     * @param res
     */

    public void asyncGetNetiLocComplete(String res)
    {
        Log.d(TAG, "LOGGER!! LocComplete: " + res);
        //srvLocation = res;
        StreamLog.location=res;

        AddLogInfoTask li = new AddLogInfoTask();
        li.setDataElements(StreamLog.srvurl,StreamLog.serveraddress,StreamLog.channelid,StreamLog.location,StreamLocalData.user,StreamLog.channelid,
                0,StreamLog.maxabr,StreamLog.vrate,StreamLog.brate,StreamLocalData.loc_lon,StreamLocalData.loc_lat, StreamLog.delay,
        StreamLocalData.location);

        li.execute((Void) null); // log data

        View view = fab.getRootView();
        Snackbar.make(view, "Connected to server: " + StreamLog.serveraddress + "  " +  StreamLog.location, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

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





    // Callback when login to backend failed
    public void loginFailed(String userName) {
        Log.d(TAG, "RSMediaPlayerEvent loginFailed for user " + userName);

        CharSequence text = "Login with username '" + userName + "' failed";
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(mAppContext, text, duration);
        toast.show();

        // launch an activity to get new credentials
        //  Intent intent = new Intent(this, LoginActivity.class);
        //  startActivity(intent);
    }


    // callback when the format of the video frame has changed
    public void videoFormatChanged(String instanceName, final int width, final int height) {
        Log.d(TAG, "RSMediaPlayerEvent videoFormatChanged from instance " + instanceName + " : " + width + "x" + height);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                //doStuff(width, height); // typically readjust the aspect ratio
            }
        });
    }

    // callback when abr has changed
    public void abrLevelChanged(String instanceName, final int abrLevel) {
        Log.d(TAG, "RSMediaPlayerEvent abrLevelChanged to " + abrLevel);

        //logStats();

    }

    // callback about NTP sync
    public void onNtpSync(int status)   {
        switch(status) {
            case 1: Log.d(TAG, "RSMediaPlayerEvent NTP sync status: SEARCHING"); break;
            case 2: Log.d(TAG, "RSMediaPlayerEvent NTP server reported insane time"); break;
            case 0: Log.d(TAG, "RSMediaPlayerEvent NTP time now SYNCHRONIZED"); break;
        }
    }

    ////**** new stuff ****//
    ////**** new stuff ****//
////**** new stuff ****//

    // adapt the surface visibility and matrix once started
    private void playbackStarted(String instanceName) {
        Log.d(TAG, "SyeMediaPlayerEvent playbackStarted for player=" + instanceName);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                mTextureView.setVisibility(View.VISIBLE);
                Matrix m1 = new Matrix();
                m1.postScale(scaleW, scaleH);
                m1.postTranslate(0, panHeight);

                Log.d(TAG, "Transla scaleW=" + scaleW + " scaleH=" + scaleH);
                mTextureView.setTransform(m1);

                progress.dismiss();
            }
        });
    }

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

    // init the settings of the player
    void initSyePlayer(String playerName) {
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
       // boolean clientSetDelayOn = prefs.getBoolean("set_delay_from_client", false);
       // int delayValue = prefs.getInt("client_delay_value", 3500);
        int delayValue =0;

        mStreamDelayValue=StreamLocalData.getDelay(mCurrentChannel);
        boolean clientSetDelayOn=false;

        if (mStreamDelayValue > 0)
            clientSetDelayOn=true;

        Log.w(TAG, "INIT playback ch: " + mCurrentChannel + " delay: " + mStreamDelayValue);

        SyeMediaPlayer.playerSettings settings = syeMediaPlayer.getSettings();
        settings.setABRMode(1);
        settings.setDelayFromClient(clientSetDelayOn,mStreamDelayValue);
        settings.setStatsCallback(false, 1000); // get CB every second

        syeMediaPlayer.configure(settings);
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
                logData();
                syeSession.Play();

                break;
            case starting:
/** TODO get info from other source **/
                Log.w(TAG, "starting playback ch: " + mCurrentChannel);

                mStreamDelayValue=StreamLocalData.getDelay(mCurrentChannel);
                if (mStreamDelayValue > 1000)
                   mStreamBar.setProgress(StreamLocalData.getDelay(mCurrentChannel)/100);
                else
                    mStreamBar.setProgress(mStreamDelayValue);

                mDelayValue.setText("DELAY: " + Integer.toString(mStreamDelayValue) + " Chan: " + mCurrentChannel + " " + StreamLocalData.getChName(mCurrentChannel));
                break;
        }

        //logData ();
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



    //////////////////////////////// renderer callbacks ///////////////////////////////////////
    // renderer state machine callback
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

    // triggered when the video resolution changed
    public void onVideoFormatChanged(String instanceName, final int width, final int height) {
        Log.d(TAG, "SyeMediaPlayerEvent videoFormatChanged from instance " + instanceName );
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                adjustAspectRatio(width, height);
            }
        });
    }

    //////////////////////////////// TextureView callback///////////////////////////////////////
  /**
    public void onSurfaceTextureAvailable(SurfaceTexture st, int width, int height) {

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int mHeight = displaymetrics.heightPixels;
        int mWidth = displaymetrics.widthPixels;
        Log.d(TAG, "Screen size = " + mWidth + "x" + mHeight);

        scaleW = (float) 1;
        scaleH = (float) 1;
        panHeight = 0;
**/
        // one way to get username and password
       // SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
       // String userName = prefs.getString("myLogin", "player");      // round robin
        //String userName = prefs.getString("myLogin", "playerva");    // CA
        //String userName = prefs.getString("myLogin", "playeror");    // OR
        //String userName = prefs.getString("myLogin", "playerca");      // VA
       // String password = prefs.getString("myPassword", "neti");
        //String password = prefs.getString("myPassword", "player");
        //String password = prefs.getString("myPassword", "playerva");
       // String password = prefs.getString("myPassword", "playeror");
        //String password = prefs.getString("myPassword", "playerca");
     //   String userName = StreamLocalData.login;
     //   String password = StreamLocalData.pass;

        /* RS player is created and given the settings */
       // rsMediaPlayer = new RSMediaPlayer("PLAYER_1", mTextureView, this, mAppContext, mBaseUrl, false, userName, password);
       // rsMediaPlayer.setFixedAbrLevel(mFixedAbrLevel);
//
        /* RS player is started */
      //  rsMediaPlayer.start();
       // Log.d(TAG, "####onSurfaceTextureAvailable started player");

    //}


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture st, int width, int height) {

        scaleW = (float) 1;
        scaleH = (float) 1;
        panHeight = 0;

        // one way to get username and password
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //String userName = prefs.getString("myLogin", "");
        //String password = prefs.getString("myPassword", "");
       // String userName = "player";
       // String password = "neti";
          String userName = StreamLocalData.login;
          String password = StreamLocalData.pass;

        /* Sye player is created and given the baseURL and the Sye user/password */
        //String baseURL = prefs.getString("base_url", "");

        //String baseURL = "http://52.14.195.244";
        String baseURL = StreamLocalData.controlip;

        Log.d(TAG, "StreamActivity SETTING LOGIN: login: " + userName + " pass: " + password  );

        syeMediaPlayer = new SyeMediaPlayer("PLAYER_1", this, mAppContext, baseURL, userName, password);

        /* Sye player is created and given the baseURL and the credentials for authentification */
        //String credentials = "this is the app developer credentials";
        //syeMediaPlayer = new SyeMediaPlayer("PLAYER_1", this, mAppContext, baseURL, credentials);

        Log.d(TAG, "####onSurfaceTextureAvailable started player");

    }




    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture st, int width, int height) {
        // ignore
        Log.d(TAG, "####onSurfaceTextureSizeChanged width=" + width + " height=" + height);
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


    //////////////////////////////// SyeMediaPlayer callbacks ///////////////////////////////////////
    // player state machine
    public void onChangedPlayerState(String playerName, PlayerState state) {
        Log.d(TAG, "onChangedPlayerState " + playerName + ": " + state.toString());

        switch(state) {
            case idle: initSyePlayer(playerName); break;
            case initialized:
                mSyeChannels = syeMediaPlayer.getChannelList();
                if (mSyeChannels == null || mSyeChannels.size() < 1) return;

                syeSession = syeMediaPlayer.createSession("session1");
                mCurrentChannel = 0;
                final SyeChannel startChannel = mSyeChannels.get(mCurrentChannel);
                syeSession.prepare(startChannel);


                thechannel=startChannel;
                //** TODO   start logging here ***//
               // logData();

                break;
        }
    }

    // event or error in the player
    public void onSyeMediaPlayerError(String playerName, SyeMediaPlayerEvent.Error error, String message) {
        Log.e(TAG, "onError for " + playerName + " error=" + error + "(" + message + ")");

        switch (error) {
            case LOGIN_FAILED: loginFailed(message); break;
            case GET_CHANNELS_REQUEST_FAILED:
                Toast.makeText(mAppContext, "Problem connecting to backend", Toast.LENGTH_SHORT).show();
                finish();
                break;
        }
    }

    @Override
    public void onListFragmentInteraction(Keyword item) {
       // Log.d("StreamActivity", "onListFragmentInteraction " + item.word);
        if(item.word.equals("MATCHED")){
            Log.d("StreamActivity", "onListFragmentInteraction " + item.id);
            String message = "Clicked word: " + item.ner + " id: "  + item.id;
            showAppToast(message);
            openWebStory(item.id);
        }
    }

    public void asyncComplete()
    {

    }

    public void  asyncSyncComplete()
    {
        Log.d(TAG, "SYNC DB Complete");
    }


    @Override
    public void onGraphicsShow(String id) {
        Log.d(TAG,"onGraphicsShow " + id);
        if (!id.equals(NO_MATCH_GRAPHICS_ID)) {
            mCurrentGraphicsId = id;
            //drawGraphicsRectangle();
        }
    }

    @Override
    public void onGraphicsHide(){
        Log.d(TAG,"onGraphicsHide " + mCurrentGraphicsId);
        mCurrentGraphicsId = null;
        //clearRectangle();
    }

    void openWebStory(String id) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();


        final View alertView = inflater.inflate(R.layout.dialog_webview, null);
        alertDialog.setView(alertView);

        alertDialog.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        WebView wv = (WebView) alertView.findViewById(R.id.webView);
        final ProgressBar progressBar = (ProgressBar) alertView.findViewById(R.id.progressBar);
        wv.loadUrl("http://www.cnbc.com/id/" + id);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setLoadWithOverviewMode(true);
        wv.getSettings().setDisplayZoomControls(true);
        wv.getSettings().setUseWideViewPort(true);
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                super.onPageCommitVisible(view, url);
                progressBar.setVisibility(View.INVISIBLE);
            }
        });

        wv.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onReceivedTitle(WebView view, String title) {
                alertDialog.setTitle(title);
            }
        });
        alertDialog.show();
    }

    void showAppToast(String message){
        Toast t = Toast.makeText(mAppContext, message, Toast.LENGTH_SHORT);
        t.show();
    }
}

