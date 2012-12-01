package es.glasspixel.wlanaudit.activities;

import java.util.List;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.AlertDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.fragments.MapFragment;
import es.glasspixel.wlanaudit.fragments.SavedKeysMenuFragment;
import es.glasspixel.wlanaudit.fragments.SavedKeysMenuFragment.OnSavedKeySelectedListener;

public class SlidingMapActivity extends SlidingFragmentActivity implements
		OnSavedKeySelectedListener {

	private static final int SHOW_MENU = 0;
	private MapFragment mContent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.map_layout_locations_list_title);

		setContentView(R.layout.responsive_content_frame);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// check if the content frame contains the menu frame
		if (findViewById(R.id.menu_frame) == null) {

			setBehindContentView(R.layout.menu_frame);
			setSlidingActionBarEnabled(false);
			getSlidingMenu().setSlidingEnabled(true);
			getSlidingMenu().setMode(SlidingMenu.LEFT);
			// getSlidingMenu().setShadowDrawable(R.drawable.shadowright);
			getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
			// show home as up so we can toggle

		} else {
			// add a dummy view
			View v = new View(this);
			setBehindContentView(v);
			getSlidingMenu().setSlidingEnabled(false);
			getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		}

		// set the Above View Fragment
		if (savedInstanceState != null)
			mContent = (MapFragment) getSupportFragmentManager().getFragment(
					savedInstanceState, "mContent");
		if (mContent == null)
			mContent = new MapFragment(0);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, mContent).commit();

		// set the Behind View Fragment
		SavedKeysMenuFragment s = new SavedKeysMenuFragment();
		s.addListener(this);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.menu_frame, s).commit();

		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindScrollScale(0.25f);
		sm.setFadeDegree(0.25f);

		// show the explanation dialog
		// if (savedInstanceState == null)
		// new AlertDialog.Builder(this).setTitle(R.string.what_is_this)
		// .setMessage(R.string.responsive_explanation).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getSupportMenuInflater().inflate(R.menu.menu_map_location, menu);
		if (getSlidingMenu().isSlidingEnabled()) {
			menu.add(0, SHOW_MENU, 1, getResources().getString(R.string.show_keys_list));
			menu.getItem(1).setIcon(R.drawable.ic_menu_account_list);
			menu.getItem(1).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
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
				getSlidingMenu().showAbove();
			}
		}, 50);
	}

	@Override
	public void onSavedKeySelected(SavedKey s) {

		getSlidingMenu().showAbove();
		((MapFragment) getSupportFragmentManager().findFragmentById(
				R.id.content_frame)).setFocused(s);

	}
}
