package es.glasspixel.wlanaudit.fragments;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import es.glasspixel.wlanaudit.activities.NetworkListActivity;
import es.glasspixel.wlanaudit.activities.SavedKey;
import es.glasspixel.wlanaudit.activities.WLANAuditPreferencesActivity;
import es.glasspixel.wlanaudit.adapters.KeysSavedAdapter;
import es.glasspixel.wlanaudit.adapters.WifiNetworkAdapter;
import es.glasspixel.wlanaudit.database.KeysSQliteHelper;

public class ScanFragment extends SherlockFragment {

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

}
