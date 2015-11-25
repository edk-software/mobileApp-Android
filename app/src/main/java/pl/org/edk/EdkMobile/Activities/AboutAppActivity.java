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
public class AboutAppActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_app);
    }

    public void sendEmailOnClick(View view) {
        String address = "pawel.wawrzynek+edk@gmail.com";
        String subject = "Wiadomość z aplikacji EdkMobile";

        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.parse("mailto:" + address + "?subject=" + subject);
        intent.setData(data);
        startActivity(intent);
    }
}
