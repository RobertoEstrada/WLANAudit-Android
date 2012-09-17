package es.glasspixel.wlanaudit.fragments;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.actions.AutoScanAction;
import es.glasspixel.wlanaudit.actions.RefreshAction;
import es.glasspixel.wlanaudit.activities.AboutActivity;
import es.glasspixel.wlanaudit.activities.KeyListActivity;
import es.glasspixel.wlanaudit.activities.NetworkDetailsActivity;
import es.glasspixel.wlanaudit.activities.NetworkListActivity;
import es.glasspixel.wlanaudit.activities.SavedKey;
import es.glasspixel.wlanaudit.activities.WLANAuditPreferencesActivity;
import es.glasspixel.wlanaudit.adapters.KeysSavedAdapter;
import es.glasspixel.wlanaudit.adapters.WifiNetworkAdapter;
import es.glasspixel.wlanaudit.database.KeysSQliteHelper;
import es.glasspixel.wlanaudit.util.ChannelCalculator;
import es.glasspixel.wlanaudit.util.IKeyCalculator;
import es.glasspixel.wlanaudit.util.WLANXXXXKeyCalculator;
import es.glasspixel.wlanaudit.util.WiFiXXXXXXKeyCalculator;

public class ScanFragment extends SherlockFragment implements
		OnItemClickListener {

	View myFragmentView;

	/**
	 * Manager of the wifi network interface
	 */
	private WifiManager mWifiManager;

	/**
	 * Broadcast receiver to control the completion of a scan
	 */
	private BroadcastReceiver mCallBackReceiver;

	private SharedPreferences prefs;

	/**
	 * Handle to the action bar's autoscan action to activate or deactivate the
	 * autoscan on lifecycle events that require it
	 */
	private AutoScanAction mAutoScanAction;

	/**
	 * Handle to the action bar's refresh action
	 */
	private RefreshAction mRefreshAction;

	private List<String> mKeyList;

	private TextView mDefaultPassValue;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Intent launchingIntent = getActivity().getIntent();

		myFragmentView = inflater.inflate(R.layout.saved_keys_fragment,
				container, false);

		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
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

		((ListView) myFragmentView.findViewById(R.id.listView1))
				.setOnItemClickListener(this);

		return myFragmentView;
	}

	public void initScan() {
		getActivity();
		// WifiManager initialization
		mWifiManager = (WifiManager) getActivity().getSystemService(
				Context.WIFI_SERVICE);

		// If WiFi is disabled, enable it
		if (!mWifiManager.isWifiEnabled()
				&& prefs.getBoolean("wifi_autostart", true)) {
			mWifiManager.setWifiEnabled(true);
		}

		setupNetworkScanCallBack();
		StartPassButtonListener();
		mWifiManager.startScan();
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
				((ListView) myFragmentView.findViewById(R.id.listView1))
						.setAdapter(new WifiNetworkAdapter(getActivity(),
								R.layout.network_list_element_layout,
								mWifiManager.getScanResults()));
			}
		};
		getActivity().registerReceiver(mCallBackReceiver, i);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Add your menu entries here
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.networklistactivity_menu, menu);
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
			i = new Intent(getActivity(), WLANAuditPreferencesActivity.class);
			startActivity(i);
			return true;
		case R.id.aboutOption:
			i = new Intent(getActivity(), AboutActivity.class);
			startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Context mContext = getSherlockActivity().getApplicationContext();
		Dialog dialog = new Dialog(mContext);

		dialog.setContentView(R.layout.network_details_dialog);
		dialog.setTitle("Custom Dialog");

		ScanResult s = (ScanResult) arg0.getItemAtPosition(arg2);

		int signalLevel = WifiManager.calculateSignalLevel(s.level,
				WifiNetworkAdapter.MAX_SIGNAL_STRENGTH_LEVEL);

		((ImageView) myFragmentView.findViewById(R.id.networkIcon))
				.setImageLevel(signalLevel);
		if (WifiNetworkAdapter.getSecurity(s) != WifiNetworkAdapter.SECURITY_NONE) {
			// Set an icon of encrypted wi-fi hotspot
			((ImageView) myFragmentView.findViewById(R.id.networkIcon))
					.setImageState(WifiNetworkAdapter.ENCRYPTED_STATE_SET,
							false);
		}
		// Setting network's values
		((TextView) myFragmentView.findViewById(R.id.networkName))
				.setText(s.SSID);
		((TextView) myFragmentView.findViewById(R.id.bssid_value))
				.setText(s.BSSID);
		((TextView) myFragmentView.findViewById(R.id.encryption_value))
				.setText(s.capabilities);
		((TextView) myFragmentView.findViewById(R.id.frequency_value))
				.setText(s.frequency + " MHz");
		((TextView) myFragmentView.findViewById(R.id.channel_value))
				.setText(String.valueOf(ChannelCalculator
						.getChannelNumber(s.frequency)));
		((TextView) myFragmentView.findViewById(R.id.intensity_value))
				.setText(s.level + " dBm");
		// Calculating key
		if (s.SSID.matches("(?:WLAN|JAZZTEL)_([0-9a-fA-F]{4})")) {
			IKeyCalculator keyCalculator = new WLANXXXXKeyCalculator();
			mKeyList = keyCalculator.getKey(s);
			if (mKeyList != null) {
				mDefaultPassValue.setText(mKeyList.get(0));
			} else {
				mDefaultPassValue.setText(getString(R.string.no_default_key));

				((Button) myFragmentView.findViewById(R.id.copyPasswordButton))
						.setEnabled(false);
			}
		} else if (s.SSID.matches("(?:WLAN|YACOM|WiFi)([0-9a-fA-F]{6})")) {
			IKeyCalculator keyCalculator = new WiFiXXXXXXKeyCalculator();
			mKeyList = keyCalculator.getKey(s);
			mDefaultPassValue.setText(String.valueOf(mKeyList.size()) + " "
					+ getText(R.string.number_of_keys_found));
		} else {
			mDefaultPassValue.setText(getString(R.string.no_default_key));
			((Button) myFragmentView.findViewById(R.id.copyPasswordButton))
					.setEnabled(false);
		}

	}

	private void StartPassButtonListener() {
		((Button) myFragmentView.findViewById(R.id.copyPasswordButton))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						AlertDialog.Builder dialogBuilder = new Builder(
								getSherlockActivity());
						dialogBuilder
								.setIcon(android.R.drawable.ic_dialog_alert);
						dialogBuilder.setTitle(getResources().getText(
								R.string.key_copy_warn_title));
						dialogBuilder.setMessage(getResources().getText(
								R.string.key_copy_warn_text));
						dialogBuilder.setPositiveButton(R.string.ok_button,
								new DialogInterface.OnClickListener() {

									@TargetApi(11)
									@Override
									public void onClick(DialogInterface dialog,
											int unkn) {
										if (mKeyList.size() == 1) {
											// Clipboard copy
											int sdk = android.os.Build.VERSION.SDK_INT;
											if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
												android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSherlockActivity()
														.getSystemService(
																Context.CLIPBOARD_SERVICE);
												clipboard
														.setText(mDefaultPassValue
																.getText());
											} else {
												android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSherlockActivity()
														.getSystemService(
																Context.CLIPBOARD_SERVICE);
												android.content.ClipData clip = android.content.ClipData
														.newPlainText(
																"text label",
																mDefaultPassValue
																		.getText());
												clipboard.setPrimaryClip(clip);
											}

											KeysSQliteHelper usdbh = new KeysSQliteHelper(
													getSherlockActivity(),
													"DBKeys", null, 1);

											SQLiteDatabase db = usdbh
													.getWritableDatabase();

											// Si hemos abierto correctamente la
											// base de
											// datos
											if (db != null) {
												Cursor c = db
														.query("Keys",
																new String[] {
																		"nombre",
																		"key" },
																"nombre like ?",
																new String[] { ((TextView) myFragmentView
																		.findViewById(R.id.networkName))
																		.getText()
																		.toString() },
																null, null,
																"nombre ASC");
												if (c.getCount() > 0) {

												} else {

													// Insertamos los datos en
													// la tabla
													try {
														db.execSQL("INSERT INTO Keys (nombre, key) "
																+ "VALUES ('"
																+ ((TextView) myFragmentView
																		.findViewById(R.id.networkName))
																		.getText()
																+ "', '"
																+ mDefaultPassValue
																		.getText()
																+ "')");

														// Cerramos la base de
														// datos
													} catch (SQLException e) {
														Toast.makeText(
																getSherlockActivity()
																		.getApplicationContext(),
																getResources()
																		.getString(
																				R.string.error_saving_key),
																Toast.LENGTH_LONG)
																.show();
													}
													db.close();
												}
											}

											// ClipboardManager clipboard =
											// (ClipboardManager)
											// getSystemService(Context.CLIPBOARD_SERVICE);
											// clipboard.setText(mDefaultPassValue
											// .getText());
											// Dialog dismissing
											dialog.dismiss();
											// Copy notification
											Toast notificationToast = Toast
													.makeText(
															getSherlockActivity(),
															getResources()
																	.getString(
																			R.string.key_copy_success),
															Toast.LENGTH_SHORT);
											notificationToast.setGravity(
													Gravity.CENTER, 0, 0);
											notificationToast.show();
										} else if (mKeyList.size() > 1) {
											Intent i = new Intent(
													getSherlockActivity(),
													KeyListActivity.class);
											i.putStringArrayListExtra(
													KeyListActivity.KEY_LIST_KEY,
													(ArrayList<String>) mKeyList);
											startActivity(i);
										}
									}
								});
						dialogBuilder.show();

					}
				});
	}
}
