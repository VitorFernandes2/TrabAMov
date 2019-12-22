package com.example.sudoku;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProviders;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class GameModeFragment extends Fragment {

    private GameModeViewModel mViewModel;
    private Button btnSingle;
    private Button btnTwoPlayers;
    private Button btnMultiplayer;

    public static GameModeFragment newInstance() {
        return new GameModeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.game_mode_fragment, container, false);

        btnSingle = (Button) root.findViewById(R.id.btnSinglePlayer);
        btnSingle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Show dialogue
                // setup the alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.cDificulties);

                // add a list
                String[] dificulties = {getString(R.string.hard), getString(R.string.medium), getString(R.string.easy)};
                builder.setItems(dificulties, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent;
                        int value = 3;
                        switch (which) {
                            case 0: // Hard
                                value = 7;
                                break;
                            case 1: // Medium
                                value = 5;
                                break;
                            case 2: // Easy
                                value = 3;
                                break;
                        }

                        intent = new Intent(getActivity(), M1Activity.class);
                        intent.putExtra("difficulty", value);
                        startActivity(intent);

                    }
                });

                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        btnTwoPlayers = (Button) root.findViewById(R.id.btnTwoPlayers);
        btnTwoPlayers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Show dialogue
                // setup the alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.cDificulties);

                // add a list
                String[] dificulties = {getString(R.string.hard), getString(R.string.medium), getString(R.string.easy)};
                builder.setItems(dificulties, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: // Hard
                                break;
                            case 1: // Medium
                                break;
                            case 2: // Easy
                                break;
                        }
                    }
                });

                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        btnMultiplayer = (Button) root.findViewById(R.id.btnMultiplayer);
        btnMultiplayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Show dialogue
                // setup the alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.cDificulties);

                // add a list
                String[] dificulties = {getString(R.string.hard), getString(R.string.medium), getString(R.string.easy)};
                builder.setItems(dificulties, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: // Hard
                                break;
                            case 1: // Medium
                                break;
                            case 2: // Easy
                                break;
                        }
                    }
                });

                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(GameModeViewModel.class);
        // TODO: Use the ViewModel
    }

}
