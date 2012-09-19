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
package es.glasspixel.wlanaudit.test;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import es.glasspixel.wlanaudit.keyframework.NetData;
import es.glasspixel.wlanaudit.keyframework.WLANXXXXKeyCalculator;

/**
 * Unit tests for the WLAN4X Key Calculator
 * 
 * @author Roberto Estrada
 */
public class WLAN4XKeyCalculatorTest extends TestCase {
    /**
     * Test set of pairs of networks - result keys
     */
    private Map<NetData,String> mValidTestNetworks;
    
    public void setUp() throws Exception {
        super.setUp();
        // Random network data, default keys calculated with proven reference key generators
        mValidTestNetworks = new HashMap<NetData, String>();        
        mValidTestNetworks.put(new NetData("WLAN_A5B1", "64:68:0C:1B:3B:05"), "2466d5c92322112fe706");
        mValidTestNetworks.put(new NetData("WLAN_6306", "00:1D:20:A2:6B:BB"), "0298743917ce4c0de6d6");
        mValidTestNetworks.put(new NetData("WLAN_9456", "00:1B:20:36:B6:AC"), "cb3fbf6a9cfafbd40deb");
        mValidTestNetworks.put(new NetData("WLAN_BF42", "38:72:C0:82:DF:8E"), "36c59fe4b2796db7bc00");
        mValidTestNetworks.put(new NetData("JAZZTEL_75EA", "00:23:F8:B5:93:F8"), "9197053d5437794da011");
        mValidTestNetworks.put(new NetData("JAZZTEL_21DB", "00:1F:A4:3B:7E:51"), "B4B97A37CB924BCD231D");
        mValidTestNetworks.put(new NetData("JAZZTEL_8FAC", "F4:3E:61:63:DE:C3"), "D5B06B761196A47BA1EC");
        mValidTestNetworks.put(new NetData("JAZZTEL_CF28", "40:4A:03:6E:A0:6C"), "CCB9C3D4B3CBF508F57A");
    }
    
    public void testGetKey() {
        // Tests valid keys
        for(NetData network : mValidTestNetworks.keySet()) {
            assertEquals(mValidTestNetworks.get(network), new WLANXXXXKeyCalculator().getKey(network).get(0));
        }
        // Tests invalid combinations
        assertEquals(null, new WLANXXXXKeyCalculator().getKey(new NetData("JAZZTEL_46AF", "38:72:C0:62:B0:CA")));
    }
}
