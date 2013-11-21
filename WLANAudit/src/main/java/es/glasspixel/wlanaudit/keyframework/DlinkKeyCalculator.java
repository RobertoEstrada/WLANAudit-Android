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
 * Default key generator for DLink routers. Documentation about the algorithm
 * can be found at: http://lixei.me/codigo-fonte-wpa-dlink-php-c/
 *
 * @author Roberto Estrada
 */
public class DlinkKeyCalculator implements IKeyCalculator {
    private static final char HASH[] = {
            'X', 'r', 'q', 'a', 'H', 'N', 'p', 'd', 'S', 'Y', 'w', '8', '6', '2', '1', '5'
    };

    @Override
    public List<String> getKey(NetData network) {
        char key[] = new char[20];
        char newkey[] = new char[20];
        int index;
        char t;
        String trimmedBssid = network.BSSID.replace(":", "");
        List<String> result = null;

        key[0] = trimmedBssid.charAt(11);
        key[1] = trimmedBssid.charAt(0);
        key[2] = trimmedBssid.charAt(10);
        key[3] = trimmedBssid.charAt(1);
        key[4] = trimmedBssid.charAt(9);
        key[5] = trimmedBssid.charAt(2);
        key[6] = trimmedBssid.charAt(8);
        key[7] = trimmedBssid.charAt(3);
        key[8] = trimmedBssid.charAt(7);
        key[9] = trimmedBssid.charAt(4);
        key[10] = trimmedBssid.charAt(6);
        key[11] = trimmedBssid.charAt(5);
        key[12] = trimmedBssid.charAt(1);
        key[13] = trimmedBssid.charAt(6);
        key[14] = trimmedBssid.charAt(8);
        key[15] = trimmedBssid.charAt(9);
        key[16] = trimmedBssid.charAt(11);
        key[17] = trimmedBssid.charAt(2);
        key[18] = trimmedBssid.charAt(4);
        key[19] = trimmedBssid.charAt(10);

        for (int i = 0; i < 20; i++) {
            t = key[i];
            if ((t >= '0') && (t <= '9'))
                index = t - '0';
            else {
                t = Character.toUpperCase(t);
                if ((t >= 'A') && (t <= 'F'))
                    index = t - 'A' + 10;
                else
                    return result;
            }

            newkey[i] = HASH[index];
        }

        result = new ArrayList<String>();
        result.add(String.valueOf(newkey));

        return result;
    }

}
