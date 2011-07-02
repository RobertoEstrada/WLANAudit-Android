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
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

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
     * Handle to the action bar's autoscan action to activate or deactivate the
     * autoscan on lifecycle events that require it
     */
    private AutoScanAction mAutoScanAction;

    /**
     * Lifecycle management: Activity creation
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.network_list_layout);

        // Action bar initialization
        mActionBar = (ActionBar) findViewById(R.id.actionBar);
        if (savedInstanceState != null && savedInstanceState.getBoolean("autoscan_state")) {
            mAutoScanAction = new AutoScanAction(this, true);
        } else {
            mAutoScanAction = new AutoScanAction(this);
        }
        mActionBar.addAction(mAutoScanAction);
        mActionBar.addAction(new RefreshAction(this));

        // WifiManager initialization
        mWifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
    }

    /**
     * Lifecycle management: Activity is about to be shown
     */
    protected void onStart() {
        super.onStart();
        setupNetworkScanCallBack();
        mWifiManager.startScan();
    }

    /**
     * Lifecycle management: Activity is being resumed, we need to refresh its
     * contents
     */
    protected void onResume() {
        super.onResume();
        mWifiManager.startScan();
    }

    /**
     * Lifecycle management: Activity is being stopped, we need to unregister
     * the broadcast receiver
     */
    protected void onStop() {
        super.onStop();
        // Unsubscribing from receiving updates about changes of the WiFi
        // networks
        unregisterReceiver(mCallBackReceiver);
    }

    /**
     * Lifecycle management: Activity state is saved to be restored later
     */
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("autoscan_state", mAutoScanAction.isAutoScanEnabled());
    }

    protected void onDestroy() {
        super.onDestroy();
        mAutoScanAction.stopAutoScan();
    }
    
    /**
     * Handles the event of clicking on a list element. This method opens the
     * detail view associated to the clicked element.
     */
    protected void onListItemClick(ListView l, View v, int position, long id){
        Intent i = new Intent(this, NetworkDetailsActivity.class);
        i.putExtra(NetworkDetailsActivity.SCAN_RESULT_KEY, (ScanResult)getListView().getItemAtPosition(position));
        startActivity(i);
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
