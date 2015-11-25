package pl.org.edk.EdkMobile.Activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import pl.org.edk.R;

/**
 * Created by Pawel on 2015-03-19.
 */
public class AboutEdkActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.about_edk);
    }

    public void visitWebSiteOnClick(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.edk.org.pl"));
        startActivity(browserIntent);
    }
}
