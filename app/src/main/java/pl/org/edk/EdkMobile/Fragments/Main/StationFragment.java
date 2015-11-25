package pl.org.edk.EdkMobile.Fragments.Main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import pl.org.edk.EdkMobile.Activities.MainActivity;
import pl.org.edk.EdkMobile.Entities.CrossStation;
import pl.org.edk.EdkMobile.Managers.AppConfiguration;
import pl.org.edk.R;

import java.util.ArrayList;

/**
 * Created by Pawel on 2015-03-03.
 */

public class StationFragment extends MainActivityFragmentBase implements View.OnClickListener {
    private final int NONE = 0,
            DESC = 1,
            REF = 2;

    private CrossStation crossStation;
    private boolean textDisplayed = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.frag_station, container, false);

        int stationNumber = ((CrossStation) getArguments().getParcelable("DataContext")).getStationNumber();
        crossStation = AppConfiguration.getInstance().getCrossStation(stationNumber);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        InitializeView();
    }

    @Override
    public void onResume() {
        super.onResume();

        InitializeView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.station_btnAchieved:
                AchievedButtonOnClick(v);
                break;

            case R.id.station_btnReflections:
                ReflectionsButtonOnClick(v);
                break;
            case R.id.station_btnDescription:
                DescriptionButtonOnClick(v);
                break;
            case R.id.station_btnNavigationStart:
                NavigationButtonOnClick(v);
                break;

            case R.id.station_btnPreviousStation:
                PrevButtonOnClick(v);
                break;
            case R.id.station_btnNextStation:
                NextButtonOnClick(v);
                break;

            case R.id.station_btnHelp:
                HelpButtonOnClick(v);
                break;
            case R.id.station_btnContact:
                contactButtonOnClick(v);
                break;
        }
    }

    // View methods =======================
    private void InitializeView(){
        // View name
        TextView txtTitle = (TextView)findViewById(R.id.station_txtTitle);
        txtTitle.setText(crossStation.getDisplayName());

        // Distances
        TextView txtDone = (TextView)findViewById(R.id.station_txtDistDone);
        txtDone.setText(String.valueOf(crossStation.getDistanceDone()));
        TextView txtLeft = (TextView)findViewById(R.id.station_txtDistLeft);
        txtLeft.setText(String.valueOf(crossStation.getDistanceLeft()));

        // Buttons
        SetButtonsActions(crossStation.getStationNumber(), crossStation.isValid());
        SetStationActive(!crossStation.isAchieved());
        DisplayTextView(NONE);
    }

    private void SetButtonsActions(int stationNumber, boolean hasNavigation){
        ArrayList<View> buttons = (findViewById(R.id.station_mainLayout)).getTouchables();
        buttons.addAll(findViewById(R.id.station_bottomBar).getTouchables());
        for(View button : buttons){
            button.setOnClickListener(this);
        }

        // Navigation
        ImageButton naviButton = (ImageButton)findViewById(R.id.station_btnNavigationStart);
        if(!hasNavigation) {
            naviButton.setEnabled(false);
        }
        else{
            naviButton.setEnabled(true);
        }

        // Previous / Next
        ImageButton prevButton = (ImageButton) findViewById(R.id.station_btnPreviousStation);
        ImageButton nextButton = (ImageButton) findViewById(R.id.station_btnNextStation);
        if( stationNumber == 1){
            prevButton.setVisibility(View.INVISIBLE);
            nextButton.setVisibility(View.VISIBLE);
        }
        else if(stationNumber == 14) {
            prevButton.setVisibility(View.VISIBLE);
            nextButton.setVisibility(View.INVISIBLE);
        }
        else{
            prevButton.setVisibility(View.VISIBLE);
            nextButton.setVisibility(View.VISIBLE);
        }
    }

    private void SetStationActive(boolean isActive){
        TextView stationText = (TextView) findViewById(R.id.station_txtTitle);
        ImageButton refButton = (ImageButton) findViewById(R.id.station_btnReflections);
        ImageButton achievedButton = (ImageButton) findViewById(R.id.station_btnAchieved);
        ImageButton naviButton = (ImageButton) findViewById(R.id.station_btnNavigationStart);
        ImageButton descButton = (ImageButton) findViewById(R.id.station_btnDescription);

        if(isActive) {
            stationText.setBackground(getResources().getDrawable(R.drawable.active_border_transparent));
            refButton.setImageResource(R.drawable.station_reflections_active);
            achievedButton.setImageResource(R.drawable.station_achieve_active);
            if(crossStation.isValid())
                naviButton.setImageResource(R.drawable.station_navi_active);
            else
                naviButton.setImageResource(R.drawable.station_navi_disabled);
            descButton.setImageResource(R.drawable.station_desc_active);
        }
        else{
            stationText.setBackground(getResources().getDrawable(R.drawable.inactive_border_transparent));
            refButton.setImageResource(R.drawable.station_reflections_inactive);
            achievedButton.setImageResource(R.drawable.station_achieve_inactive);
            naviButton.setImageResource(R.drawable.station_navi_inactive);
            descButton.setImageResource(R.drawable.station_desc_inactive);
        }
    }

    private void DisplayTextView(int option){
        GridLayout mainLayout = (GridLayout)findViewById(R.id.station_mainLayout);
        ScrollView textScroll = (ScrollView)findViewById(R.id.station_popupScroll);
        TextView textContent = (TextView)findViewById(R.id.station_txtPopUpContent);
        TextView textName = (TextView)findViewById(R.id.station_txtPopUpName);

        switch (option){
            case NONE:
                mainLayout.setVisibility(View.VISIBLE);
                textScroll.setVisibility(View.GONE);
                textDisplayed = false;
                break;
            case DESC:
                mainLayout.setVisibility(View.GONE);
                textScroll.setVisibility(View.VISIBLE);
                textContent.setText(crossStation.getDescription());
                textName.setText(getResources().getText(R.string.description));
                textDisplayed = true;
                break;
            case REF:
                mainLayout.setVisibility(View.GONE);
                textScroll.setVisibility(View.VISIBLE);
                textContent.setText(crossStation.getReflections());
                textName.setText(getResources().getText(R.string.reflections));
                textDisplayed = true;
                break;
        }

        TextView textTitle = (TextView) findViewById(R.id.station_txtPopUpTitle);
        textTitle.setText(crossStation.getTitle());
    }
    // ====================================

    // Helper methods =====================
    private void SendHelpMessage() {
        String phoneNumber = AppConfiguration.getInstance().getCrossRoute().getContactNumber();
        SmsManager manager = SmsManager.getDefault();

        int stationNumber = AppConfiguration.getInstance().getLastAchievedStationNumber();
        String message = "Uczestnik EDK prosi o pomoc!";

        LatLng position = AppConfiguration.getInstance().getCurrentGpsPosition();
        if(position != null) {
            String currentLat = String.valueOf(position.latitude);
            String currentLon = String.valueOf(position.longitude);
            message += "\nJego współrzędne to: " + currentLat + ", " + currentLon;
            message += "\nZobacz na mapie: " + "https://www.google.pl/maps/@";
            message += currentLat + "," + currentLon + "," + "14z";
        }
        else {
            message += "\nWspółrzędne nieznane. Stacja: " + Integer.toString(stationNumber) + ".";
        }

        manager.sendTextMessage(phoneNumber, null, message, null, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setMessage(getString(R.string.station_helpConfirmation)).show();
    }

    private void MakeCall(){
        String telUrl = "tel:" + AppConfiguration.getInstance().getCrossRoute().getContactNumber();
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(telUrl));
        startActivity(intent);
    }

    private void openSms(){
        String smsUri = "sms:" + AppConfiguration.getInstance().getCrossRoute().getContactNumber();
        Intent sendIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(smsUri));
        startActivity(sendIntent);
    }
    // ====================================

    // Buttons ============================
    void AchievedButtonOnClick(View view) {
        if(crossStation.isAchieved()) {
            AppConfiguration.getInstance().setLastAchievedStation(crossStation.getStationNumber() - 1);
            SetStationActive(true);
        }
        else {
            AppConfiguration.getInstance().setLastAchievedStation(crossStation.getStationNumber());
            SetStationActive(false);
        }
        ((MainActivity)getActivity()).RefreshMap();
    }

    void HelpButtonOnClick(View view) {
        DialogInterface.OnClickListener dialogClick = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == DialogInterface.BUTTON_POSITIVE){
                    SendHelpMessage();
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setMessage(getString(R.string.station_help))
                .setNegativeButton("Nie", dialogClick)
                .setPositiveButton("Tak", dialogClick)
                .show();
    }

    void contactButtonOnClick(View view) {
        DialogInterface.OnClickListener dialogClick = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == DialogInterface.BUTTON_POSITIVE){
                    MakeCall();
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setMessage(getString(R.string.station_contact))
                .setPositiveButton("ZadzwoĹ„", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MakeCall();
                    }
                })
                .setNeutralButton("WyĹ›lij SMSa", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openSms();
                    }
                })
                .setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    void PrevButtonOnClick(View view) {
        crossStation = AppConfiguration.getInstance().getCrossStation(crossStation.getStationNumber() - 1);
        InitializeView();
    }

    void NextButtonOnClick(View view) {
        crossStation = AppConfiguration.getInstance().getCrossStation(crossStation.getStationNumber() + 1);
        InitializeView();
    }

    void ReflectionsButtonOnClick(View view) {
        DisplayTextView(REF);
    }

    void DescriptionButtonOnClick(View view) {
        DisplayTextView(DESC);
    }

    void NavigationButtonOnClick(View view) {
        startActivity(crossStation.toGpsIntent());
    }
    // ====================================

    // Public methods =====================
    public boolean onBackPressed(){
        // Close text
        if(textDisplayed){
            DisplayTextView(NONE);
            return false;
        }
        else {
            return true;
        }
    }
    // ====================================
}
