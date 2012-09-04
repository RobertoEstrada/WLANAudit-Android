package es.glasspixel.wlanaudit.activities;

public class SavedKey {

	private String wlan_name, key;

	public SavedKey(String wlan_name, String key) {
		this.wlan_name = wlan_name;
		this.key = key;
	}

	public String getWlan_name() {
		return wlan_name;
	}

	public void setWlan_name(String wlan_name) {
		this.wlan_name = wlan_name;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
