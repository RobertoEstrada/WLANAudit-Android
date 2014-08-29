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

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

import es.glasspixel.wlanaudit.keyframework.DiscusKeyCalculator;
import es.glasspixel.wlanaudit.keyframework.NetData;

/**
 * Unit tests for the Discus Key Calculator
 * 
 * @author Roberto Estrada
 */
public class DiscusKeyCalculatorTest extends TestCase {
    /**
     * Test set of pairs of networks - result keys
     */
    private Map<NetData,String> mValidTestNetworks;
    
    public void setUp() throws Exception {
        super.setUp();
        // Random network data, default keys calculated with proven reference key generators
        mValidTestNetworks = new HashMap<NetData, String>();        
        mValidTestNetworks.put(new NetData("Discus--A5B1AA", "00:16:3E:E6:0A:55"), "YW0-708257");
        mValidTestNetworks.put(new NetData("Discus--630643", "00:1C:14:39:27:4F"), "YW0-1800571");
        mValidTestNetworks.put(new NetData("Discus--94567C", "00:16:3E:F8:27:C8"), "YW0-992621");
        mValidTestNetworks.put(new NetData("Discus--BF1942", "00:0C:29:24:89:86"), "YW0-292027");
        mValidTestNetworks.put(new NetData("Discus--75EA34", "00:16:3E:10:7E:E2"), "YW0-1491071");        
    }
    
    public void testGetKey() {
        // Tests valid keys
        for(NetData network : mValidTestNetworks.keySet()) {
            assertEquals(mValidTestNetworks.get(network), new DiscusKeyCalculator().getKey(network).get(0));
        }
    }
}
