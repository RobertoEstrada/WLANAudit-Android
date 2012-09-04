package es.glasspixel.wlanaudit.fragments;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.MultiChoiceModeListener;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockListFragment;

import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.activities.NetworkDetailsActivity;
import es.glasspixel.wlanaudit.activities.NetworkListActivity;
import es.glasspixel.wlanaudit.activities.SavedKey;
import es.glasspixel.wlanaudit.adapters.KeysSavedAdapter;
import es.glasspixel.wlanaudit.database.KeysSQliteHelper;

public class SavedKeysFragment extends SherlockFragment {

	View myFragmentView;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Intent launchingIntent = getActivity().getIntent();
		// String content = launchingIntent.getData().toString();
		myFragmentView = inflater.inflate(R.layout.saved_keys_fragment,
				container, false);
		((ListView) myFragmentView.findViewById(R.id.listView1))
				.setAdapter(new KeysSavedAdapter(getActivity(),
						R.layout.network_list_element_layout,
						android.R.layout.simple_list_item_1, getSavedKeys()));
		((ListView) myFragmentView.findViewById(R.id.listView1))
				.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		((ListView) myFragmentView.findViewById(R.id.listView1))
				.setMultiChoiceModeListener(new MultiChoiceModeListener() {

					@TargetApi(Build.VERSION_CODES.HONEYCOMB)
					@Override
					public boolean onActionItemClicked(ActionMode mode,
							android.view.MenuItem item) {

						switch (item.getItemId()) {
						case R.id.delete_context_menu:
							SparseBooleanArray i = ((ListView) myFragmentView
									.findViewById(R.id.listView1))
									.getCheckedItemPositions();
							KeysSQliteHelper usdbh = new KeysSQliteHelper(
									getActivity(), "DBKeys", null, 1);
							SQLiteDatabase db = usdbh.getWritableDatabase();
							for (int j = 0; j < ((ListView) myFragmentView
									.findViewById(R.id.listView1)).getCount(); j++) {
								if (i.get(j) == true) {

									String nombre_wlan, clave;
									nombre_wlan = ((SavedKey) ((ListView) myFragmentView
											.findViewById(R.id.listView1))
											.getAdapter().getItem(j))
											.getWlan_name();
									clave = ((SavedKey) ((ListView) myFragmentView
											.findViewById(R.id.listView1))
											.getAdapter().getItem(j)).getKey();
									db.delete("keys",
											"nombre like ? AND key like ?",
											new String[] { nombre_wlan, clave });
								}

							}

							mode.finish();
							((ListView) myFragmentView
									.findViewById(R.id.listView1))
									.setAdapter(new KeysSavedAdapter(
											getActivity(),
											R.layout.network_list_element_layout,
											android.R.layout.simple_list_item_1,
											getSavedKeys()));
							return true;
						case R.id.copy_context_menu:
							mode.finish();
							return true;
						default:
							return false;
						}
					}

					@TargetApi(Build.VERSION_CODES.HONEYCOMB)
					@Override
					public boolean onCreateActionMode(ActionMode mode,
							android.view.Menu menu) {
						android.view.MenuInflater inflater = mode
								.getMenuInflater();
						inflater.inflate(
								R.menu.saved_keys_elements_context_menu, menu);

						return true;
					}

					@TargetApi(Build.VERSION_CODES.HONEYCOMB)
					@Override
					public void onDestroyActionMode(ActionMode mode) {
						// TODO Auto-generated method stub

					}

					@TargetApi(Build.VERSION_CODES.HONEYCOMB)
					@Override
					public boolean onPrepareActionMode(ActionMode mode,
							android.view.Menu menu) {
						// TODO Auto-generated method stub
						return false;
					}

					@Override
					public void onItemCheckedStateChanged(ActionMode mode,
							int position, long id, boolean checked) {

					}
				});
		((ListView) myFragmentView.findViewById(R.id.listView1))
				.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						String mDefaultPassValue = ((TextView) arg1
								.findViewById(R.id.networkKey)).getText()
								.toString();

						int sdk = android.os.Build.VERSION.SDK_INT;
						if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
							android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity()
									.getSystemService(Context.CLIPBOARD_SERVICE);
							clipboard.setText(mDefaultPassValue);
						} else {
							android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity()
									.getSystemService(Context.CLIPBOARD_SERVICE);
							android.content.ClipData clip = android.content.ClipData
									.newPlainText("text label",
											mDefaultPassValue);
							clipboard.setPrimaryClip(clip);
						}
						Toast.makeText(
								getActivity(),
								getResources().getString(
										R.string.key_copy_success),
								Toast.LENGTH_SHORT).show();

					}
				});

		return myFragmentView;
	}

	protected List<SavedKey> getSavedKeys() {
		List<SavedKey> mKeys = new ArrayList<SavedKey>();
		KeysSQliteHelper usdbh = new KeysSQliteHelper(getActivity(), "DBKeys",
				null, 1);

		SQLiteDatabase db = usdbh.getReadableDatabase();
		Cursor c = db.query("Keys", new String[] { "nombre", "key" }, null,
				null, null, null, "nombre ASC");
		while (c.moveToNext()) {
			SavedKey k = new SavedKey(c.getString(c.getColumnIndex("nombre")),
					c.getString(c.getColumnIndex("key")));
			mKeys.add(k);
		}
		return mKeys;

	}

}
