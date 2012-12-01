package es.glasspixel.wlanaudit.fragments;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.ActionMode.Callback;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseBooleanArray;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//import com.actionbarsherlock.app.SherlockFragment;
//import com.actionbarsherlock.app.SherlockListFragment;

import es.glasspixel.wlanaudit.R;

import es.glasspixel.wlanaudit.activities.AboutActivity;

import es.glasspixel.wlanaudit.activities.SavedKey;
import es.glasspixel.wlanaudit.activities.SlidingMapActivity;
import es.glasspixel.wlanaudit.activities.WLANAuditPreferencesActivity;
import es.glasspixel.wlanaudit.adapters.KeysSavedAdapter;
import es.glasspixel.wlanaudit.database.KeysSQliteHelper;
import es.glasspixel.wlanaudit.dominio.SavedKeysUtils;
import es.glasspixel.wlanaudit.keyframework.IKeyCalculator;
import es.glasspixel.wlanaudit.keyframework.KeyCalculatorFactory;
import es.glasspixel.wlanaudit.keyframework.NetData;

public class SavedKeysFragment extends SherlockFragment {

	View myFragmentView;
	protected ActionMode mActionMode;
	protected int context_menu_item_position;
	private boolean screenIsLarge;
	private List<SavedKey> mKeys;
	protected SavedKey mKey;
	private LocationManager locationManager;
	private LocationProvider provider;
	protected double keyLatitude = 0f;
	protected double keyLongitude = 0f;
	private String bestProvider;
	private Editor e;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		locationManager = (LocationManager) getSherlockActivity()
				.getSystemService(Context.LOCATION_SERVICE);
		locationManager = (LocationManager) getSherlockActivity()	
				.getSystemService(Context.LOCATION_SERVICE);

		Criteria criteria = new Criteria();
		bestProvider = locationManager.getBestProvider(criteria, false);

		Log.d("MapActivity", "best provider: " + bestProvider);

		locationManager.requestLocationUpdates(bestProvider, 100, 50, listener);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		myFragmentView = inflater.inflate(R.layout.saved_keys_fragment,
				container, false);
		((TextView) myFragmentView.findViewById(R.id.empty))
				.setText(getSherlockActivity().getResources().getString(
						R.string.no_data_saved_keys));

		((ListView) myFragmentView.findViewById(android.R.id.list))
				.setEmptyView(myFragmentView.findViewById(R.id.empty));
		((ListView) myFragmentView.findViewById(android.R.id.list))
				.setAdapter(new KeysSavedAdapter(getSherlockActivity(),
						R.layout.network_list_element_layout,
						android.R.layout.simple_list_item_1, loadSavedKeys()));
		((ListView) myFragmentView.findViewById(android.R.id.list))
				.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		((ListView) myFragmentView.findViewById(android.R.id.list))
				.setOnItemLongClickListener(new OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> arg0,
							View view, int position, long arg3) {
						if (mActionMode != null) {
							return false;
						}
						context_menu_item_position = position;

						// Start the CAB using the ActionMode.Callback defined
						// above
						mActionMode = getSherlockActivity().startActionMode(
								mActionCallBack);
						mKey = mKeys.get(position);
						view.setSelected(true);
						return true;
					}
				});

		((ListView) myFragmentView.findViewById(android.R.id.list))
				.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						String mDefaultPassValue = ((TextView) arg1
								.findViewById(R.id.networkKey)).getText()
								.toString();
						int sdk = android.os.Build.VERSION.SDK_INT;
						if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
							android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSherlockActivity()
									.getSystemService(Context.CLIPBOARD_SERVICE);
							clipboard.setText(mDefaultPassValue);
						} else {
							android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSherlockActivity()
									.getSystemService(Context.CLIPBOARD_SERVICE);
							android.content.ClipData clip = android.content.ClipData
									.newPlainText("text label",
											mDefaultPassValue);
							clipboard.setPrimaryClip(clip);
						}
						Toast.makeText(
								getActivity(),
								getResources().getString(
										R.string.key_copy_success),
								Toast.LENGTH_SHORT).show();

					}
				});

		screenIsLarge = getSherlockActivity().getResources().getBoolean(
				R.bool.screen_large);

		return myFragmentView;
	}

	private final LocationListener listener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			keyLatitude = location.getLatitude();
			keyLongitude = location.getLongitude();

		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}

	};

	protected List<SavedKey> loadSavedKeys() {

		mKeys = SavedKeysUtils.loadSavedKeys(getSherlockActivity());
		return mKeys;

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.networklistactivity_savedkeys_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);

	}

	/**
	 * Menu option handling
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		// Handle item selection
		switch (item.getItemId()) {

		case R.id.preferenceOption:
			i = new Intent(getSherlockActivity(),
					WLANAuditPreferencesActivity.class);
			e = getSherlockActivity().getSharedPreferences("viewpager",
					Context.MODE_PRIVATE).edit();
			e.putInt("viewpager_index", 1);
			e.commit();
			startActivity(i);
			return true;
		case R.id.aboutOption:
			i = new Intent(getSherlockActivity(), AboutActivity.class);
			e = getSherlockActivity().getSharedPreferences("viewpager",
					Context.MODE_PRIVATE).edit();
			e.putInt("viewpager_index", 1);
			e.commit();
			startActivity(i);
			return true;
		case R.id.mapOption:
			i = new Intent(getSherlockActivity(), SlidingMapActivity.class);

			startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void copyClipboard(CharSequence text) {
		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
			android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity()
					.getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(text);
		} else {
			android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity()
					.getSystemService(Context.CLIPBOARD_SERVICE);
			android.content.ClipData clip = android.content.ClipData
					.newPlainText("text label", text);
			clipboard.setPrimaryClip(clip);
		}
		Toast.makeText(getSherlockActivity(),
				getResources().getString(R.string.key_copy_success),
				Toast.LENGTH_SHORT).show();

	}

	

	private ActionMode.Callback mActionCallBack = new Callback() {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.saved_keys_elements_context_menu, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.delete_context_menu:

				KeysSQliteHelper usdbh = new KeysSQliteHelper(getActivity(),
						"DBKeys", null, 1);
				SQLiteDatabase db = usdbh.getWritableDatabase();

				String address_wlan,
				clave;
				address_wlan = mKey.getAddress();

				if (mKey.getKeys().size() == 1) {

					clave = mKey.getKeys().get(0);
					db.delete("keys", "address like ? ",
							new String[] { address_wlan });

					((ListView) myFragmentView.findViewById(android.R.id.list))
							.setAdapter(new KeysSavedAdapter(
									getSherlockActivity(),
									R.layout.network_list_element_layout,
									android.R.layout.simple_list_item_1,
									loadSavedKeys()));
				} else {
					// TODO mostrar dialogo para borrar una o todas las claves
				}
				mode.finish();

				return true;
			case R.id.copy_context_menu:
				if (mKey.getKeys().size() == 1) {
					copyClipboard(mKey.getKeys().get(0));

				} else {
					// TODO mostrar en un diálogo todas las claves posibles y al
					// tocar que se copien
				}

				mode.finish();
				return true;
			default:
				return true;
			}

		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;

		}
	};

}
