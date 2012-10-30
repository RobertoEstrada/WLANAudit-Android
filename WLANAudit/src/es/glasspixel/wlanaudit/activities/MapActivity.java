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

import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.adapters.MapElementsAdapter;
import es.glasspixel.wlanaudit.database.KeysSQliteHelper;
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
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MapActivity extends SherlockActivity {

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_layout);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
		myMapController = myOpenMapView.getController();
		myMapController.setZoom(4);
		myOpenMapView.setMultiTouchControls(true);

		Location location = locationManager.getLastKnownLocation(bestProvider);
		if (location != null) {
			showLocation(location);
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

	}

	private void showLocation(Location l) {
		final GeoPoint gp = new GeoPoint(l.getLatitude(), l.getLongitude());
		myMapController.setCenter(gp);
		myMapController.setZoom(6);
		Toast.makeText(getApplicationContext(), "Show current position",
				Toast.LENGTH_LONG).show();
		mCurrentTrackOverlay = new SimpleLocationOverlay(
				getApplicationContext()) {
			@Override
			public void draw(Canvas canavas, MapView mapView, boolean arg2) {

				Point from = new Point();

				from = mapView.getProjection().toPixels(gp, from);

				Paint p = new Paint();

				canavas.drawBitmap(BitmapFactory.decodeResource(getResources(),
						R.drawable.social_google), from.x, from.y, p);

			}
		};
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
			myMapController.setCenter(new GeoPoint(keyLatitude, keyLongitude));
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
		Cursor c = db.query("Keys", new String[] { "nombre", "key" }, null,
				null, null, null, "nombre ASC");
		while (c.moveToNext()) {
			SavedKey k = new SavedKey(c.getString(c.getColumnIndex("nombre")),
					c.getString(c.getColumnIndex("key")), c.getFloat(c
							.getColumnIndex("latitude")), c.getFloat(c
							.getColumnIndex("longitude")));
			mKeys.add(k);
		}

	}

	private void loadElements() {

		this.loadSavedKeys();

		anotherOverlayItemArray = new ArrayList<OverlayItem>();

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

			return true;
		}

	};

}
