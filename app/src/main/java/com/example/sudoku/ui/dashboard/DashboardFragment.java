package com.example.sudoku.ui.dashboard;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.JsonReader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.sudoku.R;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    private ConstraintLayout pos1,pos2,pos3;
    static SharedPreferences sharedPref;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        pos1 = root.findViewById(R.id.pos1);
        pos2 = root.findViewById(R.id.pos2);
        pos3 = root.findViewById(R.id.pos3);

        sharedPref = getActivity().getSharedPreferences("results", MODE_PRIVATE);

        JSONObject json1 = null,json2 = null,json3 = null;
        try {
            json1 = new JSONObject(sharedPref.getString("result1","null"));

            TextView tbname = root.findViewById(R.id.oponentname1);
            TextView tbtipe = root.findViewById(R.id.gamemodename1);
            TextView tbdesc = root.findViewById(R.id.resultname1);

            tbname.setText(json1.getString("nome"));
            tbtipe.setText(json1.getString("tipo"));
            tbdesc.setText(json1.getString("desc"));
        } catch (JSONException e) {
            pos1.setVisibility(View.INVISIBLE);
        }

        try {
            json2 = new JSONObject(sharedPref.getString("result2","null"));

            TextView tbname = root.findViewById(R.id.oponentname2);
            TextView tbtipe = root.findViewById(R.id.gamemodename2);
            TextView tbdesc = root.findViewById(R.id.resultname2);

            tbname.setText(json2.getString("nome"));
            tbtipe.setText(json2.getString("tipo"));
            tbdesc.setText(json2.getString("desc"));
        } catch (JSONException e) {
            pos2.setVisibility(View.INVISIBLE);
        }

        try {
            json3 = new JSONObject(sharedPref.getString("result3","null"));

            TextView tbname = root.findViewById(R.id.oponentname3);
            TextView tbtipe = root.findViewById(R.id.gamemodename3);
            TextView tbdesc = root.findViewById(R.id.resultname3);

            tbname.setText(json3.getString("nome"));
            tbtipe.setText(json3.getString("tipo"));
            tbdesc.setText(json3.getString("desc"));
        } catch (JSONException e) {
            pos3.setVisibility(View.INVISIBLE);
        }


        return root;
    }
}