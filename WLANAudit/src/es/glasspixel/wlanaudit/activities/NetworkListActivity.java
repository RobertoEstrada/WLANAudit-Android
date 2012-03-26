/*
 * Copyright (C) 2012 Roberto Estrada
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

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.actions.AutoScanAction;
import es.glasspixel.wlanaudit.actions.RefreshAction;
import es.glasspixel.wlanaudit.adapters.WifiNetworkAdapter;
import es.glasspixel.wlanaudit.ads.Key;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

/**
 * Application main activity. Shows the user a list of the surrounding WiFi
 * networks available. The user then picks one to see details of the network
 * 
 * @author Roberto Estrada
 */
public class NetworkListActivity extends SherlockListActivity {

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
     * Handle to the action bar's refresh action
     */
    private RefreshAction mRefreshAction;

    /**
     * Advertisement
     */
    private AdView mAd;

    /**
     * Lifecycle management: Activity creation
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.network_list_layout);

        // Action bar actions initialization
        if (savedInstanceState != null && savedInstanceState.getBoolean("autoscan_state")) {
            mAutoScanAction = new AutoScanAction(this, true);
        } else {
            mAutoScanAction = new AutoScanAction(this);
        }

        mRefreshAction = new RefreshAction(this);

        // WifiManager initialization
        mWifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // If preference does not exist
        if (!prefs.contains("wifi_autostart")) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("wifi_autostart", true);
            editor.commit();
        }

        // If WiFi is disabled, enable it
        if (!mWifiManager.isWifiEnabled() && prefs.getBoolean("wifi_autostart", true)) {
            mWifiManager.setWifiEnabled(true);
        }

        // Ads Initialization
        LinearLayout layout = (LinearLayout) findViewById(R.id.adLayout);
        mAd = new AdView(this, AdSize.BANNER, Key.ADMOB_KEY);
        layout.addView(mAd);
    }

    /**
     * Lifecycle management: Menu creation
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.networklistactivity_menu, menu);
        return true;
    }

    /**
     * Menu option handling
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.scanOption:
                mRefreshAction.performAction();
                return true;
            case R.id.toggleAutoscanOption:
                mAutoScanAction.performAction();                
                return true;
            case R.id.preferenceOption:
                i = new Intent(this, WLANAuditPreferencesActivity.class);
                startActivity(i);
                return true;
            case R.id.aboutOption:
                i = new Intent(this, AboutActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Lifecycle management: Activity is about to be shown
     */
    protected void onStart() {
        super.onStart();
        setupNetworkScanCallBack();
        mWifiManager.startScan();
        mAd.loadAd(new AdRequest());
    }

    /**
     * Lifecycle management: Activity is being resumed, we need to refresh its
     * contents
     */
    protected void onResume() {
        super.onResume();
        mWifiManager.startScan();
        mAd.loadAd(new AdRequest());
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
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent i = new Intent(this, NetworkDetailsActivity.class);
        i.putExtra(NetworkDetailsActivity.SCAN_RESULT_KEY, (ScanResult) getListView()
                .getItemAtPosition(position));
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
