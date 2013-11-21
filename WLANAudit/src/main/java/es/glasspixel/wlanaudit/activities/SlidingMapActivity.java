/*
 * Copyright (C) 2013 The WLANAudit project contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package es.glasspixel.wlanaudit.activities;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.espiandev.showcaseview.ShowcaseView;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.inject.Inject;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.SlidingMenu.OnOpenListener;

import org.orman.mapper.Model;

import java.util.List;

import javax.annotation.Nullable;

import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.database.entities.Network;
import es.glasspixel.wlanaudit.fragments.GMapsMapFragment;
import es.glasspixel.wlanaudit.fragments.SavedNetworksMenuFragment;
import es.glasspixel.wlanaudit.fragments.SavedNetworksMenuFragment.OnSavedKeySelectedListener;
import es.glasspixel.wlanaudit.util.GMSLocationServicesWrapper;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;
import roboguice.util.RoboContext;

public class SlidingMapActivity extends SlidingFragmentActivity implements
        OnSavedKeySelectedListener, RoboContext, ShowcaseView.OnShowcaseEventListener, GooglePlayServicesClient.ConnectionCallbacks {
    /**
     * Key to store and recover the map fragment in/from the saved state bundle
     */
    private static final String MAP_FRAGMENT_KEY = "MAP_FRAGMENT";
    /**
     * Constant which represents the action on the actionbar that opens the side menu
     */
    private static final int SHOW_MENU = 0;
    /**
     * GMaps V2 map fragment
     */
    private SupportMapFragment mMapFragment;
    /**
     * GMaps V2 map controller
     */
    private GoogleMap mMap;
    /**
     * Wrapper to deal with all the pain of Google Play Services setup
     */
    private GMSLocationServicesWrapper mLocationServicesWrapper;
    /**
     * Client to the Google Play Services location service
     */
    private LocationClient mLocationClient;

    @Inject
    private LocationManager mLocationManager;

    @InjectView(R.id.showcase)
    @Nullable
    ShowcaseView mShowcaseView;

    @InjectResource(R.drawable.shadow)
    Drawable showcase_shadow;

    @InjectView(R.id.showcase_button)
    @Nullable
    Button showcase_button;

    @InjectResource(R.string.map_layout_locations_list_title)
    String ab_title;

    @InjectResource(R.string.show_keys_list)
    String show_keys_list;

    @InjectResource(R.drawable.ic_menu_account_list)
    Drawable ic_menu_account_list;

    private boolean showcaseView = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(ab_title);
        setContentView(R.layout.responsive_content_frame);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // UI form factor check
        if (findViewById(R.id.menu_frame) == null) {
            // If this is phone UI
            showcaseView = true;
            setBehindContentView(R.layout.menu_frame);
            setSlidingActionBarEnabled(false);
            getSlidingMenu().setSlidingEnabled(true);
            //getSlidingMenu().setMode(SlidingMenu.LEFT);
            getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        } else {
            // If this is tablet UI
            getSlidingMenu().setSlidingEnabled(false);
            setBehindContentView(new View(this));
        }

        // set the Above View Fragment
        if (savedInstanceState != null)
            mMapFragment = (GMapsMapFragment) getSupportFragmentManager().getFragment(
                    savedInstanceState, MAP_FRAGMENT_KEY);
        if (mMapFragment == null) {
            mMapFragment = GMapsMapFragment.newInstance();
        }

        // Location client setup
        mLocationServicesWrapper = new GMSLocationServicesWrapper(this, this);
        mLocationClient = mLocationServicesWrapper.getLocationClient();

        // Set the map fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, mMapFragment)
                .commit();

        // Set the network list fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.menu_frame, new SavedNetworksMenuFragment()).commit();

        // customize the SlidingMenu
        SlidingMenu sm = getSlidingMenu();
        setUpSlidingMenu(sm);

        // Set up the showcase view
        setUpShowCaseView(sm);
    }

    /*
    * Called when the Activity is restarted, even before it becomes visible.
    */
    @Override
    public void onStart() {
        super.onStart();
        /*
         * Connect the client. Don't re-start any requests here;
         * instead, wait for onResume()
         */
        mLocationClient.connect();
    }

    /*
     * Called when the Activity is no longer visible at all.
     * Stop updates and disconnect.
     */
    @Override
    public void onStop() {
        // After disconnect() is called, the client is considered "dead".
        mLocationClient.disconnect();
        super.onStop();
    }

    /**
     * {@inheritDoc}
     */
    public void onResume() {
        super.onResume();
        // Setup the map
        setUpMapIfNeeded();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getSlidingMenu().isSlidingEnabled()) {
            menu.add(0, SHOW_MENU, 1, show_keys_list);
            menu.getItem(0).setIcon(ic_menu_account_list);
            menu.getItem(0).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                break;
            case SHOW_MENU:
                toggle();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, MAP_FRAGMENT_KEY, mMapFragment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSavedKeySelected(Network s) {
        getSlidingMenu().showBehind();
        CameraPosition camPos = CameraPosition.builder()
                .target(new LatLng(s.mLatitude, s.mLongitude))
                .zoom(18).bearing(0.0f)
                .tilt(45.0f)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
    }

    private void setUpSlidingMenu(SlidingMenu sm) {
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setShadowWidthRes(R.dimen.shadow_width);
        sm.setShadowDrawable(showcase_shadow);
        sm.setBehindScrollScale(0.25f);
        sm.setFadeDegree(0.25f);
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void setUpShowCaseView(SlidingMenu sm) {
        if (showcaseView) {

            mShowcaseView.setShotType(ShowcaseView.TYPE_ONE_SHOT);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                mShowcaseView.setShowcasePosition(0, size.y / 2);
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
                mShowcaseView.setShowcasePosition(0, getWindowManager().getDefaultDisplay().getHeight() / 2);
            }

            showcase_button.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mShowcaseView.isShown()) {
                        mShowcaseView.hide();
                        showcase_button.setOnClickListener(null);
                        mShowcaseView.onClick(v);

                    }

                }
            });
            sm.setOnOpenListener(new OnOpenListener() {

                @Override
                public void onOpen() {
                    if (mShowcaseView.isShown()) {
                        mShowcaseView.hide();
                        mShowcaseView.onClick(showcase_button);
                    }

                }
            });
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView
     * MapView}) will show a prompt for the user to install/update the Google Play services APK on
     * their device.
     * <p/>
     * A user can return to this Activity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the Activity may not have been
     * completely destroyed during this process (it is likely that it would only be stopped or
     * paused), {@link #onCreate(Bundle)} may not be called again so we should call this method in
     * {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = mMapFragment.getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        mMap.setIndoorEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        List<Network> savedNetworks = Model.fetchAll(Network.class);
        for (Network savedNetwork : savedNetworks) {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(savedNetwork.mLatitude, savedNetwork.mLongitude))
                    .title(savedNetwork.mSSID).snippet(savedNetwork.mBSSID));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowcaseViewHide(ShowcaseView showcaseView) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowcaseViewShow(ShowcaseView showcaseView) {
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location loc = null;
        if (mLocationServicesWrapper.servicesConnected()) {
            loc = mLocationClient.getLastLocation();
        }

        if (loc != null) {
            CameraPosition camPos = CameraPosition.builder()
                    .target(new LatLng(loc.getLatitude(), loc.getLongitude())).zoom(18)
                    .bearing(0.0f).tilt(45.0f).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
        }
    }

    @Override
    public void onDisconnected() {
    }
}
