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
package es.glasspixel.wlanaudit.keyframework;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.net.wifi.ScanResult;

/**
 * Data structure to represent a vulnerable combination of ssid and bssid
 * 
 * @author Roberto Estrada
 */
class VulnerablePattern {
    /**
     * Compiled SSID Pattern
     */
    private final Pattern mSsidPattern;

    /**
     * Compiled BSSID Pattern
     */
    private final Pattern mBssidPattern;

    public VulnerablePattern(String ssidPattern, String bssidPattern) {
        mSsidPattern = Pattern.compile(ssidPattern,Pattern.CASE_INSENSITIVE);
        mBssidPattern = Pattern.compile(bssidPattern,Pattern.CASE_INSENSITIVE);
    }

    public boolean isVulnerable(ScanResult network) {
        Matcher ssidMatcher = mSsidPattern.matcher(network.SSID);
        Matcher bssidMatcher = mBssidPattern.matcher(network.BSSID);
        return ssidMatcher.matches() && bssidMatcher.matches();
    }
}
