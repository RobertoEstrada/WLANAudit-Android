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
package es.glasspixel.wlanaudit.activities;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import es.glasspixel.wlanaudit.R;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class AboutActivity extends SherlockActivity {
	/**
	 * Label which shows current version number
	 */
	private TextView mVersionValueLabel;
	/**
	 * Label which shows current release number
	 */
	private TextView mReleaseValueLabel;

	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_layout);
		mVersionValueLabel = (TextView) findViewById(R.id.versionValue);
		mReleaseValueLabel = (TextView) findViewById(R.id.releaseValue);
	}

	/**
	 * @see android.app.Activity#onStart
	 */
	@Override
	protected void onStart() {
		super.onStart();
		mVersionValueLabel.setText(getVersion());
		mReleaseValueLabel.setText(String.valueOf(getRelease()));
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                Intent intent = new Intent(this,  NetworkListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

	private int getRelease() {
		int release = -1;
		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(
					getPackageName(), PackageManager.GET_META_DATA);
			release = pInfo.versionCode;
		} catch (NameNotFoundException e1) {
			Log.e(this.getClass().getSimpleName(), "Name not found", e1);
		}
		return release;
	}

	private String getVersion() {
		String version = null;
		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(
					getPackageName(), PackageManager.GET_META_DATA);
			version = pInfo.versionName;
		} catch (NameNotFoundException e1) {
			Log.e(this.getClass().getSimpleName(), "Name not found", e1);
		}
		return version;
	}
}
