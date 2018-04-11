package medialabs.nbcu.com.diagtool2;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.MotionEvent;
import android.support.v4.view.GestureDetectorCompat;
import android.provider.Settings.Secure;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import medialabs.nbcu.com.diagtool2.async.GetNetiBest;
import medialabs.nbcu.com.diagtool2.data.StreamLocalData;
import medialabs.nbcu.com.diagtool2.data.TestData;
import medialabs.nbcu.com.diagtool2.async.GetNetiData;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Locale;

import android.util.DisplayMetrics;

import com.lisnr.sdk.LisnrContentManager;
import com.lisnr.sdk.LisnrManager;

import org.json.JSONArray;

import medialabs.nbcu.com.diagtool2.async.IGetNetiDataAsyncListner;
import medialabs.nbcu.com.diagtool2.async.IGetNetiBestAsyncListner;

public class MainActivity extends AppCompatActivity
        implements GestureDetector.OnGestureListener,
        View.OnKeyListener,
        GestureDetector.OnDoubleTapListener, IGetNetiDataAsyncListner,
        IGetNetiBestAsyncListner
{

    LisnrManager lisnr;
    LisnrContentManager contentManager;

    private TextView dataControlIP;
    private TextView dataDeviceID;
    private TextView dataEdgeIP;
    private TextView dataEdgeLocation;
    private TextView dataClientLocation;
    private TextView dataDistance;
    private TextView dataTotalStreams;
    private TextView dataStreamID;
    private TextView dataNetiLogin;
    private Button butStream;

    private ProgressDialog progress;


    GetNetiData gdata;

    String androiduser="";
    protected static Context mAppContext;

    private GestureDetectorCompat gDetector;
    TestData netdata = new TestData();

    private final String TAG = "[MainActvity] LOGGER";

    double loc_lat=0;
    double loc_lon=0;

    String mLocation ="";
    LocationManager mLocationManager;


    boolean showQueryToast=false;
    boolean hasConnectionData=false;
    boolean showConnectionDistance=true;

    boolean isGetNetiDataRunning=false;
    boolean isPlayingStream=false;        /// keeping the GPS from killing the stream

    //http://ec2loader:9080/appi/getnetitest.jsp?user=b4c6deafae11cb1c




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_main2);
        setContentView(R.layout.activity_main_linear);

        lisnr = LisnrManager.getConfiguredInstance("36e875e6-f591-4204-8695-65f4c4c4ba7b", this.getApplication());
        contentManager = new LisnrContentManager(lisnr);

        StreamLocalData.setPref (this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Snackbar.make(view, "android dev: " + androiduser + " - Using " + StreamLocalData.city[StreamLocalData.selected_location] + " as location - miles to edge: " + StreamLocalData.distance, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        Log.d(TAG, " DISPLAY  height:: " + dpHeight + " Width: " + dpWidth);

        int permission = ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, " location error:  NO PERMISSION!!");
            showPermissionDialog();
        }
        else {

            Log.d(TAG, " location - getting location");
            showProgess ();
            /// ---
            /// location start-up
            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            try {
                Location currLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateLocationData(currLocation);


                mLocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,mLocationListener, this.getMainLooper());
                mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,mLocationListener, this.getMainLooper());
/*
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 900000, 500000,
                        mLocationListener, this.getMainLooper());
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 900000, 500000,
                        mLocationListener, this.getMainLooper());
                        */

            } catch (SecurityException e) {
                // e.printStackTrace();
                Log.d(TAG, " location error: " + e);
                //showPermissionDialog();
            }
        }


        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioLocationChoice);

        radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if(checkedId == R.id.radioGPS) {
                    // 0
                    StreamLocalData.selected_location=0;
                    //Toast.makeText(getApplicationContext(), "choice: GPS",
                    //       Toast.LENGTH_SHORT).show();
                } else if(checkedId == R.id.radioSeattle) {
                    // 2
                    StreamLocalData.selected_location=2;
                    // Toast.makeText(getApplicationContext(), "choice: Seattle",
                    //       Toast.LENGTH_SHORT).show();
                }
                else if(checkedId == R.id.radioPhila) {
                    // 1
                    StreamLocalData.selected_location=1;
                    //Toast.makeText(getApplicationContext(), "choice: Philadelphia",
                    //       Toast.LENGTH_SHORT).show();
                }
                else if(checkedId == R.id.radioLA) {
                    // 3
                    StreamLocalData.selected_location=3;
                    //Toast.makeText(getApplicationContext(), "choice: Los Angeles",
                    //       Toast.LENGTH_SHORT).show();
                }
                else if(checkedId == R.id.radioOH) {
                    // 4
                    StreamLocalData.selected_location=4;
                    // Toast.makeText(getApplicationContext(), "choice: Round Robin",
                    //        Toast.LENGTH_SHORT).show();
                }
                else if(checkedId == R.id.radioRoundRobin) {
                    // 5
                    StreamLocalData.selected_location=5;
                    // Toast.makeText(getApplicationContext(), "choice: Round Robin",
                    //        Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "choice: NONE",
                            Toast.LENGTH_SHORT).show();
                }
                changeLocation();
            }


        });



        ///---
        dataControlIP =
                (TextView)findViewById(R.id.dataControlIP);
        dataDeviceID =
                (TextView)findViewById(R.id.dataDeviceID);
        dataEdgeIP =
                (TextView)findViewById(R.id.dataEdgeIP);
        dataEdgeLocation =
                (TextView)findViewById(R.id.dataEdgeLocation);
        dataClientLocation =
                (TextView)findViewById(R.id.dataClientLocation);
        dataDistance =
                (TextView)findViewById(R.id.dataDistance);
        dataTotalStreams =
                (TextView)findViewById(R.id.dataTotalStreams);
        dataStreamID =
                (TextView)findViewById(R.id.dataStreamId);
        dataNetiLogin =
                (TextView)findViewById(R.id.dataNetiLogin);

        butStream =
                (Button)findViewById(R.id.butStream);

        butStream.setClickable(false);  // need to get creds

        StreamLocalData.loc_lat=loc_lat;
        StreamLocalData.loc_lon=loc_lon;

        StreamLocalData.selected_lat[0]=loc_lat;
        StreamLocalData.selected_lon[0]=loc_lon;

        mAppContext = getApplicationContext();
        // getting the device id - used by netiinsight as the user id

        androiduser = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
        StreamLocalData.user=androiduser;

        gdata = new GetNetiData(this,androiduser);

        Log.d(TAG, " androiduser: " + androiduser);
        Button data = (Button)findViewById(R.id.butQuery);
        data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                // will only run if this is false
                if (!isGetNetiDataRunning)
                    gdata.execute((Void) null);
            }
        });

        Button windows = (Button)findViewById(R.id.butWindows);
        windows.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getApplicationContext(), StreamWindowsActivity.class);
                //startActivity(intent);
                isPlayingStream=true;
                Log.d(TAG, " playing stream: " + isPlayingStream);
                startActivityForResult(intent,1);
            }
        });

        dataControlIP.setText(StreamLocalData.controlip);
        Button stream = (Button)findViewById(R.id.butStream);
        stream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // newData.setText("");
                Intent intent = new Intent(getApplicationContext(), StreamActivity.class);
                //startActivity(intent);
                isPlayingStream=true;
                Log.d(TAG, " playing stream: " + isPlayingStream);
                startActivityForResult(intent,1);
            }
        });

        // getting the log data
        // TODO - get the user from Neti
        isGetNetiDataRunning=true;
        gdata.execute((Void) null);

        this.gDetector = new GestureDetectorCompat(this,this);
        gDetector.setOnDoubleTapListener(this);

    }


    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            Log.d("LOGGER_LOCATION","onLocationChanged " + location + " playing stream: " + isPlayingStream);
            if(!isPlayingStream)
                updateLocationData(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    public void changeLocation ()
    {
        if (StreamLocalData.selected_location !=5)
        {
            if (!isPlayingStream) {
                loc_lat = StreamLocalData.selected_lat[StreamLocalData.selected_location];
                loc_lon = StreamLocalData.selected_lon[StreamLocalData.selected_location];
                GetNetiBest best = new GetNetiBest(this, loc_lat, loc_lon);
                best.execute((Void) null);
            }
        }
    }

    private void showProgess ()
    {
        Log.d(TAG, "location- showing progress");
        progress = new ProgressDialog(this);
        progress.setTitle("GPS Data");
        progress.setMessage("Getting best stream edge based on GPS data ...");
        progress.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (progress.isShowing()) {
                    // processKeyEvent(keyCode);
                    Log.d(TAG, "Processing progressbar keyevent");
                    progress.dismiss();
                }
                return true;
            }
        });
        if (!progress.isShowing())
            progress.show();
    }

    @Override
    public void asyncGetNetiDataComplete(JSONArray res)
    {
        try {
            netdata.loadData(res);
            HashMap d = new HashMap();
            dataDeviceID.setText(androiduser);

            if (!netdata.raw.isEmpty()) {
                Log.d(TAG, "Data Count: " + netdata.raw.size());
                d = netdata.raw.get(0);
                // dataDeviceID.setText((String)d.get("user"));
                //user.setText((String)d.get("user"));
                String addr = StreamLocalData.controlip;
                String addr2 = "";
                if (addr.length() > 20)
                    addr2 = addr.substring(0, 20);
                else
                    addr2 = addr;

                Log.d(TAG, "addr: " + addr + " : addr2: " + addr2);

                dataEdgeIP.setText((String) d.get("serveraddr"));
                dataClientLocation.setText((String) d.get("location"));
                dataTotalStreams.setText(String.valueOf(netdata.hitcount));
                dataStreamID.setText((String) d.get("streamid"));
                dataEdgeLocation.setText((String) d.get("srvlocation"));
                dataControlIP.setText(addr2);
            } else
                Log.d(TAG, "NO Data returned for getnetidata query");

            gdata.cancel(true);
            gdata = null;
            gdata = new GetNetiData(this, androiduser);

            //newData.setText(" ");  // space fill this element
            if (showQueryToast) {
                Toast t = Toast.makeText(mAppContext, "Data returned for id: " + androiduser, Toast.LENGTH_SHORT);
                t.show();
            }
        }catch (Exception e) {}

    }

    private void updateLocationData(Location location)
    {

        if (isPlayingStream)
            return;

        Address address = null;
        try {

            if (location == null) {
                Log.d("LOGGER_LOCATION", "updateLocationData: location is NULL");

                GetNetiBest best = new GetNetiBest(this, loc_lat, loc_lon);
                best.execute((Void) null);
                return;
            }

        } catch (Exception e)
        { }


        try {
            loc_lat = location.getLatitude();
            loc_lon = location.getLongitude();
            Log.d("LOGGER_LOCATION", loc_lat + " :: " + loc_lon);
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.US);
            address = geocoder.getFromLocation(loc_lat, loc_lon, 1).get(0);
            mLocation = address.getLocality() + "," + address.getAdminArea() + "," + address.getPostalCode();
            Log.d("LOGGER_LOCATION", "location: " + location);
            dataClientLocation.setText(mLocation);  // space fill this element
            StreamLocalData.location=mLocation;
            StreamLocalData.loc_lat=loc_lat;
            StreamLocalData.loc_lon=loc_lon;

            StreamLocalData.selected_lat[0]=loc_lat;
            StreamLocalData.selected_lon[0]=loc_lon;
            showQueryToast=true;

            // getting the Neti user - only first time
            // if (showConnectionDistance) {
            GetNetiBest best = new GetNetiBest(this, loc_lat, loc_lon);
            best.execute((Void) null);
            // }
            // Toast t = Toast.makeText(mAppContext,"GPS Location: " + mLocation, Toast.LENGTH_SHORT);
            // t.show();
        } catch (Exception e) {
            // going with default location if we find an error
            Log.d("LOGGER_LOCATION", "GPS ADDRESS  error  " + e.getMessage() + " playing stream: " + isPlayingStream);
            loc_lat=0;
            loc_lon=0;
            StreamLocalData.loc_lat=loc_lat;
            StreamLocalData.loc_lon=loc_lon;
            if (!isPlayingStream) {
                GetNetiBest best = new GetNetiBest(this, loc_lat, loc_lon);
                best.execute((Void) null);
            }
            //e.printStackTrace();
        }
    }




    private void showPermissionDialog() {
        if (!checkPermission(this)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    public static boolean checkPermission(final Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                // permission  granted
                if (checkPermission(this)) {
                    /*
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 900000, 500000,
                            mLocationListener, this.getMainLooper());
                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 900000, 500000,
                            mLocationListener, this.getMainLooper());
                            */
                    mLocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,mLocationListener, this.getMainLooper());
                    mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,mLocationListener, this.getMainLooper());
                }
                showQueryToast=true;
            } else {
                loc_lat=0;
                loc_lon=0;
                StreamLocalData.loc_lat=loc_lat;
                StreamLocalData.loc_lon=loc_lon;
                if (!isPlayingStream) {
                    GetNetiBest best = new GetNetiBest(this, loc_lat, loc_lon);
                    best.execute((Void) null);
                }
            }
        }
    }
    @Override
    public void asyncGetNetiBestComplete(String res)
    {

        try {
            if (progress.isShowing()) {
                Log.d(TAG, "location - not showing progress");
                progress.dismiss();
            }
        }catch (Exception e) {}

        try {
            String[] results = res.split(":");

            Log.d(TAG, "GetNetiBest Complete: " + results + "");

            StreamLocalData.login = results[0];
            StreamLocalData.pass = results[1];

            Log.d(TAG, "GetNetiBest Complete: " + results + " login: " + StreamLocalData.login + " pass: " + StreamLocalData.pass);

            String[] m = results[2].split("\\.");
            String miles = m[0];
            StreamLocalData.distance = miles;

            hasConnectionData = true;
            butStream.setClickable(true);  // need to get creds

            dataNetiLogin.setText(results[0] + " : " + results[3]);
            dataDistance.setText(miles + " miles from " + StreamLocalData.city[StreamLocalData.selected_location]);

            if (showQueryToast) {
                if (showConnectionDistance) {
                    Toast t = Toast.makeText(mAppContext, "Connection distance based on GPS: " + results[2], Toast.LENGTH_SHORT);
                    t.show();
                }
            }
            showConnectionDistance = false;
        }catch (Exception e) {
            hasConnectionData = true;
            butStream.setClickable(true);
            StreamLocalData.distance="10";
            StreamLocalData.login ="userva";
            StreamLocalData.pass = "userva";
            dataNetiLogin.setText("userva : userva");
            dataDistance.setText("10 miles from No City");


        }

    }

    /**
     * called when the stream ends
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        Log.d(TAG, "ACTIVITY--- Stream Complete - Getting Data " + " playing stream: " + isPlayingStream);

        isPlayingStream=false;
        if(!isPlayingStream)
            gdata.execute((Void) null);
        Log.d(TAG, "Stream Complete - Getting Data");


    }//onActivityResult

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        Log.d(TAG, "  onKey called");
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gDetector.onTouchEvent(event);
        // Be sure to call the superclass implementation
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent event) {
        // gestureText.setText ("onDown");
        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2,
                           float velocityX, float velocityY) {
        if (hasConnectionData) {
            dataControlIP.setText(StreamLocalData.controlip);
            //newData.setText("");
            Intent intent = new Intent(getApplicationContext(), StreamActivity.class);
            startActivityForResult(intent, 1);
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        //gestureText.setText("onLongPress");
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2,
                            float distanceX, float distanceY) {
        //gestureText.setText("onScroll");
        return true;
    }

    @Override
    public void onShowPress(MotionEvent event) {
        // gestureText.setText("onShowPress");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        //gestureText.setText("onSingleTapUp");
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        // gestureText.setText("onDoubleTap");
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        //gestureText.setText("onDoubleTapEvent");
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        //gestureText.setText("onSingleTapConfirmed");
        return true;
    }
}
