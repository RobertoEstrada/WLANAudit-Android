package es.glasspixel.wlanaudit.activities;

import java.util.ArrayList;
import java.util.List;

import net.simonvt.widget.MenuDrawer;
import net.simonvt.widget.MenuDrawerManager;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.SimpleLocationOverlay;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.adapters.MapElementsAdapter;
import es.glasspixel.wlanaudit.database.KeysSQliteHelper;
import es.glasspixel.wlanaudit.dominio.ActivityConstants;
import es.glasspixel.wlanaudit.dominio.DataUtils;
import es.glasspixel.wlanaudit.interfaces.SavedKeyListener;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MapActivity extends SherlockActivity implements OnGestureListener,
		SavedKeyListener {

	private MapView myOpenMapView;
	private MapController myMapController;
	private ArrayList<SavedKey> mKeys;
	private ArrayList<OverlayItem> anotherOverlayItemArray;
	private boolean screenIsLarge;
	private LocationManager locationManager;
	private LocationProvider provider;
	protected double keyLatitude, mLatitude = 0;
	protected double keyLongitude, mLongitude = 0;
	private String bestProvider;
	private SimpleLocationOverlay mCurrentTrackOverlay;
	private OverlayItem positionOverlay;
	private GestureDetector myGesture;
	private ArrayList<OverlayItem> positionOverlayItemArray;
	private MenuDrawerManager mMenuDrawer;
	private ArrayList<Object> items;
	private es.glasspixel.wlanaudit.activities.MenuListView mList;
	private MenuAdapter mAdapter;
	private LinearLayout l;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_map_layout);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		screenIsLarge = getResources().getBoolean(R.bool.screen_large);

		setContentView(R.layout.activity_map_layout);
		l = ((LinearLayout) findViewById(R.id.swipeBezelMap));

		if (l != null) {
			mMenuDrawer = new MenuDrawerManager(this,
					MenuDrawer.MENU_DRAG_CONTENT,
					MenuDrawer.MENU_POSITION_RIGHT);
			mMenuDrawer.setContentView(R.layout.activity_map_layout);

			mMenuDrawer
					.getMenuDrawer()
					.setMenuWidth(
							getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? ((((WindowManager) getSystemService(Context.WINDOW_SERVICE))
									.getDefaultDisplay().getWidth() / 4) * 3)
									: (((WindowManager) getSystemService(Context.WINDOW_SERVICE))
											.getDefaultDisplay().getWidth() / 2));

			items = new ArrayList<Object>();
			items.add(new Category(getResources().getString(R.string.action2)
					.toUpperCase()));

			((LinearLayout) findViewById(R.id.swipeBezelMap))
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							// TODO Auto-generated method stub
							int width = (int) TypedValue
									.applyDimension(
											TypedValue.COMPLEX_UNIT_DIP,
											getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? (((WindowManager) getSystemService(Context.WINDOW_SERVICE))
													.getDefaultDisplay()
													.getHeight() / 2)
													: (((WindowManager) getSystemService(Context.WINDOW_SERVICE))
															.getDefaultDisplay()
															.getWidth() / 2),
											getResources().getDisplayMetrics());

							mMenuDrawer.toggleMenu();
							// SlideoutActivity.prepare(MapActivity.this,
							// R.id.swipeBezelMap, width);
							// Intent i = new Intent(MapActivity.this,
							// MenuActivity.class);
							// i.putExtra("calling-activity",
							// ActivityConstants.ACTIVITY_2);
							// i.putExtra("width", width);
							// startActivityForResult(i, 1);
							// overridePendingTransition(0, 0);
						}
					});

		} else {
			setContentView(R.layout.activity_map_layout);
		}

		// A custom ListView is needed so the drawer can be notified when it's
		// scrolled. This is to update the position
		// of the arrow indicator.
		mList = new MenuListView(this);

		myGesture = new GestureDetector(getBaseContext(),
				(OnGestureListener) this);

		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		// List all providers:
		List<String> providers = locationManager.getAllProviders();
		// for (String provider : providers) {
		// printProvider(provider);
		// }

		Criteria criteria = new Criteria();
		bestProvider = locationManager.getBestProvider(criteria, false);
		Log.d("MapActivity", "best provider: " + bestProvider);

		myOpenMapView = (MapView) findViewById(R.id.openmapview);
		myOpenMapView.setBuiltInZoomControls(true);
		myOpenMapView.setMultiTouchControls(true);
		myMapController = myOpenMapView.getController();
		myMapController.setZoom(4);

		anotherOverlayItemArray = new ArrayList<OverlayItem>();
		positionOverlayItemArray = new ArrayList<OverlayItem>();

		Location location = locationManager.getLastKnownLocation(bestProvider);
		if (location != null) {
			showLocation(location);
		} else {
			Toast.makeText(getApplicationContext(),
					"Your location is unavailable now", Toast.LENGTH_LONG)
					.show();
		}

		locationManager.requestLocationUpdates(bestProvider, 20, 0, listener);

		loadElements();

		if (mKeys.size() == 0) {

			Location lastKnownLocation = locationManager
					.getLastKnownLocation(bestProvider);
			if (lastKnownLocation != null) {
				// mLatitude = lastKnownLocation.getLatitude();
				// mLongitude = lastKnownLocation.getLongitude();
				showLocation(lastKnownLocation);

			}

		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			String wlan_selected = DataUtils.getInstance(
					getApplicationContext()).getSavedkeyselected();
			Log.d("MapActivity", "wlan selected on menu: " + wlan_selected);
			int i = 0;
			for (SavedKey s : mKeys) {
				if (s.getWlan_name().equals(wlan_selected)) {
					this.centerMap(
							new GeoPoint(s.getLatitude(), s.getLongitude()),
							false);
					// centerMap(anotherOverlayItemArray.get(i).mGeoPoint);
					break;
				}
				i++;
			}

		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void showLocation(Location l) {
		final GeoPoint gp = new GeoPoint(l.getLatitude(), l.getLongitude());

		Toast.makeText(getApplicationContext(),
				getResources().getString(R.string.position_refreshed),
				Toast.LENGTH_LONG).show();

		changePositionInMap(l);
		this.centerMap(gp, false);

	}

	private void centerMap(GeoPoint g, boolean zoom) {
		myMapController.animateTo(g);
		// myMapController.setCenter(g);

		if (zoom)
			myMapController.setZoom(myOpenMapView.getMaxZoomLevel() - 5);

	}

	private void changePositionInMap(Location l) {
		// IF (POSITIONOVERLAY != NULL
		// && POSITIONOVERLAYITEMARRAY.CONTAINS(POSITIONOVERLAY)) {
		// POSITIONOVERLAYITEMARRAY.REMOVE(POSITIONOVERLAY);
		// }
		if (positionOverlay != null
				&& positionOverlayItemArray.contains(positionOverlay)) {
			positionOverlayItemArray.remove(positionOverlay);
		}

		positionOverlay = new OverlayItem("My position", "", new GeoPoint(
				l.getLatitude(), l.getLongitude()));
		positionOverlay.setMarker(this.getResources().getDrawable(
				R.drawable.marker_blue));
		// anotherOverlayItemArray.add(positionOverlay);
		// anotherOverlayItemArray.add(0, positionOverlay);
		positionOverlayItemArray.add(positionOverlay);
		ItemizedOverlayWithFocus<OverlayItem> positiontemizedIconOverlay = new ItemizedOverlayWithFocus<OverlayItem>(
				this, positionOverlayItemArray, null);
		// myOpenMapView.getOverlays().clear();

		myOpenMapView.getOverlays().add(positiontemizedIconOverlay);

	}

	private void printProvider(String provider) {
		LocationProvider info = locationManager.getProvider(provider);
		Log.d("MapActivity", info.getName());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getSupportMenuInflater().inflate(R.menu.menu_map_location, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			break;
		case R.id.check_location_menu:
			Location lastKnownLocation = locationManager
					.getLastKnownLocation(bestProvider);
			if (lastKnownLocation != null) {
				this.showLocation(lastKnownLocation);
			}
			break;

		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	public void onBackPressed() {
		if (mMenuDrawer != null) {
			final int drawerState = mMenuDrawer.getDrawerState();
			if (drawerState == MenuDrawer.STATE_OPEN
					|| drawerState == MenuDrawer.STATE_OPENING) {
				mMenuDrawer.closeMenu();
				return;
			}
		}

		super.onBackPressed();
	}

	@Override
	protected void onPause() {
		locationManager.removeUpdates(listener);
		super.onPause();
	}

	private final LocationListener listener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			keyLatitude = location.getLatitude();
			keyLongitude = location.getLongitude();
			// changePositionInMap(location);
			// myMapController.setCenter(new GeoPoint(keyLatitude,
			// keyLongitude));
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			bestProvider = provider;

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}

	};

	protected List<SavedKey> loadSavedKeys() {
		mKeys = new ArrayList<SavedKey>();
		KeysSQliteHelper usdbh = new KeysSQliteHelper(this, "DBKeys", null, 1);

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

	private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			mActivePosition = position - 1;
			mMenuDrawer.setActiveView(view, mActivePosition);

			// String wlan_selected = mKeys.get(mActivePosition).getWlan_name();
			// Log.d("MapActivity", "wlan selected on menu: " + wlan_selected);
			// int i = 0;
			// for (SavedKey s : mKeys) {
			// if (s.getWlan_name().equals(wlan_selected)) {
			// centerMap(new GeoPoint(s.getLatitude(), s.getLongitude()));
			// // centerMap(anotherOverlayItemArray.get(i).mGeoPoint);
			// break;
			// }
			// i++;
			// }

			hideBaloons();

			centerMap(anotherOverlayItemArray.get(mActivePosition).mGeoPoint,
					true);

			mMenuDrawer.closeMenu();
		}
	};

	private void loadElements() {

		this.loadSavedKeys();

		for (SavedKey s : mKeys) {
			anotherOverlayItemArray.add(new OverlayItem(s.getWlan_name(), s
					.getKeys().size() == 1 ? s.getKeys().get(0) : printKeys(s
					.getKeys()),
					new GeoPoint(s.getLatitude(), s.getLongitude())));
			if (!(screenIsLarge && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE))
				items.add(new Item(s.getWlan_name(),
						s.getKeys().size() == 1 ? s.getKeys().get(0)
								: printKeys(s.getKeys())));
		}
		if (l != null) {

			mAdapter = new MenuAdapter(items);
			mList.setAdapter(mAdapter);
			mList.setOnItemClickListener(mItemClickListener);
			mList.setOnScrollChangedListener(new MenuListView.OnScrollChangedListener() {
				@Override
				public void onScrollChanged() {
					mMenuDrawer.getMenuDrawer().invalidate();
				}
			});

			mMenuDrawer.setMenuView(mList);
		}

		ItemizedOverlayWithFocus<OverlayItem> anotherItemizedIconOverlay = new ItemizedOverlayWithFocus<OverlayItem>(
				this, anotherOverlayItemArray, myOnItemGestureListener);
		myOpenMapView.getOverlays().add(anotherItemizedIconOverlay);
		anotherItemizedIconOverlay.setFocusItemsOnTap(true);

		if (screenIsLarge
				&& getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

			((ListView) findViewById(R.id.listViewMap)).setEmptyView(this
					.findViewById(R.id.empty));

			((ListView) findViewById(R.id.listViewMap))
					.setAdapter(new MapElementsAdapter(this, mKeys));
			((ListView) findViewById(R.id.listViewMap))
					.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1,
								int arg2, long arg3) {
							// myMapController.setCenter(new GeoPoint(mKeys.get(
							// arg2).getLatitude(), mKeys.get(arg2)
							// .getLongitude()));
							centerMap(
									anotherOverlayItemArray.get(arg2).mGeoPoint,
									true);
							myOnItemGestureListener.onItemSingleTapUp(arg2,
									anotherOverlayItemArray.get(arg2));

						}

					});

		}

	}

	protected void hideBaloons() {
		for (OverlayItem o : anotherOverlayItemArray) {

		}

	}

	private String printKeys(List<String> keys) {
		String r = "";
		for (String s : keys) {
			r += s + ",";
		}
		return r;
	}

	OnItemGestureListener<OverlayItem> myOnItemGestureListener = new OnItemGestureListener<OverlayItem>() {

		@Override
		public boolean onItemLongPress(int arg0, OverlayItem arg1) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onItemSingleTapUp(int index, OverlayItem item) {

			return true;
		}

	};
	public int mActivePosition;

	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;

	@Override
	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.e("Flags Touch", "Flags: " + event.getEdgeFlags());
		return myGesture.onTouchEvent(event);
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		Log.e("Event", "onFling");
		Log.e("Flags", "Flags: " + e1.getEdgeFlags());

		if (e1.getEdgeFlags() == MotionEvent.EDGE_LEFT) {
			// code to handle swipe from left edge
			Log.e("!!!!!", "Edge fling!");
		}

		try {
			// do not do anything if the swipe does not reach a certain length
			// of distance
			if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
				return false;

			// right to left swipe
			if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
					&& Math.abs(velocityX) < SWIPE_THRESHOLD_VELOCITY) {

			}
			// left to right swipe
			else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
					&& Math.abs(velocityX) < SWIPE_THRESHOLD_VELOCITY) {

			}
		} catch (Exception e) {
			// nothing
		}
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onKeySlected(String data) {
		// TODO Auto-generated method stub

	}

	private static class Category {

		String mTitle;

		Category(String title) {
			mTitle = title;
		}
	}

	private static class Item {

		String mTitle;
		String mSubTitle;

		Item(String title, String subtitle) {
			mTitle = title;
			mSubTitle = subtitle;

		}
	}

	private class MenuAdapter extends BaseAdapter {

		private List<Object> mItems;

		MenuAdapter(List<Object> items) {
			mItems = items;
		}

		@Override
		public int getCount() {
			return mItems.size();
		}

		@Override
		public Object getItem(int position) {
			return mItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public int getItemViewType(int position) {
			return getItem(position) instanceof Item ? 0 : 1;
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public boolean isEnabled(int position) {
			return getItem(position) instanceof Item;
		}

		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			Object item = getItem(position);

			if (item instanceof Category) {
				if (v == null) {
					v = getLayoutInflater().inflate(R.layout.menu_row_category,
							parent, false);
				}

				((TextView) v).setText(((Category) item).mTitle);

			} else {
				if (v == null) {
					// v = getLayoutInflater().inflate(R.layout.menu_row_item,
					// parent, false);
					v = getLayoutInflater().inflate(
							R.layout.key_saved_list_element, parent, false);
				}

				((TextView) v.findViewById(R.id.networkName))
						.setText(((Item) item).mTitle);
				((TextView) v.findViewById(R.id.networkKey))
						.setText(((Item) item).mSubTitle);

				// TextView tv = (TextView) v;
				// tv.setText(((Item) item).mTitle);
				// tv.setCompoundDrawablesWithIntrinsicBounds(
				// ((Item) item).mIconRes, 0, 0, 0);
			}

			v.setTag(R.id.mdActiveViewPosition, position);

			if (position == mActivePosition) {
				mMenuDrawer.setActiveView(v, position);
			}

			return v;
		}
	}

}
