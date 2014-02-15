package es.nosys.postgresql.pgconfiguration.pgconfiguration_android;

import java.util.HashSet;
import java.util.Set;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity 
	implements SharedPreferences.OnSharedPreferenceChangeListener {

	public static final String PREF_SERVER = "pref_server";
	public static final String PREF_ADD_SERVER = "pref_add_server";
	public static final String PREF_CHOSE_SERVER = "pref_chose_server";
	public static final String PREF_FILENAME = "pref_filename";

	@Override
	@SuppressWarnings("deprecation")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		SharedPreferences sharedPreferences = 
				getSharedPreferences(PREF_ADD_SERVER, MODE_PRIVATE);
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (PREF_ADD_SERVER.equals(key)) {
			String server = sharedPreferences.getString(PREF_ADD_SERVER, null);
			Set<String> serverSet = sharedPreferences.getStringSet(
					PREF_CHOSE_SERVER, null);
			if (serverSet == null) {
				serverSet = new HashSet<String>();
			} else {
				serverSet = new HashSet<String>(serverSet);
			}
			serverSet.add(server);
			
			Editor editor = sharedPreferences.edit();
			editor.putStringSet(PREF_CHOSE_SERVER, serverSet);
			editor.commit();
		}
	}

}
