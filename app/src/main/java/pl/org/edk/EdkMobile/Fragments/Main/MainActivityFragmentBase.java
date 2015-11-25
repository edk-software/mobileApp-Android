package pl.org.edk.EdkMobile.Fragments.Main;

import android.support.v4.app.Fragment;
import android.view.View;

/**
 * Created by Pawel on 2015-03-12.
 */
public class MainActivityFragmentBase extends Fragment {
    protected View findViewById(int id){
        return getView().findViewById(id);
    }
}
