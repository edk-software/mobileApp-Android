package pl.org.edk.EdkMobile.Fragments.Main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pl.org.edk.R;

/**
 * Created by Pawel on 2015-03-12.
 */
public class StatisticsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.frag_statistics, container, false);
        return rootView;
    }
}
