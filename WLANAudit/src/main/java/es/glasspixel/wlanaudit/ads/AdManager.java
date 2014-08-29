/*
 * Copyright (C) 2014 The WLANAudit project contributors.
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
package es.glasspixel.wlanaudit.ads;

import android.os.Handler;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class AdManager extends AdListener {

    private static final int AD_RETRY_INTERVAL_SECONDS = 60;
    private AdView mAdView;

    private Handler mRefreshHandler;
    private Runnable mRefreshRunnable;

    public AdManager(AdView adView) {
        mAdView = adView;
        mRefreshHandler = new Handler();
        mRefreshRunnable = new RefreshRunnable();
    }

    @Override
    public void onAdLoaded() {
        super.onAdLoaded();
        mAdView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAdFailedToLoad(int errorCode) {
        super.onAdFailedToLoad(errorCode);
        mAdView.setVisibility(View.GONE);
        mRefreshHandler.removeCallbacks(mRefreshRunnable);
        mRefreshHandler.postDelayed(
                mRefreshRunnable, AD_RETRY_INTERVAL_SECONDS * 1000);
    }

    private class RefreshRunnable implements Runnable {
        @Override
        public void run() {
            mAdView.loadAd(new AdRequest.Builder().build());
        }
    }
}
