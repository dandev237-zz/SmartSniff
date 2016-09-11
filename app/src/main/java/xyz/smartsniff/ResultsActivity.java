package xyz.smartsniff;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        List<Device> devices = new ArrayList<>(lastSessionDevices);
        ResultAdapter adapter = new ResultAdapter(devices, this);

        resultsListView.setAdapter(adapter);
    }
}
