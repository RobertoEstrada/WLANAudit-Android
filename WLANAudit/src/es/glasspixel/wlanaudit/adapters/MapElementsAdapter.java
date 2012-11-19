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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MapElementsAdapter extends BaseAdapter {

	private List<SavedKey> elements;
	private Context mContext;

	public MapElementsAdapter(Context c, List<SavedKey> elements) {
		mContext = c;
		this.elements = elements;
	}

	@Override
	public int getCount() {

		return elements.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View listItem = convertView;
		// If the view is null, we need to inflate it from XML layout
		if (listItem == null) {
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			listItem = inflater.inflate(R.layout.key_saved_list_element, null);
		}

		if (elements.get(position) != null) {
			((TextView) listItem.findViewById(R.id.networkName))
					.setText(elements.get(position).getWlan_name());
			((TextView) listItem.findViewById(R.id.networkKey))
					.setText(elements.get(position).getKeys().size() == 1 ? elements
							.get(position).getKeys().get(0)
							: printKeys(elements.get(position).getKeys()));
		}
		return listItem;
	}

	private String printKeys(List<String> keys) {
		String r = "";
		for (String s : keys) {
			r += s + ",";
		}
		return r;
	}
}
