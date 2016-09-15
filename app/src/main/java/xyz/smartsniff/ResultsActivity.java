package xyz.smartsniff;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Results activity. Contains a presentation of the results of any given session
 *
 * Author: Daniel Castro García
 * Email: dandev237@gmail.com
 * Date: 11/09/2016
 */
public class ResultsActivity extends AppCompatActivity {

    private TextView initDateTextView, endDateTextView, discoveriesTextView;
    private ListView resultsListView;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        initDateTextView = (TextView) findViewById(R.id.resultInitDateTextView);
        endDateTextView = (TextView) findViewById(R.id.resultEndDateTextView);
        discoveriesTextView = (TextView) findViewById(R.id.resultDiscoveriesTextView);

        Intent resultsIntent = getIntent();
        String initDate = resultsIntent.getStringExtra("lastSessionInitDate");
        String endDate = resultsIntent.getStringExtra("lastSessionEndDate");
        HashSet<Device> lastSessionDevices = Utils.gson.fromJson(
                resultsIntent.getStringExtra("lastSessionDevices"),
                new TypeToken<HashSet<Device>>(){}.getType()
        );

        initDateTextView.setText(initDate);
        endDateTextView.setText(endDate);
        discoveriesTextView.setText(String.valueOf(lastSessionDevices.size()));

        resultsListView = (ListView) findViewById(R.id.resultListView);

        final List<Device> devices = new ArrayList<>(lastSessionDevices);
        ResultAdapter adapter = new ResultAdapter(devices, this);

        resultsListView.setAdapter(adapter);

        resultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                displayDetailsDialog((Device) adapterView.getItemAtPosition(i));
            }
        });

        dbHelper = DatabaseHelper.getInstance(this);
    }

    private void displayDetailsDialog(Device device) {
        Dialog dialog = new Dialog(ResultsActivity.this);
        dialog.setContentView(R.layout.result_details_layout);
        dialog.setTitle(R.string.result_title);

        TextView ssidTextView, bssidTextView, manufacturerTextView, capabilitiesTextView, channelWidthTextView,
                frequencyTextView, signalTextView;

        ssidTextView = (TextView) dialog.findViewById(R.id.ssidTextView);
        ssidTextView.setText(device.getSsid());

        bssidTextView = (TextView) dialog.findViewById(R.id.bssidTextView);
        bssidTextView.setText(device.getBssid());

        manufacturerTextView = (TextView) dialog.findViewById(R.id.manufacturerTextView);
        if(device.getManufacturer() == null){
            device.setManufacturer(dbHelper.getManufacturerOfDevice(device.getBssid()));
        }
        manufacturerTextView.setText(device.getManufacturer());

        capabilitiesTextView = (TextView) dialog.findViewById(R.id.capabilitiesTextView);
        capabilitiesTextView.setText(device.getCharacteristics());

        if(device.getType().equals(DeviceType.WIFI)){
            channelWidthTextView = (TextView) dialog.findViewById(R.id.channelWidthTextView);
            channelWidthTextView.setText(device.getChannelWidth());

            frequencyTextView = (TextView) dialog.findViewById(R.id.frequencyTextView);
            frequencyTextView.setText(String.format("%s MHz", String.valueOf(device.getFrequency())));

            signalTextView = (TextView) dialog.findViewById(R.id.signalTextView);
            signalTextView.setText(String.format("%s dBm", String.valueOf(device.getSignalIntensity())));
        }else{
            TableLayout wifiDetailsLayout = (TableLayout) dialog.findViewById(R.id.wifiDetailsLayout);
            wifiDetailsLayout.removeAllViews();

            TextView capabilitiesRowText = (TextView) dialog.findViewById(R.id.capabilitiesRowText);
            capabilitiesRowText.setText(R.string.result_device_type);
        }

        dialog.show();

    }
}
