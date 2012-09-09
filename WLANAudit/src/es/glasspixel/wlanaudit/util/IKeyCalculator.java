/*
 * Copyright (C) 2011 Roberto Estrada
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

import java.util.List;

import android.net.wifi.ScanResult;

public interface IKeyCalculator {
	/**
	 * Returns a possible default key or list of them for the passed network
	 * only derived from publicly available data, like the SSID
	 * 
	 * @param network
	 *            The network from which to derive a possible default key
	 * @return The derived default key or keys, it is guaranteed to match the
	 *         network key if and only if the default key has not been changed
	 *         and if the right algorithm is beign used
	 */
	public List<String> getKey(ScanResult network);
}
