package pl.org.edk.menu;

import java.lang.reflect.Field;

import pl.org.edk.ActivityWithGPSMenu;
import pl.org.edk.Extra;
import pl.org.edk.R;
import pl.org.edk.Settings;
import pl.org.edk.util.NumConverter;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

public class ConsiderationsViewActivity extends ActivityWithGPSMenu {

	private int currentStationId;
	private TextView tv;
	private AudioButtonController audioController;
	private View left;
	private View right;
	private ScrollView scrollView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.considerations_view);

		tv = (TextView) findViewById(R.id.considerationsText);
		scrollView = (ScrollView) findViewById(R.id.scrollViewRecords);

		left = findViewById(R.id.left);
		left.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showPrevious(v);
			}
		});
		right = findViewById(R.id.right);
		right.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showNext(v);
			}
		});

		final Button button = (Button) findViewById(R.id.listen);
		audioController = new AudioButtonController(button, R.raw.stacja_1);
		if (Settings.get(this).getInt(Settings.YEAR_ID, 0) != 0) {
			button.setVisibility(View.GONE);
		}

		currentStationId = getIntent().getIntExtra(Extra.STATION_ID, 0);
		changeNavigationButtonsVisibility();
		initConsiderations();
	}

	private void changeNavigationButtonsVisibility() {
		if (currentStationId == 0) {
			left.setVisibility(View.INVISIBLE);
		} else {
			left.setVisibility(View.VISIBLE);
		}
		if (currentStationId == 15) {
			right.setVisibility(View.INVISIBLE);
		} else {
			right.setVisibility(View.VISIBLE);
		}
	}

	public void showPrevious(View v) {
		currentStationId--;
		changeNavigationButtonsVisibility();
		initConsiderations();
		audioController.removePlayer();
	}

	public void showNext(View v) {
		currentStationId++;
		changeNavigationButtonsVisibility();
		initConsiderations();
		audioController.removePlayer();
	}

	private void initConsiderations() {
		tv.setText(getConsiderations());
		scrollView.scrollTo(0, 0);
		String title = getActivityTitle();

		setTitle(title);
		audioController.setResourceId(getAudioId(currentStationId));
	}

	private String getActivityTitle() {
		String title;
		if (currentStationId == 0) {
			title = getString(R.string.considerations_start_title);
		} else if (currentStationId == 15) {
			title = getString(R.string.considerations_end_title);
		} else {
			title = getString(R.string.station) + NumConverter.toRoman(currentStationId);
		}
		return title;
	}

	private int getAudioId(int station) {
		try {
			Field field = R.raw.class.getField("stacja_" + station);
			return field.getInt(null);
		} catch (Exception e) {
			Log.e("EDK", "Cannot find audio for given station " + station, e);
		}
		return R.raw.stacja_1;
	}

	@Override
	public void finishEDK(MenuItem item) {
		audioController.removePlayer();
		super.finishEDK(item);
	}

	@Override
	public void onBackPressed() {
		audioController.removePlayer();
		super.onBackPressed();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		currentStationId = getIntent().getIntExtra(Extra.STATION_ID, 0);
		changeNavigationButtonsVisibility();
		initConsiderations();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.getItem(CONSIDERATIONS_MENU_INDEX).setVisible(false);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		Settings settings = Settings.get(this);
		if (!settings.getBoolean(Settings.IS_BACKGROUND_TRACKING_ON) && settings.get(Settings.TRACK_NAME) == null) {
			menu.getItem(MAP_MENU_INDEX).setVisible(false);
		}
		return true;
	}

	private String getConsiderations() {
		String rozw = "Rozwazania";
		int yearId = Settings.get(this).getInt(Settings.YEAR_ID, 0);
//		if (yearId == 2) {
//			if (currentStationId <= 1) {
//				rozw = getResources().getString(R.string.EDK2013S01);
//			}
//			if (currentStationId == 2) {
//				rozw = getResources().getString(R.string.EDK2013S02);
//			}
//			if (currentStationId == 3) {
//				rozw = getResources().getString(R.string.EDK2013S03);
//			}
//			if (currentStationId == 4) {
//				rozw = getResources().getString(R.string.EDK2013S04);
//			}
//			if (currentStationId == 5) {
//				rozw = getResources().getString(R.string.EDK2013S05);
//			}
//			if (currentStationId == 6) {
//				rozw = getResources().getString(R.string.EDK2013S06);
//			}
//			if (currentStationId == 7) {
//				rozw = getResources().getString(R.string.EDK2013S07);
//			}
//			if (currentStationId == 8) {
//				rozw = getResources().getString(R.string.EDK2013S08);
//			}
//			if (currentStationId == 9) {
//				rozw = getResources().getString(R.string.EDK2013S09);
//			}
//			if (currentStationId == 10) {
//				rozw = getResources().getString(R.string.EDK2013S10);
//			}
//			if (currentStationId == 11) {
//				rozw = getResources().getString(R.string.EDK2013S11);
//			}
//			if (currentStationId == 12) {
//				rozw = getResources().getString(R.string.EDK2013S12);
//			}
//			if (currentStationId == 13) {
//				rozw = getResources().getString(R.string.EDK2013S13);
//			}
//			if (currentStationId >= 14) {
//				rozw = getResources().getString(R.string.EDK2013S14);
//			}
//		}
//		if (yearId == 1) {
//			if (currentStationId <= 1) {
//				rozw = getResources().getString(R.string.EDK2014S01);
//			}
//			if (currentStationId == 2) {
//				rozw = getResources().getString(R.string.EDK2014S02);
//			}
//			if (currentStationId == 3) {
//				rozw = getResources().getString(R.string.EDK2014S03);
//			}
//			if (currentStationId == 4) {
//				rozw = getResources().getString(R.string.EDK2014S04);
//			}
//			if (currentStationId == 5) {
//				rozw = getResources().getString(R.string.EDK2014S05);
//			}
//			if (currentStationId == 6) {
//				rozw = getResources().getString(R.string.EDK2014S06);
//			}
//			if (currentStationId == 7) {
//				rozw = getResources().getString(R.string.EDK2014S07);
//			}
//			if (currentStationId == 8) {
//				rozw = getResources().getString(R.string.EDK2014S08);
//			}
//			if (currentStationId == 9) {
//				rozw = getResources().getString(R.string.EDK2014S09);
//			}
//			if (currentStationId == 10) {
//				rozw = getResources().getString(R.string.EDK2014S10);
//			}
//			if (currentStationId == 11) {
//				rozw = getResources().getString(R.string.EDK2014S11);
//			}
//			if (currentStationId == 12) {
//				rozw = getResources().getString(R.string.EDK2014S12);
//			}
//			if (currentStationId == 13) {
//				rozw = getResources().getString(R.string.EDK2014S13);
//			}
//			if (currentStationId >= 14) {
//				rozw = getResources().getString(R.string.EDK2014S14);
//			}
//		}
		if (yearId == 0) {
			if (currentStationId == 0) {
				rozw = getResources().getString(R.string.EDK2015S00);
			}
			if (currentStationId == 1) {
				rozw = getResources().getString(R.string.EDK2015S01);
			}
			if (currentStationId == 2) {
				rozw = getResources().getString(R.string.EDK2015S02);
			}
			if (currentStationId == 3) {
				rozw = getResources().getString(R.string.EDK2015S03);
			}
			if (currentStationId == 4) {
				rozw = getResources().getString(R.string.EDK2015S04);
			}
			if (currentStationId == 5) {
				rozw = getResources().getString(R.string.EDK2015S05);
			}
			if (currentStationId == 6) {
				rozw = getResources().getString(R.string.EDK2015S06);
			}
			if (currentStationId == 7) {
				rozw = getResources().getString(R.string.EDK2015S07);
			}
			if (currentStationId == 8) {
				rozw = getResources().getString(R.string.EDK2015S08);
			}
			if (currentStationId == 9) {
				rozw = getResources().getString(R.string.EDK2015S09);
			}
			if (currentStationId == 10) {
				rozw = getResources().getString(R.string.EDK2015S10);
			}
			if (currentStationId == 11) {
				rozw = getResources().getString(R.string.EDK2015S11);
			}
			if (currentStationId == 12) {
				rozw = getResources().getString(R.string.EDK2015S12);
			}
			if (currentStationId == 13) {
				rozw = getResources().getString(R.string.EDK2015S13);
			}
			if (currentStationId == 14) {
				rozw = getResources().getString(R.string.EDK2015S14);
			}
			if (currentStationId == 15) {
				rozw = getResources().getString(R.string.EDK2015S15);
			}
		}
		return rozw;
	}

	private final class AudioButtonController implements OnClickListener, OnCompletionListener {
		private final Button button;
		private MediaPlayer mediaPlayer;
		private int resId;

		private AudioButtonController(Button button, int resId) {
			this.button = button;
			this.button.setOnClickListener(this);
			this.resId = resId;
		}

		public void setResourceId(int resId) {
			this.resId = resId;
		}

		@Override
		public void onClick(View v) {
			if (button.getText() == getString(R.string.listen)) {
				button.setText(R.string.stop_listening);
				mediaPlayer = MediaPlayer.create(ConsiderationsViewActivity.this, resId);
				mediaPlayer.setOnCompletionListener(this);
				mediaPlayer.start();
			} else {
				removePlayer();
			}
		}

		public void removePlayer() {
			button.setText(R.string.listen);
			if (mediaPlayer != null) {
				mediaPlayer.release();
				mediaPlayer = null;
			}
		}

		@Override
		public void onCompletion(MediaPlayer arg0) {
			removePlayer();
		}
	}

}