package es.nosys.postgresql.pgconfiguration.pgconfiguration_android;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import es.nosys.postgresql.pgconfiguration.pgconfiguration_android.model.Param;

public class MainActivity extends ApiActivity implements View.OnKeyListener {

	private ProgressBar mProgressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
		mProgressBar = progressBar;
		EditText searchText = (EditText) findViewById(R.id.search_text);
		searchText.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				searchParams(s.toString());
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void afterTextChanged(Editable s) {
			}
		});

		getParams();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		
		MenuItem actionSettings = menu.findItem(R.id.action_settings);
		actionSettings.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
				startActivity(intent);
				SharedPreferences sharedPreferences = PreferenceManager
						.getDefaultSharedPreferences(MainActivity.this);
				sharedPreferences.registerOnSharedPreferenceChangeListener(MainActivity.this);
				
				return true;
			}
		});
		MenuItem actionSave = menu.findItem(R.id.action_save);
		actionSave.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				callApi(ApiCall.CALL_SAVE, false, new OnApiCallReturn() {
					public void onApiCallReturn(Object result) {
						if (result != null) {
					    	Toast.makeText(MainActivity.this, 
					    			getResources().getString(R.string.saved), 
					    			Toast.LENGTH_SHORT).show();
						}
					}
				});
				
				return true;
			}
		});
		MenuItem actionDump = menu.findItem(R.id.action_dump);
		actionDump.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				Intent intent = new Intent(MainActivity.this, DumpActivity.class);
				startActivity(intent);
				
				return true;
			}
		});
		
		return true;
	}

	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			openContextMenu(v);
		}

		return false;
	}

	private void getParams() {
		searchParams(" ");
	}

	@SuppressWarnings("unchecked")
	private void searchParams(String query) {
		if (findViewById(R.id.progress_bar) == null) {
			LinearLayout searchLayout = (LinearLayout) findViewById(R.id.search_layout);
			searchLayout.removeAllViews();
			searchLayout.addView(mProgressBar);
		}

		if (query.isEmpty()) {
			query = " ";
		}
		
		final String finalQuery = query;
		
		callApi(ApiCall.CALL_SEARCH, finalQuery, true, new OnApiCallReturn() {
			public void onApiCallReturn(Object result) {
				if (result != null) {
					List<Param> paramList = (List<Param>) result;

					fillLayout(paramList);
				}

				if (oneRunningApiCallBacks(ApiCall.CALL_SEARCH)) {
					LinearLayout searchLayout = (LinearLayout) findViewById(R.id.search_layout);
					searchLayout.removeAllViews();

					if (result == null) {
						Button retryButton = new Button(MainActivity.this);
						retryButton.setBackgroundResource(R.drawable.menu_refresh);
						retryButton.setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								searchParams(finalQuery);
							}
						});
						searchLayout.addView(retryButton);
					}
				}
			}
		});
	}

	protected void fillLayout(final List<Param> paramList) {
		final Handler handler = new Handler();
		Thread thread = new Thread() {
			@Override
			public void run() {
				Collections.sort(paramList, new Comparator<Param>() {
					public int compare(Param lhs, Param rhs) {
						int compare = lhs.getCategory().compareTo(
								rhs.getCategory());

						if (compare == 0) {
							compare = lhs.getParam().compareTo(rhs.getParam());
						}

						return compare;
					}
				});

				ListIterator<Param> paramListIterator = paramList
						.listIterator();
				String lastCategory = "";
				while (paramListIterator.hasNext()) {
					Param param = paramListIterator.next();
					String category = param.getCategory();

					if (!category.equals(lastCategory)) {
						Param categoryParam = new Param();
						categoryParam.setCategory(category);

						paramListIterator.previous();
						paramListIterator.add(categoryParam);
						paramListIterator.next();

						lastCategory = category;
					}
				}

				Runnable runnable = new Runnable() {
					public void run() {
						writeLayout(paramList);
					}
				};
				handler.post(runnable);
			}
		};

		thread.start();
	}

	protected void writeLayout(final List<Param> paramList) {
		GridView gridView = (GridView) findViewById(R.id.category_layout);
		gridView.setAdapter(new BaseAdapter() {
			@Override
			public int getItemViewType(int position) {
				int type = 0;

				Param param = (Param) getItem(position);

				if (param.getParam() == null) {
					type = 1;
				}

				return type;
			}

			@Override
			public int getViewTypeCount() {
				return 2;
			}

			public View getView(int position, View convertView, ViewGroup parent) {
				Param param = (Param) getItem(position);

				if (getItemViewType(position) == 1) {
					TextView categoryLabel = null;

					if (convertView == null) {
						categoryLabel = new TextView(MainActivity.this);
					} else {
						categoryLabel = (TextView) convertView;
					}

					categoryLabel.setText(param.getCategory());
					convertView = categoryLabel;
				} else {
					Button paramView = null;

					if (convertView == null) {
						paramView = new Button(MainActivity.this);
						paramView
								.setOnClickListener(new View.OnClickListener() {
									public void onClick(View view) {
										Intent editParamIntent = new Intent(
												MainActivity.this,
												EditParamActivity.class);
										editParamIntent.putExtra(
												EditParamActivity.PARAM,
												(Serializable) view.getTag());
										startActivity(editParamIntent);
									}
								});
					} else {
						paramView = (Button) convertView;
					}

					paramView.setTag(param);
					paramView.setText(param.getParam());

					convertView = paramView;
				}

				return convertView;
			}

			public long getItemId(int position) {
				return position;
			}

			public Object getItem(int position) {
				return paramList.get(position);
			}

			public int getCount() {
				return paramList.size();
			}
		});
	}

}
