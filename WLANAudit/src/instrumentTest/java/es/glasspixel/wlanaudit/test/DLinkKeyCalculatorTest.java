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

import es.glasspixel.wlanaudit.keyframework.DlinkKeyCalculator;
import es.glasspixel.wlanaudit.keyframework.NetData;

/**
 * Unit tests for the DLink Key Calculator
 * 
 * @author Roberto Estrada
 */
public class DLinkKeyCalculatorTest extends TestCase {
    /**
     * Test set of pairs of networks - result keys
     */
    private Map<NetData,String> mValidTestNetworks;
    
    public void setUp() throws Exception {
        super.setUp();
        // Random network data, default keys calculated with proven reference key generators
        mValidTestNetworks = new HashMap<NetData, String>();        
        mValidTestNetworks.put(new NetData("DLink-A5B1AA", "00:16:3E:E6:0A:55"), "NXNXwrXppa11X1XwNraN");
        mValidTestNetworks.put(new NetData("DLink-630643", "00:1C:14:39:27:4F"), "5XHXdrq6YraHXaqd5rrH");
        mValidTestNetworks.put(new NetData("DLink-94567C", "00:16:3E:F8:27:C8"), "SX6XdrqpSa51X5qdSra6");
        mValidTestNetworks.put(new NetData("DLink-BF1942", "00:0C:29:24:89:86"), "pXSXYXS6HqqYXqSYpXqS");
        mValidTestNetworks.put(new NetData("DLink-75EA34", "00:16:3E:10:7E:E2"), "qX1X1rdpXar1Xrd1qra1");        
    }
    
    public void testGetKey() {
        // Tests valid keys
        for(NetData network : mValidTestNetworks.keySet()) {
            assertEquals(mValidTestNetworks.get(network), new DlinkKeyCalculator().getKey(network).get(0));
        }
    }
}
