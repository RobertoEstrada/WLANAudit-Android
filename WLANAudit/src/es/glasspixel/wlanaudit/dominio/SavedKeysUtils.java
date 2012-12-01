package es.glasspixel.wlanaudit.dominio;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.ScanResult;
import android.widget.Toast;
import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.activities.SavedKey;
import es.glasspixel.wlanaudit.database.KeysSQliteHelper;
import es.glasspixel.wlanaudit.keyframework.IKeyCalculator;
import es.glasspixel.wlanaudit.keyframework.KeyCalculatorFactory;
import es.glasspixel.wlanaudit.keyframework.NetData;

public class SavedKeysUtils {

	public static List<SavedKey> loadSavedKeys(Context context) {
		ArrayList<SavedKey> mKeys = new ArrayList<SavedKey>();
		KeysSQliteHelper usdbh = new KeysSQliteHelper(context, "DBKeys", null,
				1);

		SQLiteDatabase db = usdbh.getReadableDatabase();
		Cursor c = db.query("Keys", new String[] { "nombre","address", "latitude",
				"longitude" }, null, null, null, null, "nombre ASC");
		// if (c.moveToFirst()) {
		while (c.moveToNext()) {

			String ssid, bssid;
			ssid = c.getString(c.getColumnIndex("nombre"));
			bssid = c.getString(c.getColumnIndex("address"));

			List<String> a = new ArrayList<String>();

			SavedKey k = new SavedKey(ssid, bssid, a, c.getFloat(c
					.getColumnIndex("latitude")), c.getFloat(c
					.getColumnIndex("longitude")));

			// Calculating key
			IKeyCalculator keyCalculator = KeyCalculatorFactory
					.getKeyCalculator(new NetData(ssid, bssid));
			if (keyCalculator != null) {
				k.setKeys(keyCalculator.getKey(new NetData(ssid, bssid)));
			}

			mKeys.add(k);

			// String name = c.getString(c.getColumnIndex("nombre"));
			// boolean nueva = true;
			// for (SavedKey s : mKeys) {
			// if (name.equals(s.getWlan_name())) {
			// s.getKeys().add(c.getString(c.getColumnIndex("key")));
			// nueva = false;
			// break;
			// }
			// }

		}
		// }
		c.close();
		return mKeys;

	}

	public static void saveWLANKey(Context context, ScanResult s,
			double latitude, double longitude) {
		KeysSQliteHelper usdbh = new KeysSQliteHelper(context, "DBKeys", null,
				1);

		SQLiteDatabase db = usdbh.getWritableDatabase();
		if (db != null) {
			Cursor c = db.query("Keys", new String[] { "address" },
					"address like ?", new String[] { s.BSSID }, null, null,
					"nombre ASC");
			if (c.getCount() > 0) {

			} else {

				try {
					db.execSQL("INSERT INTO Keys (nombre, address,latitude,longitude) "
							+ "VALUES ('"
							+ s.SSID

							+ "','"
							+ s.BSSID
							+ "','"
							+ latitude
							+ "', '"
							+ longitude + "')");

				} catch (SQLException e) {
					Toast.makeText(
							context.getApplicationContext(),
							context.getResources().getString(
									R.string.error_saving_key)
									+ " " + e.getMessage(), Toast.LENGTH_LONG)
							.show();
				}
				db.close();
			}
		}
		usdbh.close();

	}

	public static boolean existSavedNetwork(String bssid, Context context) {

		KeysSQliteHelper usdbh = new KeysSQliteHelper(context, "DBKeys", null,
				1);

		SQLiteDatabase db = usdbh.getReadableDatabase();
		Cursor c = db.query("Keys", new String[] { "address" },
				"address like ?", new String[] { bssid }, null, null, null);
		// if (c.moveToFirst()) {
		while (c.moveToNext()) {

			return true;

		}
		// }
		return false;
	}
}
