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

package es.glasspixel.wlanaudit.util;

/**
 * This class implements a simple function to calculate the channel number
 * from the WiFi frequency. Uses this table to return results
 * (Took from http://stackoverflow.com/a/5488225)
 * <p/>
 * CHA    LOWER   CENTER  UPPER
 * NUM    FREQ    FREQ    FREQ
 * MHZ     MHZ     MHZ
 * <p/>
 * 1     2 401   2 412   2 423
 * 2     2 404   2 417   2 428
 * 3     2 411   2 422   2 433
 * 4     2 416   2 427   2 438
 * 5     2 421   2 432   2 443
 * 6     2 426   2 437   2 448
 * 7     2 431   2 442   2 453
 * 8     2 436   2 447   2 458
 * 9     2 441   2 452   2 463
 * 10     2 451   2 457   2 468
 * 11     2 451   2 462   2 473
 * 12     2 456   2 467   2 478
 * 13     2 461   2 472   2 483
 *
 * @author RobertoEstrada
 */
public class ChannelCalculator {

    /**
     * Returns the channel number associated with the imput frequency. Android
     * framework returns the frequency values for the center frequency.
     *
     * @param frequency The frequency to calculate the channel.
     * @return The channel number associated with the imput frequency
     */
    public static int getChannelNumber(int frequency) {
        int channel = ((frequency - 2412) / 5) + 1;
        return (channel > 0 && channel < 14) ? channel : -1;
    }
}
