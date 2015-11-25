package pl.org.edk.EdkMobile.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import pl.org.edk.EdkMobile.Managers.*;
import pl.org.edk.R;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        InitializeView();
    }

    @Override
    public void onBackPressed(){
        startActivity(new Intent(this, WelcomeActivity.class));
    }

    private void InitializeView(){
        // Set view
        setContentView(R.layout.list);

        // Get all buttons
        List<Button> buttons = new ArrayList<Button>(14);
        buttons.add((Button) findViewById(R.id.btnStation1));
        buttons.add((Button)findViewById(R.id.btnStation2));
        buttons.add((Button)findViewById(R.id.btnStation3));
        buttons.add((Button)findViewById(R.id.btnStation4));
        buttons.add((Button)findViewById(R.id.btnStation5));
        buttons.add((Button)findViewById(R.id.btnStation6));
        buttons.add((Button)findViewById(R.id.btnStation7));
        buttons.add((Button)findViewById(R.id.btnStation8));
        buttons.add((Button)findViewById(R.id.btnStation9));
        buttons.add((Button)findViewById(R.id.btnStation10));
        buttons.add((Button)findViewById(R.id.btnStation11));
        buttons.add((Button)findViewById(R.id.btnStation12));
        buttons.add((Button)findViewById(R.id.btnStation13));
        buttons.add((Button)findViewById(R.id.btnStation14));

        // Set all buttons' OnClick
        int current = AppConfiguration.getInstance().getLastAchievedStationNumber();
        for(int i=1; i <= 14; i++){
            Button button = buttons.get(i-1);
            if(i <= current){
                button.setBackground(getResources().getDrawable(R.drawable.list_inactive_button_selector));
            }
            else
                button.setBackground(getResources().getDrawable(R.drawable.list_active_button_selector));
            button.setOnClickListener(GetStationButtonMethod(i));
        }
    }

    private View.OnClickListener GetStationButtonMethod(final int stationNumber){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(ListActivity.this, MainActivity.class);
                newIntent.putExtra("DataContext", AppConfiguration.getInstance().getCrossStation(stationNumber));
                startActivity(newIntent);
            }
        };
    }
}
