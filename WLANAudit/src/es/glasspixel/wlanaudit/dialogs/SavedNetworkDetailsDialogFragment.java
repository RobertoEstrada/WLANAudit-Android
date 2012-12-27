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

import roboguice.fragment.RoboDialogFragment;
import roboguice.inject.InjectView;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.activities.KeyListActivity;
import es.glasspixel.wlanaudit.database.entities.Network;
import es.glasspixel.wlanaudit.util.ChannelCalculator;

public class SavedNetworkDetailsDialogFragment extends RoboDialogFragment {
    /**
     * Tag to identify the class in logcat
     */
    @SuppressWarnings("unused")
    private static final String TAG = SavedNetworkDetailsDialogFragment.class.getName();
    
    /**
     * Key to store and recover from dialog bundle the network data to display
     */
    private static String NETWORK_DETAILS_DATA_KEY = "NetworkDetailsData";

    /**
     * The network data to display on the dialog
     */
    private Network mNetworkData;
    
    /**
     * A list with the possible default keys of the network being detailed
     */
    private List<String> mKeyList;

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

    @InjectView(R.id.password_value)
    private TextView mNetworkDefaultPassTextView;

    @InjectView(R.id.copyPasswordButton)
    private Button mCopyPasswordButton;
    
    /**
     * Gets a new instance of the dialog
     * 
     * @param network The network data to display on the dialog
     * @return A ready to use instance of the dialog
     */
    public static SavedNetworkDetailsDialogFragment newInstance(Network network) {
        SavedNetworkDetailsDialogFragment frag = new SavedNetworkDetailsDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(NETWORK_DETAILS_DATA_KEY, network);
        frag.setArguments(args);
        return frag;
    }  

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNetworkData = (Network) getArguments().getSerializable(NETWORK_DETAILS_DATA_KEY);
        mKeyList = mNetworkData.getPossibleDefaultKeys();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        getDialog().setTitle(R.string.scan_fragment_dialog_title);
        View v = inflater.inflate(R.layout.saved_network_details_dialog, container, false);
        return v;
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Once view is created, it's time to fill the dialog contents
        mNetworkIcon.setImageLevel(3);
        mNetworkNameTextView.setText(mNetworkData.mSSID);
        mNetworkBssidTextView.setText(mNetworkData.mBSSID);
        mNetworkEncryptionTextView.setText(mNetworkData.mEncryption);
        mNetworkFrequencyTextView.setText(mNetworkData.mFrequency + " MHz");
        mNetworkChannelTextView.setText(String.valueOf(ChannelCalculator
                .getChannelNumber(mNetworkData.mFrequency)));

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
}