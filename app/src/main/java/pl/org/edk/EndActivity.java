package pl.org.edk;

import pl.org.edk.kml.TrackerProvider;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class EndActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_end);
		TrackerProvider.dismiss();
		TempSettings.clear(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Toast.makeText(this, R.string.instruction_how_to_end_app, Toast.LENGTH_LONG).show();
	}

}
