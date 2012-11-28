package es.glasspixel.wlanaudit.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.database.entities.Network;

public class MapElementsAdapter extends BaseAdapter {

	private List<Network> elements;
	private Context mContext;

	public MapElementsAdapter(Context c, List<Network> elements) {
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
					.setText(elements.get(position).mSSID);
			((TextView) listItem.findViewById(R.id.networkAddress))
					.setText(elements.get(position).mBSSID);
		}
		return listItem;
	}

}
