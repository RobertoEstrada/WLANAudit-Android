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
import android.content.res.Resources;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.inject.Inject;

import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.dialogs.NetworkDetailsDialogFragment;
import es.glasspixel.wlanaudit.fragments.SavedKeysFragment;
import es.glasspixel.wlanaudit.fragments.ScanFragment;
import es.glasspixel.wlanaudit.interfaces.OnDataSourceModifiedListener;

public class NetworkListActivitySwipe extends RoboSherlockFragmentActivity implements
        ScanFragment.OnNetworkSelectedListener, OnDataSourceModifiedListener {
    
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
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_list_activity_swipe);
        
        mFragments = new ArrayList<Fragment>();
        mFragments.add(new ScanFragment());
        mFragments.add(new SavedKeysFragment());

        if (mViewPager != null) {
            mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
            mViewPager.setAdapter(mSectionsPagerAdapter);
        } else {
            getSupportFragmentManager().beginTransaction()
            .replace(R.id.scan_fragment, mFragments.get(0), "tag").commit();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.item_detail_container, mFragments.get(1), "tag").commit();
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
        Fragment prev = getSupportFragmentManager().findFragmentByTag("detailsDialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        NetworkDetailsDialogFragment detailsDlg = NetworkDetailsDialogFragment.newInstance(networkData);
        detailsDlg.show(ft, "detailsDialog");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void dataSourceShouldRefresh() {
        for(Fragment f : mFragments) {
            if(f instanceof OnDataSourceModifiedListener) {
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
}
