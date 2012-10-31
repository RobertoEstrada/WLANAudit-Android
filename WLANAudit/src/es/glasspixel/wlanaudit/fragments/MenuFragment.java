package es.glasspixel.wlanaudit.fragments;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

import com.actionbarsherlock.app.SherlockFragment;

import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.activities.SavedKey;
import es.glasspixel.wlanaudit.adapters.MapElementsAdapter;
import es.glasspixel.wlanaudit.database.KeysSQliteHelper;
import es.glasspixel.wlanaudit.interfaces.OnDataPass;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.location.LocationListener;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MenuFragment extends SherlockFragment {

	private OnDataPass dataPasser;

	private ArrayList<String> l;
	private View mView;
	public Location location;
	public LocationManager lm;

	private ArrayList<SavedKey> mKeys;

	@Override
	public void onAttach(Activity a) {
		super.onAttach(a);
		dataPasser = (OnDataPass) a;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Bundle b = getArguments();
		int a = b.getInt("calling-activity");
		final int callingActivity = getArguments()
				.getInt("calling-activity", 0);
		mView = inflater.inflate(R.layout.saved_keys_map_list, null);
		loadElements(mView);

		return mView;

	}

	protected void loadSavedKeys() {
		mKeys = new ArrayList<SavedKey>();
		KeysSQliteHelper usdbh = new KeysSQliteHelper(getSherlockActivity(),
				"DBKeys", null, 1);

		SQLiteDatabase db = usdbh.getReadableDatabase();
		Cursor c = db.query("Keys", new String[] { "nombre", "key" }, null,
				null, null, null, "nombre ASC");
		while (c.moveToNext()) {
			SavedKey k = new SavedKey(c.getString(c.getColumnIndex("nombre")),
					c.getString(c.getColumnIndex("key")), c.getFloat(c
							.getColumnIndex("latitude")), c.getFloat(c
							.getColumnIndex("longitude")));
			mKeys.add(k);
		}

		if (mKeys.isEmpty()) {
			for (int i = 0; i < 4; i++) {
				SavedKey k = new SavedKey("Key " + i, "title " + i, i * 20,
						i * 60);
				mKeys.add(k);
			}
		}

	}

	private void loadElements(View v) {

		this.loadSavedKeys();

		((ListView) v.findViewById(R.id.listViewMap)).setEmptyView(v
				.findViewById(R.id.empty));

		((ListView) v.findViewById(R.id.listViewMap))
				.setAdapter(new MapElementsAdapter(getSherlockActivity(), mKeys));
		((ListView) v.findViewById(R.id.listViewMap))
				.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {

					}

				});

	}

}
