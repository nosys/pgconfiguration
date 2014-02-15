package es.nosys.postgresql.pgconfiguration.pgconfiguration_android;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.net.Uri;
import es.nosys.postgresql.pgconfiguration.pgconfiguration_android.model.Param;
import es.nosys.postgresql.pgconfiguration.model.Param.Context;
import es.nosys.postgresql.pgconfiguration.model.Param.Vartype;

public class ApiCall {

	public static final String CALL_CHECK = "check";
	public static final String CALL_CATEGORIES = "categories";
	public static final String CALL_PARAMS = "params";
	public static final String CALL_SEARCH = "search";
	public static final String CALL_PARAM = "param";
	public static final String CALL_DUMP = "dump";
	public static final String CALL_SAVE = "save";
	
	private volatile static ApiCall instance;
	private static final Object lock = new Object();
	
	public static ApiCall getApiCall() {
		if (instance == null) {
			synchronized (lock) {
				initialize();
			}
		}
		
		return instance;
	}

	private static void initialize() {
		if (instance == null) {
			instance = new ApiCall();
		}
	}
	
	public static Object apiCall(String host,
			String filename, String operation, String parameter, String value) throws Exception {
		return getApiCall().internalApiCall(host, filename, operation, parameter, value);
	}
	
	public Object internalApiCall(String host, String filename, 
			String operation, String parameter, String value)
			throws Exception {
		
		boolean isPut = false;
		StringBuilder uriBuilder = new StringBuilder(host);
		uriBuilder.append("/ws");
		if (!CALL_CHECK.equals(operation)) {
			uriBuilder.append("/");
			uriBuilder.append(filename);
			uriBuilder.append("/");
			uriBuilder.append(operation);
			if (parameter != null) {
				uriBuilder.append("/");
				uriBuilder.append(Uri.encode(parameter));
				
				if (value != null) {
					isPut = true;
				}
			}
		}

		try {
			HttpClient httpclient = new DefaultHttpClient();
					
			HttpUriRequest httpOperation = null;
			if (isPut) {
				httpOperation = new HttpPut(uriBuilder.toString());
				httpOperation.setHeader("Content-Type", "application/x-www-form-urlencoded");
				StringEntity stringEntity = new StringEntity(value);
				((HttpPut) httpOperation).setEntity(stringEntity);
			} else {
				httpOperation = new HttpGet(uriBuilder.toString());
			}
			
			//httpOperation.setHeader("Accept", "application/json");
			HttpResponse httpResponse = httpclient.execute(httpOperation);
			Object jsonResponseObject = null;
			int code = httpResponse.getStatusLine().getStatusCode();
			
			if (HttpStatus.SC_OK == code || HttpStatus.SC_NO_CONTENT == code) {
				if (HttpStatus.SC_OK == code) {
					String response = EntityUtils.toString(httpResponse.getEntity());
					if (CALL_DUMP.equals(operation)) {
						jsonResponseObject = response;
					} else {
						jsonResponseObject = new JSONTokener(response)
							.nextValue();
					}
				}
		
				if (CALL_CHECK.equals(operation)) {
					JSONArray jsonArray = (JSONArray) jsonResponseObject;
					List<String> responseList = new ArrayList<String>();
					if (jsonArray != null) {
						for (int index = 0; index < jsonArray.length(); index++) {
							String token = jsonArray.getString(index);
							responseList.add(token);
						}
					}
					jsonResponseObject = responseList;
				} else if (CALL_CATEGORIES.equals(operation)) {
					JSONArray jsonArray = (JSONArray) jsonResponseObject;
					List<Param> responseList = new ArrayList<Param>();
					if (jsonArray != null) {
						for (int index = 0; index < jsonArray.length(); index++) {
							String paramCategory = jsonArray.getString(index);
							Param param = new Param();
							param.setCategory(paramCategory);
							
							responseList.add(param);
						}
					}
					jsonResponseObject = responseList;
				} else if (CALL_PARAMS.equals(operation) || CALL_SEARCH.equals(operation)) {
					JSONArray jsonArray = (JSONArray) jsonResponseObject;
					List<Param> responseList = new ArrayList<Param>();
					if (jsonArray != null) {
						for (int index = 0; index < jsonArray.length(); index++) {
							Param param = null;
							JSONObject jsonObject = jsonArray.optJSONObject(index);
							if (jsonObject != null) {
								param = extractParam(jsonObject);
							} else {
								String paramName = jsonArray.getString(index);
								param = new Param();
								param.setParam(paramName);
							}
							
							responseList.add(param);
						}
					}
					jsonResponseObject = responseList;
				} else if (CALL_PARAM.equals(operation)) {
					JSONObject jsonObject = (JSONObject) jsonResponseObject;
		
					if (jsonObject != null) {
						Param param = extractParam(jsonObject);
						
						jsonResponseObject = param;
					}
				}
			} else {
				HttpException httpException = new HttpException(
						httpResponse.getStatusLine().getReasonPhrase());
				throw httpException;
			}
	
			return jsonResponseObject;
		} catch(Exception exception) {
//			if (exception instanceof HttpHostConnectException) {
//				exception = new RetryException(exception);
//			} else {
				exception = new Exception("Error calling " + uriBuilder.toString() + " [" + value + "]", exception);
//			}

			throw exception;
		}
	}

	protected Param extractParam(JSONObject jsonObject) throws JSONException {
		Param param = new Param();
		param.setCategory(jsonObject.getString("category"));
		param.setContext(
				Context.valueOf(jsonObject.getString("context")));
		param.setParam(jsonObject.getString("param"));
		param.setVartype(
				Vartype.valueOf(jsonObject.getString("vartype")));
		if (jsonObject.has("value")) {
			param.setValue(jsonObject.getString("value"));
		}
		if (jsonObject.has("defaultvalue")) {
			param.setDefaultvalue(jsonObject.getString("defaultvalue"));
		}
		param.setDescription(jsonObject.getString("description"));
		if (jsonObject.has("unit")) {
			param.setUnit(jsonObject.getString("unit"));
		}
		if (jsonObject.has("minval")) {
			param.setMinval(jsonObject.getString("minval"));
		}
		if (jsonObject.has("maxval")) {
			param.setMinval(jsonObject.getString("maxval"));
		}
		if (jsonObject.has("enumvalues")) {
			JSONArray jsonArray = jsonObject.getJSONArray("enumvalues");
			List<String> enumvalues = new ArrayList<String>();
			for (int index = 0; index < jsonArray.length(); index++) {
				enumvalues.add(jsonArray.getString(index));
			}
			param.setEnumvalues(enumvalues);
		}
		if (jsonObject.has("extra")) {
			param.setExtra(jsonObject.getString("extra"));
		}
		return param;
	}

	public static String slurp(final InputStream is) throws Exception {
		final char[] buffer = new char[4196];
		final StringBuilder out = new StringBuilder();
		final Reader in = new InputStreamReader(is, "UTF-8");
		try {
			for (;;) {
				int rsz = in.read(buffer, 0, buffer.length);
				if (rsz < 0)
					break;
				out.append(buffer, 0, rsz);
			}
		} finally {
			in.close();
		}

		return out.toString();
	}

}
