package pl.org.edk.menu;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
    private boolean mWarningDialogShown;

    private static final int SHOW_MAP_HOURS_FROM_START = 24;

    private static final int ACCESS_GPS_AND_WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 3;


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

    @Override
    protected void onStart() {
        super.onStart();
        checkPermissions();
    }

    private void checkPermissions() {
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION};
        if (areAllGranted(permissions)) {
            return;
        }
        if (mWarningDialogShown){
            finish();
            return;
        }
        ActivityCompat.requestPermissions(this, permissions, ACCESS_GPS_AND_WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
    }

    private boolean areAllGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != ACCESS_GPS_AND_WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            Log.w(getClass().getSimpleName(), "Unrecognized request code");
            return;
        }
        if(grantResults.length != 0){
            Settings.CAN_USE_STORAGE = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            Settings.CAN_USE_GPS = grantResults[1] == PackageManager.PERMISSION_GRANTED;
        }
        //Toast.makeText(getApplicationContext(),"MainMenuAct can use storage: "+Settings.CAN_USE_STORAGE,Toast.LENGTH_SHORT).show();
        if (anyDenied(grantResults)){
            if (mWarningDialogShown){
                finish();
                return;
            }
            /*DialogUtil.showDialog(getString(R.string.no_permission_title), getString(R.string.no_permission_message), this, true, new DialogUtil.OnCloseEventListener() {
                @Override
                public void onClose() {
                    startAppSettingsActivity();
                }
            });*/
            DialogUtil.showYesNoDialog(R.string.no_permission_title, R.string.no_permission_message_yesno, this, new DialogUtil.OnSelectedEventListener() {
                @Override
                public void onAccepted() {
                    startAppSettingsActivity();
                }

                @Override
                public void onRejected() {
                    //no action for now
                }
            });
            mWarningDialogShown = true;
        }
    }

    private void startAppSettingsActivity() {
        Intent intent = new Intent();
        intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", MainMenuActivity.this.getPackageName(), null);
        intent.setData(uri);
        MainMenuActivity.this.startActivity(intent);
    }

    private boolean anyDenied(int[] grantResults) {
        if (grantResults.length == 0){
            return true;
        }
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED){
                return true;
            }
        }

        return false;
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
