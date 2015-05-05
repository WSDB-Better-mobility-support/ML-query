package malelm.com.gquery.db;

/**
 * Created by amjad on 20/02/15.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import malelm.com.gquery.dataModel.LocationUnit;
import malelm.com.gquery.dataModel.SELatLon;

/**
 * This class will be an interface between LocationDbOpenHelper and the rest of the app
 * This class will instantiate the LocationDbOpenHelper class
 * through the returned object I'll have the ability to open connection to the db and to close it or to add a row of data to it
 */

/**
 * 1- by instantiate this class you will instantiate the LocationDbOpenHelper class and create the database if not exists
 * 2- open a connection to the database
 * 3- close the connection
 * 4- add a row of data to the database
 */

public class LocationDataSource {

    private static final String TAG = "DATABASE_TAG";

    SQLiteOpenHelper dbHelper;
    SQLiteDatabase db;

    private static final String[] allCols = {
      LocationDbOpenHelper.COL_ID,
      LocationDbOpenHelper.COL_LAT,
      LocationDbOpenHelper.COL_LON,
      LocationDbOpenHelper.COL_SPEED,
      LocationDbOpenHelper.COL_TIME
    };

    //1
    public LocationDataSource(Context context){
        dbHelper = new LocationDbOpenHelper(context);
    }

    //2
    public void open(){
       db =  dbHelper.getWritableDatabase();
        Log.i(TAG, "Database is opened");
    }

    //3
    public void close(){
        dbHelper.close();
        Log.i(TAG, "Database is closed");
    }
    //4
    public void addRow(LocationUnit lu){
        open();
        ContentValues values = new ContentValues();

        values.put(LocationDbOpenHelper.COL_LAT ,lu.getLat() );
        values.put(LocationDbOpenHelper.COL_LON, lu.getLon());
        values.put(LocationDbOpenHelper.COL_SPEED, lu.getSpeed());
        values.put(LocationDbOpenHelper.COL_TIME, lu.getTime());
        Log.i(TAG , values.toString());
        long inId = db.insert(LocationDbOpenHelper.TABLE_LOCATION, null , values);
        Log.i(TAG , "Data inserted id = "+ inId);
    }
    public void addSELatLon(double slat , double slon , double elat , double elon){
        open();
        ContentValues values = new ContentValues();

        values.put(LocationDbOpenHelper.COL_SLAT ,slat);
        values.put(LocationDbOpenHelper.COL_SLON, slon);
        values.put(LocationDbOpenHelper.COL_ELAT, elat);
        values.put(LocationDbOpenHelper.COL_ELON, elon);
        Log.i(TAG , values.toString());
        long inId = db.insert(LocationDbOpenHelper.TABLE_GUI, null , values);
        Log.i(TAG , "Data inserted int GUI table with id = "+ inId);
    }

    public SELatLon getSELatLon(){
        Cursor cursor =  db.rawQuery("SELECT * FROM "+ LocationDbOpenHelper.TABLE_GUI + " LIMIT 2" , null);
        SELatLon se = new SELatLon();
        if(cursor.getCount() > 1){
            while (cursor.moveToNext()){
                se.setSlat(cursor.getDouble(cursor.getColumnIndex(LocationDbOpenHelper.COL_SLAT)));
                se.setSlon(cursor.getDouble(cursor.getColumnIndex(LocationDbOpenHelper.COL_SLON)));
                se.setElat(cursor.getDouble(cursor.getColumnIndex(LocationDbOpenHelper.COL_ELAT)));
                se.setElon(cursor.getDouble(cursor.getColumnIndex(LocationDbOpenHelper.COL_ELON)));
            }
        }
        return se;
    }

    public List<LocationUnit> findData( long time1 , long time2){
        Cursor cursor = db.rawQuery("SELECT * FROM " + LocationDbOpenHelper.TABLE_LOCATION +
                " WHERE "+LocationDbOpenHelper.COL_TIME +" > " + time1 +" AND "+
                LocationDbOpenHelper.COL_TIME+ " < " + time2 +" ORDER BY id DESC "
                ,null);
        List<LocationUnit> locations = getLocationUnits(cursor);
        //This may return a null object if the db is empty
            return locations;
    }
    public List<LocationUnit> findData( long time){
        Cursor cursor = db.rawQuery("SELECT * FROM " + LocationDbOpenHelper.TABLE_LOCATION +
                " WHERE "+LocationDbOpenHelper.COL_TIME +" > " + time +" ORDER BY id DESC " ,null);
        List<LocationUnit> locations = getLocationUnits(cursor);
        //This may return a null object if the db is empty
        return locations;
    }
    public LocationUnit oneExtraLoc( long time){
        Cursor cursor = db.rawQuery("SELECT * FROM " + LocationDbOpenHelper.TABLE_LOCATION +
                " WHERE "+LocationDbOpenHelper.COL_TIME +" < " + time +" LIMIT 1 " ,null);
        List<LocationUnit> locations = getLocationUnits(cursor);
        LocationUnit loc = locations.get(0);
        //This may return a null object if the db is empty
        return loc;
    }

    // this method will return only the first raw of data
    public LocationUnit findFirstRow(){
        //Cursor cursor = db.query(LocationDbOpenHelper.TABLE_LOCATION , allCols,null,null,null,null,null,"1");
        Cursor cursor = db.rawQuery("SELECT * FROM "+LocationDbOpenHelper.TABLE_LOCATION +" ORDER BY id DESC LIMIT 1 ", null);
        LocationUnit lu = new LocationUnit();
        if(cursor != null) {
            cursor.moveToFirst();
                Log.d("DATABASE_lDSource", "FROM findFirstRow");
                lu.setId(cursor.getLong(cursor.getColumnIndex(LocationDbOpenHelper.COL_ID)));
                lu.setTime(cursor.getLong(cursor.getColumnIndex(LocationDbOpenHelper.COL_TIME)));
                lu.setLat(cursor.getDouble(cursor.getColumnIndex(LocationDbOpenHelper.COL_LAT)));
                lu.setLon(cursor.getDouble(cursor.getColumnIndex(LocationDbOpenHelper.COL_LON)));
                lu.setSpeed(cursor.getDouble(cursor.getColumnIndex(LocationDbOpenHelper.COL_SPEED)));
                cursor.close();

        }else{
            Log.d("DATA_CURSOR" , "cursor is null");
        }
        return lu;
     }

    private List<LocationUnit> getLocationUnits(Cursor cursor) {
        List<LocationUnit> locations = new ArrayList<>();
        Log.i(TAG, "Returned " + cursor.getCount() + " rows");
        if (cursor.getCount() > 0){
            while (cursor.moveToNext()){
                Log.d("DATABASE_lDSource" , "FROM getLocationUnits");
                LocationUnit lu = new LocationUnit();
                lu.setId(cursor.getLong( cursor.getColumnIndex(LocationDbOpenHelper.COL_ID) ) );
                lu.setTime(cursor.getLong(cursor.getColumnIndex(LocationDbOpenHelper.COL_TIME)));
                lu.setLat(cursor.getDouble(cursor.getColumnIndex(LocationDbOpenHelper.COL_LAT)));
                lu.setLon(cursor.getDouble(cursor.getColumnIndex(LocationDbOpenHelper.COL_LON)));
                lu.setSpeed(cursor.getDouble(cursor.getColumnIndex(LocationDbOpenHelper.COL_SPEED)));
                locations.add(lu);
            }
        }
        return locations;
    }

}
