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

package es.glasspixel.wlanaudit.util;

import java.util.HashMap;
import java.util.Map;

import android.net.wifi.ScanResult;
import android.util.Log;

public class KeyCalculatorFactory {
    private static final String TAG = "KeyCalculatorFactory";

    /**
     * Singleton instance for the factory
     */
    private static KeyCalculatorFactory instance;

    /**
     * Map which associates the patterns which are vulnerable with the appropriate
     * calculator
     */
    protected Map<VulnerablePattern, Class<? extends IKeyCalculator>> mCalculatorDict;

    private KeyCalculatorFactory() {
        mCalculatorDict = new HashMap<VulnerablePattern, Class<? extends IKeyCalculator>>();
        addPatterns();
    }

    /**
     * Returns the appropiate key calculator implementation
     * 
     * @param network The network to find an appropiate key calculator
     * @return A key calculator or null if an appropriate one is not found
     */
    public static IKeyCalculator getKeyCalculator(ScanResult network) {
        if (instance == null) {
            instance = new KeyCalculatorFactory();
        }
        for (VulnerablePattern vp : instance.mCalculatorDict.keySet()) {
            if (vp.isVulnerable(network)) {
                try {
                    return (IKeyCalculator) Class.forName(
                            instance.mCalculatorDict.get(vp).getName()).newInstance();
                } catch (InstantiationException e) {
                    Log.e(TAG, "Failed to instantiate the required calculator.");
                } catch (IllegalAccessException e) {
                    Log.e(TAG, "Failed access to the desired field or method.");
                } catch (ClassNotFoundException e) {
                    Log.e(TAG,
                            "The specified class was not found therefore I was unable to instantiate it.");
                }
            }
        }
        return null;
    }
    
    /**
     * Add in this method the pattern-calculator combinations to instance the appropriate calculator
     */
    private void addPatterns() {
        //WLAN4X Patterns
        addPattern("(?:WLAN|JAZZTEL)_([0-9a-fA-F]{4})", "(64:68:0C:[0-9A-Fa-f:]{8})", WLANXXXXKeyCalculator.class);
        addPattern("(?:WLAN|JAZZTEL)_([0-9a-fA-F]{4})", "(00:1D:20:[0-9A-Fa-f:]{8})", WLANXXXXKeyCalculator.class);
        addPattern("(?:WLAN|JAZZTEL)_([0-9a-fA-F]{4})", "(00:1B:20:[0-9A-Fa-f:]{8})", WLANXXXXKeyCalculator.class);
        addPattern("(?:WLAN|JAZZTEL)_([0-9a-fA-F]{4})", "(38:72:C0:[0-9A-Fa-f:]{8})", WLANXXXXKeyCalculator.class);
        addPattern("(?:WLAN|JAZZTEL)_([0-9a-fA-F]{4})", "(00:23:F8:[0-9A-Fa-f:]{8})", WLANXXXXKeyCalculator.class);
        addPattern("(?:WLAN|JAZZTEL)_([0-9a-fA-F]{4})", "(00:1F:A4:[0-9A-Fa-f:]{8})", WLANXXXXKeyCalculator.class);
        addPattern("(?:WLAN|JAZZTEL)_([0-9a-fA-F]{4})", "(F4:3E:61:[0-9A-Fa-f:]{8})", WLANXXXXKeyCalculator.class);
        addPattern("(?:WLAN|JAZZTEL)_([0-9a-fA-F]{4})", "(40:4A:03:[0-9A-Fa-f:]{8})", WLANXXXXKeyCalculator.class);
        //WiFi6X Patterns
        addPattern("(?:WLAN|YACOM|WiFi)([0-9a-fA-F]{6})","([0-9A-Fa-f:]{17})", WiFiXXXXXXKeyCalculator.class);
    }
    /**
     * Registers a new vulnerable pattern in the system
     * @param ssidPattern The vulnerable SSID
     * @param bssidPattern The vulnerable BSSID
     * @param calculator The calculator that can handle  the key calculation for the pattern
     */
    private void addPattern(String ssidPattern, String bssidPattern, Class<? extends IKeyCalculator> calculator) {
        mCalculatorDict.put(new VulnerablePattern(ssidPattern, bssidPattern), calculator);
    }
}
