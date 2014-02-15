package es.nosys.postgresql.pgconfiguration.pgconfiguration_android;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;

public class ApiCallTask extends AsyncTask<String, Integer, Object> {

	private List<OnApiCallReturn> mOnApiCallReturnList;

	public ApiCallTask() {
		mOnApiCallReturnList = new ArrayList<OnApiCallReturn>();
	}

	public void addOnApiCallReturn(OnApiCallReturn onApiCallReturn) {
		mOnApiCallReturnList.add(onApiCallReturn);
	}

	@Override
	protected Object doInBackground(String... params) {
		try {
			return apiCall((String) params[0], (String) params[1],
					(String) params[2], (String) params[3], (String) params[4]);
		} catch (Exception exception) {
			Log.e(ApiCallTask.class.getSimpleName(), "Api call error",
					exception);
			return exception;
		}
	}

	protected Object apiCall(String host, String filename, String operation,
			String parameter, String value) throws Exception {
		Object result = ApiCall.apiCall(host, filename, operation, parameter,
				value);

		return result;
	}

	@Override
	protected void onPostExecute(Object result) {
		for (OnApiCallReturn onApiCallReturn : mOnApiCallReturnList) {
			onApiCallReturn.onApiCallReturn(result);
		}

		super.onPostExecute(result);
	}

}
