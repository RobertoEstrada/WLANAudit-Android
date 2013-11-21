/*
 * Copyright (C) 2013 The WLANAudit project contributors.
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
package es.glasspixel.wlanaudit.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockListFragment;

import org.orman.mapper.Model;

import java.util.List;

import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.activities.SlidingMapActivity;
import es.glasspixel.wlanaudit.database.entities.Network;
import roboguice.inject.InjectResource;

public class SavedNetworksMenuFragment extends RoboSherlockListFragment implements
        OnItemClickListener {
    /**
     * List of saved keys
     */
    private List<Network> mKeys;
    /**
     * instance of parent listener
     */
    private SlidingMapActivity listener;

    LayoutInflater mInflater;

    /**
     * Empty saved keys list text
     */
    @InjectResource(R.string.no_data_saved_keys)
    private String no_data_text;

    @Override
    public void onAttach(Activity activity) {
        if (activity instanceof SlidingMapActivity)
            addListener((SlidingMapActivity) activity);
        super.onAttach(activity);
    }

    public void addListener(SlidingMapActivity a) {
        listener = a;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mInflater = inflater;
        return inflater.inflate(R.layout.menu_saved_keys_fragment, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mKeys = loadSavedKeys();

        setListAdapter(new MenuAdapter());

        View empty = mInflater.inflate(R.layout.empty_list, null);

        ((TextView) empty.findViewById(R.id.textView1)).setText(no_data_text);

        getListView().setEmptyView(empty);

        getListView().setOnItemClickListener(this);
    }

    public interface OnSavedKeySelectedListener {
        public void onSavedKeySelected(Network s);
    }


    protected List<Network> loadSavedKeys() {

        return mKeys = Model.fetchAll(Network.class);

    }

    private class MenuAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mKeys.size();
        }

        @Override
        public Object getItem(int position) {
            return mKeys.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                v = getSherlockActivity().getLayoutInflater().inflate(
                        R.layout.key_saved_list_menu_element, parent, false);
            }

            ((TextView) v.findViewById(R.id.networkName)).setText(mKeys
                    .get(position).mSSID);
            if (mKeys.get(position).mLatitude > -999999999
                    && mKeys.get(position).mLongitude > -999999999)
                ((ImageView) v.findViewById(R.id.location_icon_saved_key))
                        .setVisibility(View.VISIBLE);

            ((TextView) v.findViewById(R.id.networkAddress)).setText(mKeys
                    .get(position).mBSSID);
            return v;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        if (mKeys.get(arg2).mLatitude > -999999999
                && mKeys.get(arg2).mLongitude > -999999999)
            listener.onSavedKeySelected(mKeys.get(arg2));

    }

    public void onMapItemSelected(int index) {
        getListView().getChildAt(index).setSelected(true);
    }
}
