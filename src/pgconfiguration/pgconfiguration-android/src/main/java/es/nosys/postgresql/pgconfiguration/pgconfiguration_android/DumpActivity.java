package es.nosys.postgresql.pgconfiguration.pgconfiguration_android;

import android.os.Bundle;
import android.widget.TextView;

public class DumpActivity extends ApiActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dump);

		callApi(ApiCall.CALL_DUMP, false, new OnApiCallReturn() {
			public void onApiCallReturn(Object result) {
				if (result != null) {
					TextView textView = (TextView) findViewById(R.id.config_text);
					textView.setText((String) result);
				} else {
					finish();
				}
			}
		});
	}

}
