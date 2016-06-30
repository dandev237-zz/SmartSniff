package xyz.smartsniff;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This class handles database operations such as reading, writing and upgrading.
 *
 * Autor: Daniel Castro García
 * Email: dandev237@gmail.com
 * Fecha: 30/06/2016
 */
public class SessionDatabaseHelper extends SQLiteOpenHelper {
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
                KEY_DEVICE_BSSID + " TEXT," +
                KEY_DEVICE_CHARACTERISTICS + " TEXT," +
                KEY_DEVICE_TYPE + " TEXT" +
                ")";

        String CREATE_LOCATIONS_TABLE = "CREATE TABLE " + TABLE_LOCATIONS +
                "(" +
                KEY_LOCATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_LOCATION_DATE + " TEXT," +
                KEY_LOCATION_COORDINATES + " TEXT" +
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
     * Called when the database needs to be upgraded (the database already
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

    //TODO: ADD CRUD OPERATIONS
}
