package es.glasspixel.wlanaudit.fragments;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.actions.AutoScanAction;
import es.glasspixel.wlanaudit.actions.RefreshAction;
import es.glasspixel.wlanaudit.adapters.WifiNetworkAdapter;

public class ScanFragment extends RoboSherlockFragment implements
		OnItemClickListener {

	@InjectResource(R.string.improve_precision_dialog_title)
	String improve_preciosion_dialog_title;

	@InjectResource(R.string.improve_precision_dialog_message)
	String improve_precision_dialog_message;

		

	
	/**
	 * Empty text for {@empty_text}
	 */
	@InjectResource(R.string.no_networks_found)
	String no_networks_found;

	
	/**
	 * Cancel text for dialogfragments
	 */
	@InjectResource(android.R.string.cancel)
	String cancel;

	
	/**
	 * Textview for empty listviews
	 */
	@InjectView(android.R.id.empty)
	TextView empty_text;

	
	/**
	 * wifi scan results listview
	 */
	@InjectView(android.R.id.list)
	ListView list_view;
	
	
	private boolean isScanning = false;

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

		public void scanCompleted();

		public void scanStart();
	}

	/**
	 * SavedKeysFragment instance to inflate
	 */
	View saved_keys_fragment;

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

	
	/**
	 * App preferences
	 */
	private SharedPreferences prefs;

	/**
	 * Handle to the action bar's autoscan action to activate or deactivate the
	 * autoscan on lifecycle events that require it
	 */
	public AutoScanAction mAutoScanAction;


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// RoboGuice.getInjector(getActivity()).injectMembersWithoutViews(this);
		if (savedInstanceState != null
				&& savedInstanceState.getBoolean("autoscan_state")) {
			mAutoScanAction = new AutoScanAction(getActivity(), true);
		} else {
			mAutoScanAction = new AutoScanAction(getActivity());
		}

		getSherlockActivity().getResources().getBoolean(
				R.bool.screen_large);

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

		saved_keys_fragment = inflater.inflate(R.layout.saved_keys_fragment,
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

		// initScan();

		return saved_keys_fragment;
	}

	@Override
	public void onDetach() {

		super.onDetach();
		mCallback = null;
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

			new RefreshAction(getActivity());

			setupNetworkScanCallBack();
			// StartPassButtonListener();
			if (!isScanning)
				this.startScan();
		} else {

			mCallback.scanCompleted();
			empty_text.setText(getSherlockActivity().getResources().getString(
					R.string.no_networks_found));
			list_view.setEmptyView(getSherlockActivity().findViewById(
					R.id.empty));
		}
	}

	/**
	 * Init the wifi scan and notify to de listener callback
	 */
	public void startScan() {
		mCallback.scanStart();
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

				List<ScanResult> res = mWifiManager.getScanResults();
				isScanning = false;

				if (saved_keys_fragment != null && getSherlockActivity() != null) {
					mCallback.scanCompleted();
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

		mCallback.onNetworkSelected((ScanResult) parent
				.getItemAtPosition(position));

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

		initScan();

		// mAd.loadAd(new AdRequest());
	}

	@Override
	public void onPause() {
		if (mAutoScanAction != null)
			mAutoScanAction.stopAutoScan();
		if (mCallBackReceiver != null) {
			try {
				getSherlockActivity().unregisterReceiver(mCallBackReceiver);
			} catch (IllegalArgumentException e) {
				Log.d("ScanFragment", e.getMessage().toString());
			}
		}
		super.onPause();
	}

}