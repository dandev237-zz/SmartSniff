package xyz.smartsniff;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.util.ArrayList;
import java.util.Map;

/**
 * Class with all the map-related functionality.
 *
 * Author: Daniel Castro García
 * Email: dandev237@gmail.com
 * Date: 17/08/2016
 */
public class MapManager {

    private GoogleMap googleMap;
    private TileProvider provider;
    private TileOverlay overlay;

    private Activity mainActivity;
    private ProgressDialog progressDialog;
    private DatabaseHelper databaseHelper;

    public MapManager (GoogleMap googleMap, Activity mainActivity){
        this.googleMap = googleMap;
        this.mainActivity = mainActivity;

        reloadHeatMapPoints(true);
    }

    public void animateCamera(LatLng coordinates){
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, Utils.ZOOM_LEVEL));
    }

    public void addSinglePointToHeatMap(final Location locationToAdd) {
        Thread addPointThread = new Thread(){
            public void run(){
                ArrayList<WeightedLatLng> data = new ArrayList<>();

                WeightedLatLng locationLatLng = new WeightedLatLng(locationToAdd.getCoordinates(), locationToAdd.getNumOfLocatedDevices() * 1.0);
                data.add(locationLatLng);

                provider = new HeatmapTileProvider.Builder().weightedData(data)
                        .radius(Utils.HEATMAP_RADIUS).opacity(Utils.HEATMAP_OPACITY).build();

                overlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
            }
        };

        addPointThread.run();
    }

    /**
     * Used when it is necessary to reload the map
     * @param firstLoad If it is the first time the app loads the map
     */
    public void reloadHeatMapPoints(Boolean firstLoad){
        initializeProgressDialog(firstLoad);
        //Show a loading screen
        progressDialog.show();
        if(!firstLoad)
            clearMap();

        new LoadMapTask().execute();
    }

    public void clearMap() {
        googleMap.clear();
        overlay.remove();
        overlay.clearTileCache();
    }

    private void initializeProgressDialog(Boolean firstLoad){
        progressDialog = new ProgressDialog(mainActivity);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        if(firstLoad)
            progressDialog.setMessage(mainActivity.getString(R.string.mapmanager_loading_map));
        else
            progressDialog.setMessage(mainActivity.getString(R.string.mapmanager_updating_map));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(false);
    }

    /**
     * This method is responsible for the reloading of the heatmap displayed in the main interface
     * of the application.
     */
    private class LoadMapTask extends AsyncTask<Void, Void, ArrayList<WeightedLatLng>> {

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
                databaseHelper = DatabaseHelper.getInstance(mainActivity);
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
            //onPostExecute runs on the UI thread, so we can paint the points on the map
            paintPointsOnMap(result);

            //Dismiss the loading screen
            progressDialog.dismiss();
        }
    }

    private void paintPointsOnMap(ArrayList<WeightedLatLng> points){
        if(points.size() > 0) {
            provider = new HeatmapTileProvider.Builder().weightedData(points)
                    .radius(Utils.HEATMAP_RADIUS).opacity(Utils.HEATMAP_OPACITY).build();

            overlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
        }
    }

}
