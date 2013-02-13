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

package es.glasspixel.wlanaudit.activities;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.ClipboardManager;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.ads.Key;

@SuppressWarnings("deprecation")
public class KeyListActivity extends SherlockListActivity {

	/**
	 * Unique identifier of the scan result inside the intent extra or the
	 * savedInstanceState bundle.
	 */
	public static final String KEY_LIST_KEY = "key_list";

	/**
	 * The list of keys to be displayed
	 */
	private List<String> mKeyList;

	/**
	 * Advertisement
	 */
	private AdView mAd;

	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.key_list_layout);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		// If a previous instance state was saved
		if (savedInstanceState != null
				&& savedInstanceState.get(KEY_LIST_KEY) != null) {
			// Load the state
			mKeyList = savedInstanceState
					.getStringArrayList(KEY_LIST_KEY);
		} else {
			// Read the network from the intent extra passed to this activity
			mKeyList = (List<String>) getIntent().getExtras()
					.getStringArrayList(KEY_LIST_KEY);
		}

		// Ads Initialization
		LinearLayout layout = (LinearLayout) findViewById(R.id.keyListAdLayout);
		mAd = new AdView(this, AdSize.SMART_BANNER, Key.ADMOB_KEY);
		layout.addView(mAd);

		// List display
		setListAdapter(new ArrayAdapter<String>(this,
				R.layout.key_list_element_layout, R.id.keyString, mKeyList));
	}

	/**
	 * Lifecycle management: Activity is about to be started/resumed
	 */
	protected void onResume() {
		super.onResume();
		mAd.loadAd(new AdRequest());
	}

	/**
	 * Lifecycle management: Activity state is saved to be restored later
	 */
	protected void onSaveInstanceState(Bundle outState) {
		outState.putStringArrayList(KeyListActivity.KEY_LIST_KEY,
				(ArrayList<String>) mKeyList);
	}

	/**
	 * Handles the event of clicking on a list element.
	 */
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// Clipboard copy
		ClipboardManager clipBoard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		clipBoard.setText(mKeyList.get(position));
		// Copy notification
		Toast notificationToast = Toast.makeText(this, getResources()
				.getString(R.string.key_copy_success), Toast.LENGTH_SHORT);
		notificationToast.setGravity(Gravity.CENTER, 0, 0);
		notificationToast.show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; go home
			NavUtils.navigateUpFromSameTask(this);
			overridePendingTransition(R.anim.slide_in_from_left,
					R.anim.slide_out_to_right);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onBackPressed() {
		finish();
		overridePendingTransition(R.anim.slide_in_from_left,
				R.anim.slide_out_to_right);
	}
}