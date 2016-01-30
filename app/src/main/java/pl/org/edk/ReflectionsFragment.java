package pl.org.edk;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by darekpap on 2015-11-30.
 */
public class ReflectionsFragment extends Fragment {


    public void selectStation(int stationIndex) {
        TextView textView = (TextView) getActivity().findViewById(R.id.reflection);
        textView.setText("Obecna stacja to " + stationIndex);
    }


    public ReflectionsFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reflections, container, false);
    }
}
