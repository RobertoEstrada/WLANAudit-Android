package es.glasspixel.wlanaudit.fragments;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.SherlockListFragment;

import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.activities.KeyListActivity;
import es.glasspixel.wlanaudit.activities.SavedKey;
import es.glasspixel.wlanaudit.activities.SlidingMapActivity;
import es.glasspixel.wlanaudit.database.KeysSQliteHelper;
import es.glasspixel.wlanaudit.dominio.SavedKeysUtils;
import es.glasspixel.wlanaudit.keyframework.IKeyCalculator;
import es.glasspixel.wlanaudit.keyframework.KeyCalculatorFactory;
import es.glasspixel.wlanaudit.keyframework.NetData;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class SavedKeysMenuFragment extends SherlockListFragment implements
		OnItemClickListener {
	private List<SavedKey> mKeys;
	private SlidingMapActivity listener;
	private int mPosition;
	LayoutInflater mInflater;

	public SavedKeysMenuFragment(int i) {
		mPosition = i;
	}

	public SavedKeysMenuFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mInflater = inflater;
		return inflater.inflate(R.layout.menu_saved_keys_fragment, null);
	}

	public void addListener(SlidingMapActivity a) {
		listener = a;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mKeys = loadSavedKeys();

		setListAdapter(new MenuAdapter());

		View empty = mInflater.inflate(R.layout.empty_list, null);

		((TextView) empty.findViewById(R.id.textView1))
				.setText(getSherlockActivity().getResources().getString(
						R.string.no_data_saved_keys));

		getListView().setEmptyView(empty);

		getListView().setOnItemClickListener(this);
	}

	public interface OnSavedKeySelectedListener {
		public void onSavedKeySelected(SavedKey s);
	}

	protected List<SavedKey> loadSavedKeys() {

		return SavedKeysUtils.loadSavedKeys(getSherlockActivity());

	}

	// the meat of switching the above fragment
	private void switchFragment(Fragment fragment) {
		if (getActivity() == null)
			return;

		if (getActivity() instanceof SlidingMapActivity) {
			SlidingMapActivity ra = (SlidingMapActivity) getActivity();
			ra.switchContent(fragment);
		}
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
						R.layout.key_saved_list_element, parent, false);
			}

			((TextView) v.findViewById(R.id.networkName)).setText(mKeys.get(
					position).getWlan_name());
			// ((TextView) v.findViewById(R.id.networkKey))
			// .setText(printKeys(mKeys.get(position).getKeys()));

			((TextView) v.findViewById(R.id.networkKey)).setText(mKeys.get(
					position).getAddress());
			return v;
		}
	}

	private String printKeys(List<String> keys) {
		String r = "";
		for (String s : keys) {
			r += s + ",";
		}
		return r;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (mKeys.get(arg2).getLatitude() > -1
				&& mKeys.get(arg2).getLongitude() > -1)
			listener.onSavedKeySelected(mKeys.get(arg2));

	}
}
