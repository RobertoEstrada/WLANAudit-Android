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

package es.glasspixel.wlanaudit.actions;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.view.View;

import com.markupartist.android.widget.ActionBar.Action;

import es.glasspixel.wlanaudit.R;

/**
 * Implements the action bar autoscan toggle logic
 */
public class AutoScanAction implements Action {

    /**
     * Time before first scan
     */
    private static final int TIME_BEFORE_START = 500;

    /**
     * Status of the autoscan feature
     */
    private boolean mIsAutoScanEnabled = false;
    /**
     * Context for the action
     */
    private Context mContext;
    /**
     * Clock to manage the interval between scan requests
     */
    private Timer mAutoScanTimer;

    public AutoScanAction(Context context) {
        mContext = context;
        mAutoScanTimer = new Timer();
    }

    public int getDrawable() {
        return R.drawable.ic_action_autoscan;
    }

    public void performAction(View view) {
        if(!mIsAutoScanEnabled) {
            mIsAutoScanEnabled = true;
            mAutoScanTimer.scheduleAtFixedRate(new AutoScanTask(), TIME_BEFORE_START, 5000);
        }else {
            mIsAutoScanEnabled = false;
            mAutoScanTimer.cancel();
        }
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
