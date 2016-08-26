package xyz.smartsniff;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;

/**
 * Settings activity. Contains the configuration options available to the user.
 *
 * Author: Daniel Castro García
 * Email: dandev237@gmail.com
 * Date: 17/07/2016
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String MINIMUM_INTERVAL_SCAN_VALUE_STR = "2";
    private static final int MINIMUM_INTERVAL_SCAN_VALUE_INT = 2;
    private static final String MAXIMUM_INTERVAL_SCAN_VALUE_STR = "10";
    private static final int MAXIMUM_INTERVAL_SCAN_VALUE_INT = 10;

    private SharedPreferences preferences;
    private SharedPreferences.OnSharedPreferenceChangeListener prefChangeListener;
    private CheckBox energySavingCheckBox;
    private EditText intervalEditText;

    private boolean energySavingMode, preferencesChanged;
    private int energyPref, scanIntervalPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferences = getSharedPreferences(Utils.PREFS_NAME, Context.MODE_PRIVATE);
        prefChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener(){
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                preferencesChanged = true;
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(prefChangeListener);
        preferencesChanged = false;

        loadUserSettings();
    }

    @Override
    protected void onStop(){
        super.onStop();
        saveUserSettings();
        preferences.unregisterOnSharedPreferenceChangeListener(prefChangeListener);
    }

    private void loadUserSettings() {
        energySavingCheckBox = (CheckBox) findViewById(R.id.energySavingCheckBox);
        energyPref = preferences.getInt(Utils.PREF_GPS_PRIORITY, Utils.GPS_PRIORITY_DEFAULT);
        if(energyPref == LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY){
            setEnergySavingMode(true);
            energySavingCheckBox.setChecked(energySavingMode);
        }

        intervalEditText = (EditText) findViewById(R.id.intervalEditText);
        intervalEditText.addTextChangedListener(intervalWatcher);
        scanIntervalPref = preferences.getInt(Utils.PREF_SCAN_INTERVAL, Utils.SCAN_INTERVAL_DEFAULT);
        scanIntervalPref /= 1000;
        intervalEditText.setText(String.valueOf(scanIntervalPref));
    }

    private void saveUserSettings() {
        setEnergySavingMode(energySavingCheckBox.isChecked());
        if(isEnergySavingMode())
            energyPref = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
        else
            energyPref = LocationRequest.PRIORITY_HIGH_ACCURACY;

        setScanInterval(Integer.parseInt(intervalEditText.getText().toString()));
        preferences.edit().putInt(Utils.PREF_GPS_PRIORITY, energyPref)
                .putInt(Utils.PREF_SCAN_INTERVAL, getScanInterval()*1000).apply();

        if(preferencesChanged)
            Toast.makeText(getApplicationContext(), "Configuración guardada con éxito.", Toast.LENGTH_SHORT).show();
    }

    private final TextWatcher intervalWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if(charSequence.length() > 0){
                int number = Integer.parseInt(intervalEditText.getText().toString());

                if (number <= MINIMUM_INTERVAL_SCAN_VALUE_INT - 1) {
                    intervalEditText.setText(MINIMUM_INTERVAL_SCAN_VALUE_STR);
                } else if (number > MAXIMUM_INTERVAL_SCAN_VALUE_INT + 1) {
                    intervalEditText.setText(MAXIMUM_INTERVAL_SCAN_VALUE_STR);
                }
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {}
    };

    public boolean isEnergySavingMode() {
        return energySavingMode;
    }

    public void setEnergySavingMode(boolean energySavingMode) {
        this.energySavingMode = energySavingMode;
    }

    public int getScanInterval() {
        return scanIntervalPref;
    }

    public void setScanInterval(int scanInterval) {
        scanIntervalPref = scanInterval;
    }
}
