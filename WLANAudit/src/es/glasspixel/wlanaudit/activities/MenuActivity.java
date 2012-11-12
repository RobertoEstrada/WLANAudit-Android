package es.glasspixel.wlanaudit.activities;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.korovyansk.android.slideout.SlideoutHelper;

import es.glasspixel.wlanaudit.dominio.ActivityConstants;
import es.glasspixel.wlanaudit.dominio.DataUtils;
import es.glasspixel.wlanaudit.fragments.MenuFragment;
import es.glasspixel.wlanaudit.interfaces.OnDataPass;
import es.glasspixel.wlanaudit.interfaces.SavedKeyListener;

import android.os.Bundle;

import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;

public class MenuActivity extends SherlockFragmentActivity implements
		OnDataPass {

	private SavedKeyListener keyListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().hide();
		mSlideoutHelper = new SlideoutHelper(this, true);
		mSlideoutHelper.activate();
		int callingActivity = getIntent().getIntExtra("calling-activity", 0);
		int width = getIntent().getIntExtra("width", 0);
		// switch (callingActivity) {
		// case ActivityConstants.ACTIVITY_2:
		//
		// break;
		// }
		MenuFragment fragment = new MenuFragment();
		Bundle bundle = new Bundle();
		bundle.putInt("calling-activity", callingActivity);
		bundle.putInt("width", width);
		fragment.setArguments(bundle);
		getSupportFragmentManager()
				.beginTransaction()
				.add(com.korovyansk.android.slideout.R.id.slideout_placeholder,
						fragment, "menu").commit();
		mSlideoutHelper.open();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			mSlideoutHelper.close();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public SlideoutHelper getSlideoutHelper() {
		return mSlideoutHelper;
	}

	private SlideoutHelper mSlideoutHelper;

	@Override
	public void onDataPass(String data) {
		DataUtils.getInstance(getApplicationContext())
				.setSavedkeyselected(data);
		mSlideoutHelper.close();
	}

}
