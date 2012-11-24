
package es.glasspixel.wlanaudit.activities;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.InjectView;
import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.SherlockFragment;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.inject.Inject;

import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.fragments.SavedKeysFragment;
import es.glasspixel.wlanaudit.fragments.ScanFragment;

public class NetworkListActivitySwipe extends RoboSherlockFragmentActivity implements
        ScanFragment.OnNetworkSelectedListener {
    
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
    private List<SherlockFragment> mFragments;

    /**
     * The {@link ViewPager} that will host the activty fragments.
     */
    @InjectView(R.id.pager)
    private ViewPager mViewPager;

    /**
     * Handle to resources
     */
    @Inject
    private Resources mResources;
    
    /**
     * Detects if this screen is large or not, it is loaded
     * from a resource boolean value which is true in large screen devices
     * and false otherwise.
     */
    private boolean mScreenIsLarge;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_list_activity_swipe);
        mScreenIsLarge = mResources.getBoolean(R.bool.screen_large);

        if (mViewPager != null) {
            mFragments = new ArrayList<SherlockFragment>();
            mFragments.add(new ScanFragment());
            mFragments.add(new SavedKeysFragment());

            mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

            mViewPager.setAdapter(mSectionsPagerAdapter);

        } else {
            SherlockFragment fragment = new SavedKeysFragment();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.item_detail_container, fragment, "tag").commit();
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
    public void onItemSelected(ScanResult s) {
        /*if (mScreenIsLarge) {
            SherlockFragment fragment = new SavedKeysFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.item_detail_container, fragment).commit();
        } else {
            mViewPager.setAdapter(mSectionsPagerAdapter);
        }*/
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
        public SherlockFragment getItem(int position) {
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
                    return getResources().getString(R.string.action1).toUpperCase();
                case 1:
                    return getResources().getString(R.string.action2).toUpperCase();

            }
            return null;
        }
    }
}
