package pl.org.edk.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.SupportMapFragment;

import pl.org.edk.MainActivity;
import pl.org.edk.R;
import pl.org.edk.Settings;
import pl.org.edk.TempSettings;

/**
 * Created by wojciech stadnicki on 2017-03-06.
 */
public class ViewRouteFragment extends MapFragment {

    public static ViewRouteFragment newInstance(boolean followLocationTurnedOn){
        ViewRouteFragment fragment = new ViewRouteFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(FOLLOW_LOCATION_KEY,followLocationTurnedOn);
        fragment.setArguments(bundle);
        return fragment;
    }

    private static String FOLLOW_LOCATION_KEY = "FOLLOW_LOCATION_KEY";

    private boolean followLocationTurnedOn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_route,container,false);

        if(getArguments().containsKey(FOLLOW_LOCATION_KEY)){
            followLocationTurnedOn = (boolean) getArguments().get(FOLLOW_LOCATION_KEY);
        }
        if(followLocationTurnedOn){
            Settings.get(getContext()).set(Settings.FOLLOW_LOCATION_ON_MAP,false);
        }

        Button chooseButton = (Button)view.findViewById(R.id.view_route_choose);
        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TempSettings.get(getActivity()).set(TempSettings.START_TIME, System.currentTimeMillis());
                startActivity(new Intent(getActivity(), MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_container);
        if (mapFragment == null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            mapFragment = SupportMapFragment.newInstance();
            fragmentTransaction.replace(R.id.map_container, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(followLocationTurnedOn){
            Settings.get(getContext()).set(Settings.FOLLOW_LOCATION_ON_MAP,true);
        }
    }
}
