package xyz.smartsniff;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Date;
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

    private Toolbar appBar;
    private MapFragment mapFragment;
    private TableLayout scanLayout;
    private ToggleButton scanButton;
    private TextView discoveriesTextView, initDateTextView;

    private WifiManager wifiManager;
    private CustomReceiver receiver;
    private GeolocationGPS geoGPS;
    private android.location.Location location;

    private Date startDate, endDate;
    private int sessionResults;
    private long interval = 3000; //millis
    private Thread scanningThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appBar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(appBar);

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        scanLayout = (TableLayout) findViewById(R.id.scanLayout);
        discoveriesTextView = (TextView) findViewById(R.id.discoveriesTextView);
        initDateTextView = (TextView) findViewById(R.id.initDateTextView);
        scanLayout.setVisibility(View.INVISIBLE);

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        geoGPS = new GeolocationGPS(getApplicationContext(), this);
        sessionResults = 0;

        receiver = new CustomReceiver();

        scanButton = (ToggleButton) findViewById(R.id.scanToggleButton);
        scanButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    //Log.d("Scan Button", "SCAN BUTTON PRESSED. STATUS: CHECKED");
                    scanLayout.setVisibility(View.VISIBLE);
                    //Permissions check at runtime
                    Utils.handlePermissions(getApplicationContext(), MainActivity.this);

                    //Scanning procedure
                    beginScanningProcedure();
                } else {
                    //Log.d("Scan Button", "SCAN BUTTON PRESSED. STATUS: UNCHECKED");

                    scanLayout.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Escaneo terminado. Hallazgos: " + sessionResults +
                            ".", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void beginScanningProcedure() {
        scanningThread = new Thread() {
            long lastScanTime = 0;

            public void run() {
                startDate = new Date();
                geoGPS.connect();
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
                geoGPS.disconnect();
                endDate = new Date();
            }
        };
        scanningThread.start();
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
            return true;
        }

        if (id == R.id.action_send_data) {
            Log.d("AppBar Send", "APPBAR: SEND BUTTON PRESSED");
            return true;
        }

        if (id == R.id.action_settings) {
            Log.d("AppBar Configuration", "APPBAR: CONFIGURATION BUTTON PRESSED");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    //----------------------------------------------------------------------------------------------------------------------

    //Map Fragment
    //----------------------------------------------------------------------------------------------------------------------
    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("MarkerTest"));
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
                    //Log.d("NEW DEVICE FOUND", "New device found!!");
                } /*else {
                    Log.d("SAME DEVICE DISCOVERED", "I've seen a device already recorded!");
                }*/
            }

            /*for(Device d : location.getLocatedDevices()){
                Log.d("FOUND DEVICE", d.getBssid());
            }
            Log.d("LOCATION", location.getCoordinatesString() + ". Date: " + location.getDate());*/
            lastKnownLocation = location;
        }
    }

}
