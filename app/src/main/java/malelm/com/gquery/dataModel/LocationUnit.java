package malelm.com.gquery.dataModel;

/**
 * Created by amjad on 20/02/15.
 */

import android.util.Log;

/**
 * This class represent on data unit
 */
public class LocationUnit {

    private static final String TAG = "DATABASE_TAG";

    private double lat;
    private double lon;
    private double speed;
    private long time;
    private long id;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        Log.i(TAG , "Lat is added");
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        Log.i(TAG , "Lon is added");
        this.lon = lon;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        Log.i(TAG , "Speed is added");
        this.speed = speed;
    }

    public double getTime() {
        return time;
    }

    public void setTime(long time) {
        Log.i(TAG , "Time is added");
        this.time = time;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


}
