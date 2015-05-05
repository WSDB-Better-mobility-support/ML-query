package malelm.com.gquery;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.security.cert.LDAPCertStoreParameters;
import java.util.Calendar;
import java.util.List;

import malelm.com.gquery.dataModel.LocationUnit;
import malelm.com.gquery.db.LocationDataSource;

/**
 * 1- make this activity location aware. Using fussed location api
 * a- permissions
 * b- add dependencies for google play services
 * c-implements interfaces
 * d-check for the availability of google play services
 * e- Accessing google play services
 * f- Get a single location object
 * Get location updates
 * g-Create location request
 * h-Ask for the location updates
 * i-get the updates and display them
 * j-remove the location updates
 * 2- Database
 * 1- instantiate the LocationDatabase to have access to database functionality
 * 1.5 open/close connection to database
 * 1.5 put the open method in the onResume method to keep a persistence  connection to the database
 * 2- instantiate the LocationUnit to package up the data (Not very necessary here) but i chose to do it
 * 3- adding a row of data to database
 * 4- add to database control
 */
public class MainActivity extends ActionBarActivity
        //c
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static final int CHECK_INTERVAL = 10 * 1000;
    public static final int START_QUERYING_IN = 5 ;
    public static final int LOCATION_REQUEST_INTERVAL = 300;
    public static final int FASTEST_LOCATION_INTERVAL = 100;
    public static final int ERROR_CODE = 9001;
    private GoogleApiClient gac;
    private LocationRequest lr;
    private Location location;
    private LocationDataSource lDSource;
    private TextView tv;
    private AlarmManager am;
    private WakeLock wakelock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        widgetInitializer();
        //d
        if (!isGooglePlayServicesOk()) { return;}
        //e
        gac = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        if (gac != null) {
            gac.connect();
        }
        //g
        createLocationRequestObject();
        //1
        lDSource = new LocationDataSource(this);
        lDSource.open();

        //to prevent the app when it was crashed from using the old PendingIntent
        Intent i = new Intent(this, QueryReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this,0, i, 0);
        am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);

        // keep the cpu awake
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        wakelock.acquire();
    }

    private void widgetInitializer() {
        tv = (TextView) findViewById(R.id.textView);
    }

    public void gQuery(View v){
        Intent i = new Intent(this, QueryReceiver.class);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, START_QUERYING_IN);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), CHECK_INTERVAL, pi);
    }

    public void stopService(View v) {
        //Cancel the Alarm
        Intent i = new Intent(this, QueryReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this,0, i, 0);
        am.cancel(pi);
        // Stop the service
        Intent intent = new Intent(this, QueryReceiver.class);
        stopService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //1.5
        lDSource.open();
    }

    //@Override
   // protected void onPause() {
       // super.onPause();
        //1.5
      //  lDSource.close();
   // }


    private void createLocationRequestObject() {
        lr = new LocationRequest();
        lr.setInterval(LOCATION_REQUEST_INTERVAL);
        lr.setFastestInterval(FASTEST_LOCATION_INTERVAL);
        lr.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnected(Bundle bundle) {
        //f
        location = LocationServices.FusedLocationApi.getLastLocation(gac);

        tv.setText(
                "onConnection \n"+
               "Latit = " + location.getLatitude() + "\n"+
               "Longi = " + location.getLongitude() +"\n"+
               "Speed = " + location.getSpeed() +"\n"+
               "Accur = " + location.getAccuracy()
        );

        if (location != null) {
            //2
            LocationUnit lu = new LocationUnit();
            lu.setTime(System.currentTimeMillis());
            lu.setLat(location.getLatitude());
            lu.setLon(location.getLongitude());
            lu.setSpeed(location.getSpeed());
            //4
            if (lu != null) {
                //3
                lDSource.addRow(lu);
            }
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(gac, lr, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        //i
        this.location = location;

        tv.setText(
                "onLocationChanged \n"+
                        "Latit = " + location.getLatitude() + "\n"+
                        "Longi = " + location.getLongitude() +"\n"+
                        "Speed = " + location.getSpeed() +"\n"+
                        "Accur = " + location.getAccuracy()+"\n"

        );

        if (location != null) {
            //2
            LocationUnit lu = new LocationUnit();
            lu.setTime(System.currentTimeMillis());
            lu.setLat(location.getLatitude());
            lu.setLon(location.getLongitude());
            lu.setSpeed(location.getSpeed());
            //4
            if (lu != null) {
                //3
                lDSource.addRow(lu);
            }
        }
    }

    @Override
    protected void onDestroy() {

        //j
        lDSource.close();
        LocationServices.FusedLocationApi.removeLocationUpdates(gac, this);
        wakelock.release();
        super.onDestroy();
    }

    //d
    private boolean isGooglePlayServicesOk() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (status == ConnectionResult.SUCCESS) {
            return true;
        } else if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
            GooglePlayServicesUtil.getErrorDialog(status, this, ERROR_CODE);
        } else {
            Toast.makeText(this, "Can't connect to google play services", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}