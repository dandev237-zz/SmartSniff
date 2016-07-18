package xyz.smartsniff;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Main activity of the application. Contains the scanning interface and the
 * appbar.
 * <p/>
 * Autor: Daniel Castro García
 * Email: dandev237@gmail.com
 * Fecha: 29/06/2016
 */

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{

    private static final float ZOOM_LEVEL = 17.0f;
    private static final double HEATMAP_OPACITY = 0.6;
    private static final int HEATMAP_RADIUS = 40;

    private Toolbar appBar;
    private MapFragment mapFragment;
    private GoogleMap googleMap;
    private TableLayout scanLayout;
    private ToggleButton scanButton;
    private TextView discoveriesTextView, initDateTextView;

    private SessionDatabaseHelper databaseHelper;
    private WifiManager wifiManager;
    private CustomReceiver receiver;
    private GeolocationGPS geoGPS;
    private LinkedList<Location> locationList;
    private SharedPreferences preferences;

    private Date startDate, endDate;
    private int sessionResults;
    private Thread scanningThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences(Utils.PREFS_NAME, Context.MODE_PRIVATE);

        appBar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(appBar);

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        scanLayout = (TableLayout) findViewById(R.id.scanLayout);
        discoveriesTextView = (TextView) findViewById(R.id.discoveriesTextView);
        initDateTextView = (TextView) findViewById(R.id.initDateTextView);
        scanLayout.setVisibility(View.INVISIBLE);

        databaseHelper = SessionDatabaseHelper.getInstance(getApplicationContext());

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        geoGPS = new GeolocationGPS(getApplicationContext(), this);
        locationList = new LinkedList<>();
        sessionResults = 0;

        receiver = new CustomReceiver();

        scanButton = (ToggleButton) findViewById(R.id.scanToggleButton);
        scanButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    //Log.d("Scan Button", "SCAN BUTTON PRESSED. STATUS: CHECKED");
                    scanLayout.setVisibility(View.VISIBLE);
                    discoveriesTextView.setText(String.valueOf(sessionResults));

                    geoGPS.connect();
                    startDate = new Date();
                    initDateTextView.setText(Utils.formatDate(startDate));

                    //Scanning procedure
                    beginScanningProcedure();
                } else {
                    geoGPS.disconnect();
                    endDate = new Date();

                    Session session = new Session(startDate, endDate);
                    databaseHelper.addSession(session);
                    for(Location loc : locationList){
                        databaseHelper.addLocation(loc);
                        for(Device dev : loc.getLocatedDevices()){
                            databaseHelper.addDevice(dev);
                            databaseHelper.addAssociation();
                        }
                    }

                    //Log.d("Scan Button", "SCAN BUTTON PRESSED. STATUS: UNCHECKED");
                    //Log.d("Scan Button", "SESSION ENDED. START DATE: " + startDate + ", END DATE: " + endDate);
                    scanLayout.setVisibility(View.INVISIBLE);

                    Toast.makeText(getApplicationContext(), "Escaneo terminado. Hallazgos: " + sessionResults +
                            ".", Toast.LENGTH_SHORT).show();

                    sessionResults = 0;
                }
            }
        });
    }

    private void beginScanningProcedure() {
        scanningThread = new Thread() {
            long lastScanTime = 0;
            int interval = preferences.getInt(Utils.PREF_SCAN_INTERVAL, Utils.SCAN_INTERVAL_DEFAULT);

            public void run() {
                try {
                    //The scan will begin after an interval of time
                    //Log.d("SCANNING THREAD", "SLEEP");
                    Thread.sleep(interval);
                    //Log.d("SCANNING THREAD", "NO SLEEP");
                } catch (InterruptedException e) {
                    Log.d("SCANNING THREAD", "SCAN HAS BEEN INTERRUPTED");
                }
                registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

                while (scanButton.isChecked()) {
                    long scanTime = System.currentTimeMillis();
                    long delay = scanTime - lastScanTime;

                    if (delay >= interval) {
                        //Log.d("STARTSCAN", "STARTING SCAN AT TIME " + scanTime);
                        wifiManager.startScan();
                        lastScanTime = scanTime;
                    }
                }
                unregisterReceiver(receiver);
            }
        };
        scanningThread.start();
    }

    private void addHeatMap(LatLng location, int numOfDevices){
        WeightedLatLng locationLatLng = new WeightedLatLng(location, numOfDevices * 1.0);
        ArrayList<WeightedLatLng> data = new ArrayList<>();
        data.add(locationLatLng);

        HeatmapTileProvider provider = new HeatmapTileProvider.Builder().weightedData(data)
                .radius(HEATMAP_RADIUS).opacity(HEATMAP_OPACITY).build();

        TileOverlay overlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //Action bar
    //----------------------------------------------------------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete_data) {
            Log.d("AppBar Delete", "APPBAR: DELETE BUTTON PRESSED");
            showDeleteAlertDialog();
        }

        if (id == R.id.action_send_data) {
            Log.d("AppBar Send", "APPBAR: SEND BUTTON PRESSED");
            return true;
        }

        if (id == R.id.action_settings) {
            Log.d("AppBar Configuration", "APPBAR: CONFIGURATION BUTTON PRESSED");
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDeleteAlertDialog() {
        //Build first alert dialog
        AlertDialog.Builder builderFirst = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.alertDialog));
        builderFirst.setMessage(getString(R.string.delete_alert_dialog_message))
                .setTitle(R.string.delete_alert_dialog_title)
                .setPositiveButton(R.string.delete_alert_dialog_possitive_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Delete all database records
                        databaseHelper.deleteDatabase(getApplicationContext());
                    }
                })
                .setNegativeButton(R.string.delete_alert_dialog_negative_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Leave empty
                    }
                });

        AlertDialog dialog = builderFirst.create();
        dialog.show();
    }
    //----------------------------------------------------------------------------------------------------------------------

    //Map Fragment
    //----------------------------------------------------------------------------------------------------------------------
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }
    //----------------------------------------------------------------------------------------------------------------------

    /**
     * Custom BroadcastReceiver to handle the detected networks/devices
     */
    private class CustomReceiver extends BroadcastReceiver {
        private Location lastKnownLocation;

        @Override
        public void onReceive(Context context, Intent intent) {
            //Get a location
            LatLng locationCoordinates = new LatLng(geoGPS.getLatitude(), geoGPS.getLongitude());

            //Get a date
            Date locationDate = new Date();

            //Create a Location object
            Location location;
            if (lastKnownLocation != null && lastKnownLocation.getCoordinates().equals(locationCoordinates))
                //I'm in the same place, use the Location object contained in lastKnownLocation
                location = lastKnownLocation;
            else
                //I'm in a new spot, create a new Location object
                location = new Location(locationDate, locationCoordinates);


            //Get the scan results
            List<ScanResult> scanResultList = wifiManager.getScanResults();

            //For each scan result, create a Device and add it to the Location devices list
            //If the user hasn't moved, any devices already discovered on that location won't be registered again
            for (ScanResult s : scanResultList) {
                Device device = new Device(s.SSID, s.BSSID, s.capabilities, DeviceType.WIFI, getApplicationContext());
                if (!location.getLocatedDevices().contains(device)) {
                    //If the device is new for this location, get his manufacturer
                    device.getManufacturerFromBssid(device.getBssid());
                    location.addFoundDevice(device);
                    sessionResults++;
                    discoveriesTextView.setText(String.valueOf(sessionResults));
                    //Log.d("NEW DEVICE FOUND", "New device found!!");
                } /*else {
                    Log.d("SAME DEVICE DISCOVERED", "I've seen a device already recorded!");
                }*/
            }

            /*for(Device d : location.getLocatedDevices()){
                Log.d("FOUND DEVICE", d.getBssid());
            }
            Log.d("LOCATION", location.getCoordinatesString() + ". Date: " + location.getDate());*/
            locationList.add(location);
            addHeatMap(location.getCoordinates(), location.getNumOfLocatedDevices());
            //CAMERA AND VIEW DOCUMENTATION
            //https://developers.google.com/maps/documentation/android-api/views
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location.getCoordinates(), ZOOM_LEVEL));
            lastKnownLocation = location;
        }
    }

}
