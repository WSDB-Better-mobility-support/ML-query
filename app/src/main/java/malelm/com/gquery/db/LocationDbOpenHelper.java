package malelm.com.gquery.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by amjed on 20/02/15.
 */

/**
 * This class extends SQLiteOpenHelper that will give me the ability to
 * create database update it and open a connection to it and close it
 */
public class LocationDbOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = "DATABASE_TAG";

    private static final String DB_NAME = "LocationDb";
    private static final int DB_VERSION = 3;
    public static final String TABLE_LOCATION = "LocationT";
    public static final String COL_ID = "id";
    public static final String COL_LAT = "lat";
    public static final String COL_LON = "lon";
    public static final String COL_SPEED = "speed";
    public static final String COL_TIME = "time";

    //second Table for the GUI longitude and latitude
    public static final String TABLE_GUI = "guiTable";
    public static final String COL_GUI_ID = "guiId";
    public static final String COL_SLAT = "slat";
    public static final String COL_SLON = "slon";
    public static final String COL_ELAT = "elat";
    public static final String COL_ELON = "elon";


    public static final String CREATE_TABLE ="CREATE TABLE " + TABLE_LOCATION +" ( "+
             COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_LAT + " DOUBLE, " +
            COL_LON + " DOUBLE, " +
            COL_SPEED + " DOUBLE, " +
            COL_TIME + " DOUBLE " +
            ")";

    public static final String CREATE_GUI_TABLE ="CREATE TABLE " + TABLE_GUI +" ( "+
             COL_GUI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_SLAT + " DOUBLE, " +
            COL_SLON + " DOUBLE, " +
            COL_ELAT + " DOUBLE, " +
            COL_ELON + " DOUBLE " +
            ")";


    public LocationDbOpenHelper(Context context) {
        super(context, DB_NAME, null , DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
        db.execSQL(CREATE_GUI_TABLE);
        Log.i(TAG , "Database is created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GUI);
        onCreate(db);
        Log.i(TAG , "Database is upgraded");
    }
}
