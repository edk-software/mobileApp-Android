package pl.org.edk.menu;

import pl.org.edk.*;
import pl.org.edk.database.DbManager;
import pl.org.edk.database.entities.Route;
import pl.org.edk.managers.WebServiceManager;
import pl.org.edk.services.GPSService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import pl.org.edk.util.DialogUtil;
import pl.org.edk.webServices.FileDownloader;

import java.util.concurrent.ExecutionException;

public class MainMenuActivity extends Activity {
	// ---------------------------------------
	// Subclasses
	// ---------------------------------------
	private final class TrackButtonListener implements OnClickListener {
		public void onClick(View v) {
			Intent i = new Intent(MainMenuActivity.this, TerritoryChooserActivity.class);
			MainMenuActivity.this.startActivity(i);
		}
	}

	private final class ReflectionsButtonListener implements OnClickListener {
		public void onClick(View v) {
			Intent i = new Intent(MainMenuActivity.this, ReflectionsActivity.class);
			MainMenuActivity.this.startActivity(i);
		}
	}

	private final class SettingsButtonListener implements OnClickListener {
		public void onClick(View v) {
			openSettings();
		}
	}

	private final class InfoButtonListener implements OnClickListener {
		public void onClick(View v) {
			Intent i = new Intent(MainMenuActivity.this, EDKInfoActivity.class);
			MainMenuActivity.this.startActivity(i);
		}
	}

	// ---------------------------------------
	// Members
	// ---------------------------------------
	private Button tracksButton;
	private Button considerationsButton;
	private Button settingsButton;
	private Button infoButton;
	private ImageButton tracksImageButton;
	private ImageButton considerationsImageButton;
	private ImageButton settingsImageButton;
	private ImageButton infoImageButton;

	// ---------------------------------------
	// Protected methods
	// ---------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main2);

        if (TempSettings.get(this).isUserOnTrack() && Settings.get(this).getBoolean(Settings.IS_BACKGROUND_TRACKING_ON)) {
            //TODO ask the user whether to go to map or sth
//			Intent serviceIntent = new Intent(this, GPSService.class);
//			startService(serviceIntent);
//			Intent intent = new Intent(this, MainActivity.class);
//			startActivity(intent);
		} else {
			TempSettings.clear(this);
		}

		// Initialize application global stuff (singletons etc.)
		BootStrap.initialize(getApplicationContext());

		initUI();

		checkDB();
	}

	// ---------------------------------------
	// Private methods
	// ---------------------------------------
	private void initUI(){
		tracksButton = (Button) findViewById(R.id.tracksButton);
		considerationsButton = (Button) findViewById(R.id.considerationsButton);
		settingsButton = (Button) findViewById(R.id.settingsButton);
		infoButton = (Button) findViewById(R.id.infoButton);

		tracksImageButton = (ImageButton) findViewById(R.id.tracksImageButton);
		considerationsImageButton = (ImageButton) findViewById(R.id.considerationsImageButton);
		settingsImageButton = (ImageButton) findViewById(R.id.settingsImageButton);
		infoImageButton = (ImageButton) findViewById(R.id.infoImageButton);

		tracksButton.setOnClickListener(new TrackButtonListener());
		tracksImageButton.setOnClickListener(new TrackButtonListener());

		considerationsButton.setOnClickListener(new ReflectionsButtonListener());
		considerationsImageButton.setOnClickListener(new ReflectionsButtonListener());

		settingsButton.setOnClickListener(new SettingsButtonListener());
		settingsImageButton.setOnClickListener(new SettingsButtonListener());

		infoButton.setOnClickListener(new InfoButtonListener());
		infoImageButton.setOnClickListener(new InfoButtonListener());
	}

	private void checkDB(){
		if(DbManager.getInstance(this).getTerritoryService().getTerritories().size() == 0){
			DialogUtil.showYesNoDialog(getString(R.string.main_menu_get_data_title),
					getString(R.string.main_menu_get_data_message),
					this, new DialogUtil.OnSelectedEventListener() {
				@Override
				public void onAccepted() {
					openSettings();
				}

				@Override
				public void onRejected() { /* Just proceed */ }
			});
		}
	}

	private void openSettings(){
		Intent i = new Intent(MainMenuActivity.this, SettingsActivity.class);
		MainMenuActivity.this.startActivity(i);
	}
}
