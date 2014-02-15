package es.nosys.postgresql.pgconfiguration.pgconfiguration_android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

public abstract class ApiActivity extends Activity implements
		OnSharedPreferenceChangeListener {

	public Map<String, List<ApiCallTask>> mApiCallBackMap = Collections
			.synchronizedMap(new HashMap<String, List<ApiCallTask>>());

	protected boolean mIsServerOnline = false;

	protected String mFilename = "postgresql.conf";
	protected String mServer = "http://192.168.1.105:8543";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		updateConfig();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		updateConfig();
	}

	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		updateConfig();
	}

	protected void updateConfig() {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		String server = sharedPreferences.getString(
				SettingsActivity.PREF_SERVER,
				getResources().getString(R.string.default_pref_server));
		String filename = sharedPreferences.getString(
				SettingsActivity.PREF_FILENAME,
				getResources().getString(R.string.default_pref_filename));

		mServer = server;
		mFilename = filename;

		removeRunningCalls();
	}

	public boolean oneRunningApiCallBacks(String operation) {
		return mApiCallBackMap.get(operation).size() == 1;
	}

	protected void checkServerOnline() {
		callApi(ApiCall.CALL_CHECK, true, new OnApiCallReturn() {
			public void onApiCallReturn(Object result) {
				mIsServerOnline = result != null;
			}
		});
	}

	protected void removeRunningCalls() {
		for (List<ApiCallTask> apiCallTaskList : mApiCallBackMap.values()) {
			for (ApiCallTask previousApiCallTask : apiCallTaskList) {
				previousApiCallTask.cancel(true);
			}
			apiCallTaskList.clear();
		}
	}

	protected void callApi(String operation, boolean removePreiovusCalls,
			OnApiCallReturn onApiCallReturn) {
		callApi(operation, null, removePreiovusCalls, onApiCallReturn);
	}

	protected void callApi(String operation, String parameter,
			boolean removePreiovusCalls, OnApiCallReturn onApiCallReturn) {
		callApi(operation, parameter, null, removePreiovusCalls,
				onApiCallReturn);
	}

	protected void callApi(String operation, String parameter, String value,
			boolean removePreiovusCalls, OnApiCallReturn onApiCallReturn) {
		callApi(operation, parameter, value, removePreiovusCalls,
				onApiCallReturn, null);
	}

	private synchronized void callApi(final String operation,
			final String parameter, final String value,
			final boolean removePreiovusCalls,
			final OnApiCallReturn onApiCallReturn,
			OnApiCallReturn finalOnApiCallReturn) {
		final String apiCallHash = operation;
		final ApiCallTask apiCallTask = new ApiCallTask();

		List<ApiCallTask> apiCallTaskList = mApiCallBackMap.get(apiCallHash);
		if (apiCallTaskList == null) {
			apiCallTaskList = Collections
					.synchronizedList(new ArrayList<ApiCallTask>());
			mApiCallBackMap.put(apiCallHash, apiCallTaskList);
		}

		if (removePreiovusCalls) {
			for (ApiCallTask previousApiCallTask : apiCallTaskList) {
				previousApiCallTask.cancel(true);
			}
			apiCallTaskList.clear();
		}

		apiCallTaskList.add(apiCallTask);

		if (finalOnApiCallReturn != null) {
			apiCallTask.addOnApiCallReturn(finalOnApiCallReturn);
		} else {
			apiCallTask.addOnApiCallReturn(new OnApiCallReturn() {
				private int mRetry = 0;

				public void onApiCallReturn(Object result) {
					try {
						if (result instanceof RetryException && mRetry < 3) {
							Toast.makeText(
									ApiActivity.this,
									getResources().getString(R.string.retry)
											+ " " + mRetry, Toast.LENGTH_SHORT)
									.show();

							mRetry++;

							callApi(operation, parameter, value, false,
									onApiCallReturn, this);

							return;
						}

						if (result instanceof Exception) {
							Toast.makeText(
									ApiActivity.this,
									getResources()
											.getString(R.string.api_error),
									Toast.LENGTH_LONG).show();

							result = null;
						}

						onApiCallReturn.onApiCallReturn(result);
					} finally {
						mApiCallBackMap.get(apiCallHash).remove(apiCallTask);
					}
				}
			});
		}

		apiCallTask.execute(mServer, mFilename, operation, parameter, value);
	}

}
