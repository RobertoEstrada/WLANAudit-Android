/*
 * Copyright (C) 2013 The WLANAudit project contributors.
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

import android.content.Context;
import android.net.wifi.WifiManager;

/**
 * Implements the action bar network refresh logic
 */
public class RefreshAction implements Action {

    /**
     * A context for this action
     */
    private final Context mContext;

	public RefreshAction(Context context) {
		mContext = context;
	}

	public void performAction() {
		WifiManager wifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		wifiManager.startScan();
	}
}
