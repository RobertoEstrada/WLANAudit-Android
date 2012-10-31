package es.glasspixel.wlanaudit.activities;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.korovyansk.android.slideout.SlideoutHelper;

import es.glasspixel.wlanaudit.fragments.MenuFragment;
import es.glasspixel.wlanaudit.interfaces.OnDataPass;

import android.os.Bundle;

import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;

public class MenuActivity extends SherlockFragmentActivity implements
		OnDataPass {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().hide();
		mSlideoutHelper = new SlideoutHelper(this, true);
		mSlideoutHelper.activate();
		int callingActivity = getIntent().getIntExtra("calling-activity", 0);
		MenuFragment fragment = new MenuFragment();
		Bundle bundle = new Bundle();
		bundle.putInt("calling-activity", callingActivity);
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
		// TODO Auto-generated method stub
		if (data.equals("maintain")) {
			// if (mSlideoutHelper != null) {
			// mSlideoutHelper.close();
			// }
		}
	}

}
