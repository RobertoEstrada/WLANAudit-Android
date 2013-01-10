/*
 * Copyright (C) 2012 Roberto Estrada
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

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;
import roboguice.util.RoboContext;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.espian.showcaseview.ShowcaseView;
import com.google.inject.Key;

import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.SlidingMenu.OnOpenListener;

import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.database.entities.Network;
import es.glasspixel.wlanaudit.fragments.MapFragment;
import es.glasspixel.wlanaudit.fragments.SavedNetworksMenuFragment;
import es.glasspixel.wlanaudit.fragments.MapFragment.OnMapNetworkSelected;
import es.glasspixel.wlanaudit.fragments.SavedNetworksMenuFragment.OnSavedKeySelectedListener;

public class SlidingMapActivity extends SlidingFragmentActivity implements
		OnSavedKeySelectedListener, RoboContext, OnMapNetworkSelected,
		ShowcaseView.OnShowcaseEventListener {

	protected HashMap<Key<?>, Object> scopedObjects = new HashMap<Key<?>, Object>();

	private static final int SHOW_MENU = 0;

	private MapFragment mContent;
	@InjectView(R.id.showcase)
	@Nullable
	ShowcaseView sv;

	@InjectView(R.id.showcase_button)
	@Nullable
	Button showcase_button;

	@InjectResource(R.string.map_layout_locations_list_title)
	String ab_title;

	@InjectResource(R.string.show_keys_list)
	String show_keys_list;

	@InjectResource(R.drawable.ic_menu_account_list)
	Drawable ic_menu_account_list;

	@InjectResource(R.drawable.shadow)
	Drawable showcase_shadow;

	private boolean showcaseView = false;

	@SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(ab_title);

		setContentView(R.layout.responsive_content_frame);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// check if the content frame contains the menu frame
		if (findViewById(R.id.menu_frame) == null) {
			showcaseView = true;
			setBehindContentView(R.layout.menu_frame);
			setSlidingActionBarEnabled(false);
			getSlidingMenu().setSlidingEnabled(true);
			getSlidingMenu().setMode(SlidingMenu.LEFT);
			getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);

		} else {

			View v = new View(this);
			setBehindContentView(v);
			getSlidingMenu().setSlidingEnabled(false);
			getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		}

		// set the Above View Fragment
		if (savedInstanceState != null)
			mContent = (MapFragment) getSupportFragmentManager().getFragment(
					savedInstanceState, "mContent");
		if (mContent == null) {
			mContent = new MapFragment();
		}

		// set the OSM map fragment
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, mContent).commit();

		// set the Behind View Fragment (saved keys fragment)
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.menu_frame, new SavedNetworksMenuFragment()).commit();

		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(showcase_shadow);
		sm.setBehindScrollScale(0.25f);
		sm.setFadeDegree(0.25f);

		if (showcaseView) {

			sv.setShotType(ShowcaseView.TYPE_ONE_SHOT);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				Display display = getWindowManager().getDefaultDisplay();
				Point size = new Point();
				display.getSize(size);

				// set showcase view parameters

				// set the showcase ring at a medium-heigh point of display
				sv.setShowcasePosition(0, size.y / 2);
			} else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				sv.setShowcasePosition(0, getWindowManager()
						.getDefaultDisplay().getHeight() / 2);
			}

			showcase_button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (sv.isShown()) {
						sv.hide();
						showcase_button.setOnClickListener(null);
						sv.onClick(v);

					}

				}
			});
			sm.setOnOpenListener(new OnOpenListener() {

				@Override
				public void onOpen() {
					if (sv.isShown()) {
						sv.hide();
						sv.onClick(showcase_button);
					}

				}
			});
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getSupportMenuInflater().inflate(R.menu.menu_map_location, menu);
		if (getSlidingMenu().isSlidingEnabled()) {
			menu.add(0, SHOW_MENU, 1, show_keys_list);
			menu.getItem(1).setIcon(ic_menu_account_list);
			menu.getItem(1).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onBackPressed() {
		finish();
		overridePendingTransition(R.anim.slide_in_from_left,
				R.anim.slide_out_to_right);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			overridePendingTransition(R.anim.slide_in_from_left,
					R.anim.slide_out_to_right);
			break;
		case R.id.check_location_menu:

			((MapFragment) getSupportFragmentManager().findFragmentById(
					R.id.content_frame)).showLocation();
			((MapFragment) getSupportFragmentManager().findFragmentById(
					R.id.content_frame)).clearAllFocused();

			break;
		case SHOW_MENU:
			toggle();

			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		getSupportFragmentManager().putFragment(outState, "mContent", mContent);
	}

	public void switchContent(final Fragment fragment) {
		mContent = (MapFragment) fragment;
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, fragment).commit();
		Handler h = new Handler();
		h.postDelayed(new Runnable() {
			public void run() {
				getSlidingMenu().showContent();
			}
		}, 50);
	}

	@Override
	public void onSavedKeySelected(Network s) {

		getSlidingMenu().showContent();
		((MapFragment) getSupportFragmentManager().findFragmentById(
				R.id.content_frame)).setFocused(s);

	}

	@Override
	public Map<Key<?>, Object> getScopedObjectMap() {
		return scopedObjects;
	}

	@Override
	public void onShowcaseViewHide(ShowcaseView showcaseView) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onShowcaseViewShow(ShowcaseView showcaseView) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMapNetworkSelected(int selected_network_index) {

		((SavedNetworksMenuFragment) getSupportFragmentManager().findFragmentById(
				R.id.menu_frame)).onMapItemSelected(selected_network_index);
	}
}
