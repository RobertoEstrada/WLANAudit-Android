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
	 * Router kinds to use the appropiate default key generarion algorithm.
	 */
	private static enum RouterKind {
		COM_KIND, ZYX_KIND, UNK_KIND
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getKey(ScanResult network) {
		String formattedESSID = null;
		if (network.SSID.contains("JAZZTEL_")) {
			if(network.BSSID.matches("(38:72:C0:[0-9A-Fa-f:]{8})"))
				return null;
			formattedESSID = network.SSID.replace("JAZZTEL_", "").toUpperCase();
		} else if (network.SSID.contains("WLAN_")) {
			formattedESSID = network.SSID.replace("WLAN_", "").toUpperCase();
		}

		RouterKind kind = getRouterKind(network.BSSID.toUpperCase());
		String stringToHash = null;
		String trimmedBSSID = null;

		// SavedKey calculation based on router kind
		switch (kind) {
		case COM_KIND:
			trimmedBSSID = network.BSSID.replaceAll(":", "").toUpperCase();
			stringToHash = "bcgbghgg" + trimmedBSSID.substring(0, 8)
					+ formattedESSID + trimmedBSSID;
			break;
		case ZYX_KIND:
			formattedESSID.toLowerCase();
			trimmedBSSID = network.BSSID.replaceAll(":", "").toLowerCase();
			stringToHash = (trimmedBSSID.substring(0, 8) + formattedESSID)
					.toLowerCase();
			break;
		case UNK_KIND:
			return null;
		}

		List<String> keyList = new ArrayList<String>();
		keyList.add(hashString(stringToHash));

		return keyList;
	}

	/**
	 * Returns the appropiate router kind based on its bssid to generate the
	 * right default key
	 * 
	 * @param bssid
	 *            The BSSID of the AP being audited
	 * @return The kind of the router.
	 */
	private RouterKind getRouterKind(String bssid) {
		if (bssid.matches("(64:68:0C:[0-9A-Fa-f:]{8})")) {
			return RouterKind.COM_KIND;
		} else if (bssid.matches("(00:1D:20:[0-9A-Fa-f:]{8})")) {
			return RouterKind.COM_KIND;
		} else if (bssid.matches("(00:1B:20:[0-9A-Fa-f:]{8})")) {
			return RouterKind.COM_KIND;
		} else if (bssid.matches("(38:72:C0:[0-9A-Fa-f:]{8})")) {
			return RouterKind.COM_KIND;
		} else if (bssid.matches("(00:23:F8:[0-9A-Fa-f:]{8})")) {
			return RouterKind.COM_KIND;
		} else if (bssid.matches("(00:1F:A4:[0-9A-Fa-f:]{8})")) {
			return RouterKind.ZYX_KIND;
		} else if (bssid.matches("(F4:3E:61:[0-9A-Fa-f:]{8})")) {
			return RouterKind.ZYX_KIND;
		}
		return RouterKind.UNK_KIND;
	}

	private String hashString(String stringToHash) {
		// Hashing
		MessageDigest hasher;
		try {
			hasher = MessageDigest.getInstance("MD5");
			return (getHex(hasher.digest(stringToHash.getBytes("UTF-8")))
					.substring(0, 20).toLowerCase());
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
