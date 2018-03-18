package pl.org.edk.menu;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

import pl.org.edk.BootStrap;
import pl.org.edk.BuildConfig;
import pl.org.edk.MainActivity;
import pl.org.edk.R;
import pl.org.edk.Settings;
import pl.org.edk.TempSettings;
import pl.org.edk.database.DbManager;
import pl.org.edk.services.GPSService;
import pl.org.edk.util.DialogUtil;
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

    private static final int SHOW_MAP_HOURS_FROM_START = 24;

    // ---------------------------------------
	// Protected methods
	// ---------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main2);

        if (TempSettings.get(this).isUserOnTrack()) {
            if(Settings.get(this).getBoolean(Settings.IS_BACKGROUND_TRACKING_ON)) {
                startService(new Intent(this, GPSService.class));
            }
            long mStartTime = TempSettings.get(this).getLong(TempSettings.START_TIME, System.currentTimeMillis());
            if (System.currentTimeMillis() - mStartTime < SHOW_MAP_HOURS_FROM_START * 3600000) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
        } else {
			TempSettings.clear(this);
		}

		// Initialize application global stuff (singletons etc.)
		BootStrap.initialize(getApplicationContext());
		resetDbOnFirstRun();

		initUI();
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

	private void openSettings(){
		Intent i = new Intent(MainMenuActivity.this, SettingsActivity.class);
		MainMenuActivity.this.startActivity(i);
	}

    private void resetDbOnFirstRun() {

        int version = Settings.get(this).getInt(Settings.VERSION_CODE);
        if (version == BuildConfig.VERSION_CODE){
            return;
        }
        Settings.get(this).set(Settings.VERSION_CODE, BuildConfig.VERSION_CODE);
        DbManager.getInstance(this).reset();

    }
}
