package malelm.com.gquery; /**
 * Created by amjed on 21/02/15.
 */

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import malelm.com.gquery.logging.*;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static java.lang.Thread.sleep;

public class GoogleSpectrumQuery
{
    // API key.
    private static String apiKey1 = "AIzaSyBW2b0HgPnv4922F9b6KtH7P9CrPU2H4GU";
    private static String apiKey2 = "AIzaSyDl6Oef9zk_nrGLAxEfBawVCt1y9NryXd0";
    private static String apiKey3 = "AIzaSyAWUyKg34_1DiKyjIRm6RpVnqDYFEV-Jeg";
    private static String apiKey4 = "AIzaSyCDb6siAMhx1pPN_Wdms6_BdZ5uLX5yeCc";
    private static String apiKey5 = "AIzaSyBOSKwMyPII6Jm7fq2Qy6yk7vZBXQhqT6o";

    private static String apiKey6 = "AIzaSyBNk7twXyeLVReBPiyPx2_2fnvyOpAzUJQ";
    private static String apiKey7 = "AIzaSyA0fhxB6ElnqYXPpyZnv4XmqvpvFca7er0";
    private static String apiKey8 = "AIzaSyA49B-Vm6Nh0td03v4eJXpOQ2MAhzaeBho";
    private static String apiKey9 = "AIzaSyCgCvElFlvB413f_B1r0K_GokydOPjssfw";
    private static String apiKey10 = "AIzaSyAEHyYlYbuealIsdSWXc4G3HLR6MU8VElk";
    private static String apiKey11 = "AIzaSyAF3D6geK2hJm5iDtadkbDYe66mwiVEJtE";
    private static String apiKey12 = "AIzaSyAFnWosvYlOgm4PtzcgqoOZ6XsH4twOHjE";
    private static String apiKey13 = "AIzaSyDSSLVLmXG-1Z7TB4KkLWy912hlwGzo6ig";
    private static String apiKey14 = "AIzaSyDgLBJTUpwPQ2q1HOEMxkAKG8qYmH4rqHo";
    private static String apiKey15 = "AIzaSyDRcbZiM8y8lPqmEp7p7CBWku9GmnV5uPQ";


    private static int methodCallCounter = 0;

    private static int numOfLoc = 1;
    public static long timeBeforeQuery = 0;
    public static long timeAfterQuery = 0;
    public static int sleepTime = 1000;


    // Constants
    private final static String fccId = "TEST";
    private final static String mode = "MODE_1";

    private static JSONObject createFromStrings(String ... str) throws JSONException
    {
        JSONObject object = new JSONObject();

        for (int i=0; i<str.length; i+=2)
            object.put(str[i], str[i+1]);

        return object;
    }

    private static JSONObject createPoint(double latitude, double longitude) throws JSONException
    {
        JSONObject object = new JSONObject();

        JSONObject center = new JSONObject();
        center.put("latitude", latitude);
        center.put("longitude", longitude);

        JSONObject point = new JSONObject();
        point.put("center", center);

        object.put("point", point);

        return object;
    }

    private static JSONObject createAntenna() throws JSONException
    {
        JSONObject object = new JSONObject();

        object.put("height", 30.0);
        object.put("heightType", "AGL");

        return object;
    }

    private static JSONObject createOwner() throws JSONException
    {
        JSONObject object = new JSONObject();
        object.put("owner", new JSONObject());

        return object;
    }

    //	private static JSONObject createQuery(double latitude, double longitude) throws JSONException
    private static JSONArray createQuery(double latitude ,double longitude, double latChangeFactor , double lonChangeFactor , double numOfLoc) throws JSONException
    {
        // create a JSON array to send more than one json objects

        methodCallCounter+=numOfLoc;
        if (methodCallCounter  < 1000){
            //do nothing
        }
        else if(methodCallCounter < 2000){
            apiKey1 = apiKey2;
        }else if(methodCallCounter < 3000){
            apiKey1 = apiKey3;
        }else if(methodCallCounter < 4000){
            apiKey1 = apiKey4;
        }else if(methodCallCounter < 5000){
            apiKey1 = apiKey5;
        }else if(methodCallCounter < 6000){
            apiKey1 = apiKey6;
        }else if(methodCallCounter < 7000){
            apiKey1 = apiKey7;
        }else if(methodCallCounter < 8000){
            apiKey1 = apiKey8;
        }else if(methodCallCounter < 9000){
            apiKey1 = apiKey9;
        }else if(methodCallCounter < 10000){
            apiKey1 = apiKey10;
        }else if(methodCallCounter < 11000){
            apiKey1 = apiKey11;
        }else if(methodCallCounter < 12000){
            apiKey1 = apiKey12;
        }else if(methodCallCounter < 13000){
            apiKey1 = apiKey13;
        }else if(methodCallCounter < 14000){
            apiKey1 = apiKey14;
        }else{
            apiKey1 = apiKey15;
        }

        JSONArray arr = new JSONArray();

        for(int i = 0 ; i < numOfLoc ; i++){

            // 0.02 * i = a step forward each iteration
            double lat =latitude+0.001*i *  latChangeFactor;
            double lng = longitude+0.001*i *  lonChangeFactor ;

            JSONObject object = new JSONObject();
            object.put("jsonrpc", "2.0");
            object.put("method", "spectrum.paws.getSpectrum");
            object.put("apiVersion", "v1explorer");
            object.put("id", "any_string");
            JSONObject params = new JSONObject();
            params.put("type", "AVAIL_SPECTRUM_REQ");
            params.put("version", "1.0");
            params.put("deviceDesc", createFromStrings("serialNumber", "your_serial_number", "fccId", fccId, "fccTvbdDeviceType", mode));
            params.put("location", createPoint(lat, lng));
            params.put("antenna", createAntenna());
            params.put("owner", createOwner());
            params.put("key", apiKey1);
            object.put("params", params);

            arr.put(object);
            object = null;
        }
        return arr	;
    }

    public static void query(final double lat, final double lon,
                             final double latChangeFactor, final double lonChangeFactor , final double numOfLoc)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    timeBeforeQuery = System.currentTimeMillis();
                    sleep(sleepTime); // This extra time is to allow the location service provide the app with location info.
                    Logger.log(String.format("google-query-start"));
                    HttpClient client = new DefaultHttpClient();
                    HttpPost request = new HttpPost("https://www.googleapis.com/rpc");
                    HttpConnectionParams.setConnectionTimeout(client.getParams(), 20 * 1000);
                    HttpConnectionParams.setSoTimeout(client.getParams(), 20 * 1000);

                    request.addHeader("Content-Type", "application/json");
                    //TODO createQuery needs to take the lonChangeFactor/latChangeFactor into account
                    request.setEntity(new StringEntity(createQuery(lat, lon, latChangeFactor, lonChangeFactor, numOfLoc).toString(), HTTP.UTF_8));
                    HttpResponse response = client.execute(request);

                    BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    String line = "";
                    boolean first = true;
                    while ((line = rd.readLine()) != null)
                    {
                        if (first)
                        {
                            Logger.log("google-query-first-data");
                            first = false;
                        }
                        Logger.log(line);
                    }
                    Logger.log("google-query-done");

                    timeAfterQuery = System.currentTimeMillis() ;
                }catch(UnsupportedEncodingException e)
                {
                    Logger.log("google-query-error Unsupported Encoding: " + e.getMessage());
                    timeAfterQuery = System.currentTimeMillis() ;
                }catch(JSONException e)
                {
                    Logger.log("google-query-error JSON exception: " + e.getMessage());
                    timeAfterQuery = System.currentTimeMillis() ;
                }catch(IOException e)
                {
                    Logger.log("google-query-error i/o exception: " + e.getMessage());
                    timeAfterQuery = System.currentTimeMillis() ;
                }catch(Exception e){
                    Logger.log("google-query-error exception: " + e.getMessage());
                    timeAfterQuery = System.currentTimeMillis() ;
                }

            }
        }).start();

    }

}
