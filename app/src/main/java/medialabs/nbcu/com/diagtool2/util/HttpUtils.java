package medialabs.nbcu.com.diagtool2.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Bill on 1/24/17.
 */

public class HttpUtils {
    private static final String LOG_TAG = "HttpUtils";
    //ec2loader IP address
    public static final String ASSETS_HOST_IP = "http://23.21.66.213";
    public static final String ASSETS_HOST_PORT = "9080";
    public static final String ASSETS_HOST_APP_NAME = "appi";
    public static final String HOST_NAME = ASSETS_HOST_IP + ":" + ASSETS_HOST_PORT + "/" + ASSETS_HOST_APP_NAME + "/";
    //ec2loader IP address
    public static final String CONTENT_HOST_IP = "http://23.21.66.213";
    static public String requestContent(String apiUrl) {
        try {
            URL url = new URL(HOST_NAME + apiUrl);
            Log.i(LOG_TAG,url.toString());
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                return stringBuilder.toString();
            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            Log.e(LOG_TAG,e.toString());
            return null;
        }
    }
}
