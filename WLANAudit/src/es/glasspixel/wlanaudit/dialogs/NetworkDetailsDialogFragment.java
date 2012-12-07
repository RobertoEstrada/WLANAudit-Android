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

package es.glasspixel.wlanaudit.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.orman.mapper.Model;
import org.orman.mapper.ModelQuery;
import org.orman.sql.C;

import roboguice.fragment.RoboDialogFragment;
import roboguice.inject.InjectView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.novoda.location.Locator;
import com.novoda.location.LocatorFactory;
import com.novoda.location.LocatorSettings;
import com.novoda.location.exception.NoProviderAvailable;

import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.WLANAuditApplication;
import es.glasspixel.wlanaudit.activities.KeyListActivity;
import es.glasspixel.wlanaudit.adapters.WifiNetworkAdapter;
import es.glasspixel.wlanaudit.database.entities.Network;
import es.glasspixel.wlanaudit.interfaces.OnDataSourceModifiedListener;
import es.glasspixel.wlanaudit.keyframework.IKeyCalculator;
import es.glasspixel.wlanaudit.keyframework.KeyCalculatorFactory;
import es.glasspixel.wlanaudit.keyframework.NetData;
import es.glasspixel.wlanaudit.util.ChannelCalculator;

public class NetworkDetailsDialogFragment extends RoboDialogFragment {
    /**
     * Tag to identify the class in logcat
     */
    private static final String TAG = NetworkDetailsDialogFragment.class.getName();
    
    /**
     * Key to store and recover from dialog bundle the network data to displau
     */
    private static String NETWORK_DETAILS_DATA_KEY = "NetworkDetailsData";

    /**
     * The network data to display on the dialog
     */
    private ScanResult mNetworkData;
    
    /**
     * A list with the possible default keys of the network being detailed
     */
    private List<String> mKeyList;

    /**
     * Last known location
     */
    private Location mLastKnownLocation;
    
    /**
     * Handle to power-efficient location services
     */
    private Locator mLocator;
    
    /**
     * Broadcast receiver to watch for location updates
     */
    private BroadcastReceiver mLocationAvailableCallBackReceiver;
    
    /**
     * Callback handle to the datasource observer
     */
    private OnDataSourceModifiedListener mCallback;

    @InjectView(R.id.networkIcon)
    private ImageView mNetworkIcon;

    @InjectView(R.id.networkName)
    private TextView mNetworkNameTextView;

    @InjectView(R.id.bssid_value)
    private TextView mNetworkBssidTextView;

    @InjectView(R.id.encryption_value)
    private TextView mNetworkEncryptionTextView;

    @InjectView(R.id.frequency_value)
    private TextView mNetworkFrequencyTextView;

    @InjectView(R.id.channel_value)
    private TextView mNetworkChannelTextView;

    @InjectView(R.id.intensity_value)
    private TextView mNetworkIntensityTextView;

    @InjectView(R.id.password_value)
    private TextView mNetworkDefaultPassTextView;

    @InjectView(R.id.copyPasswordButton)
    private Button mCopyPasswordButton;
    
    @InjectView(R.id.starNetworkButton)
    private ImageButton mStarNetworkButton;

    /**
     * Gets a new instance of the dialog
     * 
     * @param network The network data to display on the dialog
     * @return A ready to use instance of the dialog
     */
    public static NetworkDetailsDialogFragment newInstance(ScanResult network) {
        NetworkDetailsDialogFragment frag = new NetworkDetailsDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(NETWORK_DETAILS_DATA_KEY, network);
        frag.setArguments(args);
        return frag;
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnDataSourceModifiedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnDataSourceModifiedListener");
        }
    }
    
    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNetworkData = getArguments().getParcelable(NETWORK_DETAILS_DATA_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        getDialog().setTitle(R.string.scan_fragment_dialog_title);
        View v = inflater.inflate(R.layout.network_details_dialog, container, false);
        return v;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        setupLocationServices();
        startReceivingLocationUpdates();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        stopReceivingLocationUpdates();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Once view is created, it's time to fill the dialog contents
        int signalLevel = WifiManager.calculateSignalLevel(mNetworkData.level,
                WifiNetworkAdapter.MAX_SIGNAL_STRENGTH_LEVEL);
        mNetworkIcon.setImageLevel(signalLevel);
        mNetworkNameTextView.setText(mNetworkData.SSID);
        mNetworkBssidTextView.setText(mNetworkData.BSSID);
        mNetworkEncryptionTextView.setText(mNetworkData.capabilities);
        mNetworkFrequencyTextView.setText(mNetworkData.frequency + " MHz");
        mNetworkChannelTextView.setText(String.valueOf(ChannelCalculator
                .getChannelNumber(mNetworkData.frequency)));
        mNetworkIntensityTextView.setText(mNetworkData.level + " dBm");
        
        // Setting up button callbacks
        mCopyPasswordButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mKeyList.size() == 1) {
                    copyClipboard(mNetworkDefaultPassTextView.getText().toString());
                } else if (mKeyList.size() > 1) {
                    Intent i = new Intent(getActivity(), KeyListActivity.class);
                    i.putStringArrayListExtra(KeyListActivity.KEY_LIST_KEY,
                            (ArrayList<String>) mKeyList);
                    startActivity(i);

                }
                dismiss();
            }
        });
        mStarNetworkButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNetwork(mNetworkData, mLastKnownLocation);
                dismiss();
            }
        });

        // Calculating key
        IKeyCalculator keyCalculator = KeyCalculatorFactory.getKeyCalculator(new NetData(
                mNetworkData.SSID, mNetworkData.BSSID));
        if (keyCalculator != null) {
            mKeyList = keyCalculator.getKey(new NetData(mNetworkData.SSID,
                    mNetworkData.BSSID));
            if (mKeyList != null) {
                if (mKeyList.size() > 1) {
                    mNetworkDefaultPassTextView.setText(String.valueOf(mKeyList.size()) + " "
                            + getText(R.string.number_of_keys_found));
                } else if (mKeyList.size() == 1) {
                    mNetworkDefaultPassTextView.setText(mKeyList.get(0));
                }                
            } else {
                mNetworkDefaultPassTextView.setText(getString(R.string.no_default_key));
                mCopyPasswordButton.setEnabled(false);
            }
        } else {
            mNetworkDefaultPassTextView.setText(getString(R.string.no_default_key));
            mCopyPasswordButton.setEnabled(false);
        }
    }
    
    private void setupLocationServices() {
        LocatorSettings settings = new LocatorSettings(WLANAuditApplication.PACKAGE_NAME,
                WLANAuditApplication.LOCATION_UPDATE_ACTION);
        settings.setUpdatesInterval(3 * 60 * 1000);
        settings.setUpdatesDistance(50);
        mLocator = LocatorFactory.getInstance();
        mLocator.prepare(getActivity().getApplicationContext(), settings);
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
        getActivity().getApplicationContext().registerReceiver(mLocationAvailableCallBackReceiver, f);
        try {
            mLocator.startLocationUpdates();
        } catch (NoProviderAvailable np) {
            Log.d(TAG, "No location provider available at this time");
        }
    }

    private void stopReceivingLocationUpdates() {
        getActivity().getApplicationContext().unregisterReceiver(mLocationAvailableCallBackReceiver);
        mLocator.stopLocationUpdates();
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private void copyClipboard(CharSequence text) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity()
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("text label",
                    text);
            clipboard.setPrimaryClip(clip);
        } else {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity()
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        }
        Toast.makeText(getActivity(), getResources().getString(R.string.key_copy_success),
                Toast.LENGTH_SHORT).show();
    }

    private void saveNetwork(ScanResult networkData, Location networkLocation) {
        // Existence check
        int exists = Integer.parseInt((String) Model.fetchSingleValue(ModelQuery.select().from(Network.class)
                .where(C.eq("m_bssid", networkData.BSSID)).count().getQuery()));
        if (exists == 0) {
            Network networkToSave = new Network();
            networkToSave.mBSSID = networkData.BSSID;
            networkToSave.mSSID = networkData.SSID;
            networkToSave.mEncryption = networkData.capabilities;
            networkToSave.mFrequency = networkData.frequency;
            networkToSave.mChannel = ChannelCalculator.getChannelNumber(networkData.frequency);
            networkToSave.mLatitude = networkLocation.getLatitude();
            networkToSave.mLongitude = networkLocation.getLongitude();

            // Insertion onto the DB
            networkToSave.insert();
            
            // Notification to listeners
            mCallback.dataSourceShouldRefresh();
        }
    }
}
