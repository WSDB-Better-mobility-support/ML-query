package malelm.com.gquery;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.Calendar;

public class MainActivity extends ActionBarActivity {

    public static final int CHECK_INTERVAL = 10 * 1000;
    public static final int START_QUERYING_IN = 5 ;
    public static final int ERROR_CODE = 9001;
    private AlarmManager am;
    private WakeLock wakelock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       // widgetInitializer();

        if (!isGooglePlayServicesOk()) { return;}
        //When the app is lunched start location service
        Intent intent = new Intent(this, LocationService.class);
        startService(intent);

        //to prevent the app when it was crashed from using the old PendingIntent
        Intent i = new Intent(this, QueryReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this,0, i, 0);
        am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);

        // keep the cpu awake
//        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
//        wakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MyWakelockTag");
//        wakelock.acquire();
    }

//    private void widgetInitializer() {
//        tv = (TextView) findViewById(R.id.textView);
//    }

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
        Intent intent2 = new Intent(this, LocationService.class);
        stopService(intent2);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //1.5

    }

    //@Override
   // protected void onPause() {
       // super.onPause();
        //1.5
      //  lDSource.close();
   // }







    @Override
    protected void onDestroy() {

        //j
      //  wakelock.release();
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

}