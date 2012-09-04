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

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.actions.AutoScanAction;
import es.glasspixel.wlanaudit.actions.RefreshAction;
import es.glasspixel.wlanaudit.adapters.KeysSavedAdapter;
import es.glasspixel.wlanaudit.adapters.WifiNetworkAdapter;
import es.glasspixel.wlanaudit.ads.Key;
import es.glasspixel.wlanaudit.database.KeysSQliteHelper;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Application main activity. Shows the user a list of the surrounding WiFi
 * networks available. The user then picks one to see details of the network
 * 
 * @author Roberto Estrada
 */
@SuppressLint({ "NewApi", "NewApi" })
public class NetworkListActivity extends SherlockFragmentActivity implements
		OnItemClickListener {

	/**
	 * Manager of the wifi network interface
	 */
	private WifiManager mWifiManager;

	/**
	 * Broadcast receiver to control the completion of a scan
	 */
	private BroadcastReceiver mCallBackReceiver;

	/**
	 * Handle to the action bar's autoscan action to activate or deactivate the
	 * autoscan on lifecycle events that require it
	 */
	private AutoScanAction mAutoScanAction;

	/**
	 * Handle to the action bar's refresh action
	 */
	private RefreshAction mRefreshAction;

	/**
	 * Advertisement
	 */
	private AdView mAd;

	private ActionBar mActionBar;

	private String[] actions = new String[2];

	private SharedPreferences prefs;

	private int mPosition = 0;

	private MenuItem refreshScan;
	private MenuItem automaticScan;

	boolean screenIsLarge;

	boolean orientationIsPortrait;

	private Resources res;

	private ListView mListView;

	/**
	 * Lifecycle management: Activity creation
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		res = getResources();
		screenIsLarge = res.getBoolean(R.bool.screen_large);
		orientationIsPortrait = res.getBoolean(R.bool.portrait);
		if (screenIsLarge == false) {
			mListView = (ListView) findViewById(R.id.network_listview);
			mListView.setOnItemClickListener(this);
			actions[0] = getResources().getString(R.string.action1);
			actions[1] = getResources().getString(R.string.action2);
			// Action bar actions initialization
			if (savedInstanceState != null
					&& savedInstanceState.getBoolean("autoscan_state")) {
				mAutoScanAction = new AutoScanAction(this, true);
			} else {
				mAutoScanAction = new AutoScanAction(this);
			}

			mActionBar = getSupportActionBar();
			mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					getBaseContext(),
					android.R.layout.simple_spinner_dropdown_item, actions);

			mActionBar.setListNavigationCallbacks(adapter,
					new OnNavigationListener() {

						@Override
						public boolean onNavigationItemSelected(
								int itemPosition, long itemId) {
							if (itemPosition == 0) {
								initScan();

							} else if (itemPosition == 1) {
								try {
									unregisterReceiver(mCallBackReceiver);
								} catch (IllegalArgumentException e) {
									// Do nothing, just avoid that activity
									// crashes
								}

								if (mAutoScanAction.isAutoScanEnabled() == true)
									mAutoScanAction.stopAutoScan();
								refreshScan.setEnabled(false);
								automaticScan.setEnabled(false);

								mListView.setAdapter(new KeysSavedAdapter(
										NetworkListActivity.this,
										R.layout.network_list_element_layout,
										android.R.layout.simple_list_item_1,
										getSavedKeys()));
								// getListView().setOnItemLongClickListener(
								// new OnItemLongClickListener() {
								//
								// @Override
								// public boolean onItemLongClick(
								// AdapterView<?> arg0, View arg1,
								// int arg2, long arg3) {
								// if (mActionMode != null) {
								// return false;
								// }
								// // Start the CAB using the
								// // ActionMode.Callback defined above
								// mActionMode = NetworkListActivity.this
								// .startActionMode(mActionModeCallback);
								//
								// return true;
								//
								// }
								// });
								mListView
										.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
								mListView
										.setMultiChoiceModeListener(new MultiChoiceModeListener() {

											@Override
											public boolean onActionItemClicked(
													ActionMode mode,
													android.view.MenuItem item) {

												switch (item.getItemId()) {
												case R.id.delete_context_menu:
													SparseBooleanArray i = mListView
															.getCheckedItemPositions();
													KeysSQliteHelper usdbh = new KeysSQliteHelper(
															NetworkListActivity.this,
															"DBKeys", null, 1);
													SQLiteDatabase db = usdbh
															.getWritableDatabase();
													for (int j = 0; j < mListView
															.getCount(); j++) {
														if (i.get(j) == true) {

															String nombre_wlan, clave;
															nombre_wlan = ((SavedKey) mListView
																	.getAdapter()
																	.getItem(j))
																	.getWlan_name();
															clave = ((SavedKey) mListView
																	.getAdapter()
																	.getItem(j))
																	.getKey();
															db.delete(
																	"keys",
																	"nombre like ? AND key like ?",
																	new String[] {
																			nombre_wlan,
																			clave });
														}

													}

													mode.finish();
													mListView
															.setAdapter(new KeysSavedAdapter(
																	NetworkListActivity.this,
																	R.layout.network_list_element_layout,
																	android.R.layout.simple_list_item_1,
																	getSavedKeys()));
													return true;
												case R.id.copy_context_menu:
													mode.finish();
													return true;
												default:
													return false;
												}
											}

											@Override
											public boolean onCreateActionMode(
													ActionMode mode,
													android.view.Menu menu) {
												android.view.MenuInflater inflater = mode
														.getMenuInflater();
												inflater.inflate(
														R.menu.saved_keys_elements_context_menu,
														menu);

												return true;
											}

											@Override
											public void onDestroyActionMode(
													ActionMode mode) {
												// TODO Auto-generated method
												// stub

											}

											@Override
											public boolean onPrepareActionMode(
													ActionMode mode,
													android.view.Menu menu) {
												// TODO Auto-generated method
												// stub
												return false;
											}

											@Override
											public void onItemCheckedStateChanged(
													ActionMode mode,
													int position, long id,
													boolean checked) {

											}
										});

							}
							mPosition = itemPosition;
							return false;
						}
					});

			prefs = PreferenceManager.getDefaultSharedPreferences(this);
			// If preference does not exist
			if (!prefs.contains("wifi_autostart")) {
				SharedPreferences.Editor editor = prefs.edit();
				editor.putBoolean("wifi_autostart", true);
				editor.commit();
			}

			if (!prefs.contains("autoscan_interval")) {
				SharedPreferences.Editor editor = prefs.edit();
				editor.putInt("autoscan_interval", 30);
				editor.commit();
			}

			initScan();
			// Ads Initialization
			LinearLayout layout = (LinearLayout) findViewById(R.id.adLayout);
			mAd = new AdView(this, AdSize.SMART_BANNER, Key.ADMOB_KEY);
			layout.addView(mAd);
		}

	}

	// private ActionMode.Callback mActionModeCallback = new
	// ActionMode.Callback() {
	//
	// // Called when the action mode is created; startActionMode() was called
	// @SuppressLint("NewApi")
	// @Override
	// public boolean onCreateActionMode(ActionMode mode,
	// android.view.Menu menu) {
	// // Inflate a menu resource providing context menu items
	// android.view.MenuInflater inflater = mode.getMenuInflater();
	// inflater.inflate(R.menu.saved_keys_elements_context_menu, menu);
	// return true;
	// }
	//
	// // Called each time the action mode is shown. Always called after
	// // onCreateActionMode, but
	// // may be called multiple times if the mode is invalidated.
	// @Override
	// public boolean onPrepareActionMode(ActionMode mode,
	// android.view.Menu menu) {
	// return false; // Return false if nothing is done
	// }
	//
	// // Called when the user selects a contextual menu item
	// @SuppressLint({ "NewApi" })
	// @Override
	// public boolean onActionItemClicked(ActionMode mode,
	// android.view.MenuItem item) {
	// switch (item.getItemId()) {
	// case R.id.copy_context_menu:
	// String mDefaultPassValue = ((TextView) v
	// .findViewById(R.id.networkKey)).getText().toString();
	//
	// int sdk = android.os.Build.VERSION.SDK_INT;
	// if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
	// android.text.ClipboardManager clipboard = (android.text.ClipboardManager)
	// getSystemService(Context.CLIPBOARD_SERVICE);
	// clipboard.setText(mDefaultPassValue);
	// } else {
	// android.content.ClipboardManager clipboard =
	// (android.content.ClipboardManager)
	// getSystemService(Context.CLIPBOARD_SERVICE);
	// android.content.ClipData clip = android.content.ClipData
	// .newPlainText("text label", mDefaultPassValue);
	// clipboard.setPrimaryClip(clip);
	// }
	// Toast.makeText(NetworkListActivity.this,
	// getResources().getString(R.string.key_copy_success),
	// Toast.LENGTH_SHORT).show();
	// mode.finish();
	// return true;
	// case R.id.delete_context_menu:
	//
	// mode.finish();
	// return true;
	// default:
	// return false;
	// }
	// }
	//
	// // Called when the user exits the action mode
	// @SuppressLint("NewApi")
	// @Override
	// public void onDestroyActionMode(ActionMode mode) {
	// mActionMode = null;
	// }
	// };

	protected List<SavedKey> getSavedKeys() {
		List<SavedKey> mKeys = new ArrayList<SavedKey>();
		KeysSQliteHelper usdbh = new KeysSQliteHelper(this, "DBKeys", null, 1);

		SQLiteDatabase db = usdbh.getReadableDatabase();
		Cursor c = db.query("Keys", new String[] { "nombre", "key" }, null,
				null, null, null, "nombre ASC");
		while (c.moveToNext()) {
			SavedKey k = new SavedKey(c.getString(c.getColumnIndex("nombre")),
					c.getString(c.getColumnIndex("key")));
			mKeys.add(k);
		}
		return mKeys;

	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		refreshScan = menu.getItem(0);
		automaticScan = menu.getItem(1);
		return true;
	}

	private void initScan() {

		if (refreshScan != null && automaticScan != null) {
			refreshScan.setEnabled(true);
			automaticScan.setEnabled(true);
		}

		mRefreshAction = new RefreshAction(this);

		// WifiManager initialization
		mWifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);

		// If WiFi is disabled, enable it
		if (!mWifiManager.isWifiEnabled()
				&& prefs.getBoolean("wifi_autostart", true)) {
			mWifiManager.setWifiEnabled(true);
		}

		setupNetworkScanCallBack();
		mWifiManager.startScan();

	}

	/**
	 * Lifecycle management: Menu creation
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.networklistactivity_menu, menu);
		this.refreshScan = menu.getItem(0);
		this.automaticScan = menu.getItem(1);
		return true;
	}

	/**
	 * Menu option handling
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.scanOption:
			// if (mPosition == 0)
			mRefreshAction.performAction();
			return true;
		case R.id.toggleAutoscanOption:
			// if (mPosition == 0)
			mAutoScanAction.performAction();

			return true;
		case R.id.preferenceOption:
			i = new Intent(this, WLANAuditPreferencesActivity.class);
			startActivity(i);
			return true;
		case R.id.aboutOption:
			i = new Intent(this, AboutActivity.class);
			startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Lifecycle management: Activity is about to be shown
	 */
	protected void onStart() {
		super.onStart();

		// mAd.loadAd(new AdRequest());
	}

	/**
	 * Lifecycle management: Activity is being resumed, we need to refresh its
	 * contents
	 */
	protected void onResume() {
		super.onResume();
		// mWifiManager.startScan();
		mAd.loadAd(new AdRequest());
	}

	/**
	 * Lifecycle management: Activity is being stopped, we need to unregister
	 * the broadcast receiver
	 */
	protected void onStop() {
		super.onStop();
		// Unsubscribing from receiving updates about changes of the WiFi
		// networks
		try {
			unregisterReceiver(mCallBackReceiver);
		} catch (IllegalArgumentException e) {
			// Do nothing
		}
	}

	/**
	 * Lifecycle management: Activity state is saved to be restored later
	 */
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("autoscan_state",
				mAutoScanAction.isAutoScanEnabled());
	}

	protected void onDestroy() {
		super.onDestroy();
		mAutoScanAction.stopAutoScan();
	}

	/**
	 * Sets up the logic to execute when a scan is complete. This is done this
	 * way because the SCAN_RESULTS_AVAILABLE_ACTION must be caught by a
	 * BroadCastReceiver.
	 */
	private void setupNetworkScanCallBack() {
		IntentFilter i = new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		mCallBackReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// Network scan complete, datasource needs to be updated and
				// ListView refreshed
				if (mPosition == 0)
					mListView.setAdapter(new WifiNetworkAdapter(
							NetworkListActivity.this,
							R.layout.network_list_element_layout, mWifiManager
									.getScanResults()));
			}
		};
		registerReceiver(mCallBackReceiver, i);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
		if (mPosition == 0) {
			Intent i = new Intent(this, NetworkDetailsActivity.class);
			i.putExtra(NetworkDetailsActivity.SCAN_RESULT_KEY,
					(ScanResult) mListView.getItemAtPosition(position));
			startActivity(i);
		} else {
			String mDefaultPassValue = ((TextView) v
					.findViewById(R.id.networkKey)).getText().toString();

			int sdk = android.os.Build.VERSION.SDK_INT;
			if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
				android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				clipboard.setText(mDefaultPassValue);
			} else {
				android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				android.content.ClipData clip = android.content.ClipData
						.newPlainText("text label", mDefaultPassValue);
				clipboard.setPrimaryClip(clip);
			}
			Toast.makeText(NetworkListActivity.this,
					getResources().getString(R.string.key_copy_success),
					Toast.LENGTH_SHORT).show();

		}

	}
}
