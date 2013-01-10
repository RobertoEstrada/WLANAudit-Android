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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import roboguice.inject.InjectView;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.inject.Inject;

import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.actions.AutoScanAction;
import es.glasspixel.wlanaudit.database.entities.Network;
import es.glasspixel.wlanaudit.dialogs.NetworkDetailsDialogFragment;
import es.glasspixel.wlanaudit.dialogs.SavedNetworkDetailsDialogFragment;
import es.glasspixel.wlanaudit.fragments.SavedNetworksFragment;
import es.glasspixel.wlanaudit.fragments.ScanFragment;
import es.glasspixel.wlanaudit.interfaces.OnDataSourceModifiedListener;

public class NetworkListActivitySwipe extends RoboSherlockFragmentActivity
		implements ScanFragment.ScanFragmentListener,
		SavedNetworksFragment.SavedNetworkFragmentListener {

	/**
	 * Constant to define how many fragments this activity handles
	 */
	private static final int CHILDREN_FRAGMENT_NUMBER = 2;

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	private SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The list of fragments that this activity has under its control
	 */
	private List<Fragment> mFragments;

	/**
	 * The {@link ViewPager} that will host the activty fragments.
	 */
	@InjectView(R.id.pager)
	@Nullable
	private ViewPager mViewPager;

	/**
	 * Handle to resources
	 */
	@Inject
	private Resources mResources;
	/**
	 * Menu item to handle wifi scan
	 */

	private MenuItem refresh;
	/**
	 * Menu item to enable/disable automatic scan setting
	 */

	private MenuItem automatic_scan;
	/**
	 * AutoScan action
	 */

	private AutoScanAction mAutoScanAction;

	/**
	 * Menu itme to launch map activity
	 */
	private MenuItem map_menu_item;

	/**
	 * Lifecycle management: Activity is being created
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_network_list_activity_swipe);

		if (savedInstanceState != null
				&& savedInstanceState.getBoolean("autoscan_state")) {
			mAutoScanAction = new AutoScanAction(this, true);
		} else {
			mAutoScanAction = new AutoScanAction(this);
		}

		mFragments = new ArrayList<Fragment>();
		mFragments.add(new ScanFragment());
		SavedNetworksFragment saved = new SavedNetworksFragment();
		Bundle args = new Bundle();
		args.putBoolean("screen_large", mViewPager == null);
		saved.setArguments(args);
		mFragments.add(saved);

		if (mViewPager != null) {
			mSectionsPagerAdapter = new SectionsPagerAdapter(
					getSupportFragmentManager());
			mViewPager.setAdapter(mSectionsPagerAdapter);
			mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

				@Override
				public void onPageSelected(int arg0) {

				}

				@Override
				public void onPageScrolled(int arg0, float arg1, int arg2) {
					checkMenuItems();

				}

				@Override
				public void onPageScrollStateChanged(int arg0) {
					// TODO Auto-generated method stub

				}
			});
		} else {
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.scan_fragment, mFragments.get(0), "tag")
					.commit();
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.item_detail_container, mFragments.get(1),
							"tag2").commit();
		}

	}

	/**
	 * Lifecycle management: Activity must save its state
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// super.onSaveInstanceState(outState);
		outState.remove("android:support:fragments");
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		checkMenuItems();
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.networklistactivity_menu, menu);
		refresh = menu.findItem(R.id.scanOption);
		automatic_scan = menu.findItem(R.id.toggleAutoscanOption);
		map_menu_item = menu.findItem(R.id.mapOption);

		checkAutoScanStatus();

		return true;
		// return super.onCreateOptionsMenu(menu);
	}

	private void checkMenuItems() {
		if (mViewPager != null) {
			if (mViewPager.getCurrentItem() == 0) {
				refresh.setVisible(true);
				automatic_scan.setVisible(true);
				map_menu_item.setVisible(false);

			} else {
				refresh.setVisible(false);
				automatic_scan.setVisible(false);
				map_menu_item.setVisible(true);
			}
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.scanOption:

			((ScanFragment) mFragments.get(0)).startScan();

			return true;
		case R.id.toggleAutoscanOption:
			// if (mPosition == 0)
			((ScanFragment) mFragments.get(0)).mAutoScanAction.performAction();
			checkAutoScanStatus();

			return true;
		case R.id.preferenceOption:
			i = new Intent(this, WLANAuditPreferencesActivity.class);

			/**
			 * slide out to left + slide in from right --> aparece por la
			 * izquierda la nueva actividad ;; slide out to right + slide in
			 * from left --> intenta aparece la actividad que se cierra por la
			 * izquierda ;; slide in from right + slide out to left --> nada ;;
			 * slide in from left + slide out to right --> si pero no efecto
			 * deseado ;; slide in form right + slide out to right --> no
			 */

			startActivity(i);
			overridePendingTransition(R.anim.slide_in_from_right,
					R.anim.slide_out_to_left);
			return true;
		case R.id.aboutOption:
			i = new Intent(this, AboutActivity.class);

			startActivity(i);
			overridePendingTransition(R.anim.slide_in_from_right,
					R.anim.slide_out_to_left);
			return true;
		case R.id.mapOption:
			i = new Intent(this, SlidingMapActivity.class);

			startActivity(i);
			overridePendingTransition(R.anim.slide_in_from_right,
					R.anim.slide_out_to_left);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	public void checkAutoScanStatus() {
		if (mAutoScanAction.isAutoScanEnabled()) {
			automatic_scan.setIcon(R.drawable.ic_autoscan);
		} else {
			automatic_scan.setIcon(R.drawable.ic_autoscan_disabled);
		}

	}

	/**
	 * Lifecycle management: Activity is about to be shown
	 */
	protected void onStart() {
		super.onStart();
		// mAd.loadAd(new AdRequest());
	}

	/**
	 * Lifecycle management: Activity is being resumed, we need to refresh its
	 * contents
	 */
	protected void onResume() {
		super.onResume();
		// mAd.loadAd(new AdRequest());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onNetworkSelected(ScanResult networkData) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment prev = getSupportFragmentManager().findFragmentByTag(
				"detailsDialog");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		// Create and show the dialog.
		NetworkDetailsDialogFragment detailsDlg = NetworkDetailsDialogFragment
				.newInstance(networkData);
		detailsDlg.show(ft, "detailsDialog");
	}

	@Override
	public void onNetworkSelected(Network networkData) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment prev = getSupportFragmentManager().findFragmentByTag(
				"detailsDialog");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		// Create and show the dialog.
		SavedNetworkDetailsDialogFragment detailsDlg = SavedNetworkDetailsDialogFragment
				.newInstance(networkData);
		detailsDlg.show(ft, "detailsDialog");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dataSourceShouldRefresh() {
		for (Fragment f : mFragments) {
			if (f instanceof OnDataSourceModifiedListener) {
				OnDataSourceModifiedListener listener = (OnDataSourceModifiedListener) f;
				listener.dataSourceShouldRefresh();
			}
		}
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	private class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return mFragments.get(position);
		}

		@Override
		public int getCount() {
			return CHILDREN_FRAGMENT_NUMBER;
		}

		@SuppressLint("DefaultLocale")
		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return mResources.getString(R.string.action1).toUpperCase();
			case 1:
				return mResources.getString(R.string.action2).toUpperCase();

			}
			return null;
		}
	}

	@Override
	public void scanCompleted() {
		if (refresh != null)
			refresh.setActionView(null);

	}

	@Override
	public void scanStart() {
		if (refresh != null)
			refresh.setActionView(R.layout.indeterminate_progress_action);

	}
}