package xyz.smartsniff;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
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

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

    private static final String MANUFACTURER_REQUEST_URL = "http://api.macvendors.com/";
    private static final String MANUFACTURER_NOT_FOUND = "NotFound";

    private static final int REQUEST_ENABLE_INTENT = 123;

    private ProgressDialog progressDialog;

    private TableLayout scanLayout;
    private ToggleButton scanButton;
    private TextView discoveriesTextView, initDateTextView;

    private GoogleMap googleMap;
    private HeatmapTileProvider provider;
    private TileOverlay overlay;

    private SessionDatabaseHelper databaseHelper;
    private WifiManager wifiManager;
    private CustomReceiver receiver;
    private GeolocationGPS geoGPS;
    private SharedPreferences preferences;

    private BluetoothAdapter bluetoothAdapter;

    private RequestQueue queue;

    private Date startDate, endDate;
    private int sessionResults;
    private boolean disableAppBarFlag = false;
    private boolean isBluetoothSupported = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences(Utils.PREFS_NAME, Context.MODE_PRIVATE);

        Toolbar appBar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(appBar);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        scanLayout = (TableLayout) findViewById(R.id.scanLayout);
        discoveriesTextView = (TextView) findViewById(R.id.discoveriesTextView);
        initDateTextView = (TextView) findViewById(R.id.initDateTextView);
        scanLayout.setVisibility(View.INVISIBLE);

        databaseHelper = SessionDatabaseHelper.getInstance(MainActivity.this);

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null){
            //Device doesn't support Bluetooth functionality
            isBluetoothSupported = false;
        }
        geoGPS = new GeolocationGPS(MainActivity.this, this);

        sessionResults = 0;

        receiver = new CustomReceiver();

        scanButton = (ToggleButton) findViewById(R.id.scanToggleButton);
        scanButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    disableAppBarFlag = true;

                    scanLayout.setVisibility(View.VISIBLE);
                    discoveriesTextView.setText(String.valueOf(sessionResults));

                    if(isBluetoothSupported && !bluetoothAdapter.isEnabled()){
                        Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_INTENT);
                    }

                    geoGPS.connect();
                    startDate = new Date();
                    initDateTextView.setText(Utils.formatDate(startDate));

                    Session session = new Session(startDate);
                    databaseHelper.addSession(session);

                    beginScanningProcedure();
                } else {
                    scanLayout.setVisibility(View.INVISIBLE);

                    unregisterReceiver(receiver);
                    if(isBluetoothSupported)
                        bluetoothAdapter.cancelDiscovery();

                    geoGPS.disconnect();

                    endDate = new Date();

                    databaseHelper.updateSession(Utils.formatDate(endDate));
                    reloadHeatMapPoints(false);

                    Toast.makeText(MainActivity.this, "Escaneo terminado. Hallazgos: " + sessionResults +
                            ".", Toast.LENGTH_SHORT).show();

                    sessionResults = 0;

                    disableAppBarFlag = false;
                }
            }
        });
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        //To be sure we are not scanning
        if(bluetoothAdapter != null){
            bluetoothAdapter.cancelDiscovery();
        }
    }

    private void beginScanningProcedure() {
        Thread wifiScanningThread = new Thread() {
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
            }
        };

        //Register receiver and request first bluetooth discovery scan
        if(isBluetoothSupported) {
            registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
            bluetoothAdapter.startDiscovery();
        }

        wifiScanningThread.start();
    }

    private void storeLocationInDb(final Location location) {
        Thread storeLocationThread = new Thread(){
            public void run(){
                databaseHelper.addLocation(location);
            }
        };

        storeLocationThread.run();
    }

    private void storeDeviceInDb(final Device device){
        Thread storeDeviceThread = new Thread(){
            public void run(){
                databaseHelper.addDevice(device);
            }
        };

        storeDeviceThread.start();
    }

    private void createAssociation(final Device device){
        Thread storeAssociationThread = new Thread(){
            public void run(){
                databaseHelper.addDevice(device);
                databaseHelper.addAssociation();
            }
        };

        storeAssociationThread.start();
    }

    private void addSinglePointToHeatMap(final Location locationToAdd) {
        Thread addPointThread = new Thread(){
            public void run(){
                ArrayList<WeightedLatLng> data = new ArrayList<>();

                WeightedLatLng locationLatLng = new WeightedLatLng(locationToAdd.getCoordinates(), locationToAdd.getNumOfLocatedDevices() * 1.0);
                data.add(locationLatLng);

                provider = new HeatmapTileProvider.Builder().weightedData(data)
                        .radius(HEATMAP_RADIUS).opacity(HEATMAP_OPACITY).build();

                overlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
            }
        };

        addPointThread.run();
    }

    /**
     * Used when it is necessary to reload the map
     * @param firstLoad If it is the first time the app loads the map
     */
    private void reloadHeatMapPoints(Boolean firstLoad){
        initializeProgressDialog(firstLoad);
        progressDialog.show();
        if(!firstLoad)
            clearMap();

        new LoadMapTask().execute();
    }

    private void clearMap() {
        googleMap.clear();
        overlay.remove();
        overlay.clearTileCache();
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
    public boolean onPrepareOptionsMenu(Menu menu){
        menu.findItem(R.id.action_delete_data).setEnabled(!disableAppBarFlag);
        menu.findItem(R.id.action_send_data).setEnabled(!disableAppBarFlag);
        menu.findItem(R.id.action_settings).setEnabled(!disableAppBarFlag);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        databaseHelper = SessionDatabaseHelper.getInstance(MainActivity.this);

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if(id == R.id.action_update_map){
            Log.d("AppBar Update Map", "APPBAR: UPDATE MAP BUTTON PRESSED");
            reloadHeatMapPoints(false);
        }*/

        if (id == R.id.action_delete_data) {
            Log.d("AppBar Delete", "APPBAR: DELETE BUTTON PRESSED");
            if(databaseHelper.selectSessions() > 0)
                showDeleteAlertDialog();
            else
                Toast.makeText(MainActivity.this, "ERROR: No hay datos que borrar", Toast.LENGTH_SHORT)
                        .show();
        }

        if (id == R.id.action_send_data) {
            Log.d("AppBar Send", "APPBAR: SEND BUTTON PRESSED");
            sendDataToServer();
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
                        databaseHelper.deleteDatabase(MainActivity.this);
                        clearMap();
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

    private void sendDataToServer(){
        if(databaseHelper.selectSessions() > 0){
            //Collect all the stored data
            List<Session> storedSessions = databaseHelper.getAllSesions();
            List<Device> storedDevices = databaseHelper.getAllDevices();
            List<Location> storedLocations = databaseHelper.getAllLocations();
            List<Association> storedAssociations = databaseHelper.getAllAssociations();

            //Build a JSON object containing all the data
            String myOwnDeviceAddress = wifiManager.getConnectionInfo().getMacAddress();
            JSONGenerator jsonGenerator = new JSONGenerator(myOwnDeviceAddress);
            JSONObject localDataJSON = jsonGenerator.buildJsonObject(storedSessions, storedDevices, storedLocations,
                    storedAssociations);

            //Send it to the server using the RESTful API
            String url = "";
            //final int[] statusCode = new int[1];
            JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, localDataJSON,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            NetworkResponse netResponse = Utils.gson.fromJson(response.toString(), NetworkResponse.class);
                            if(netResponse.statusCode == 201)
                                Toast.makeText(MainActivity.this, "Datos enviados con éxito", Toast.LENGTH_SHORT)
                                        .show();

                            /*
                            if(statusCode[0] == 201)
                                Toast.makeText(MainActivity.this, "Datos enviados con éxito", Toast.LENGTH_SHORT)
                                        .show();
                             */
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(MainActivity.this, "ERROR: No se pudieron enviar los datos", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    })/*{

                @Override
                protected Response<JSONObject> parseNetworkResponse(NetworkResponse response){
                    statusCode[0] = response.statusCode;
                    return super.parseNetworkResponse(response);
                }
            }*/;
            if(queue == null)
                queue = Volley.newRequestQueue(MainActivity.this);

            queue.add(postRequest);
        }else{
            Toast.makeText(MainActivity.this, "ERROR: No hay datos que enviar", Toast.LENGTH_SHORT)
                    .show();
        }
    }
    //----------------------------------------------------------------------------------------------------------------------

    //Map Fragment
    //----------------------------------------------------------------------------------------------------------------------
    @Override
    public void onMapReady(GoogleMap googleMap) {
        CameraChangeListener listener = new CameraChangeListener();
        googleMap.setOnCameraChangeListener(listener);
        this.googleMap = googleMap;

        //Load heatmap
        reloadHeatMapPoints(true);
    }
    //----------------------------------------------------------------------------------------------------------------------

    /**
     * Custom BroadcastReceiver to handle the detected networks/devices
     */
    private class CustomReceiver extends BroadcastReceiver {
        private Location lastKnownLocation;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            //Check intent action
            if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
                //The Receiver is responsible for requesting another discovery scan
                if(bluetoothAdapter.isDiscovering())
                    bluetoothAdapter.startDiscovery();
            }else{
                //Time to check those results
                if(action.equals(BluetoothDevice.ACTION_FOUND)){
                    //Bluetooth device
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    String btName = device.getName();
                    String macAddress = device.getAddress();
                    BluetoothClass btClass = device.getBluetoothClass();
                    String deviceClass = determineBtMajorDevice(btClass);

                    Device btDevice = new Device(btName, macAddress, deviceClass, DeviceType.BLUETOOTH);

                    if(!databaseHelper.deviceExistsInDb(btDevice)){
                        getManufacturerFromBssid(btDevice, btDevice.getBssid());
                        sessionResults++;
                        storeDeviceInDb(btDevice);
                    }

                    discoveriesTextView.setText(String.valueOf(sessionResults));
                    /*Toast.makeText(getApplicationContext(), "BTDevice added: " + btDevice.getBssid() + ", " + btDevice
                            .getCharacteristics(), Toast.LENGTH_SHORT).show();
                    Log.e("BTDevice", "BTDevice added: " + btDevice.getSsid() + ", " + btDevice.getCharacteristics());*/
                }else{
                    //Wifi device
                    //Get a location
                    LatLng locationCoordinates = new LatLng(geoGPS.getLatitude(), geoGPS.getLongitude());

                    //Get a date
                    Date locationDate = new Date();

                    //Create a Location object
                    Location location;
                    if (isSameLocation(locationCoordinates))
                        //I'm in the same place, use the Location object contained in lastKnownLocation
                        location = lastKnownLocation;
                    else{
                        //I'm in a new spot, create a new Location object
                        location = new Location(locationDate, locationCoordinates);
                        storeLocationInDb(location);
                    }

                    //Get the scan results
                    List<ScanResult> scanResultList = wifiManager.getScanResults();

                    //For each scan result, create a Device and add it to the Location devices list
                    //If the user hasn't moved, any devices already discovered on that location won't be registered again
                    for (ScanResult s : scanResultList) {
                        Device wifiDevice = new Device(s.SSID, s.BSSID, s.capabilities, DeviceType.WIFI);
                        location.addFoundDevice(wifiDevice);

                        //Add the device to the database if and only if it doesn't exist in it
                        if(!databaseHelper.deviceExistsInDb(wifiDevice)){
                            getManufacturerFromBssid(wifiDevice, wifiDevice.getBssid());
                            sessionResults++;
                        }
                        createAssociation(wifiDevice);

                        discoveriesTextView.setText(String.valueOf(sessionResults));
                        //Log.d("NEW DEVICE FOUND", "New device found!!");

                        if(!isSameLocation(locationCoordinates))
                            addSinglePointToHeatMap(location);

                        //Map camera update
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location.getCoordinates(), ZOOM_LEVEL));

                        //The list of found devices must not transfer from one location to another
                        location.getLocatedDevices().clear();

                        lastKnownLocation = location;
                    }
                }
            }
        }

        private String determineBtMajorDevice(BluetoothClass btClass){
            String result = "";

            switch(btClass.getMajorDeviceClass()){
                case BluetoothClass.Device.Major.AUDIO_VIDEO:
                    result = "AUDIO-VIDEO";
                    break;
                case BluetoothClass.Device.Major.COMPUTER:
                    result = "COMPUTER";
                    break;
                case BluetoothClass.Device.Major.HEALTH:
                    result = "HEALTH";
                    break;
                case BluetoothClass.Device.Major.IMAGING:
                    result = "IMAGING";
                    break;
                case BluetoothClass.Device.Major.MISC:
                    result = "MISC";
                    break;
                case BluetoothClass.Device.Major.NETWORKING:
                    result = "NETWORKING";
                    break;
                case BluetoothClass.Device.Major.PERIPHERAL:
                    result = "PERIPHERAL";
                    break;
                case BluetoothClass.Device.Major.PHONE:
                    result = "PHONE";
                    break;
                case BluetoothClass.Device.Major.TOY:
                    result = "TOY";
                    break;
                case BluetoothClass.Device.Major.UNCATEGORIZED:
                    result = "UNCATEGORIZED";
                    break;
                case BluetoothClass.Device.Major.WEARABLE:
                    result = "WEARABLE";
                    break;
                default:
                    break;
            }

            return result;
        }

        private boolean isSameLocation(LatLng locationCoordinates) {
            return lastKnownLocation != null && lastKnownLocation.getCoordinates().equals(locationCoordinates);
        }
    }

    /**
     * Custom OnCameraChangeListener to react to the changes in zoom made by the user.
     * @see <a href="https://developers.google.com/maps/documentation/android-api/views">
     *     GOOGLE API Camera View Documentation</a>
     */

    private class CameraChangeListener implements GoogleMap.OnCameraChangeListener {
        @Override
        public void onCameraChange(CameraPosition cameraPosition) {
            if(cameraPosition.zoom > ZOOM_LEVEL){
                provider.setRadius((int) ((HEATMAP_RADIUS * cameraPosition.zoom)/ZOOM_LEVEL));
                overlay.clearTileCache();
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_LEVEL));
            }
        }
    }

    /**
     * Obtains the manufacturer of the ethernet/bluetooth card based on the MAC address of the device.
     * This method is only called when the device must be associated with a particular location (i.e.
     * the device hasn't been discovered yet) in order to minimize the number of requests sent to the
     * API server.
     *
     * @see <a href="http://www.macvendors.com/api"> API Documentation</a>
     * @see <a href="https://developer.android.com/training/volley/simple.html">Volley Documentation</a>
     * @param bssid MAC Address
     */
    public void getManufacturerFromBssid(final Device device, final String bssid) {
        //Fix to avoid creating a requestQueue for each request (OutOfMemory error)
        if(queue == null)
            queue = Volley.newRequestQueue(MainActivity.this);

        Thread requestManufacturerThread = new Thread(){
            public void run(){
                //http://api.macvendors.com/00:11:22:33:44:55
                String url = MANUFACTURER_REQUEST_URL + bssid;

                //Request a response from the provided URL
                StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Get the manufacturer from the response string
                        //Log.d(TAG, "MANUFACTURER RECEIVED SUCCESSFULLY: " + manufacturer);
                        device.setManufacturer(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Code 404 was received: no manufacturer found
                        //Log.d(TAG, "MANUFACTURER NOT FOUND");
                        device.setManufacturer(MANUFACTURER_NOT_FOUND);
                    }
                });
                //Add the request to the queue
                queue.add(request);
            }
        };

        requestManufacturerThread.start();
    }

    //Loading Screen
    //----------------------------------------------------------------------------------------------------------------------
    private void initializeProgressDialog(Boolean firstLoad){
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        if(firstLoad)
            progressDialog.setMessage("Cargando mapa de calor, espere por favor...");
        else
            progressDialog.setMessage("Actualizando mapa de calor, espere por favor...");
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(false);
    }

    private class LoadMapTask extends AsyncTask<Void, Void, ArrayList<WeightedLatLng>>{

        @Override
        protected ArrayList<WeightedLatLng> doInBackground(Void... voids) {
            ArrayList<WeightedLatLng> data;
            synchronized (this){
                //Show the progress dialog for a little while
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Map<Location, Integer> locationData;
                databaseHelper = SessionDatabaseHelper.getInstance(MainActivity.this);
                locationData = databaseHelper.selectLocationsForHeatmap();

                data = new ArrayList<>();
                for(Location loc: locationData.keySet()){
                    WeightedLatLng locationToInsert = new WeightedLatLng(loc.getCoordinates(),
                            locationData.get(loc));

                    data.add(locationToInsert);
                }

            }
            return data;
        }

        @Override
        protected void onPostExecute(ArrayList<WeightedLatLng> result){
            //onPostExecute runs on the UI thread, so we cant paint the points on the map
            paintPointsOnMap(result);

            progressDialog.dismiss();
        }
    }

    private void paintPointsOnMap(ArrayList<WeightedLatLng> points){
        if(points.size() > 0) {
            provider = new HeatmapTileProvider.Builder().weightedData(points)
                    .radius(HEATMAP_RADIUS).opacity(HEATMAP_OPACITY).build();

            overlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
        }
    }
    //----------------------------------------------------------------------------------------------------------------------
}
