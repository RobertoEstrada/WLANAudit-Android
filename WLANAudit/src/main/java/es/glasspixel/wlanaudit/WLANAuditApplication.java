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

package es.glasspixel.wlanaudit;

import android.app.Application;

import org.orman.dbms.Database;
import org.orman.dbms.sqliteandroid.SQLiteAndroid;
import org.orman.mapper.MappingSession;
import org.orman.util.logging.AndroidLogger;
import org.orman.util.logging.Log;

import es.glasspixel.wlanaudit.database.entities.Network;

public class WLANAuditApplication extends Application {
	/**
	 * App package name
	 */
	public static final String PACKAGE_NAME = "es.glasspixel.wlanaudit";
	/**
	 * Database name on disk
	 */
	public static final String DATABASE_NAME = "networksDB.db";
	/**
	 * Database name on disk
	 */
	public static final int DATABASE_VERSION = 1;

	@Override
	public void onCreate() {
		super.onCreate();
		Database db = new SQLiteAndroid(getApplicationContext(), DATABASE_NAME,
				DATABASE_VERSION);
		MappingSession.registerDatabase(db);
		MappingSession.registerEntity(Network.class);
		Log.setLogger(new AndroidLogger(PACKAGE_NAME));
		MappingSession.start();
	}
}
