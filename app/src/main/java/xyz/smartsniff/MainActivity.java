package xyz.smartsniff;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TableLayout;
import android.widget.ToggleButton;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Main activity of the application. Contains the scanning interface and the
 * appbar.
 *
 * Autor: Daniel Castro García
 * Email: dandev237@gmail.com
 * Fecha: 29/06/2016
 */

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{

    private Toolbar appBar;
    private MapFragment mapFragment;
    private TableLayout scanLayout;
    private ToggleButton scanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appBar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(appBar);

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        scanLayout = (TableLayout) findViewById(R.id.scanLayout);
        scanLayout.setVisibility(View.INVISIBLE);

        scanButton = (ToggleButton) findViewById(R.id.scanToggleButton);
        scanButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    Log.d("Scan Button", "SCAN BUTTON PRESSED. STATUS: CHECKED");
                    //TODO: Begin scanning procedure
                }else{
                    Log.d("Scan Button", "SCAN BUTTON PRESSED. STATUS: UNCHECKED");
                    //TODO: Stop scanning procedure
                }
            }
        });
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
}
