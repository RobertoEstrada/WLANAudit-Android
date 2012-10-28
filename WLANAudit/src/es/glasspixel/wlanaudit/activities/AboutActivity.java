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

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import es.glasspixel.wlanaudit.R;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
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
	 * Button to display OSS License dialog
	 */
	private Button mOssLicensesButton;
	/**
	 * Button to open Facebook official page
	 */
	private ImageButton mFacebookButton;
	/**
	 * Button to open official Twitter page
	 */
	private ImageButton mTwitterButton;
	/**
	 * Button to open official G+ page
	 */
	private ImageButton mGplusButton;

	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_layout);
		ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
		mVersionValueLabel = (TextView) findViewById(R.id.versionValue);
		mReleaseValueLabel = (TextView) findViewById(R.id.releaseValue);
		mOssLicensesButton = (Button) findViewById(R.id.oss_button);
		mFacebookButton = (ImageButton) findViewById(R.id.fbButton);
		mTwitterButton = (ImageButton) findViewById(R.id.twitterButton);
		mGplusButton = (ImageButton) findViewById(R.id.gPlusButton);
	}

	/**
	 * @see android.app.Activity#onStart
	 */
	@Override
	protected void onStart() {
		super.onStart();
		mVersionValueLabel.setText(getVersion());
		mReleaseValueLabel.setText(String.valueOf(getRelease()));
		mOssLicensesButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder alert = new AlertDialog.Builder(
						AboutActivity.this);

				alert.setTitle(getText(R.string.oss_licenses_dialog_title));
				WebView wv = new WebView(AboutActivity.this);

				wv.loadUrl("file:///android_asset/licenses.html");
				alert.setView(wv);
				alert.show();
			}
		});
		mFacebookButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
						.parse("https://www.facebook.com/wlanaudit"));
				startActivity(browserIntent);
			}
		});
		mTwitterButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
						.parse("https://twitter.com/#!/TitoXamps"));
				startActivity(browserIntent);
			}
		});
		mGplusButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent browserIntent = new Intent(
						Intent.ACTION_VIEW,
						Uri.parse("https://plus.google.com/b/113060827862322417267/113060827862322417267"));
				startActivity(browserIntent);
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; go home
			NavUtils.navigateUpFromSameTask(this);
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
