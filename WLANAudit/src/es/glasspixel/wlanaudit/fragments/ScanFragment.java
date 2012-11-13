package es.glasspixel.wlanaudit.fragments;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewConfiguration;
import android.view.Window;

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
import es.glasspixel.wlanaudit.activities.MapActivity;

import es.glasspixel.wlanaudit.activities.SavedKey;
import es.glasspixel.wlanaudit.activities.WLANAuditPreferencesActivity;
import es.glasspixel.wlanaudit.adapters.KeysSavedAdapter;
import es.glasspixel.wlanaudit.adapters.WifiNetworkAdapter;
import es.glasspixel.wlanaudit.database.KeysSQliteHelper;
import es.glasspixel.wlanaudit.keyframework.IKeyCalculator;
import es.glasspixel.wlanaudit.keyframework.NetData;
import es.glasspixel.wlanaudit.keyframework.WLANXXXXKeyCalculator;
import es.glasspixel.wlanaudit.util.ChannelCalculator;

public class ScanFragment extends SherlockFragment implements
		OnItemClickListener {

	private Callbacks mCallbacks = sDummyCallbacks;

	public interface Callbacks {

		public void onItemSelected(ScanResult s);

	}

	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(ScanResult s) {
		}
	};

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

	private boolean screenIsLarge;

	private double mLatitude = 0, mLongitude = 0;

	private LocationManager locationManager;

	private String bestProvider;

	private MenuItem refresh;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null
				&& savedInstanceState.getBoolean("autoscan_state")) {
			mAutoScanAction = new AutoScanAction(getActivity(), true);
		} else {
			mAutoScanAction = new AutoScanAction(getActivity());
		}
		screenIsLarge = getSherlockActivity().getResources().getBoolean(
				R.bool.screen_large);

		// if (!screenIsLarge) {
		// this.setHasOptionsMenu(true);
		// }

		locationManager = (LocationManager) getSherlockActivity()
				.getSystemService(Context.LOCATION_SERVICE);

		// List all providers:
		List<String> providers = locationManager.getAllProviders();
		for (String provider : providers) {
			printProvider(provider);
		}

		Criteria criteria = new Criteria();
		bestProvider = locationManager.getBestProvider(criteria, false);
		Log.d("MapActivity", "best provider: " + bestProvider);

		Location location = locationManager.getLastKnownLocation(bestProvider);
		if (location != null) {
			showLocation(location);
		}

		locationManager.requestLocationUpdates(bestProvider, 20, 0, listener);

		// loadFakeWlan();
	}

	private final LocationListener listener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			mLatitude = location.getLatitude();
			mLongitude = location.getLongitude();

		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			bestProvider = provider;

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}

	};

	private MenuItem automatic_scan;

	private void printProvider(String provider) {
		LocationProvider info = locationManager.getProvider(provider);
		Log.d("MapActivity", info.getName());
	}

	private void showLocation(Location l) {
		mLatitude = l.getLatitude();
		mLongitude = l.getLongitude();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		myFragmentView = inflater.inflate(R.layout.saved_keys_fragment,
				container, false);
		((TextView) myFragmentView.findViewById(R.id.empty))
				.setText(getSherlockActivity().getResources().getString(
						R.string.no_networks_found));
		((ListView) myFragmentView.findViewById(R.id.listView1))
				.setEmptyView(getSherlockActivity().findViewById(R.id.empty));

		((ListView) myFragmentView.findViewById(R.id.listView1))
				.setAdapter(new WifiNetworkAdapter(getSherlockActivity(),
						R.layout.network_list_element_layout,
						new ArrayList<ScanResult>()));

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

		((ListView) myFragmentView.findViewById(R.id.listView1))
				.setOnItemClickListener(this);

		initScan();

		setHasOptionsMenu(true);

		return myFragmentView;
	}

	public void initScan() {

		// WifiManager initialization
		if (mWifiManager == null)
			mWifiManager = (WifiManager) getActivity().getSystemService(
					Context.WIFI_SERVICE);

		if (mWifiManager != null) {

			// If WiFi is disabled, enable it
			if (!mWifiManager.isWifiEnabled()
					&& prefs.getBoolean("wifi_autostart", true)) {
				mWifiManager.setWifiEnabled(true);
			}

			mRefreshAction = new RefreshAction(getActivity());

			setupNetworkScanCallBack();
			// StartPassButtonListener();
			this.startScan();
		} else {
			if (refresh != null) {
				refresh.setActionView(null);
				refresh.setEnabled(false);
			}
			((TextView) myFragmentView.findViewById(R.id.empty))
					.setText(getSherlockActivity().getResources().getString(
							R.string.no_networks_found));
			((ListView) myFragmentView.findViewById(R.id.listView1))
					.setEmptyView(getSherlockActivity()
							.findViewById(R.id.empty));
		}
	}

	public void startScan() {
		boolean start = mWifiManager.startScan();
		if (refresh != null && start == true)
			refresh.setActionView(R.layout.indeterminate_progress_action);
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

				if (mWifiManager.getScanResults().size() > 0) {
					((ListView) myFragmentView.findViewById(R.id.listView1))
							.setAdapter(new WifiNetworkAdapter(
									getSherlockActivity(),
									R.layout.network_list_element_layout,
									mWifiManager.getScanResults()));
				} else {
					((ListView) myFragmentView.findViewById(R.id.listView1))
							.setEmptyView(getSherlockActivity().findViewById(
									R.id.empty));
				}

				if (refresh != null && refresh.getActionView() != null)
					refresh.setActionView(null);
			}
		};
		getSherlockActivity().registerReceiver(mCallBackReceiver, i);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Add your menu entries here
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.networklistactivity_menu, menu);
		refresh = (MenuItem) menu.findItem(R.id.scanOption);
		automatic_scan = (MenuItem) menu.findItem(R.id.toggleAutoscanOption);
		checkAutoScanStatus();
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
			if (refresh != null)
				refresh.setActionView(R.layout.indeterminate_progress_action);
			return true;
		case R.id.toggleAutoscanOption:
			// if (mPosition == 0)
			mAutoScanAction.performAction();
			checkAutoScanStatus();

			return true;
		case R.id.preferenceOption:
			i = new Intent(getActivity(), WLANAuditPreferencesActivity.class);
			startActivity(i);
			return true;
		case R.id.aboutOption:
			i = new Intent(getActivity(), AboutActivity.class);
			startActivity(i);
			return true;
		case R.id.mapOption:
			i = new Intent(getSherlockActivity(), MapActivity.class);
			startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void checkAutoScanStatus() {
		if (mAutoScanAction.isAutoScanEnabled()) {
			automatic_scan.setIcon(R.drawable.ic_autoscan);
		} else {
			automatic_scan.setIcon(R.drawable.ic_autoscan_disabled);
		}

	}

	@Override
	public void onAttach(Activity activity) {

		super.onAttach(activity);
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Context mContext = getActivity();
		final Dialog dialog = new Dialog(mContext);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.network_details_dialog);
		// dialog.setTitle(getActivity().getResources().getString(
		// R.string.scan_fragment_dialog_title));

		mDefaultPassValue = (TextView) dialog.findViewById(R.id.password_value);

		final ScanResult s = (ScanResult) arg0.getItemAtPosition(arg2);

		int signalLevel = WifiManager.calculateSignalLevel(s.level,
				WifiNetworkAdapter.MAX_SIGNAL_STRENGTH_LEVEL);

		((ImageView) dialog.findViewById(R.id.networkIcon))
				.setImageLevel(signalLevel);
		if (WifiNetworkAdapter.getSecurity(s) != WifiNetworkAdapter.SECURITY_NONE) {
			// Set an icon of encrypted wi-fi hotspot
			((ImageView) dialog.findViewById(R.id.networkIcon)).setImageState(
					WifiNetworkAdapter.ENCRYPTED_STATE_SET, false);
		}
		// Setting network's values
		((TextView) dialog.findViewById(R.id.networkName)).setText(s.SSID);
		((TextView) dialog.findViewById(R.id.bssid_value)).setText(s.BSSID);
		((TextView) dialog.findViewById(R.id.encryption_value))
				.setText(s.capabilities);
		((TextView) dialog.findViewById(R.id.frequency_value))
				.setText(s.frequency + " MHz");
		((TextView) dialog.findViewById(R.id.channel_value)).setText(String
				.valueOf(ChannelCalculator.getChannelNumber(s.frequency)));
		((TextView) dialog.findViewById(R.id.intensity_value)).setText(s.level
				+ " dBm");

		// Calculating key
		if (s.SSID.matches("(?:WLAN|JAZZTEL)_([0-9a-fA-F]{4})")) {
			IKeyCalculator keyCalculator = new WLANXXXXKeyCalculator();
			mKeyList = keyCalculator.getKey(new NetData(s.SSID, s.BSSID));
			// keyCalculator.getKey(network);
			if (mKeyList != null) {
				mDefaultPassValue.setText(mKeyList.get(0));
				((Button) dialog.findViewById(R.id.copyPasswordButton))
						.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {

								copyClipboard(mDefaultPassValue.getText()
										.toString());

								saveWLANKey(s.SSID, mDefaultPassValue.getText()
										.toString());

								// if (screenIsLarge == true) {
								mCallbacks.onItemSelected(s);
								// }
								dialog.dismiss();

							}
						});
			} else {
				mDefaultPassValue.setText(getString(R.string.no_default_key));

				((Button) dialog.findViewById(R.id.copyPasswordButton))
						.setEnabled(false);
			}

		} else if (s.SSID.matches("(?:WLAN|YACOM|WiFi)([0-9a-fA-F]{6})")) {
			IKeyCalculator keyCalculator = new WLANXXXXKeyCalculator();
			mKeyList = keyCalculator.getKey(new NetData(s.SSID, s.BSSID));
			if (mKeyList != null) {
				mDefaultPassValue.setText(String.valueOf(mKeyList.size()) + " "
						+ getText(R.string.number_of_keys_found));
				((Button) dialog.findViewById(R.id.copyPasswordButton))
						.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {

								copyClipboard(mDefaultPassValue.getText()
										.toString());
								saveWLANKey(s.SSID, mDefaultPassValue.getText()
										.toString());

								// if (screenIsLarge == true) {
								mCallbacks.onItemSelected(s);
								// }
								dialog.dismiss();

							}
						});
			} else {
				mDefaultPassValue.setText(getString(R.string.no_default_key));

				((Button) dialog.findViewById(R.id.copyPasswordButton))
						.setEnabled(false);
			}
		} else {
			mDefaultPassValue.setText(getString(R.string.no_default_key));
			((Button) dialog.findViewById(R.id.copyPasswordButton))
					.setEnabled(false);
		}
		dialog.show();

	}

	private void copyClipboard(CharSequence text) {
		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
			android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity()
					.getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(text);
		} else {
			android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity()
					.getSystemService(Context.CLIPBOARD_SERVICE);
			android.content.ClipData clip = android.content.ClipData
					.newPlainText("text label", text);
			clipboard.setPrimaryClip(clip);
		}
		Toast.makeText(getSherlockActivity(),
				getResources().getString(R.string.key_copy_success),
				Toast.LENGTH_SHORT).show();

	}

	private void saveWLANKey(String name, CharSequence key) {
		KeysSQliteHelper usdbh = new KeysSQliteHelper(getActivity(), "DBKeys",
				null, 1);

		SQLiteDatabase db = usdbh.getWritableDatabase();
		if (db != null) {
			Cursor c = db.query("Keys", new String[] { "nombre", "key" },
					"nombre like ?", new String[] { name }, null, null,
					"nombre ASC");
			if (c.getCount() > 0) {

			} else {

				try {
					db.execSQL("INSERT INTO Keys (nombre, key,latitude,longitude) "
							+ "VALUES ('"
							+ name
							+ "', '"
							+ key
							+ "','"
							+ mLatitude + "', '" + mLongitude + "')");

				} catch (SQLException e) {
					Toast.makeText(
							getActivity().getApplicationContext(),
							getResources().getString(R.string.error_saving_key),
							Toast.LENGTH_LONG).show();
				}
				db.close();
			}
		}
		usdbh.close();

	}

	private void saveFakeWLAN(String name, CharSequence key, int i) {
		KeysSQliteHelper usdbh = new KeysSQliteHelper(getActivity(), "DBKeys",
				null, 1);

		SQLiteDatabase db = usdbh.getWritableDatabase();
		if (db != null) {
			Cursor c = db.query("Keys", new String[] { "nombre", "key" },
					"nombre like ?", new String[] { name }, null, null,
					"nombre ASC");
			if (c.getCount() > 0) {

			} else {

				try {
					db.execSQL("INSERT INTO Keys (nombre, key,latitude,longitude) "
							+ "VALUES ('"
							+ name
							+ "', '"
							+ key
							+ "','"
							+ (mLatitude + (i * 5))
							+ "', '"
							+ (mLongitude + (i * 5)) + "')");

				} catch (SQLException e) {
					Toast.makeText(
							getActivity().getApplicationContext(),
							getResources().getString(R.string.error_saving_key),
							Toast.LENGTH_LONG).show();
				}
				db.close();
			}
		}
		usdbh.close();
	}

	/**
	 * Lifecycle management: Activity is about to be shown
	 */
	public void onStart() {
		super.onStart();

		// mAd.loadAd(new AdRequest());
	}

	/**
	 * Lifecycle management: Activity is being resumed, we need to refresh its
	 * contents
	 */
	public void onResume() {
		super.onResume();
		setupNetworkScanCallBack();
		// mWifiManager.startScan();
		initScan();
		startScan();
		// mAd.loadAd(new AdRequest());
	}

	/**
	 * Lifecycle management: Activity is being stopped, we need to unregister
	 * the broadcast receiver
	 */
	public void onStop() {
		super.onStop();
		// Unsubscribing from receiving updates about changes of the WiFi
		// networks
		try {
			getSherlockActivity().unregisterReceiver(mCallBackReceiver);
		} catch (IllegalArgumentException e) {
			// Do nothing
		}
	}

	public void onDestroy() {
		super.onDestroy();
		if (mAutoScanAction != null)
			mAutoScanAction.stopAutoScan();
		if (mCallBackReceiver != null) {
			try {
				getActivity().unregisterReceiver(mCallBackReceiver);
			} catch (IllegalArgumentException e) {
				Log.d("ScanFragment", e.getMessage().toString());
			}
		}
	}
}
