package pl.org.edk.EdkMobile.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import pl.org.edk.EdkMobile.Bootstrap;
import pl.org.edk.R;

/**
 * Created by Pawel on 2015-03-07.
 */
public class WelcomeActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initializations
        Bootstrap.Initialize(getApplicationContext());

        setContentView(R.layout.welcome);
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void startButtonOnClick(View view) {
        startActivity(new Intent(WelcomeActivity.this, ListActivity.class));
    }

    public void aboutButtonOnClick(View view) {
        if(view == findViewById(R.id.welcome_btnAboutEdk))
            startActivity(new Intent(WelcomeActivity.this, AboutEdkActivity.class));
        if(view == findViewById(R.id.welcome_btnAboutApp))
            startActivity(new Intent(WelcomeActivity.this, AboutAppActivity.class));
    }
}
