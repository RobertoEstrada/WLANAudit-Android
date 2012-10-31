package es.glasspixel.wlanaudit.dominio;

import android.content.Context;

public class DataUtils {

	private static DataUtils instance;
	private Context mContext;
	private String savedkeyselected;

	public DataUtils(Context context) {
		mContext = context;

	}

	public static DataUtils getInstance(Context context) {
		if (instance == null) {
			instance = new DataUtils(context);
		}
		return instance;

	}
	
	
	public String getSavedkeyselected() {
		return savedkeyselected;
	}

	public void setSavedkeyselected(String savedkeyselected) {
		this.savedkeyselected = savedkeyselected;
	}
	
	

}
