package es.glasspixel.wlanaudit.activities;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.adapters.MapElementsAdapter;
import es.glasspixel.wlanaudit.database.KeysSQliteHelper;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class MapActivity extends SherlockActivity {

	private MapView myOpenMapView;
	private MapController myMapController;
	private ArrayList<SavedKey> mKeys;
	private ArrayList<OverlayItem> anotherOverlayItemArray;
	private boolean screenIsLarge;
	private LocationManager locationManager;
	private LocationProvider provider;
	protected double keyLatitude;
	protected double keyLongitude;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_layout);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 1000, 50, listener);

		screenIsLarge = getResources().getBoolean(R.bool.screen_large);

		myOpenMapView = (MapView) findViewById(R.id.openmapview);
		myOpenMapView.setBuiltInZoomControls(true);
		myMapController = myOpenMapView.getController();
		myMapController.setZoom(4);
		myOpenMapView.setMultiTouchControls(true);

		loadElements();

		if (mKeys.size() == 0) {

			Location lastKnownLocation = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (lastKnownLocation != null) {
				keyLatitude = lastKnownLocation.getLatitude();
				keyLongitude = lastKnownLocation.getLongitude();
				myMapController.setCenter(new GeoPoint(keyLatitude,
						keyLongitude));
			}

		}

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
			// TODO Auto-generated method stub

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

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			break;
		}
		return super.onMenuItemSelected(featureId, item);
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
