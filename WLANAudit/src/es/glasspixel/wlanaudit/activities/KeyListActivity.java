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
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.ActionMode.Callback;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.adapters.KeysSavedAdapter;
import es.glasspixel.wlanaudit.ads.Key;
import es.glasspixel.wlanaudit.database.KeysSQliteHelper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.ClipboardManager;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class KeyListActivity extends SherlockListActivity implements
		OnItemLongClickListener {

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
	 * name and address of the network
	 */
	private String wlan_name, wlan_address;

	/**
	 * latitude and longitude of the network
	 */
	private float wlan_latitude, wlan_longitude;

	/**
	 * Advertisement
	 */
	private AdView mAd;

	private boolean modo_solo_copiar;

	protected ActionMode mActionMode;

	private String mKey;

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
			mKeyList = savedInstanceState.getStringArrayList(KEY_LIST_KEY);
			wlan_name = savedInstanceState.getString("wlan_name");
			wlan_address = savedInstanceState.getString("wlan_address");
			wlan_latitude = savedInstanceState.getFloat("wlan_latitude");
			wlan_longitude = savedInstanceState.getFloat("wlan_longitude");
			modo_solo_copiar = savedInstanceState.getBoolean("modo_key_list");
		} else {
			// Read the network from the intent extra passed to this activity
			mKeyList = (List<String>) getIntent().getExtras()
					.getStringArrayList(KEY_LIST_KEY);
			wlan_name = getIntent().getExtras().getString("wlan_name");
			wlan_address = getIntent().getExtras().getString("wlan_address");
			wlan_latitude = getIntent().getExtras().getFloat("wlan_latitude");
			wlan_longitude = getIntent().getExtras().getFloat("wlan_longitude");
			modo_solo_copiar = getIntent().getExtras().getBoolean(
					"modo_key_list");
		}

		// Ads Initialization
		LinearLayout layout = (LinearLayout) findViewById(R.id.keyListAdLayout);
		mAd = new AdView(this, AdSize.SMART_BANNER, Key.ADMOB_KEY);
		layout.addView(mAd);

		// List display
		setListAdapter(new ArrayAdapter<String>(this,
				R.layout.key_list_element_layout, R.id.keyString, mKeyList));
		getListView().setOnItemLongClickListener(this);
	}

	/**
	 * Lifecycle management: Activity is about to be shown
	 */
	protected void onStart() {
		super.onStart();
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
		if (modo_solo_copiar) {

			copyKey(mKeyList.get(position));

		}
	}

	private void copyKey(String string) {
		this.copyClipboard(string);
		this.saveWLANKey(wlan_name, wlan_address, string);

		// Copy notification
		Toast notificationToast = Toast.makeText(this, getResources()
				.getString(R.string.key_copy_success), Toast.LENGTH_SHORT);
		notificationToast.setGravity(Gravity.CENTER, 0, 0);
		notificationToast.show();

	}

	@SuppressLint("NewApi")
	private void copyClipboard(CharSequence text) {
		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
			android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(text);
		} else {
			android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			android.content.ClipData clip = android.content.ClipData
					.newPlainText("text label", text);
			clipboard.setPrimaryClip(clip);
		}

	}

	private void saveWLANKey(String name, String address, CharSequence key) {
		KeysSQliteHelper usdbh = new KeysSQliteHelper(this, "DBKeys", null, 1);

		SQLiteDatabase db = usdbh.getWritableDatabase();
		if (db != null) {
			Cursor c = db.query("Keys", new String[] { "address" },
					"address like ?", new String[] { address }, null, null,
					"address ASC");
			if (c.getCount() > 0) {

			} else {

				try {
					db.execSQL("INSERT INTO Keys (nombre, key,address,latitude,longitude) "
							+ "VALUES ('"
							+ name
							+ "', '"
							+ key
							+ "','"
							+ wlan_address
							+ ","
							+ wlan_latitude
							+ "', '"
							+ wlan_longitude + "')");

				} catch (SQLException e) {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(R.string.error_saving_key)
									+ e.getMessage(), Toast.LENGTH_LONG).show();
				}
				db.close();
			}
		}
		usdbh.close();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		if (mActionMode != null) {
			return false;
		}

		// Start the CAB using the ActionMode.Callback defined
		// above
		mActionMode = this.startActionMode(mActionCallBack);
		mKey = mKeyList.get(arg2);
		arg1.setSelected(true);
		return true;
	}

	private ActionMode.Callback mActionCallBack = new Callback() {

		private Object clave;

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.saved_keys_elements_context_menu, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.delete_context_menu:

				KeysSQliteHelper usdbh = new KeysSQliteHelper(
						getApplicationContext(), "DBKeys", null, 1);
				SQLiteDatabase db = usdbh.getWritableDatabase();

				if (mKeyList.size() == 1) {

					clave = mKeyList.get(0);
					db.delete("keys", "key like ? ", new String[] { mKey });

					mKeyList.remove(mKey);
					((BaseAdapter) getListView().getAdapter())
							.notifyDataSetChanged();

				} else {
					// TODO mostrar dialogo para borrar una o todas las claves
				}
				mode.finish();

				return true;
			case R.id.copy_context_menu:
				KeyListActivity.this.copyClipboard(mKey);

				mode.finish();
				return true;
			default:
				return true;
			}

		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;

		}
	};
}
