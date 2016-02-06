package pl.org.edk;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ExpandableListView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import pl.org.edk.util.ExpandableListAdapter;

/**
 * Created by darekpap on 2015-11-30.
 */
public class ReflectionsFragment extends Fragment {


    private ExpandableListView expListView;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listDataChild;
    private ExpandableListAdapter listAdapter;
    private int mCurrentStation = -1;


    public void selectStation(int stationIndex) {
        if (expListView == null) {
            Log.d("EDK", "List not ready");
            return;
        }
        openReflections(stationIndex);
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
        View view = inflater.inflate(R.layout.fragment_reflections, container, false);

        expListView = (ExpandableListView) view.findViewById(R.id.expandableList);

        prepareListData();

        listAdapter = new ExpandableListAdapter(getActivity(), listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);
        expListView.setGroupIndicator(null);

        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(final ExpandableListView parent, final View v, final int groupPosition, long id) {

                if (parent.isGroupExpanded(groupPosition)) {
                    parent.collapseGroup(groupPosition);
                } else {
                    openReflections(groupPosition);
                }
                return true;
            }
        });

        return view;
    }

    private void openReflections(final int stationId) {
        if (mCurrentStation != -1 && stationId != mCurrentStation) {
            expListView.collapseGroup(mCurrentStation);
        }
        expListView.expandGroup(stationId);
        expListView.post(new Runnable() {
            @Override
            public void run() {
                smoothScrollToPositionFromTop(expListView, stationId);
            }
        });
        mCurrentStation = stationId;
    }


//workaround from https://stackoverflow.com/questions/14479078/smoothscrolltopositionfromtop-is-not-always-working-like-it-should/20997828#20997828
    public static void smoothScrollToPositionFromTop(final AbsListView view, final int position) {
        View child = getChildAtPosition(view, position);
        // There's no need to scroll if child is already at top or view is already scrolled to its end
        if ((child != null) && ((child.getTop() == 0) || ((child.getTop() > 0) && !view.canScrollVertically(1)))) {
            return;
        }

        view.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(final AbsListView view, final int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    view.setOnScrollListener(null);

                    // Fix for scrolling bug
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            view.setSelection(position);
                        }
                    });
                }
            }

            @Override
            public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount,
                                 final int totalItemCount) {
            }
        });

        // Perform scrolling to position
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                view.smoothScrollToPositionFromTop(position, 0, 200);
            }
        });
    }

    public static View getChildAtPosition(final AdapterView view, final int position) {
        final int index = position - view.getFirstVisiblePosition();
        if ((index >= 0) && (index < view.getChildCount())) {
            return view.getChildAt(index);
        } else {
            return null;
        }
    }

    private void prepareListData() {
        listDataHeader = Arrays.asList(getResources().getStringArray(R.array.stations));
        listDataChild = new HashMap<>();

        for (int i = 0; i < listDataHeader.size(); i++) {
            String title = listDataHeader.get(i);
            List<String> children = new ArrayList<>();
            children.add(getResources().getString(getReflection(i)));
            listDataChild.put(title, children);
        }

    }

    //TODO change to service call
    private int getReflection(int station) {
        try {
            Field field = R.string.class.getField(String.format("EDK2015S%02d", station));
            return field.getInt(null);
        } catch (Exception e) {
            Log.e("EDK", "Cannot find audio for given station " + station, e);
        }
        return R.string.EDK2015S00;
    }

}
