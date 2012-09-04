package es.glasspixel.wlanaudit.activities;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import es.glasspixel.wlanaudit.R;

public class MainActivity extends SherlockFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

}
