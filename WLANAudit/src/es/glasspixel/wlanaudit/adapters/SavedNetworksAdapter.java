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
package es.glasspixel.wlanaudit.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.database.entities.Network;

public class SavedNetworksAdapter extends ArrayAdapter<Network> {

	/**
	 * Handle to the layout that has to be inflated
	 */
    private int mTextViewResourceId;
	
    public SavedNetworksAdapter(Context context, int textViewResourceId, List<Network> networks) {
		super(context, textViewResourceId, networks);
		mTextViewResourceId = textViewResourceId;
	}

	/**
	 * {@inheritDoc}
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		View listItem = convertView;
		// If the view is null, we need to inflate it from XML layout
		if (listItem == null) {
			LayoutInflater inflater = LayoutInflater.from(getContext());
			listItem = inflater.inflate(mTextViewResourceId, null);
		}
		((TextView) listItem.findViewById(R.id.networkName)).setText(getItem(
				position).mSSID);
		((TextView) listItem.findViewById(R.id.networkAddress)).setText(getItem(
				position).mBSSID);

		return listItem;
	}

}
