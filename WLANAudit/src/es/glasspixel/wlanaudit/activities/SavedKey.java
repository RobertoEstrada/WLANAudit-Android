package es.glasspixel.wlanaudit.activities;

public class SavedKey {

	private String wlan_name, key;
	private float latitude, longitude;

	public SavedKey(String wlan_name, String key, float latitude,
			float longitude) {
		this.wlan_name = wlan_name;
		this.key = key;
		this.latitude = latitude;
		this.longitude = longitude;
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

}
