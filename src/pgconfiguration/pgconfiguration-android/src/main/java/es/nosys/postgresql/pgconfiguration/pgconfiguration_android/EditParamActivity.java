package es.nosys.postgresql.pgconfiguration.pgconfiguration_android;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import es.nosys.postgresql.pgconfiguration.pgconfiguration_android.model.Param;
import es.nosys.postgresql.pgconfiguration.pgconfiguration_android.R;

public class EditParamActivity extends ApiActivity {

	public static final String PARAM = "param";

	private Param mParam;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_param);

		Intent intent = getIntent();
		mParam = (Param) intent.getExtras().get(PARAM);

		getParam();

		Button saveButton = (Button) findViewById(R.id.save_button);
		saveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				callApi(ApiCall.CALL_PARAM, mParam.getParam(),
						mParam.getValue(), true, new OnApiCallReturn() {
							public void onApiCallReturn(Object result) {
								if (result != null) {
									Toast.makeText(
											EditParamActivity.this,
											getResources().getString(
													R.string.saved),
											Toast.LENGTH_SHORT).show();
								}
							}
						});
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.edit_param, menu);
		return true;
	}

	protected void getParam() {
		callApi(ApiCall.CALL_PARAM, mParam.getParam(), true,
				new OnApiCallReturn() {
					public void onApiCallReturn(Object result) {
						if (result != null) {
							Param param = (Param) result;
							setParam(param);
						} else {
							finish();
						}
					}
				});
	}

	protected void setParam(Param param) {
		ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
		progressBar.setVisibility(View.INVISIBLE);
		TextView categoryLabel = (TextView) findViewById(R.id.category_label);
		categoryLabel.setVisibility(View.VISIBLE);
		TextView categoryView = (TextView) findViewById(R.id.category_text);
		categoryView.setVisibility(View.VISIBLE);
		categoryView.setText(param.getCategory());
		TextView contextLabel = (TextView) findViewById(R.id.context_label);
		contextLabel.setVisibility(View.VISIBLE);
		TextView contextView = (TextView) findViewById(R.id.context_text);
		contextView.setVisibility(View.VISIBLE);
		contextView.setText(param.getContext().name());
		TextView parameterLabel = (TextView) findViewById(R.id.parameter_label);
		parameterLabel.setVisibility(View.VISIBLE);
		TextView parameterView = (TextView) findViewById(R.id.parameter_text);
		parameterView.setVisibility(View.VISIBLE);
		parameterView
				.setText(param.getParam()
						+ " ("
						+ param.getDefaultvalue()
						+ (param.getMaxval() == null ? "" : "["
								+ param.getMinval() + ":" + param.getMaxval()
								+ "]")
						+ ")"
						+ (param.getUnit() == null ? "" : " ["
								+ param.getUnit() + "]"));

		View valueView = null;
		boolean isNumber = false;
		switch (param.getVartype()) {
		case BOOLEAN:
			Switch booleanSwitch = new Switch(this);
			booleanSwitch.setChecked(Boolean.valueOf(param.getValue()));
			booleanSwitch
					.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							mParam.setValue(String.valueOf(isChecked));
						}
					});
			valueView = booleanSwitch;
			break;
		case ENUM:
			Spinner spinner = new Spinner(this);
			ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
					this, android.R.layout.simple_list_item_1,
					param.getEnumvalues());
			int currentPos = param.getEnumvalues().indexOf(param.getValue());
			spinner.setSelection(currentPos);
			spinnerArrayAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> parent, View view,
						int pos, long id) {
					mParam.setValue(mParam.getEnumvalues().get(pos));
				}

				public void onNothingSelected(AdapterView<?> parent) {
				}
			});
			spinner.setAdapter(spinnerArrayAdapter);
			valueView = spinner;
			break;
		case INTEGER:
		case REAL:
			isNumber = true;
		case STRING:
		default:
			EditText editText = new EditText(this);
			editText.setId(0);
			editText.setText(param.getValue());
			editText.addTextChangedListener(new TextWatcher() {
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					mParam.setValue(s.toString());
				}

				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				public void afterTextChanged(Editable s) {
				}
			});
			if (isNumber) {
				editText.setInputType(InputType.TYPE_CLASS_NUMBER
						| InputType.TYPE_NUMBER_FLAG_DECIMAL
						| InputType.TYPE_NUMBER_FLAG_SIGNED);
			}
			valueView = editText;
			break;
		}
		ViewGroup valueLayout = (ViewGroup) findViewById(R.id.value_layout);
		valueLayout.setVisibility(View.VISIBLE);
		valueLayout.addView(valueView);
		TextView descriptionLabel = (TextView) findViewById(R.id.description_label);
		descriptionLabel.setVisibility(View.VISIBLE);
		TextView descriptionView = (TextView) findViewById(R.id.description_text);
		descriptionView.setVisibility(View.VISIBLE);
		descriptionView.setText(param.getDescription());
		TextView extraView = (TextView) findViewById(R.id.extra_text);
		extraView.setVisibility(View.VISIBLE);
		extraView.setText(param.getExtra());

		mParam = param;
	}

}
