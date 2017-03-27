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
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import pl.org.edk.MainActivity;
import pl.org.edk.R;
import pl.org.edk.Settings;
import pl.org.edk.TempSettings;
import pl.org.edk.kml.KMLTracker;

/**
 * Created by wojciech stadnicki on 2017-03-06.
 */
public class ViewRouteFragment extends MapFragment {

    private boolean challengeAccepted;

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
                challengeAccepted = true;
                MainActivity.Start(getActivity());
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

    @Override
    protected LatLng getCameraPos() {
        KMLTracker tracker = getTracker();
        List<LatLng> track = tracker.getTrack();
        return track.get(track.size() / 2);
    }

    @Override
    protected float getInitialCameraZoom() {
        return OVERVIEW_CAMERA_ZOOM;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (challengeAccepted){
            TempSettings.get(getActivity()).set(TempSettings.CAMERA_ZOOM, -1);
        } else{
            TempSettings.get(getActivity()).set(TempSettings.TRACK_WARNING_SHOWN, false);
        }
    }
}
