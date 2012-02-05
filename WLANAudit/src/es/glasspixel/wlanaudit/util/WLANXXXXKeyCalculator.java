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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import android.net.wifi.ScanResult;

/**
 * Default key calculator for networks with SSID form WLAN_XXXX or JAZZTEL_XXXX
 * 
 * @author Roberto Estrada
 */
public class WLANXXXXKeyCalculator implements IKeyCalculator {

	private static final String HEXES = "0123456789ABCDEF";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getKey(ScanResult network) {
		String trimmedBSSID = network.BSSID.replaceAll(":", "").toUpperCase();
		String formattedESSID = null;
		if (network.SSID.contains("JAZZTEL_")) {
			formattedESSID = network.SSID.replace("JAZZTEL_", "").toUpperCase();
		} else if (network.SSID.contains("WLAN_")) {
			formattedESSID = network.SSID.replace("WLAN_", "").toUpperCase();
		}
		// Key calculation
		String stringToHash = "bcgbghgg" + trimmedBSSID.substring(0, 8)
				+ formattedESSID + trimmedBSSID;
		// Hashing
		MessageDigest hasher;
		try {
			hasher = MessageDigest.getInstance("MD5");
			List<String> result = new ArrayList<String>();
			result.add(getHex(hasher.digest(stringToHash.getBytes("UTF-8")))
					.substring(0, 20).toLowerCase());
			return result;
		} catch (NoSuchAlgorithmException e) {
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}

	/**
	 * Converts a byte array to an hex string. Code from: Snippet from:
	 * http://rgagnon.com/javadetails/java-0596.html
	 * 
	 * @param data
	 *            The array of bytes to encode
	 * @return The encoded hex string
	 */
	public static String getHex(byte[] raw) {
		if (raw == null) {
			return null;
		}
		final StringBuilder hex = new StringBuilder(2 * raw.length);
		for (final byte b : raw) {
			hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(
					HEXES.charAt((b & 0x0F)));
		}
		return hex.toString();
	}
}
