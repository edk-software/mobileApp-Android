package pl.org.edk.menu;

import pl.org.edk.R;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class EDKInfoActivity extends Activity {

	Button goToWebsiteButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edk_info);
		goToWebsiteButton = (Button) findViewById(R.id.idz_do_strony_button);
		goToWebsiteButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.edk.org.pl/"));
				startActivity(browse);
			}
		});
	}
}