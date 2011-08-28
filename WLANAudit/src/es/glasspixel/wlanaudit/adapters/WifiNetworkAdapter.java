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

package es.glasspixel.wlanaudit.adapters;

import java.util.List;

import es.glasspixel.wlanaudit.R;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This class adapts the data obtained from the WiFi supplicant to a format
 * suitable to be displayed in a ListView.
 * 
 * @author Roberto Estrada
 */
public class WifiNetworkAdapter extends ArrayAdapter<ScanResult> {

	public static final int[] ENCRYPTED_STATE_SET = { R.attr.state_encrypted };

	public static final int SECURITY_NONE = 0;
	public static final int SECURITY_WEP = 1;
	public static final int SECURITY_PSK = 2;
	public static final int SECURITY_EAP = 3;

	public static final int MAX_SIGNAL_STRENGTH_LEVEL = 4;

	public WifiNetworkAdapter(Context context, int textViewResourceId,
			List<ScanResult> objects) {
		super(context, textViewResourceId, objects);
	}

	/**
	 * {@inheritDoc}
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		View listItem = convertView;
		// If the view is null, we need to inflate it from XML layout
		if (listItem == null) {
			LayoutInflater inflater = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			listItem = inflater.inflate(R.layout.network_list_element_layout,
					null);
		}
		ScanResult wifiNetwork = getItem(position);
		if (wifiNetwork != null) {
			// We need to get handles to each element of the row layout
			ImageView networkIcon = (ImageView) listItem
					.findViewById(R.id.networkSecurityIcon);
			TextView networkName = (TextView) listItem
					.findViewById(R.id.networkName);
			TextView networkDetails = (TextView) listItem
					.findViewById(R.id.networkDetails);
			// Once the handles are acquired, we're ready to populate the list
			// entry
			int signalLevel = WifiManager.calculateSignalLevel(
					wifiNetwork.level, MAX_SIGNAL_STRENGTH_LEVEL);
			networkIcon.setImageLevel(signalLevel);
			if (getSecurity(wifiNetwork) != SECURITY_NONE) {
				// Set an icon of encrypted wi-fi hotspot
				networkIcon.setImageState(ENCRYPTED_STATE_SET, false);
			}
			// Setting the network name
			networkName.setText(wifiNetwork.SSID);
			// Setting the network details
			networkDetails.setText(wifiNetwork.BSSID);
		}
		return listItem;
	}

	/**
	 * Returns the security method used in the passed scan result
	 * 
	 * @param result
	 *            The scan result to examine its security.
	 * @return The security used in this Wifi point.
	 */
	public static int getSecurity(ScanResult result) {
		if (result.capabilities.contains("WEP")) {
			return SECURITY_WEP;
		} else if (result.capabilities.contains("PSK")) {
			return SECURITY_PSK;
		} else if (result.capabilities.contains("EAP")) {
			return SECURITY_EAP;
		}
		return SECURITY_NONE;
	}
}
