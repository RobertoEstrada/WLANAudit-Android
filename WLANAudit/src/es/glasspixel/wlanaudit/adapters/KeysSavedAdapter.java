package es.glasspixel.wlanaudit.adapters;

import java.util.List;

import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.activities.SavedKey;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class KeysSavedAdapter extends ArrayAdapter<SavedKey> {

	public KeysSavedAdapter(Context context, int resource,
			int textViewResourceId, List<SavedKey> objects) {

		super(context, resource, textViewResourceId, objects);

	}

	/**
	 * {@inheritDoc}
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		View listItem = convertView;
		// If the view is null, we need to inflate it from XML layout
		if (listItem == null) {
			LayoutInflater inflater = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			listItem = inflater.inflate(R.layout.key_saved_list_element, null);
		}

		((TextView) listItem.findViewById(R.id.networkName)).setText(getItem(
				position).getWlan_name());
		// ((TextView) listItem.findViewById(R.id.networkKey))
		// .setText(getListString(getItem(position)));
		((TextView) listItem.findViewById(R.id.networkKey)).setText(getItem(
				position).getAddress());

		return listItem;
	}

	private CharSequence getListString(SavedKey item) {
		String result = "";
		if (item.getKeys().size() > 1) {
			for (String s : item.getKeys()) {
				result += s + ",";
			}
		} else {
			result = item.getKeys().get(0);
		}
		return result;
	}

}
