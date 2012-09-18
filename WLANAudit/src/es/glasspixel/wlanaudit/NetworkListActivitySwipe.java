package es.glasspixel.wlanaudit;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import es.glasspixel.wlanaudit.fragments.SavedKeysFragment;
import es.glasspixel.wlanaudit.fragments.ScanFragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

public class NetworkListActivitySwipe extends SherlockFragmentActivity {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_network_list_activity_swipe);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

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

	// /**
	// * A dummy fragment representing a section of the app, but that simply
	// * displays dummy text.
	// */
	// public static class DummySectionFragment extends SherlockFragment {
	// /**
	// * The fragment argument representing the section number for this
	// * fragment.
	// */
	// public static final String ARG_SECTION_NUMBER = "section_number";
	//
	// public DummySectionFragment() {
	// }
	//
	// @Override
	// public View onCreateView(LayoutInflater inflater, ViewGroup container,
	// Bundle savedInstanceState) {
	// // Create a new TextView and set its text to the fragment's section
	// // number argument value.
	// TextView textView = new TextView(getActivity());
	// textView.setGravity(Gravity.CENTER);
	// textView.setText(Integer.toString(getArguments().getInt(
	// ARG_SECTION_NUMBER)));
	// return textView;
	// }
	// }

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

	

}
