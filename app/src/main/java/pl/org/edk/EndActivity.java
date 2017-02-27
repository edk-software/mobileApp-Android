package pl.org.edk;

import pl.org.edk.kml.TrackerProvider;
import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.Locale;

public class EndActivity extends Activity {

	LinearLayout endLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_end);
		endLayout = (LinearLayout) findViewById(R.id.endLayout);
		if (Locale.getDefault().getLanguage().contains("en")) {
			endLayout.setBackgroundResource(R.drawable.end_screen_en);
		} else {
			endLayout.setBackgroundResource(R.drawable.end_screen);
		}
		TrackerProvider.dismiss();
		TempSettings.clear(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Toast.makeText(this, R.string.instruction_how_to_end_app, Toast.LENGTH_LONG).show();
	}

}
