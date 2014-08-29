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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import es.glasspixel.wlanaudit.keyframework.NetData;
import es.glasspixel.wlanaudit.keyframework.WiFiXXXXXXKeyCalculator;

/**
 * Unit tests for the WiFi6X Key Calculator
 * 
 * @author Roberto Estrada
 */
public class WiFi6XKeyCalculatorTest extends TestCase {
    /**
     * Test set of pairs of networks - result keys
     */
    private List<NetData> mValidTestNetworks;
    private Set<String> mValidCandidateKeys;
    
    protected void setUp() throws Exception {
        super.setUp();
        // Random network data, default keys calculated with proven reference key generators
        mValidTestNetworks = new ArrayList<NetData>();
        mValidTestNetworks.add(new NetData("WLAN4A9E2B", "00:16:3E:A3:07:76"));
        // Set of valid default keys that match the random data
        mValidCandidateKeys = new HashSet<String>();
        mValidCandidateKeys.add("209C4974156BC");
        mValidCandidateKeys.add("279C3E74126CB");
        mValidCandidateKeys.add("269C2F74136DA");
        mValidCandidateKeys.add("259C1C74106E9");
        mValidCandidateKeys.add("249C0D74116F8");
        mValidCandidateKeys.add("2B9CF2741E607");
        mValidCandidateKeys.add("2A9CE3741F616");
        mValidCandidateKeys.add("299CD0741C625");
        mValidCandidateKeys.add("289CC1741D634");
        mValidCandidateKeys.add("2F9CB6741A643");        
    }

    public void testGetKey() {
        // Tests valid keys
        for(NetData network : mValidTestNetworks) {
            List<String> generatedKeys = new WiFiXXXXXXKeyCalculator().getKey(network);
            assertNotNull(generatedKeys);
            for(String generatedKey : generatedKeys) {
                assertTrue("A candidate valid key is missing", mValidCandidateKeys.contains(generatedKey));
            }
        }
    }

}
