package es.glasspixel.wlanaudit.activities;

import java.util.List;

public class SavedKey {

	private String wlan_name, address;
	private List<String> keys;
	private float latitude, longitude;

	public SavedKey(String wlan_name, String wlan_address, List<String> keys,
			float latitude, float longitude) {
		this.address = wlan_address;
		this.wlan_name = wlan_name;
		this.setKeys(keys);
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public String getWlan_name() {
		return wlan_name;
	}

	public void setWlan_name(String wlan_name) {
		this.wlan_name = wlan_name;
	}

	public float getLatitude() {
		return latitude;
	}

	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}

	public float getLongitude() {
		return longitude;
	}

	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}

	public List<String> getKeys() {
		return keys;
	}

	public void setKeys(List<String> keys) {
		this.keys = keys;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

}
