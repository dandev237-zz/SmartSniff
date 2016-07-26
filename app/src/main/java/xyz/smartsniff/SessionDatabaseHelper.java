package xyz.smartsniff;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.Map;

/**
 * This class handles database operations such as reading, writing and upgrading.
 *
 * Autor: Daniel Castro García
 * Email: dandev237@gmail.com
 * Fecha: 30/06/2016
 */
public class SessionDatabaseHelper extends SQLiteOpenHelper {

    private long sessionId = 0, deviceId = 0, locationId = 0;

    //Singleton instance
    private static SessionDatabaseHelper singletonInstance;

    //Database info
    private static final String DATABASE_NAME = "sessionsDatabase";
    private static final int DATABASE_VERSION = 1;

    //Table Names
    private static final String TABLE_SESSIONS = "sessions";
    private static final String TABLE_DEVICES = "devices";
    private static final String TABLE_LOCATIONS = "locations";
    private static final String TABLE_ASOCSESSIONSDEVICES = "asocSessionsDevices";

    //Session Table Columns
    private static final String KEY_SESSION_ID = "id";
    private static final String KEY_SESSION_STARTDATE = "startDate";
    private static final String KEY_SESSION_ENDDATE = "endDate";

    //Device Table Columns
    private static final String KEY_DEVICE_ID = "id";
    private static final String KEY_DEVICE_SSID = "ssid";
    private static final String KEY_DEVICE_BSSID = "bssid";
    private static final String KEY_DEVICE_MANUFACTURER = "manufacturer";
    private static final String KEY_DEVICE_CHARACTERISTICS = "characteristics";
    private static final String KEY_DEVICE_TYPE = "type";

    //Location Table Columns
    private static final String KEY_LOCATION_ID = "id";
    private static final String KEY_LOCATION_DATE = "date";
    private static final String KEY_LOCATION_COORDINATES = "coordinates";

    //Association Table Columns
    private static final String KEY_ASSOCIATION_ID_SESSION_FK = "idSession";
    private static final String KEY_ASSOCIATION_ID_DEVICE_FK = "idDevice";
    private static final String KEY_ASSOCIATION_ID_LOCATION_FK = "idLocation";

    /*
    * Singleton pattern
    * In order to access the database connection:
    * SessionDatabaseHelper dbHelper = SessionDatabaseHelper.getInstance(this);
    */
    public static synchronized SessionDatabaseHelper getInstance(Context context){
        if (singletonInstance == null){
            singletonInstance = new SessionDatabaseHelper(context.getApplicationContext());
        }
        return singletonInstance;
    }

    //Constructor is private to prevent direct instantiation
    private SessionDatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /*
     * Configures database settings. Called when the database connection
     * is being configured.
     */
    @Override
    public void onConfigure(SQLiteDatabase db){
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    /*
     * Creates the database for the first time. If it already exists, this
     * method will not be called.
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_SESSIONS_TABLE = "CREATE TABLE " + TABLE_SESSIONS +
                "(" +
                KEY_SESSION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_SESSION_STARTDATE + " TEXT," +
                KEY_SESSION_ENDDATE + " TEXT" +
                ")";

        String CREATE_DEVICES_TABLE = "CREATE TABLE " + TABLE_DEVICES +
                "(" +
                KEY_DEVICE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_DEVICE_SSID + " TEXT," +
                KEY_DEVICE_BSSID + " TEXT UNIQUE," +        //MAC ADDRESS
                KEY_DEVICE_MANUFACTURER + " TEXT, " +
                KEY_DEVICE_CHARACTERISTICS + " TEXT," +
                KEY_DEVICE_TYPE + " TEXT" +
                ")";

        String CREATE_LOCATIONS_TABLE = "CREATE TABLE " + TABLE_LOCATIONS +
                "(" +
                KEY_LOCATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_LOCATION_DATE + " TEXT," +
                KEY_LOCATION_COORDINATES + " TEXT UNIQUE" +     //ONE LOCATION ONLY
                ")";

        String CREATE_ASSOCIATION_TABLE = "CREATE TABLE " + TABLE_ASOCSESSIONSDEVICES +
                "(" +
                KEY_ASSOCIATION_ID_SESSION_FK + " INTEGER REFERENCES " + TABLE_SESSIONS + "," +
                KEY_ASSOCIATION_ID_DEVICE_FK + " INTEGER REFERENCES " + TABLE_DEVICES + "," +
                KEY_ASSOCIATION_ID_LOCATION_FK + " INTEGER REFERENCES " + TABLE_LOCATIONS +
                ")";

        sqLiteDatabase.execSQL(CREATE_SESSIONS_TABLE);
        sqLiteDatabase.execSQL(CREATE_DEVICES_TABLE);
        sqLiteDatabase.execSQL(CREATE_LOCATIONS_TABLE);
        sqLiteDatabase.execSQL(CREATE_ASSOCIATION_TABLE);
    }

    /*
     * Called when the database needs to be upgraded (i.e. the database already
     * exists and the version is different from the version of the database that
     * exists in memory).
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //Simplest implementation possible
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSIONS);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICES);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ASOCSESSIONSDEVICES);

        onCreate(sqLiteDatabase);
    }

    //CRUD Operations

    public void addSession(Session session){
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try{
            ContentValues values = new ContentValues();
            values.put(KEY_SESSION_STARTDATE, session.getStartDate());
            values.put(KEY_SESSION_ENDDATE, session.getEndDate());

            long rowId = db.insertOrThrow(TABLE_SESSIONS, null, values);
            if(rowId > sessionId)           // 0 > -1
                sessionId = rowId;
            db.setTransactionSuccessful();
        }catch(SQLException e){
            Log.d("ADD SESSION TO DB", "ERROR WHILE ADDING A SESSION TO DB");
        }finally {
            db.endTransaction();
        }
    }

    public void addDevice (Device device){
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try{
            ContentValues values = new ContentValues();
            values.put(KEY_DEVICE_SSID, device.getSsid());
            values.put(KEY_DEVICE_BSSID, device.getBssid());
            values.put(KEY_DEVICE_MANUFACTURER, device.getManufacturer());
            values.put(KEY_DEVICE_CHARACTERISTICS, device.getCharacteristics());
            values.put(KEY_DEVICE_TYPE, device.getType().toString());


            long rowId = db.insertOrThrow(TABLE_DEVICES, null, values);
            if(rowId > deviceId)            // 0 > -1
                deviceId = rowId;
            db.setTransactionSuccessful();
        }catch(SQLException e){
            Log.d("ADD DEVICE TO DB", "ERROR WHILE ADDING A DEVICE TO DB");
            //The error is caused because the device already exists in the database.
            //We need to get the ID of such device.
            String[] fields = new String[]{KEY_DEVICE_ID};
            String[] args = new String[]{device.getBssid()};

            Cursor c = db.query(TABLE_DEVICES, fields, KEY_DEVICE_BSSID + "=?", args, null, null, null);
            //Check if there is at least one result
            if(c.moveToFirst()){
                deviceId = c.getInt(0);
                Log.d("ADD DEVICE TO DB", "EXISTING DEVICE ID FOUND: " + deviceId);
            }
        }finally {
            db.endTransaction();
        }
    }

    public void addLocation (Location location){
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try{
            ContentValues values = new ContentValues();
            values.put(KEY_LOCATION_DATE, location.getDate());
            values.put(KEY_LOCATION_COORDINATES, location.getCoordinatesString());

            long rowId = db.insertOrThrow(TABLE_LOCATIONS, null, values);
            if(rowId > locationId)           // 0 > -1
                locationId = rowId;
            db.setTransactionSuccessful();
        }catch(SQLException e){
            Log.d("ADD LOCATION TO DB", "ERROR WHILE ADDING A LOCATION TO DB");
        }finally {
            db.endTransaction();
        }
    }

    public void addAssociation(){
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try{
            ContentValues values = new ContentValues();
            values.put(KEY_ASSOCIATION_ID_SESSION_FK, sessionId);
            values.put(KEY_ASSOCIATION_ID_DEVICE_FK, deviceId);
            values.put(KEY_ASSOCIATION_ID_LOCATION_FK, locationId);

            db.insertOrThrow(TABLE_ASOCSESSIONSDEVICES, null, values);
            db.setTransactionSuccessful();
        }catch(SQLException e){
            Log.d("ADD ASSOCIATION TO DB", "ERROR WHILE ADDING AN ASSOCIATION TO DB");
        }finally {
            db.endTransaction();
        }
    }

    public void deleteDatabase(Context applicationContext){
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try{
            db.delete(TABLE_ASOCSESSIONSDEVICES, null, null);
            db.delete(TABLE_SESSIONS, null, null);
            db.delete(TABLE_LOCATIONS, null, null);
            db.delete(TABLE_DEVICES, null, null);

            db.setTransactionSuccessful();
            Toast.makeText(applicationContext, "Datos borrados satisfactoriamente", Toast.LENGTH_SHORT)
                        .show();
        }catch(SQLException e){
            Log.d("DELETE DATA FROM DB", "ERROR WHILE DELETING DATA FROM DB");
        }finally{
            db.endTransaction();
        }
    }

    /**
     * Returns all the locations stored in the internal database and the number
     * of devices found on each of them.
     * @return locationMap A Map with the data mentioned above
     */
    public Map<Location, Integer> selectLocationsForHeatmap(){
        Map<Location, Integer> locationMap = new ArrayMap<>();

        SQLiteDatabase db = getReadableDatabase();
        db.beginTransaction();
        try{
            String selectQuery = "SELECT * FROM " + TABLE_LOCATIONS;
            Cursor cursor = db.rawQuery(selectQuery, null);

            if(cursor.moveToFirst()){
                do {
                    String[] latlong =  cursor.getString(2).split(", ");
                    double latitude = Double.parseDouble(latlong[0]);
                    double longitude = Double.parseDouble(latlong[1]);
                    Location location = new Location(new LatLng(latitude, longitude));

                    String countQuery = "SELECT count(*) FROM " + TABLE_ASOCSESSIONSDEVICES
                            + " WHERE " + KEY_ASSOCIATION_ID_LOCATION_FK + " = "
                            + cursor.getInt(0);
                    Cursor countCursor = db.rawQuery(countQuery, null);
                    if(countCursor.moveToFirst())
                        locationMap.put(location, countCursor.getInt(0));
                }while(cursor.moveToNext());
            }

            db.setTransactionSuccessful();
        }catch(SQLException e){
            Log.d("selectLocationsHeatmap", "ERROR WHILE GETTING LOCATIONS FOR HEATMAP");
        }finally{
            db.endTransaction();
        }

        return locationMap;
    }

    /**
     * Returns the number of sessions stored in this database.
     * @return
     */
    public int selectSessions(){
        int numberOfSessions = 0;

        SQLiteDatabase db = getReadableDatabase();
        db.beginTransaction();
        try {
            String countQuery = "SELECT count(*) FROM " + TABLE_SESSIONS;
            Cursor countCursor = db.rawQuery(countQuery, null);
            if(countCursor.moveToFirst())
                numberOfSessions = countCursor.getInt(0);
        }catch(SQLException e){
            Log.d("selectLocationsHeatmap", "ERROR WHILE GETTING LOCATIONS FOR HEATMAP");
        }finally{
            db.endTransaction();
        }

        return numberOfSessions;
    }
}
