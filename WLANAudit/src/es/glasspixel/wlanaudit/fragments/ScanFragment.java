package es.glasspixel.wlanaudit.fragments;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import roboguice.RoboGuice;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

import com.actionbarsherlock.app.SherlockFragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Window;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;

import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.actions.AutoScanAction;
import es.glasspixel.wlanaudit.actions.RefreshAction;
import es.glasspixel.wlanaudit.activities.AboutActivity;
import es.glasspixel.wlanaudit.activities.KeyListActivity;
import es.glasspixel.wlanaudit.activities.SlidingMapActivity;

import es.glasspixel.wlanaudit.activities.WLANAuditPreferencesActivity;
import es.glasspixel.wlanaudit.adapters.WifiNetworkAdapter;
import es.glasspixel.wlanaudit.dominio.SavedKeysUtils;
import es.glasspixel.wlanaudit.keyframework.IKeyCalculator;
import es.glasspixel.wlanaudit.keyframework.KeyCalculatorFactory;
import es.glasspixel.wlanaudit.keyframework.NetData;
import es.glasspixel.wlanaudit.util.ChannelCalculator;

public class ScanFragment extends RoboSherlockFragment implements
		OnItemClickListener {

	@InjectResource(R.string.improve_precision_dialog_title)
	String improve_preciosion_dialog_title;

	@InjectResource(R.string.improve_precision_dialog_message)
	String improve_precision_dialog_message;

	@InjectResource(R.string.settings)
	String settings;

	@InjectResource(R.string.no_networks_found)
	String no_networks_found;

	@InjectResource(android.R.string.cancel)
	String cancel;

	@InjectView(android.R.id.empty)
	TextView empty_text;

	@InjectView(android.R.id.list)
	ListView list_view;

	/**
	 * The parent activity that listens for this fragment callbacks
	 */
	private ScanFragmentListener mCallback;

	/**
	 * Interface to pass fragment callbacks to parent activity. Parent activity
	 * must implement this to be aware of the events of the fragment.
	 */
	public interface ScanFragmentListener {
		/**
		 * Observers must implement this method to be notified of which network
		 * was selected on this fragment.
		 * 
		 * @param networkData
		 *            The network data of the selected item.
		 */
		public void onNetworkSelected(ScanResult networkData);
	}

	private static final int LOCATION_SETTINGS = 2;

	View myFragmentView;

	/**
	 * The {@link ViewPager} that will host the activty fragments.
	 */
	@InjectView(R.id.pager)
	@Nullable
	private ViewPager mViewPager;

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

	TextView mDefaultPassValue;

	private boolean screenIsLarge;

	private double mLatitude = -999999999, mLongitude = -999999999;

	private LocationManager locationManager;

	private String bestProvider;

	private MenuItem refresh;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// RoboGuice.getInjector(getActivity()).injectMembersWithoutViews(this);
		if (savedInstanceState != null
				&& savedInstanceState.getBoolean("autoscan_state")) {
			mAutoScanAction = new AutoScanAction(getActivity(), true);
		} else {
			mAutoScanAction = new AutoScanAction(getActivity());
		}
		screenIsLarge = getSherlockActivity().getResources().getBoolean(
				R.bool.screen_large);

		locationManager = (LocationManager) getSherlockActivity()
				.getSystemService(Context.LOCATION_SERVICE);

		if (!locationManager
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			AlertDialog.Builder dialogo1 = new AlertDialog.Builder(
					getSherlockActivity());
			dialogo1.setTitle(improve_preciosion_dialog_title);
			dialogo1.setMessage(improve_precision_dialog_message);
			dialogo1.setCancelable(false);
			dialogo1.setPositiveButton(settings,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialogo1, int id) {
							Intent intent = new Intent(
									Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							startActivityForResult(intent, LOCATION_SETTINGS);
						}
					});
			dialogo1.setNegativeButton(cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialogo1, int id) {
							dialogo1.dismiss();
						}
					});
			dialogo1.show();

		} else {

			initLocation();

		}
		// }
		Log.d("ScanFragment", "showing menu..");
		setHasOptionsMenu(true);

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

	private Editor e;

	private Location myLocation;

	private void printProvider(String provider) {
		// LocationProvider info = locationManager.getProvider(provider);
		// Log.d("MapActivity", info.getName());
	}

	private void showLocation(Location l) {
		mLatitude = l.getLatitude();
		mLongitude = l.getLongitude();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {

		super.onViewCreated(view, savedInstanceState);
		// RoboGuice.getInjector(getActivity()).injectViewMembers(this);
		empty_text.setText(no_networks_found);
		list_view.setEmptyView(empty_text);

		list_view.setAdapter(new WifiNetworkAdapter(getSherlockActivity(),
				R.layout.network_list_element_layout,
				new ArrayList<ScanResult>()));
		list_view.setOnItemClickListener(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

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

		return myFragmentView;
	}

	@Override
	public void onDetach() {

		super.onDetach();
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
			if (refresh != null)
				refresh.setActionView(null);
			empty_text.setText(getSherlockActivity().getResources().getString(
					R.string.no_networks_found));
			list_view.setEmptyView(getSherlockActivity().findViewById(
					R.id.empty));
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

				List<ScanResult> res = mWifiManager.getScanResults();

				if (myFragmentView != null && getSherlockActivity() != null) {
					if (refresh != null)
						refresh.setActionView(null);
					if (mWifiManager.getScanResults().size() > 0) {
						list_view.setAdapter(new WifiNetworkAdapter(
								getSherlockActivity(),
								R.layout.network_list_element_layout, res));
					} else {
						list_view.setEmptyView(getSherlockActivity()
								.findViewById(R.id.empty));
					}
				}

			}
		};
		getSherlockActivity().registerReceiver(mCallBackReceiver, i);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		inflater.inflate(R.menu.networklistactivity_menu, menu);
		refresh = (MenuItem) menu.findItem(R.id.scanOption);
		automatic_scan = (MenuItem) menu.findItem(R.id.toggleAutoscanOption);
		checkAutoScanStatus();
		super.onCreateOptionsMenu(menu, inflater);

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

			startScan();
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
			e = getSherlockActivity().getSharedPreferences("viewpager",
					Context.MODE_PRIVATE).edit();
			e.putInt("viewpager_index", 0);
			e.commit();
			startActivity(i);
			return true;
		case R.id.aboutOption:
			i = new Intent(getActivity(), AboutActivity.class);
			e = getSherlockActivity().getSharedPreferences("viewpager",
					Context.MODE_PRIVATE).edit();
			e.putInt("viewpager_index", 0);
			e.commit();
			startActivity(i);
			return true;
		case R.id.mapOption:
			i = new Intent(getSherlockActivity(), SlidingMapActivity.class);
			e = getSherlockActivity().getSharedPreferences("viewpager",
					Context.MODE_PRIVATE).edit();
			e.putInt("viewpager_index", 0);
			e.commit();
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
		try {
			mCallback = (ScanFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnNetworkSelectedListener");
		}
	}

	@Override
	public void onItemClick(final AdapterView<?> parent, View arg1,
			final int position, long arg3) {
		Context mContext = getActivity();
		final Dialog dialog = new Dialog(mContext);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.network_details_dialog);
		// dialog.setTitle(getActivity().getResources().getString(
		// R.string.scan_fragment_dialog_title));

		mDefaultPassValue = (TextView) dialog.findViewById(R.id.password_value);

		final ScanResult scannedNetwork = (ScanResult) parent
				.getItemAtPosition(position);

		int signalLevel = WifiManager.calculateSignalLevel(
				scannedNetwork.level,
				WifiNetworkAdapter.MAX_SIGNAL_STRENGTH_LEVEL);

		((ImageView) dialog.findViewById(R.id.networkIcon))
				.setImageLevel(signalLevel);
		if (WifiNetworkAdapter.getSecurity(scannedNetwork) != WifiNetworkAdapter.SECURITY_NONE) {
			// Set an icon of encrypted wi-fi hotspot
			((ImageView) dialog.findViewById(R.id.networkIcon)).setImageState(
					WifiNetworkAdapter.ENCRYPTED_STATE_SET, false);
		}
		// Setting network's values
		((TextView) dialog.findViewById(R.id.networkName))
				.setText(scannedNetwork.SSID);
		((TextView) dialog.findViewById(R.id.bssid_value))
				.setText(scannedNetwork.BSSID);
		((TextView) dialog.findViewById(R.id.encryption_value))
				.setText(scannedNetwork.capabilities);
		((TextView) dialog.findViewById(R.id.frequency_value))
				.setText(scannedNetwork.frequency + " MHz");
		((TextView) dialog.findViewById(R.id.channel_value)).setText(String
				.valueOf(ChannelCalculator
						.getChannelNumber(scannedNetwork.frequency)));
		((TextView) dialog.findViewById(R.id.intensity_value))
				.setText(scannedNetwork.level + " dBm");

		// TODO Comprobar si esta red no está ya almacenada

		if (!SavedKeysUtils.existSavedNetwork(scannedNetwork.BSSID,
				getSherlockActivity())) {
			// Calculating key
			IKeyCalculator keyCalculator = KeyCalculatorFactory
					.getKeyCalculator(new NetData(scannedNetwork.SSID,
							scannedNetwork.BSSID));
			if (keyCalculator != null) {
				mKeyList = keyCalculator.getKey(new NetData(
						scannedNetwork.SSID, scannedNetwork.BSSID));
				if (mKeyList != null) {
					if (mKeyList.size() > 1) {
						mDefaultPassValue.setText(String.valueOf(mKeyList
								.size())
								+ " "
								+ getText(R.string.number_of_keys_found));
						((Button) dialog.findViewById(R.id.copyPasswordButton))
								.setText(String.valueOf(mKeyList.size())
										+ " "
										+ getText(R.string.number_of_keys_found));
					} else if (mKeyList.size() == 1) {
						mDefaultPassValue.setText(mKeyList.get(0));
					}
					// TODO: REFACTOR!!! NO SIRVE PARA CLAVES DE MAS DE UN
					// RESULTADO
					((Button) dialog.findViewById(R.id.copyPasswordButton))
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {

									if (mKeyList.size() == 1) {

										copyClipboard(mDefaultPassValue
												.getText().toString());
										saveWLANKey(scannedNetwork);

										// if (screenIsLarge == true) {
										mCallback
												.onNetworkSelected((ScanResult) parent
														.getItemAtPosition(position));
										// }
										dialog.dismiss();
									} else {
										Intent i = new Intent(
												getSherlockActivity(),
												KeyListActivity.class);
										i.putExtra("wlan_name",
												scannedNetwork.SSID);
										i.putExtra("wlan_address",
												scannedNetwork.BSSID);
										i.putExtra("wlan_latitude", mLatitude);
										i.putExtra("wlan_longitude", mLongitude);
										i.putStringArrayListExtra(
												KeyListActivity.KEY_LIST_KEY,
												(ArrayList<String>) mKeyList);
										e = getSherlockActivity()
												.getSharedPreferences(
														"viewpager",
														Context.MODE_PRIVATE)
												.edit();
										e.putInt("viewpager_index", 0);
										e.commit();
										startActivity(i);
									}

								}
							});
				} else {
					mDefaultPassValue
							.setText(getString(R.string.no_default_key));
					((Button) dialog.findViewById(R.id.copyPasswordButton))
							.setEnabled(false);
				}
			} else {
				mDefaultPassValue.setText(getString(R.string.no_default_key));
				((Button) dialog.findViewById(R.id.copyPasswordButton))
						.setEnabled(false);
			}
		} else {
			((Button) dialog.findViewById(R.id.copyPasswordButton))
					.setText(getSherlockActivity().getResources().getString(
							R.string.key_already_store));
			((Button) dialog.findViewById(R.id.copyPasswordButton))
					.setEnabled(false);
		}

		dialog.show();

	}

	@SuppressLint("NewApi")
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

	private void saveWLANKey(ScanResult s) {

		SavedKeysUtils.saveWLANKey(getSherlockActivity(), s, mLatitude,
				mLongitude);

	}

	private void initLocation() {
		myLocation = locationManager
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 20, 0, listener);
		if (myLocation != null) {
			showLocation(myLocation);
		} else {
			Toast.makeText(getSherlockActivity(),
					"Your location is unavailable now", Toast.LENGTH_LONG)
					.show();
		}

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

		// mWifiManager.startScan();

		initScan();

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
			locationManager.removeUpdates(listener);
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