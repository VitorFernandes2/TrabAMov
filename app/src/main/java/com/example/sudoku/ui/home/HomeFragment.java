package com.example.sudoku.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.example.sudoku.GameModeFragment;
import com.example.sudoku.R;
import com.example.sudoku.ui.credits.CreditsFragment;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private Button btnPlay;
    private Button btnCredits;
    private Button btnExit;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        btnPlay = (Button) root.findViewById(R.id.btnhost);
        btnPlay.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                swapToPlay();
            }
        });

        btnCredits = (Button) root.findViewById(R.id.btnjoin);
        btnCredits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swapToCredits();
            }
        });

        btnExit = (Button) root.findViewById(R.id.btnExit);
        btnExit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                exitSudoku();
            }
        });

        return root;
    }

    private void swapToCredits(){

        CreditsFragment nCreditsFragment = new CreditsFragment();
        assert getFragmentManager() != null;
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                                                android.R.anim.fade_out);
        fragmentTransaction.replace(R.id.nav_host_fragment, nCreditsFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

    private void swapToPlay(){

        GameModeFragment nCreditsFragment = new GameModeFragment();
        assert getFragmentManager() != null;
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                android.R.anim.fade_out);
        fragmentTransaction.replace(R.id.nav_host_fragment, nCreditsFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

    private void exitSudoku(){

        getActivity().finish();
        System.exit(0);

    }

}