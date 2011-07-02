/*
 * Copyright (C) 2011 Roberto Estrada
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package es.glasspixel.wlanaudit.activities;

import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.adapters.WifiNetworkAdapter;
import es.glasspixel.wlanaudit.util.IKeyCalculator;
import es.glasspixel.wlanaudit.util.WLANXXXXKeyCalculator;
import android.app.Activity;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Activity to show the details of a given network previously scanned.
 * 
 * @author Roberto Estrada
 */
public class NetworkDetailsActivity extends Activity {

    /**
     * Unique identifier of the scan result inside the intent extra or the
     * savedInstanceState bundle.
     */
    public static final String SCAN_RESULT_KEY = "scan_result";
    /**
     * The scanned network to show details about
     */
    private ScanResult mScannedNetwork;
    /**
     * Widget to display network's icon
     */
    private ImageView mNetworkIcon;
    /**
     * Widget to display network's name
     */
    private TextView mNetworkName;
    /**
     * Widget to display network's BSSID
     */
    private TextView mBssidValue;
    /**
     * Widget to display network's encryption
     */
    private TextView mEncryptionValue;
    /**
     * Widget to display network's frequency
     */
    private TextView mFrequencyValue;
    /**
     * Widget to display network's signal strength
     */
    private TextView mIntensityValue;
    /**
     * Widget to display network's default password (if available)
     */
    private TextView mDefaultPassValue;
    /**
     * Connect button
     */
    private Button mConnectButton;
    /**
     * Copy button
     */
    private Button mCopyButton;

    /**
     * Lifecycle method: Activity creation
     * 
     * @see android.app.Activity#onCreate(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If a previous instance state was saved
        if (savedInstanceState != null && savedInstanceState.get(SCAN_RESULT_KEY) != null) {
            // Load the state
            mScannedNetwork = (ScanResult) savedInstanceState.get(SCAN_RESULT_KEY);
        } else {
            // Read the network from the intent extra passed to this activity
            mScannedNetwork = (ScanResult) getIntent().getExtras().get(SCAN_RESULT_KEY);
        }

        // Setting content view
        setContentView(R.layout.network_details_layout);

        // Obtaining handles to the widgets
        mNetworkIcon = (ImageView) findViewById(R.id.networkIcon);
        mNetworkName = (TextView) findViewById(R.id.networkName);
        mBssidValue = (TextView) findViewById(R.id.bssid_value);
        mEncryptionValue = (TextView) findViewById(R.id.encryption_value);
        mFrequencyValue = (TextView) findViewById(R.id.frequency_value);
        mIntensityValue = (TextView) findViewById(R.id.intensity_value);
        mDefaultPassValue = (TextView) findViewById(R.id.password_value);
        mConnectButton = (Button) findViewById(R.id.connectButton);
        mCopyButton = (Button) findViewById(R.id.copyPasswordButton);
        
        // Setup callbacks
        setupCopyButtonCallback();
    }

    private void setupCopyButtonCallback() {
        mCopyButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ClipboardManager clipBoard = (ClipboardManager) NetworkDetailsActivity.this
                        .getSystemService(CLIPBOARD_SERVICE);
                clipBoard.setText(mDefaultPassValue.getText());
            }
        });
    }

    /**
     * Lifecycle method: Activity is about to be shown
     * 
     * @see android.app.Activity#onStart()
     */
    protected void onStart() {
        super.onStart();
        // Setting network's icon
        int signalLevel = WifiManager.calculateSignalLevel(mScannedNetwork.level,
                WifiNetworkAdapter.MAX_SIGNAL_STRENGTH_LEVEL);
        mNetworkIcon.setImageLevel(signalLevel);
        if (WifiNetworkAdapter.getSecurity(mScannedNetwork) != WifiNetworkAdapter.SECURITY_NONE) {
            // Set an icon of encrypted wi-fi hotspot
            mNetworkIcon.setImageState(WifiNetworkAdapter.ENCRYPTED_STATE_SET, false);
        }
        // Setting network's values
        mNetworkName.setText(mScannedNetwork.SSID);
        mBssidValue.setText(mScannedNetwork.BSSID);
        mEncryptionValue.setText(mScannedNetwork.capabilities);
        mFrequencyValue.setText(mScannedNetwork.frequency + " MHz");
        mIntensityValue.setText(mScannedNetwork.level + " dBm");
        // Calculating key
        if (mScannedNetwork.SSID.matches("WLAN_....|JAZZTEL_....")) {
            IKeyCalculator keyCalculator = new WLANXXXXKeyCalculator();
            mDefaultPassValue.setText(keyCalculator.getKey(mScannedNetwork));
        } else {
            mDefaultPassValue.setText(getString(R.string.no_default_key));
            mCopyButton.setEnabled(false);
        }
    }

    /**
     * Lifecycle method: Activity state saving
     * 
     * @see android.app.Activity#onSaveInstanceState(Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SCAN_RESULT_KEY, mScannedNetwork);
    }
}
