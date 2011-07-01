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

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import com.markupartist.android.widget.ActionBar;

import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.actions.AutoScanAction;
import es.glasspixel.wlanaudit.actions.RefreshAction;
import es.glasspixel.wlanaudit.adapters.WifiNetworkAdapter;

/**
 * Application main activity. Shows the user a list of the surrounding WiFi
 * networks available. The user then picks one to see details of the network
 * 
 * @author Roberto Estrada
 */
public class NetworkListActivity extends ListActivity {

    /**
     * Activity action bar
     */
    private ActionBar mActionBar;
    /**
     * Manager of the wifi network interface
     */
    private WifiManager mWifiManager;
    /**
     * Broadcast receiver to control the completion of a scan
     */
    private BroadcastReceiver mCallBackReceiver;

    /**
     * Lifecycle management: Activity creation
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.network_list_layout);
        setupNetworkScanCallBack();

        // Action bar initialization
        mActionBar = (ActionBar) findViewById(R.id.actionBar);
        mActionBar.addAction(new AutoScanAction(this));
        mActionBar.addAction(new RefreshAction(this));

        // WifiManager initialization
        mWifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
    }

    /**
     * Lifecycle management: Activity is about to be shown
     */
    public void onStart() {
        super.onStart();
        mWifiManager.startScan();
    }

    /**
     * Lifecycle management: Activity is beig resumed, we need to refresh its
     * contents
     */
    public void onResume() {
        super.onResume();
        mWifiManager.startScan();
    }

    public void onStop() {
        super.onStop();
        // Unsubscribing from receiving updates about changes of the WiFi networks
        unregisterReceiver(mCallBackReceiver);
    }

    /**
     * Sets up the logic to execute when a scan is complete. This is done this
     * way because the SCAN_RESULTS_AVAILABLE_ACTION must be caught by a
     * BroadCastReceiver.
     */
    private void setupNetworkScanCallBack() {
        IntentFilter i = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mCallBackReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                // Network scan complete, datasource needs to be updated and
                // ListView refreshed
                getListView()
                        .setAdapter(
                                new WifiNetworkAdapter(NetworkListActivity.this,
                                        R.layout.network_list_element_layout, mWifiManager
                                                .getScanResults()));
            }
        };
        registerReceiver(mCallBackReceiver, i);
    }
}
