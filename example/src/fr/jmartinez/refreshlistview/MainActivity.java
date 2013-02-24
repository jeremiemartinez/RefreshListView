package fr.jmartinez.refreshlistview;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import fr.jmartinez.refreshlistview.RefreshListView.OnRefreshListener;

public class MainActivity extends Activity {

	private static String TAG = "RefreshListView";

	private List<String> apis;
	private RefreshListView list;
	private ArrayAdapter<String> adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		apis = getApisList();

		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, apis);

		list = (RefreshListView) findViewById(R.id.list);
		list.setAdapter(adapter);

		list.setRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh(RefreshListView listView) {
				new DataRetriever().execute();

			}
		});

		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Toast.makeText(MainActivity.this, "Position: " + position, Toast.LENGTH_SHORT).show();
			}

		});
	}

	private class DataRetriever extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				Log.e(TAG, "Something went wrong with making the thread sleep...");
			}

			return "New API 18 - Jeremie Martinez";
		}

		@Override
		protected void onPostExecute(String newApi) {
			apis.add(0, newApi);
			adapter.notifyDataSetChanged();
			list.finishRefreshing();
		}
	}

	private List<String> getApisList() {
		List<String> apis = new ArrayList<String>();
		apis.add("Android 1.5 Cupcake (API level 3)");
		apis.add("Android 1.6 Donut (API level 4)");
		apis.add("Android 2.0 Eclair (API level 5)");
		apis.add("Android 2.0.1 Eclair (API level 6)");
		apis.add("Android 2.1 Eclair (API level 7)");
		apis.add("Android 2.2Ð2.2.3 Froyo (API level 8)");
		apis.add("Android 2.3Ð2.3.2 Gingerbread (API level 9)");
		apis.add("Android 2.3.3Ð2.3.7 Gingerbread (API level 10)");
		apis.add("Android 3.0 Honeycomb (API level 11)");
		apis.add("Android 3.1 Honeycomb (API level 12)");
		apis.add("Android 3.2 Honeycomb (API level 13)");
		apis.add("Android 4.0Ð4.0.2 Ice Cream Sandwich (API level 14)");
		apis.add("Android 4.0.3Ð4.0.4 Ice Cream Sandwich (API level 15)");
		apis.add("Android 4.1 Jelly Bean (API level 16)");
		apis.add("Android 4.2 Jelly Bean (API level 17)");
		return apis;
	}
}
