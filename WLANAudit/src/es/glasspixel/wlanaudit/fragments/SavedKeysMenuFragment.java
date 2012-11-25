package es.glasspixel.wlanaudit.fragments;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.SherlockListFragment;

import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.activities.SavedKey;
import es.glasspixel.wlanaudit.activities.SlidingMapActivity;
import es.glasspixel.wlanaudit.database.KeysSQliteHelper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
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
		mKeys = new ArrayList<SavedKey>();
		KeysSQliteHelper usdbh = new KeysSQliteHelper(getSherlockActivity(),
				"DBKeys", null, 1);

		SQLiteDatabase db = usdbh.getReadableDatabase();
		Cursor c = db
				.query("Keys", new String[] { "address", "nombre", "key",
						"latitude", "longitude" }, null, null, null, null,
						"nombre ASC");
		// if (c.moveToFirst()) {
		while (c.moveToNext()) {

			String name = c.getString(c.getColumnIndex("nombre"));
			boolean nueva = true;
			for (SavedKey s : mKeys) {
				if (name.equals(s.getWlan_name())) {
					s.getKeys().add(c.getString(c.getColumnIndex("key")));
					nueva = false;
					break;
				}
			}

			if (nueva) {
				List<String> a = new ArrayList<String>();
				a.add(c.getString(c.getColumnIndex("key")));
				SavedKey k = new SavedKey(c.getString(c
						.getColumnIndex("nombre")), c.getString(c
						.getColumnIndex("address")), a, c.getFloat(c
						.getColumnIndex("latitude")), c.getFloat(c
						.getColumnIndex("longitude")));
				mKeys.add(k);
			}

		}
		// }
		c.close();
		return mKeys;

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
			Object item = getItem(position);

			if (v == null) {
				// v = getLayoutInflater().inflate(R.layout.menu_row_item,
				// parent, false);
				v = getSherlockActivity().getLayoutInflater().inflate(
						R.layout.key_saved_list_element, parent, false);
			}

			((TextView) v.findViewById(R.id.networkName)).setText(mKeys.get(
					position).getWlan_name());
			((TextView) v.findViewById(R.id.networkKey))
					.setText(printKeys(mKeys.get(position).getKeys()));

			// TextView tv = (TextView) v;
			// tv.setText(((Item) item).mTitle);
			// tv.setCompoundDrawablesWithIntrinsicBounds(
			// ((Item) item).mIconRes, 0, 0, 0);

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
		listener.onSavedKeySelected(mKeys.get(arg2));

	}
}
