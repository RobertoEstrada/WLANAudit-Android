package es.glasspixel.wlanaudit;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import es.glasspixel.wlanaudit.fragments.SavedKeysFragment;
import es.glasspixel.wlanaudit.fragments.ScanFragment;
import android.content.res.Resources;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

public class NetworkListActivitySwipe extends SherlockFragmentActivity
		implements ScanFragment.Callbacks {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;
	List<SherlockFragment> mFragments;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	private Resources res;

	private boolean screenIsLarge;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_network_list_activity_swipe);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.

		res = getResources();
		screenIsLarge = res.getBoolean(R.bool.screen_large);

		if (!screenIsLarge) {

			mFragments = new ArrayList<SherlockFragment>();

			mSectionsPagerAdapter = new SectionsPagerAdapter(
					getSupportFragmentManager());

			// Set up the ViewPager with the sections adapter.
			mViewPager = (ViewPager) findViewById(R.id.pager);
			mViewPager.setAdapter(mSectionsPagerAdapter);
		} else {
			SherlockFragment fragment = new SavedKeysFragment();
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.item_detail_container, fragment).commit();
			
		}

	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public SherlockFragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			// Fragment fragment = new DummySectionFragment();
			// Bundle args = new Bundle();
			// args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position +
			// 1);
			// fragment.setArguments(args);
			SherlockFragment fragment;
			if (position == 0)
				fragment = new ScanFragment();
			else
				fragment = new SavedKeysFragment();

			return fragment;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return getResources().getString(R.string.action1).toUpperCase();
			case 1:
				return getResources().getString(R.string.action2).toUpperCase();

			}
			return null;
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
		// mWifiManager.startScan();
		// mAd.loadAd(new AdRequest());
	}

	@Override
	public void onItemSelected(ScanResult s) {

		if (screenIsLarge) {
			SherlockFragment fragment = new SavedKeysFragment();
			FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();
			// ft.add(R.id.saved_keys_fragment, fragment).commit();
			ft.replace(R.id.item_detail_container, fragment).commit();
		} else {
			mViewPager.setAdapter(mSectionsPagerAdapter);
			// mViewPager.refreshDrawableState();
		}

	}

}
