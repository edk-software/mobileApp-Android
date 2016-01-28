package pl.org.edk.menu;

import pl.org.edk.*;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends ActivityWithGPSMenu {

	private final class InfoButtonListener implements OnClickListener {
		public void onClick(View v) {
			Intent i = new Intent(MainActivity.this, EDKInfoActivity.class);
			MainActivity.this.startActivity(i);
		}
	}

	private final class ConsiderationsButtonListener implements OnClickListener {
		public void onClick(View v) {
			Intent i = new Intent(MainActivity.this, ConsiderationsYearChooserActivity.class);
			MainActivity.this.startActivity(i);
		}
	}

	private final class TrackButtonListener implements OnClickListener {
		public void onClick(View v) {
			Intent i = new Intent(MainActivity.this, CountyChooserActivity.class);
			MainActivity.this.startActivity(i);
		}
	}

	private Button tracksButton;
	private Button considerationsButton;
	private Button infoButton;
	private ImageButton tracksImageButton;
	private ImageButton considerationsImageButton;
	private ImageButton infoImageButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main2);

		// Initialize application global stuff (singletons etc.)
		Bootstrap.Initialize(getApplicationContext());

		Settings settings = Settings.get(this);
		if (settings.getBoolean(Settings.IS_BACKGROUND_TRACKING_ON)) {
			Intent serviceIntent = new Intent(this, GPSService.class);
			startService(serviceIntent);
			Intent intent = new Intent(this, ActivityWithMap.class);
			startActivity(intent);
		} else {
			settings.clear();
		}

		tracksButton = (Button) findViewById(R.id.tracksButton);
		considerationsButton = (Button) findViewById(R.id.considerationsButton);
		infoButton = (Button) findViewById(R.id.infoButton);
		
		tracksImageButton = (ImageButton) findViewById(R.id.tracksImageButton);
		considerationsImageButton = (ImageButton) findViewById(R.id.considerationsImageButton);
		infoImageButton = (ImageButton) findViewById(R.id.infoImageButton);

		tracksButton.setOnClickListener(new TrackButtonListener());
		tracksImageButton.setOnClickListener(new TrackButtonListener());

		considerationsButton.setOnClickListener(new ConsiderationsButtonListener());
		considerationsImageButton.setOnClickListener(new ConsiderationsButtonListener());

		infoButton.setOnClickListener(new InfoButtonListener());
		infoImageButton.setOnClickListener(new InfoButtonListener());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.getItem(CONSIDERATIONS_MENU_INDEX).setVisible(false);
		menu.getItem(MAP_MENU_INDEX).setVisible(false);
		return true;
	}

}
