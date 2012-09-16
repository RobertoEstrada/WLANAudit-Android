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

package es.glasspixel.wlanaudit.actions;

import es.glasspixel.wlanaudit.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Implements the action bar autoscan toggle logic
 */
public class AutoScanAction implements Action {

    /**
     * Time before first scan
     */
    private static final int TIME_BEFORE_START = 1000;

    /**
     * Status of the autoscan feature
     */
    private static boolean mIsAutoScanEnabled = false;
    /**
     * Context for the action
     */
    private Context mContext;
    /**
     * Clock to manage the interval between scan requests
     */
    private static Timer mAutoScanTimer;

    /**
     * Constructor
     * 
     * @param context
     */
    public AutoScanAction(Context context) {
        mContext = context;
    }

    /**
     * Constructor meant for restoring the state of this action when the app
     * state is recovered.
     * 
     * @param activity Activity context
     * @param autoScanInitialState The state of the autoscan
     */
    public AutoScanAction(Context context, boolean autoScanInitialState) {
        this(context);
        mIsAutoScanEnabled = autoScanInitialState;
        scheduleScan();
    }

    public boolean isAutoScanEnabled() {
        return mIsAutoScanEnabled;
    }

    /**
     * Initiates autoscan
     */
    public void scheduleScan() {
        mIsAutoScanEnabled = true;
        mAutoScanTimer = new Timer();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mAutoScanTimer.scheduleAtFixedRate(new AutoScanTask(), TIME_BEFORE_START, prefs.getInt("autoscan_interval", 30)*1000);        
    }

    /**
     * Stops autoscan
     */
    public void stopAutoScan() {
        mIsAutoScanEnabled = false;
        if (mAutoScanTimer != null) {
            mAutoScanTimer.cancel();
        }
    }    

    /**
     * {@inheritDoc}
     */
    public void performAction() {
        if (!mIsAutoScanEnabled) {
            scheduleScan();
            showToast(mContext.getString(R.string.autoscan_enabled));
        } else {
            stopAutoScan();
            showToast(mContext.getString(R.string.autoscan_disabled));
        }
    }

    /**
     * Displays a notification toast with the specified message
     * 
     * @param message The message to display
     */
    private void showToast(String message) {
        Toast notificationToast = Toast.makeText(mContext, message, Toast.LENGTH_SHORT);
        notificationToast.setGravity(Gravity.CENTER, 0, 0);
        notificationToast.show();
    }

    /**
     * The autoscan task which is executed periodically
     */
    private class AutoScanTask extends TimerTask {

        @Override
        public void run() {
            WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            wifiManager.startScan();
        }
    }
}
