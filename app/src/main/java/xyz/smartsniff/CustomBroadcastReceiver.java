package xyz.smartsniff;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;

/**
 * Custom BroadcastReceiver, designed to perform continuous network scanning.
 *
 * Autor: Daniel Castro García
 * Email: dandev237@gmail.com
 * Fecha: 03/07/2016
 */
public class CustomBroadcastReceiver extends BroadcastReceiver{

    private Handler scanHandler = new Handler();

    @NonNull
    private Context context;
    private WifiManager wifiManager;
    @NonNull
    private ScanResultsListener scanResultsListener;
    private boolean continueScanning = false;

    private long lastScanTime = 0;
    private int scanInterval; //in milliseconds
    private Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d("SCANRUNNABLE", "ENTERED SCANRUNNABLE");
            initiateScan();
        }
    };

    /**
     * Constructor used to specify the delivery rate of the ScanResults.
     *
     * @param context   cannot be null
     * @param listener  cannot be null
     * @param interval  preferred delay between scans, in milliseconds. Cannot be negative
     *
     * @throws NullPointerException if {@code context} or {@code listener} is null
     * @throws IllegalArgumentException if {@code interval} is negative
     */
    public CustomBroadcastReceiver (@NonNull Context context, @NonNull ScanResultsListener listener, int interval){
        checkContext(context);
        checkListener(listener);
        checkInterval(interval);

        this.context = context;
        scanResultsListener = listener;
        scanInterval = interval;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    private void checkInterval(int interval) {
        if(interval < 0)
            throw new IllegalArgumentException("interval cannot be negative");
    }

    private void checkListener(@NonNull ScanResultsListener listener) {
        if(listener == null)
            throw new NullPointerException("listener cannot be null");
    }

    private void checkContext(@NonNull Context context) {
        if(context == null)
            throw new NullPointerException("context cannot be null");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //Check if this receiver was registered for the appropriate action
        if(!WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction()))
            throw new IllegalStateException("WRONG ACTION: CustomBroadcastReceiver registered for " + intent.getAction());


        if(continueScanning){
            initiateScan();
            if(!wifiManager.getScanResults().isEmpty())
                scanResultsListener.onScanResultsReceived(wifiManager.getScanResults());
        }
    }

    private void initiateScan(){
        long scanTime = System.currentTimeMillis();
        Log.d("SCANTIME", "Scan time: " + scanTime);

        if(continueScanning){
            long delay = scanTime - lastScanTime;
            if(scanInterval == 0 || delay >= scanInterval){
                //Send the results now
                wifiManager.startScan();
                lastScanTime = scanTime;
            } else {
                scanHandler.removeCallbacks(scanRunnable);
                scanHandler.postDelayed(scanRunnable, scanInterval - delay);
            }
        }
    }

    /**
     * Method to change the rate (in milliseconds) at which the app receives the ScanResults
     *
     * @param interval  preferred delay between scans, in milliseconds. Cannot be negative
     * @throws IllegalArgumentException     if interval is negative
     */
    public void changeScanInterval(int interval){
        checkInterval(interval);
        scanInterval = interval;
    }

    /**
     * Method to initiate a WiFi scan.
     */
    public void startScan(){
        if(continueScanning){
            Log.d("CustomBroadcastReceiver", "A SCAN IS ALREADY IN PROGRESS");
            return;
        }
        context.registerReceiver(this, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        continueScanning = true;
        //wifiManager.startScan();
        initiateScan();
        lastScanTime = System.currentTimeMillis();
    }

    /**
     * Method to stop an initiated scan.
     */
    public List<ScanResult> stopScan(){
        if(continueScanning){
            scanHandler.removeCallbacks(scanRunnable);
            continueScanning = false;
            context.unregisterReceiver(this);
        }

        return wifiManager.getScanResults();
    }

    /**
     * ScanResults delivery
     */
    public interface ScanResultsListener{
        void onScanResultsReceived(List<ScanResult> results);
    }
}
