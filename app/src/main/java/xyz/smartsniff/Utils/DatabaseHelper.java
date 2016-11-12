package xyz.smartsniff.Utils;

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

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import xyz.smartsniff.Model.Association;
import xyz.smartsniff.Model.Device;
import xyz.smartsniff.Model.DeviceType;
import xyz.smartsniff.Model.Location;
import xyz.smartsniff.Model.Session;

/**
 * This class handles database operations such as reading, writing and upgrading.
 *
 * Author: Daniel Castro García
 * Email: dandev237@gmail.com
 * Date: 30/06/2016
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    //Database info
    private static final String DATABASE_NAME = "sessionsDatabase";
    private static final int DATABASE_VERSION = 4;
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
    private static final String KEY_DEVICE_CHANNELWIDTH = "channelWidth";
    private static final String KEY_DEVICE_FREQUENCY = "frequency";
    private static final String KEY_DEVICE_INTENSITY = "signalIntensity";
    private static final String KEY_DEVICE_TYPE = "type";
    //Location Table Columns
    private static final String KEY_LOCATION_ID = "id";
    private static final String KEY_LOCATION_DATE = "date";
    private static final String KEY_LOCATION_COORDINATES = "coordinates";
    //Association Table Columns
    private static final String KEY_ASSOCIATION_ID_SESSION_FK = "idSession";
    private static final String KEY_ASSOCIATION_ID_DEVICE_FK = "idDevice";
    private static final String KEY_ASSOCIATION_ID_LOCATION_FK = "idLocation";
    //Singleton instance
    private static DatabaseHelper singletonInstance;
    private long sessionId = 0, deviceId = 0, locationId = 0;

    //Constructor is private to prevent direct instantiation
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Singleton pattern
     * In order to access the database connection:
     * DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
     */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (singletonInstance == null) {
            singletonInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return singletonInstance;
    }

    /**
     * Configures database settings. Called when the database connection
     * is being configured.
     */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    /**
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
                KEY_DEVICE_SSID + " TEXT NOT NULL," +
                KEY_DEVICE_BSSID + " TEXT UNIQUE," +        //MAC ADDRESS
                KEY_DEVICE_MANUFACTURER + " TEXT, " +
                KEY_DEVICE_CHARACTERISTICS + " TEXT," +
                KEY_DEVICE_CHANNELWIDTH + " TEXT," +
                KEY_DEVICE_FREQUENCY + " INTEGER," +
                KEY_DEVICE_INTENSITY + " INTEGER," +
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
                ",PRIMARY KEY (" + KEY_ASSOCIATION_ID_SESSION_FK + ", " + KEY_ASSOCIATION_ID_DEVICE_FK + ", " +
                KEY_ASSOCIATION_ID_LOCATION_FK + ")" +
                ")";

        sqLiteDatabase.execSQL(CREATE_SESSIONS_TABLE);
        sqLiteDatabase.execSQL(CREATE_DEVICES_TABLE);
        sqLiteDatabase.execSQL(CREATE_LOCATIONS_TABLE);
        sqLiteDatabase.execSQL(CREATE_ASSOCIATION_TABLE);
    }

    /**
     * Called when the database needs to be upgraded (i.e. the database already
     * exists and the version is different from the version of the database that
     * exists in memory).
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSIONS);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICES);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ASOCSESSIONSDEVICES);

        onCreate(sqLiteDatabase);
    }

    //Database operations

    public void addSession(Session session) {
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_SESSION_STARTDATE, session.getStartDateString());

            long rowId = db.insertOrThrow(TABLE_SESSIONS, null, values);
            if (rowId > sessionId)           // 0 > -1
                sessionId = rowId;
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.d("ADD SESSION TO DB", "ERROR WHILE ADDING A SESSION TO DB");
        } finally {
            db.endTransaction();
        }
    }

    public void updateSession(String formattedEndDate) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_SESSION_ENDDATE, formattedEndDate);
        String[] args = new String[]{Long.toString(sessionId)};

        db.update(TABLE_SESSIONS, values, KEY_SESSION_ID + " = ?", args);
    }

    public void addDevice(final Device device) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = null;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_DEVICE_SSID, device.getSsid());
            values.put(KEY_DEVICE_BSSID, device.getBssid());
            values.put(KEY_DEVICE_MANUFACTURER, device.getManufacturer());
            values.put(KEY_DEVICE_CHARACTERISTICS, device.getCharacteristics());
            values.put(KEY_DEVICE_CHANNELWIDTH, device.getChannelWidth());
            values.put(KEY_DEVICE_FREQUENCY, device.getFrequency());
            values.put(KEY_DEVICE_INTENSITY, device.getSignalIntensity());
            values.put(KEY_DEVICE_TYPE, device.getType().toString());

            long rowId = db.insertOrThrow(TABLE_DEVICES, null, values);
            if (rowId > deviceId)            // 0 > -1
                deviceId = rowId;
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            //Log.d("ADD DEVICE TO DB", "ERROR WHILE ADDING A DEVICE TO DB");
            //The error is caused because the device already exists in the database.
            //We need to get the ID of such device.
            String[] fields = new String[]{KEY_DEVICE_ID};
            String[] args = new String[]{device.getBssid()};

            c = db.query(TABLE_DEVICES, fields, KEY_DEVICE_BSSID + "=?", args, null, null, null);
            //Check if there is at least one result
            if (c.moveToFirst()) {
                deviceId = c.getInt(c.getColumnIndex(KEY_DEVICE_ID));
                //Log.d("ADD DEVICE TO DB", "EXISTING DEVICE ID FOUND: " + deviceId);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            if (c != null)
                c.close();
        }
    }

    public void updateDeviceWithManufacturer(Device device) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_DEVICE_MANUFACTURER, device.getManufacturer());
        String[] args = new String[]{device.getBssid()};

        db.update(TABLE_DEVICES, values, KEY_DEVICE_BSSID + " = ?", args);
    }

    public String getManufacturerOfDevice(String deviceBssid) {
        SQLiteDatabase db = getReadableDatabase();
        String manufacturer = "";

        Cursor c = null;
        db.beginTransaction();
        try {
            String selectQuery = "SELECT " + KEY_DEVICE_MANUFACTURER + " FROM " + TABLE_DEVICES + " WHERE " +
                    KEY_DEVICE_BSSID + "=?";

            c = db.rawQuery(selectQuery, new String[]{deviceBssid});
            if (c.moveToFirst())
                manufacturer = c.getString(c.getColumnIndex(KEY_DEVICE_MANUFACTURER));
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.d("GET MANUFACTURER", "ERROR WHILE GETTING MANUFACTURER FROM DEVICE");
        } finally {
            db.endTransaction();
            if (c != null)
                c.close();
        }

        return manufacturer;
    }

    public boolean deviceExistsInDb(Device device) {
        boolean exists = false;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        db.beginTransaction();
        try {
            String[] fields = new String[]{KEY_DEVICE_ID};
            String[] args = new String[]{device.getBssid()};

            cursor = db.query(TABLE_DEVICES, fields, KEY_DEVICE_BSSID + "=?", args, null, null, null);
            //Check if there is at least one result
            if (cursor.moveToFirst()) {
                deviceId = cursor.getInt(cursor.getColumnIndex(KEY_DEVICE_ID));
                exists = true;
            }
        } catch (SQLException e) {
            Log.d("selectLocationsHeatmap", "ERROR WHILE GETTING LOCATIONS FOR HEATMAP");
        } finally {
            db.endTransaction();
            if (cursor != null)
                cursor.close();
        }

        return exists;
    }

    public void addLocation(Location location) {
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_LOCATION_DATE, location.getDateString());
            values.put(KEY_LOCATION_COORDINATES, location.getCoordinatesString());

            long rowId = db.insertOrThrow(TABLE_LOCATIONS, null, values);
            if (rowId > locationId)           // 0 > -1
                locationId = rowId;
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            //Log.d("ADD LOCATION TO DB", "ERROR WHILE ADDING A LOCATION TO DB");
            //The error is caused because the location already exists in the database.
            //We need to get the ID of such location.
            String[] fields = new String[]{KEY_LOCATION_ID};
            String[] args = new String[]{location.getCoordinatesString()};

            Cursor c = db.query(TABLE_LOCATIONS, fields, KEY_LOCATION_COORDINATES + "=?", args, null, null, null);
            //Check if there is at least one result
            if (c.moveToFirst()) {
                locationId = c.getInt(c.getColumnIndex(KEY_LOCATION_ID));
            }
            c.close();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void addAssociation() {
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_ASSOCIATION_ID_SESSION_FK, sessionId);
            values.put(KEY_ASSOCIATION_ID_DEVICE_FK, deviceId);
            values.put(KEY_ASSOCIATION_ID_LOCATION_FK, locationId);

            db.insertOrThrow(TABLE_ASOCSESSIONSDEVICES, null, values);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.d("ADD ASSOCIATION TO DB", "ASSOCIATION ALREADY EXISTS: " + sessionId + "," + deviceId + "," + locationId);
        } finally {
            db.endTransaction();
        }
    }

    public void deleteDatabase(Context applicationContext) {
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            db.delete(TABLE_ASOCSESSIONSDEVICES, null, null);
            db.delete(TABLE_SESSIONS, null, null);
            db.delete(TABLE_LOCATIONS, null, null);
            db.delete(TABLE_DEVICES, null, null);

            db.setTransactionSuccessful();
            Toast.makeText(applicationContext, "Datos borrados satisfactoriamente", Toast.LENGTH_SHORT)
                    .show();
        } catch (SQLException e) {
            //Log.d("DELETE DATA FROM DB", "ERROR WHILE DELETING DATA FROM DB");
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Returns all the locations stored in the internal database and the number
     * of devices found on each of them.
     *
     * @return locationMap A Map with the data mentioned above
     */
    public Map<Location, Integer> selectLocationsForHeatmap() {
        Map<Location, Integer> locationMap = new ArrayMap<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        db.beginTransaction();
        try {
            String selectQuery = "SELECT * FROM " + TABLE_LOCATIONS;
            cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    String[] latlong = cursor.getString(2).split(", ");
                    double latitude = Double.parseDouble(latlong[0]);
                    double longitude = Double.parseDouble(latlong[1]);
                    Location location = new Location(new LatLng(latitude, longitude));

                    String countQuery = "SELECT count(*) FROM " + TABLE_ASOCSESSIONSDEVICES
                            + " WHERE " + KEY_ASSOCIATION_ID_LOCATION_FK + " = "
                            + cursor.getInt(0);
                    Cursor countCursor = db.rawQuery(countQuery, null);
                    if (countCursor.moveToFirst())
                        locationMap.put(location, countCursor.getInt(0));
                    countCursor.close();
                } while (cursor.moveToNext());
            }

            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.d("selectLocationsHeatmap", "ERROR WHILE GETTING LOCATIONS FOR HEATMAP");
        } finally {
            db.endTransaction();
            if (cursor != null)
                cursor.close();
        }

        return locationMap;
    }

    /**
     * Returns the number of sessions stored in this database.
     */
    public int getNumberOfSessions() {
        int numberOfSessions = 0;

        SQLiteDatabase db = getReadableDatabase();
        Cursor countCursor = null;
        db.beginTransaction();
        try {
            String countQuery = "SELECT count(*) FROM " + TABLE_SESSIONS;
            countCursor = db.rawQuery(countQuery, null);
            if (countCursor.moveToFirst())
                numberOfSessions = countCursor.getInt(0);
        } catch (SQLException e) {
            Log.d("selectLocationsHeatmap", "ERROR WHILE GETTING LOCATIONS FOR HEATMAP");
        } finally {
            db.endTransaction();
            if (countCursor != null)
                countCursor.close();
        }

        return numberOfSessions;
    }

    //Querying methods

    public List<Association> getAllAssociations() {
        List<Association> associations = new LinkedList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        db.beginTransaction();
        try {
            String selectQuery = "SELECT * FROM " + TABLE_ASOCSESSIONSDEVICES;
            cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    int sessionId = cursor.getInt(cursor.getColumnIndex(KEY_ASSOCIATION_ID_SESSION_FK));
                    int deviceId = cursor.getInt(cursor.getColumnIndex(KEY_ASSOCIATION_ID_DEVICE_FK));
                    int locationId = cursor.getInt(cursor.getColumnIndex(KEY_ASSOCIATION_ID_LOCATION_FK));

                    Association queriedAssociation = new Association(sessionId, deviceId, locationId);
                    associations.add(queriedAssociation);
                } while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            Log.d("getAllAssociations", "ERROR WHILE GETTING ASSOCIATIONS FROM DB");
        } finally {
            db.endTransaction();
            if (cursor != null)
                cursor.close();
        }

        return associations;
    }

    public Session getSession(int id) {
        Session session = null;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        db.beginTransaction();
        try {
            String selectQuery = "SELECT * FROM " + TABLE_SESSIONS + " WHERE " + KEY_SESSION_ID + "= " + id;
            cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                Date startDate = Utils.reverseFormatDate(cursor.getString(cursor.getColumnIndex(KEY_SESSION_STARTDATE)));
                Date endDate = Utils.reverseFormatDate(cursor.getString(cursor.getColumnIndex(KEY_SESSION_ENDDATE)));

                session = new Session(startDate, endDate);
            }
        } catch (SQLException e) {
            Log.d("getSession", "ERROR WHILE GETTING SESSION FROM DB");
        } finally {
            db.endTransaction();
            if (cursor != null)
                cursor.close();
        }

        return session;
    }

    public Location getLocation(int id) {
        Location location = null;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        db.beginTransaction();
        try {
            String selectQuery = "SELECT * FROM " + TABLE_LOCATIONS + " WHERE " + KEY_SESSION_ID + "= " + id;
            cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                Date date = Utils.reverseFormatDate(cursor.getString(cursor.getColumnIndex(KEY_LOCATION_DATE)));
                String coordinatesString = cursor.getString(cursor.getColumnIndex(KEY_LOCATION_COORDINATES));

                //coordinates string format => "lat, long"
                String[] splitString = coordinatesString.trim().split(",");
                LatLng coordinates = new LatLng(Double.parseDouble(splitString[0]), Double.parseDouble(splitString[1]));

                location = new Location(date, coordinates);
            }
        } catch (SQLException e) {
            Log.d("getLocation", "ERROR WHILE GETTING LOCATION FROM DB");
        } finally {
            db.endTransaction();
            if (cursor != null)
                cursor.close();
        }

        return location;
    }

    public Device getDevice(int id) {
        Device device = null;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        db.beginTransaction();
        try {
            String selectQuery = "SELECT * FROM " + TABLE_DEVICES + " WHERE " + KEY_DEVICE_ID + "= " + id;
            cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                String ssid = cursor.getString(cursor.getColumnIndex(KEY_DEVICE_SSID));
                String bssid = cursor.getString(cursor.getColumnIndex(KEY_DEVICE_BSSID));
                String characteristics = cursor.getString(cursor.getColumnIndex(KEY_DEVICE_CHARACTERISTICS));
                String manufacturer = cursor.getString(cursor.getColumnIndex(KEY_DEVICE_MANUFACTURER));
                String channelWidth = cursor.getString(cursor.getColumnIndex(KEY_DEVICE_CHANNELWIDTH));
                int frequency = cursor.getInt(cursor.getColumnIndex(KEY_DEVICE_FREQUENCY));
                int signalIntensity = cursor.getInt(cursor.getColumnIndex(KEY_DEVICE_INTENSITY));
                String typeString = cursor.getString(cursor.getColumnIndex(KEY_DEVICE_TYPE));

                DeviceType type = DeviceType.valueOf(typeString);

                //Second constructor
                //String ssid, String bssid, String characteristics, String manufacturer, Object channelWidth,
                // int frequency, int signalIntensity, DeviceType type
                device = new Device(ssid, bssid, characteristics, manufacturer, channelWidth, frequency, signalIntensity,
                        type);
            }
        } catch (SQLException e) {
            Log.d("getDevice", "ERROR WHILE GETTING DEVICE FROM DB");
        } finally {
            db.endTransaction();
            if (cursor != null)
                cursor.close();
        }
        return device;

    }

}
