/*
 * Copyright (C) 2013 The WLANAudit project contributors.
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

import java.util.ArrayList;
import java.util.List;


/**
 * Default key generator for the Pirelli Discus DRG A225 Documentation on the
 * algorithm can be found at:
 * 
 * http://www.remote-exploit.org/content/Pirelli_Discus_DRG_A225_WiFi_router.pdf
 * 
 * @author Roberto Estrada
 */
public class DiscusKeyCalculator implements IKeyCalculator {

    @Override
    public List<String> getKey(NetData network) {
        List<String> result = new ArrayList<String>();
        int cnst = Integer.parseInt("D0EC31",16);
        int input = Integer.parseInt(network.SSID.substring(network.SSID.length() - 6), 16);
        int res = (input - cnst) / 4;
        String key = "YW0" + Integer.toString(res);
        result.add(key);
        return result;
    }
}
