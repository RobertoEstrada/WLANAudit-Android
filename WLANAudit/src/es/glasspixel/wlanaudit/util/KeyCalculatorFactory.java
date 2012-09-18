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
     * Registers a new vulnerable pattern in the system
     * @param ssidPattern The vulnerable SSID
     * @param bssidPattern The vulnerable BSSID
     * @param calculator The calculator that can handle  the key calculation for the pattern
     */
    private void addPattern(String ssidPattern, String bssidPattern, Class<? extends IKeyCalculator> calculator) {
        mCalculatorDict.put(new VulnerablePattern(ssidPattern, bssidPattern), calculator);
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
        
        //HWei Patterns
        addPattern("(.*)", "(F4:C7:14:[0-9A-Fa-f:]{8})", HWeiKeyCalculator.class);
        addPattern("(.*)", "(64:16:F0:[0-9A-Fa-f:]{8})", HWeiKeyCalculator.class);
        addPattern("(.*)", "(5C:4C:A9:[0-9A-Fa-f:]{8})", HWeiKeyCalculator.class);
        addPattern("(.*)", "(54:A5:1B:[0-9A-Fa-f:]{8})", HWeiKeyCalculator.class);
        addPattern("(.*)", "(54:89:98:[0-9A-Fa-f:]{8})", HWeiKeyCalculator.class);
        addPattern("(.*)", "(4C:54:99:[0-9A-Fa-f:]{8})", HWeiKeyCalculator.class);
        addPattern("(.*)", "(4C:1F:CC:[0-9A-Fa-f:]{8})", HWeiKeyCalculator.class);
        addPattern("(.*)", "(40:4D:8E:[0-9A-Fa-f:]{8})", HWeiKeyCalculator.class);
        addPattern("(.*)", "(30:87:30:[0-9A-Fa-f:]{8})", HWeiKeyCalculator.class);
        addPattern("(.*)", "(28:6E:D4:[0-9A-Fa-f:]{8})", HWeiKeyCalculator.class);
        addPattern("(.*)", "(28:5F:DB:[0-9A-Fa-f:]{8})", HWeiKeyCalculator.class);
        addPattern("(.*)", "(24:DB:AC:[0-9A-Fa-f:]{8})", HWeiKeyCalculator.class);
        addPattern("(.*)", "(20:F3:A3:[0-9A-Fa-f:]{8})", HWeiKeyCalculator.class);
        addPattern("(.*)", "(20:2B:C1:[0-9A-Fa-f:]{8})", HWeiKeyCalculator.class);
        addPattern("(.*)", "(1C:1D:67:[0-9A-Fa-f:]{8})", HWeiKeyCalculator.class);
        addPattern("(.*)", "(10:C6:1F:[0-9A-Fa-f:]{8})", HWeiKeyCalculator.class);
        addPattern("(.*)", "(0C:37:DC:[0-9A-Fa-f:]{8})", HWeiKeyCalculator.class);
        addPattern("(.*)", "(08:19:A6:[0-9A-Fa-f:]{8})", HWeiKeyCalculator.class);
        addPattern("(.*)", "(04:C0:6F:[0-9A-Fa-f:]{8})", HWeiKeyCalculator.class);
        addPattern("(.*)", "(00:25:9E:[0-9A-Fa-f:]{8})", HWeiKeyCalculator.class);
        addPattern("(.*)", "(00:25:68:[0-9A-Fa-f:]{8})", HWeiKeyCalculator.class);
        addPattern("(.*)", "(00:22:A1:[0-9A-Fa-f:]{8})", HWeiKeyCalculator.class);
        addPattern("(.*)", "(00:1E:10:[0-9A-Fa-f:]{8})", HWeiKeyCalculator.class);
        addPattern("(.*)", "(00:19:15:[0-9A-Fa-f:]{8})", HWeiKeyCalculator.class);
        addPattern("(.*)", "(00:18:82:[0-9A-Fa-f:]{8})", HWeiKeyCalculator.class);
        addPattern("(.*)", "(00:11:F5:[0-9A-Fa-f:]{8})", HWeiKeyCalculator.class);
        addPattern("(.*)", "(00:0F:E2:[0-9A-Fa-f:]{8})", HWeiKeyCalculator.class);
        
        // Discus Patterns
        addPattern("Discus--([0-9a-fA-F]{6})", "([0-9A-Fa-f:]{17})", DiscusCalculator.class);
        
        // DLink Patterns
        addPattern("DLink-([0-9a-fA-F]{6})", "([0-9A-Fa-f:]{17})", DlinkKeyCalculator.class);
    }
}
