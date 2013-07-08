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
import es.glasspixel.wlanaudit.keyframework.HWeiKeyCalculator;
import es.glasspixel.wlanaudit.keyframework.NetData;

/**
 * Unit tests for the WLAN4X Key Calculator
 * 
 * @author Roberto Estrada
 */
public class HWeiKeyCalculatorTest extends TestCase {
    /**
     * Test set of pairs of networks - result keys
     */
    private Map<NetData,String> mValidTestNetworks;
    
    public void setUp() throws Exception {
        super.setUp();
        // Random network data, default keys calculated with proven reference key generators
        mValidTestNetworks = new HashMap<NetData, String>();        
        mValidTestNetworks.put(new NetData("HWEI", "F4:C7:14:92:09:A8"), "6637633230");
        mValidTestNetworks.put(new NetData("HWEI", "64:16:F0:30:0B:E1"), "6137646136");
        mValidTestNetworks.put(new NetData("HWEI", "5C:4C:A9:B6:E2:CC"), "3236383636");
        mValidTestNetworks.put(new NetData("HWEI", "54:A5:1B:DE:5D:AB"), "3266333532");
        mValidTestNetworks.put(new NetData("HWEI", "54:89:98:86:59:66"), "6238366666");
        mValidTestNetworks.put(new NetData("HWEI", "4C:54:99:FC:60:E2"), "3832313334");
        mValidTestNetworks.put(new NetData("HWEI", "4C:1F:CC:3B:92:08"), "3137323832");
        mValidTestNetworks.put(new NetData("HWEI", "40:4D:8E:6C:5C:0E"), "6237343133");  
        mValidTestNetworks.put(new NetData("HWEI", "30:87:30:61:F1:85"), "6239623339");
        mValidTestNetworks.put(new NetData("HWEI", "28:6E:D4:6A:81:D0"), "6165373636");
        mValidTestNetworks.put(new NetData("HWEI", "28:5F:DB:A8:A9:5A"), "3439333035");
        mValidTestNetworks.put(new NetData("HWEI", "24:DB:AC:E2:5F:93"), "6261366161");
        mValidTestNetworks.put(new NetData("HWEI", "20:F3:A3:D5:11:20"), "6465363837");
        mValidTestNetworks.put(new NetData("HWEI", "20:2B:C1:CA:62:5C"), "6435323861");
        mValidTestNetworks.put(new NetData("HWEI", "1C:1D:67:B2:CB:32"), "6634613136");
        mValidTestNetworks.put(new NetData("HWEI", "10:C6:1F:53:27:5D"), "3036656533");
        mValidTestNetworks.put(new NetData("HWEI", "0C:37:DC:5B:CA:4A"), "6661396334");
        mValidTestNetworks.put(new NetData("HWEI", "08:19:A6:7E:ED:6D"), "6563373834");
        mValidTestNetworks.put(new NetData("HWEI", "04:C0:6F:60:7F:08"), "3866353538");
        mValidTestNetworks.put(new NetData("HWEI", "00:25:9E:5B:74:5E"), "3361653938");
        mValidTestNetworks.put(new NetData("HWEI", "00:25:68:E2:0E:A5"), "3833373939");
        mValidTestNetworks.put(new NetData("HWEI", "00:22:A1:96:44:D5"), "6161303865");
        mValidTestNetworks.put(new NetData("HWEI", "00:1E:10:F6:5B:67"), "3830666635");
        mValidTestNetworks.put(new NetData("HWEI", "00:19:15:C9:5E:EA"), "3838666336");
        mValidTestNetworks.put(new NetData("HWEI", "00:18:82:38:3E:04"), "3765613361");
        mValidTestNetworks.put(new NetData("HWEI", "00:11:F5:5F:11:D6"), "3862393563");
        mValidTestNetworks.put(new NetData("HWEI", "00:0F:E2:CC:AC:79"), "6365363263");

    }
    
    public void testGetKey() {
        // Tests valid keys
        for(NetData network : mValidTestNetworks.keySet()) {
            assertEquals(mValidTestNetworks.get(network), new HWeiKeyCalculator().getKey(network).get(0));
        }
    }
}
