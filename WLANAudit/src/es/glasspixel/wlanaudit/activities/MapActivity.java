package es.glasspixel.wlanaudit.activities;

import java.util.ArrayList;
import java.util.List;

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
import com.korovyansk.android.slideout.SlideoutActivity;

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
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.LinearLayout;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_layout);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		myGesture = new GestureDetector(getBaseContext(),
				(OnGestureListener) this);

		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		// List all providers:
		List<String> providers = locationManager.getAllProviders();
		for (String provider : providers) {
			printProvider(provider);
		}

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

		screenIsLarge = getResources().getBoolean(R.bool.screen_large);

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

		if (!(screenIsLarge && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)) {
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
													.getHeight() / 4)
													: (((WindowManager) getSystemService(Context.WINDOW_SERVICE))
															.getDefaultDisplay()
															.getWidth() / 5),
											getResources().getDisplayMetrics());
							SlideoutActivity.prepare(MapActivity.this,
									R.id.swipeBezelMap, width);
							Intent i = new Intent(MapActivity.this,
									MenuActivity.class);
							i.putExtra("calling-activity",
									ActivityConstants.ACTIVITY_2);
							startActivityForResult(i, 1);
							overridePendingTransition(0, 0);
						}
					});

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
					this.centerMap(new GeoPoint(s.getLatitude(), s
							.getLongitude()));
					centerMap(anotherOverlayItemArray.get(i).mGeoPoint);
					break;
				}
				i++;
			}

		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void showLocation(Location l) {
		final GeoPoint gp = new GeoPoint(l.getLatitude(), l.getLongitude());

		Toast.makeText(getApplicationContext(), "Show current position",
				Toast.LENGTH_LONG).show();

		changePositionInMap(l);
		this.centerMap(gp);

		// myMapController.setZoom(7);
	}

	private void centerMap(GeoPoint g) {
		myMapController.setCenter(g);
	}

	private void changePositionInMap(Location l) {
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
		// Handle item selection
		Intent i;
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
	protected void onPause() {
		locationManager.removeUpdates(listener);
		super.onPause();
	}

	private final LocationListener listener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			keyLatitude = location.getLatitude();
			keyLongitude = location.getLongitude();
			changePositionInMap(location);
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

	protected void loadSavedKeys() {
		mKeys = new ArrayList<SavedKey>();
		KeysSQliteHelper usdbh = new KeysSQliteHelper(this, "DBKeys", null, 1);

		SQLiteDatabase db = usdbh.getReadableDatabase();
		Cursor c = db.query("Keys", new String[] { "nombre", "key", "latitude",
				"longitude" }, null, null, null, null, "nombre ASC");
		while (c.moveToNext()) {
			SavedKey k = new SavedKey(c.getString(c.getColumnIndex("nombre")),
					c.getString(c.getColumnIndex("key")), c.getFloat(c
							.getColumnIndex("latitude")), c.getFloat(c
							.getColumnIndex("longitude")));
			mKeys.add(k);
		}
		c.close();

		if (mKeys.isEmpty()) {
			for (int i = 0; i < 4; i++) {
				SavedKey k = new SavedKey("Key " + i, "title " + i, i * 20,
						i * 60);
				mKeys.add(k);
			}
		}

	}

	private void loadElements() {

		this.loadSavedKeys();

		for (SavedKey s : mKeys) {
			anotherOverlayItemArray
					.add(new OverlayItem(s.getWlan_name(), s.getKey(),
							new GeoPoint(s.getLatitude(), s.getLongitude())));
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
							myMapController.setCenter(new GeoPoint(mKeys.get(
									arg2).getLatitude(), mKeys.get(arg2)
									.getLongitude()));

						}

					});

		}

	}

	OnItemGestureListener<OverlayItem> myOnItemGestureListener = new OnItemGestureListener<OverlayItem>() {

		@Override
		public boolean onItemLongPress(int arg0, OverlayItem arg1) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onItemSingleTapUp(int index, OverlayItem item) {
			// SavedKey s = mKeys.get(index - 1);

			// centerMap(new GeoPoint(mKeys.get(index - 1).getLatitude(), mKeys
			// .get(index - 1).getLongitude()));

			// centerMap(anotherOverlayItemArray.get(index-1).mGeoPoint);

			return true;
		}

	};

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

}
