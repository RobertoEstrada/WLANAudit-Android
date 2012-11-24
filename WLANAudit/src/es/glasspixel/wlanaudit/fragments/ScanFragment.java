
package es.glasspixel.wlanaudit.fragments;

import java.util.ArrayList;

import roboguice.inject.InjectView;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.google.inject.Inject;
import com.novoda.location.Locator;
import com.novoda.location.LocatorFactory;
import com.novoda.location.LocatorSettings;
import com.novoda.location.exception.NoProviderAvailable;

import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.WLANAuditApplication;
import es.glasspixel.wlanaudit.actions.AutoScanAction;
import es.glasspixel.wlanaudit.actions.RefreshAction;
import es.glasspixel.wlanaudit.activities.AboutActivity;
import es.glasspixel.wlanaudit.activities.MapActivity;
import es.glasspixel.wlanaudit.activities.WLANAuditPreferencesActivity;
import es.glasspixel.wlanaudit.adapters.WifiNetworkAdapter;
import es.glasspixel.wlanaudit.dialogs.NetworkDetailsDialogFragment;

public class ScanFragment extends RoboSherlockFragment implements OnItemClickListener {    
    /**
     * Tag to identify the class in logcat
     */
    private static final String TAG = ScanFragment.class.getName();

    /**
     * Interface to pass fragment callbacks to parent activity. Parent activity
     * must implement this to be aware of the events of the fragment.
     */
    public interface OnNetworkSelectedListener {
        /**
         * Observers must implement this method to be notified of which network
         * was selected on this fragment.
         * 
         * @param networkData The network data of the selected item.
         * @param networkLocation TODO
         */
        public void onNetworkSelected(ScanResult networkData, Location networkLocation);
    }

    /**
     * The parent activity that listens for this fragment callbacks
     */
    private OnNetworkSelectedListener mCallback;

    /**
     * Manager of the wifi network interface
     */
    @Inject
    private WifiManager mWifiManager;

    /**
     * Broadcast receiver to control the completion of a scan
     */
    private BroadcastReceiver mNetworkScanCallBackReceiver;
    
    /**
     * Broadcast receiver to watch for location updates
     */
    private BroadcastReceiver mLocationAvailableCallBackReceiver;

    /**
     * Application preferences
     */
    @Inject
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

    /**
     * Refresh actionbar button
     */
    private MenuItem mRefreshMenuItem;

    /**
     * Autoscan toggle actionbar button
     */
    private MenuItem mAutoScanMenuItem;
    
    /**
     * Handle to power-efficient location services
     */
    private Locator mLocator;
    
    /**
     * Last known location
     */
    private Location mLastKnownLocation;
    
    /**
     * Network list widget
     */
    @InjectView(R.id.listView1)
    private ListView mNetworkListView;
    
    @InjectView(R.id.empty)
    private TextView mEmptyListMessageTextView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.getBoolean("autoscan_state")) {
            mAutoScanAction = new AutoScanAction(getActivity(), true);
        } else {
            mAutoScanAction = new AutoScanAction(getActivity());
        }

        setHasOptionsMenu(true);
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
        setupLocationServices();
        startReceivingLocationUpdates();
        initScan();
        startScan();
        // mAd.loadAd(new AdRequest());
    }
    
    /**
     * Lifecycle management: Activity is being paused, we need to unregister
     * the broadcast receivers to avoid leaking them
     */
    @Override
    public void onPause() {
        super.onPause();
        getSherlockActivity().getApplicationContext().unregisterReceiver(mNetworkScanCallBackReceiver);
        stopReceivingLocationUpdates();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mAutoScanAction != null)
            mAutoScanAction.stopAutoScan();
        if (mNetworkScanCallBackReceiver != null) {
            try {
                getSherlockActivity().getApplicationContext().unregisterReceiver(mNetworkScanCallBackReceiver);
            } catch (IllegalArgumentException e) {
                Log.d("ScanFragment", e.getMessage().toString());
            }
        }
    }

    private void setupLocationServices() {
        LocatorSettings settings = new LocatorSettings(WLANAuditApplication.PACKAGE_NAME,
                WLANAuditApplication.LOCATION_UPDATE_ACTION);
        settings.setUpdatesInterval(3 * 60 * 1000);
        settings.setUpdatesDistance(50);
        mLocator = LocatorFactory.getInstance();
        mLocator.prepare(getSherlockActivity(), settings);
        mLocationAvailableCallBackReceiver = new BroadcastReceiver() {
            
            @Override
            public void onReceive(Context context, Intent intent) {
                mLastKnownLocation = mLocator.getLocation();                
            }
        };
    }

    private void startReceivingLocationUpdates() {
        IntentFilter f = new IntentFilter();
        f.addAction(WLANAuditApplication.LOCATION_UPDATE_ACTION);
        getSherlockActivity().getApplicationContext().registerReceiver(mLocationAvailableCallBackReceiver, f);
        try {
            mLocator.startLocationUpdates();
        } catch (NoProviderAvailable np) {
            Log.d(TAG, "No location provider available at this time");
        }
    }

    private void stopReceivingLocationUpdates() {
        getSherlockActivity().getApplicationContext().unregisterReceiver(mLocationAvailableCallBackReceiver);
        mLocator.stopLocationUpdates();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        
        View fragmentView = inflater.inflate(R.layout.saved_keys_fragment, container, false);       

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

        return fragmentView;
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEmptyListMessageTextView.setText(getSherlockActivity().getResources().getString(
                R.string.no_networks_found));
        mNetworkListView.setEmptyView(mEmptyListMessageTextView);

        mNetworkListView.setAdapter(new WifiNetworkAdapter(getSherlockActivity().getApplicationContext(),
                R.layout.network_list_element_layout, new ArrayList<ScanResult>()));
        
        mNetworkListView.setOnItemClickListener(this);
    }

    public void initScan() {

        if (mWifiManager != null) {

            // If WiFi is disabled, enable it
            if (!mWifiManager.isWifiEnabled() && prefs.getBoolean("wifi_autostart", true)) {
                mWifiManager.setWifiEnabled(true);
            }

            mRefreshAction = new RefreshAction(getActivity());

            setupNetworkScanCallBack();
            startScan();
        } else {
            if (mRefreshMenuItem != null) {
                mRefreshMenuItem.setActionView(null);
                mRefreshMenuItem.setEnabled(false);
            }
            mEmptyListMessageTextView.setText(getSherlockActivity().getResources().getString(
                    R.string.no_networks_found));
            mNetworkListView.setEmptyView(mEmptyListMessageTextView);
        }
    }

    public void startScan() {
        boolean start = mWifiManager.startScan();
        if (mRefreshMenuItem != null && start == true)
            mRefreshMenuItem.setActionView(R.layout.indeterminate_progress_action);
    }

    /**
     * Sets up the logic to execute when a scan is complete. This is done this
     * way because the SCAN_RESULTS_AVAILABLE_ACTION must be caught by a
     * BroadCastReceiver.
     */
    private void setupNetworkScanCallBack() {
        IntentFilter i = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mNetworkScanCallBackReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                // Network scan complete, datasource needs to be updated and
                // ListView refreshed

                if (mWifiManager.getScanResults().size() > 0) {
                    mNetworkListView.setAdapter(new WifiNetworkAdapter(context,
                            R.layout.network_list_element_layout, mWifiManager.getScanResults()));
                } else {
                    mNetworkListView.setEmptyView(mEmptyListMessageTextView);
                }

                if (mRefreshMenuItem != null && mRefreshMenuItem.getActionView() != null)
                    mRefreshMenuItem.setActionView(null);
            }
        };
        getSherlockActivity().getApplicationContext().registerReceiver(mNetworkScanCallBackReceiver, i);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.networklistactivity_menu, menu);
        mRefreshMenuItem = (MenuItem) menu.findItem(R.id.scanOption);
        mAutoScanMenuItem = (MenuItem) menu.findItem(R.id.toggleAutoscanOption);
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
                if (mRefreshMenuItem != null)
                    mRefreshMenuItem.setActionView(R.layout.indeterminate_progress_action);
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
            mAutoScanMenuItem.setIcon(R.drawable.ic_autoscan);
        } else {
            mAutoScanMenuItem.setIcon(R.drawable.ic_autoscan_disabled);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnNetworkSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnNetworkSelectedListener");
        }
    }
    
    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mCallback.onNetworkSelected((ScanResult) parent.getItemAtPosition(position), mLastKnownLocation);
    }
}
