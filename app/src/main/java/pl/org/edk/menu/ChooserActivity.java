package pl.org.edk.menu;

import java.util.List;

import pl.org.edk.R;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public abstract class ChooserActivity extends Activity implements OnItemClickListener {

	private ListView mainListView;
	private ArrayAdapter<String> listAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.simple_item_list);
		setTitle(getChooserTitle());
		mainListView = (ListView) findViewById(R.id.mainListView);
		ActionBar actionBar = getActionBar();
		if (actionBar != null){
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		refresh(getItems());
	}

	protected void refresh(List<String> items){
		listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow, items);
		mainListView.setAdapter(listAdapter);
		mainListView.setOnItemClickListener(this);
	}

	protected abstract String getChooserTitle();

	protected abstract List<String> getItems();

	protected abstract void onItemClick(int pos);

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		onItemClick(position);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}